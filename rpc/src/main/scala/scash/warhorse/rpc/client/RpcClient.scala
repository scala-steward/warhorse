package scash.warhorse.rpc.client

import java.util.UUID
import io.circe._
import io.circe.generic.semiauto._

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }

import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.basicRequest
import sttp.client.circe._
import sttp.model.Uri

import zio.{ Task, ZLayer }

object RpcClient {
  trait Service {
    def bitcoindCall[A: Decoder](cmd: String, parameters: List[String] = List.empty): Task[Result[A]]
  }

  def make(
    uri: Uri,
    userName: String,
    password: String
  ): ZLayer[Any, Throwable, RpcClient] =
    AsyncHttpClientZioBackend
      .managed()
      .map { implicit sttp =>
        new RpcClient.Service {
          def bitcoindCall[A: Decoder](cmd: String, parameters: List[String]): Task[Result[A]] =
            basicRequest
              .response(response[A])
              .post(uri)
              .body(RpcRequest(cmd, parameters))
              .auth
              .basic(userName, password)
              .send()
              .map(_.body)
        }
      }
      .toLayer

  private def response[A: Decoder] = asJson[RpcResponse[A]].map(
    _ match {
      case Left(e) => Failure(Err(s"Json Parsing error. body: ${e.body}"))
      case Right(a) =>
        a match {
          case RpcResponse(_, Some(e), _)    => Failure(Err.EffectError(RpcResponseError.error(e.code), e.message))
          case RpcResponse(Some(a), None, _) => Successful(a)
          case _                             => Failure(Err(s"The Response is not properly constructed: $a"))
        }
    }
  )

  private case class RpcRequest(
    method: String,
    params: List[String],
    id: String = UUID.randomUUID().toString
  )

  private case class RpcResponse[A](
    result: Option[A],
    error: Option[RpcResponseError],
    id: String
  )

  private implicit def rpcResponseDecoder[A: Decoder]: Decoder[RpcResponse[A]] = deriveDecoder
  private implicit val rpcRequestEncoder: Encoder[RpcRequest]                  = deriveEncoder

}
