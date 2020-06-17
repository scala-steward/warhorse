package scash.warhorse.rpc.client

import java.util.UUID

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import io.circe._
import io.circe.generic.semiauto._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.basicRequest
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

  private def response[A: Decoder] =
    asJsonAlways[RpcResponse[A]].map {
      case Right(RpcResponse(Left(e), _)) =>
        Failure(Err.EffectError("Rpc", s"${RpcResponseError.error(e.code)}. ${e.message}"))
      case Right(RpcResponse(Right(a), _)) => Successful(a)
      case Left(e)                         => Failure(Err.ParseError("Json", s"parsing ${e.body} \n failed at: ${e.getMessage()}"))
    }

  private case class RpcRequest(
    method: String,
    params: List[Json],
    id: String = UUID.randomUUID().toString
  )

  private case class RpcResponse[A](
    result: Either[RpcResponseError, A],
    id: String
  )

  private implicit def rpcResponseDecoder[A: Decoder]: Decoder[RpcResponse[A]] =
    for {
      resp <- Decoder[Json].at("error").flatMap[Either[RpcResponseError, A]] {
        case Json.Null => Decoder[A].at("result").map(Right(_))
        case _         => Decoder[RpcResponseError].at("error").map(Left(_))
      }
      id <- Decoder.decodeString.at("id")
    } yield RpcResponse[A](resp, id)

  private implicit val rpcRequestEncoder: Encoder[RpcRequest] = deriveEncoder

}
