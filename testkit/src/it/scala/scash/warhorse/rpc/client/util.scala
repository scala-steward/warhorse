package scash.warhorse.rpc.client

import scash.warhorse.core.crypto.hash.DoubleSha256B
import scash.warhorse.core._

import scodec.bits._
import zio.test.TestFailure
import sttp.client._

object util {
  val test1Hash = hex"000000000000000002010fbeac4ccbb5ad3abafe684228219134bb5354978644".decode[DoubleSha256B]
  val test2Hash = hex"00000000000000000389159a4f6baef6c4aa7c6619405c6b674296a7df6c86c7".decode[DoubleSha256B]

  val genesisBlockHashB = hex"00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048".decode[DoubleSha256B]

  val instance = RpcClient
    .make(uri"http://127.0.0.1:8332", "user", "password")
    .mapError(error => TestFailure.fail(error))
}
