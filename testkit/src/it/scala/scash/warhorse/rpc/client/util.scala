package scash.warhorse.rpc.client

import scash.warhorse.core.crypto.DoubleSha256
import zio.test.TestFailure
import sttp.client._

object util {
  val test1Hash = DoubleSha256.validBigEndianHex("000000000000000002010fbeac4ccbb5ad3abafe684228219134bb5354978644")
  val test2Hash = DoubleSha256.validBigEndianHex("00000000000000000389159a4f6baef6c4aa7c6619405c6b674296a7df6c86c7")

  val genesisBlockHashB =
    DoubleSha256.validBigEndianHex("00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048")

  val instance = RpcClient
    .make(uri"http://127.0.0.1:8332", "user", "password")
    .mapError(error => TestFailure.fail(error))
}
