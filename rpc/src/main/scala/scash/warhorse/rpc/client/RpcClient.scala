package scash.warhorse.rpc.client

import java.util.UUID

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }

import io.circe._
import io.circe.generic.semiauto._

import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.{ basicRequest, DeserializationError, HttpError }
import sttp.client.circe._
import sttp.model.Uri

import zio.{ Task, ZLayer }

object RpcClient {
  trait Service {
    def bitcoindCall[A: Decoder](cmd: String, parameters: List[Json] = List.empty): Task[Result[A]]
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
          def bitcoindCall[A: Decoder](cmd: String, parameters: List[Json]): Task[Result[A]] = {
            Predef.println(rpcRequestEncoder(RpcRequest(cmd, parameters)))
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
      }
      .toLayer

  private def response[A: Decoder] = asJson[RpcResponse[A]].map(
    _ match {
      case Right(a) =>
        a match {
          case RpcResponse(_, Some(e), _)    => Failure(Err.EffectError(RpcResponseError.error(e.code), e.message))
          case RpcResponse(Some(a), None, _) => Successful(a)
          case _                             => Failure(Err(s"The Response is not properly constructed: $a"))
        }
      case Left(e) =>
        e match {
          case DeserializationError(body, e) =>
            Failure(Err.ParseError("Json", s"parsing $body \n failed at: ${e.getMessage()}"))
          case HttpError(body) => Failure(Err.EffectError("Http", body))
        }
    }
  )

  private case class RpcRequest(
    method: String,
    params: List[Json],
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
