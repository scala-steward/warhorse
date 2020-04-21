package scash.warhorse.rpc

import io.circe.{ Decoder, Json }
import scash.warhorse.Result
import zio.{ Has, RIO }

package object client {
  type RpcClient = Has[RpcClient.Service]

  def call[A: Decoder](cmd: String, params: List[Json] = Nil): RIO[RpcClient, Result[A]] =
    RIO.accessM[RpcClient](_.get.bitcoindCall[A](cmd, params))
}
