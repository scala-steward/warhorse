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

  private def response[A: Decoder] = asJsonAlways[RpcResponse[A]].map {
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

  private implicit def decodeResponse[A: Decoder]: Decoder[RpcResponse[A]] =
    (c: HCursor) =>
      for {
        errJs <- c.downField("error").as[Json]
        resp <- if (errJs == Json.Null) c.downField("result").as[A].map(Right(_))
               else errJs.as[RpcResponseError].map(Left(_))
        id <- c.downField("id").as[String]
      } yield RpcResponse[A](resp, id)

  private implicit val rpcRequestEncoder: Encoder[RpcRequest] = deriveEncoder

}
