package scash.warhorse.rpc.client

import scash.warhorse.core.crypto.DoubleSha256B
import scash.warhorse.rpc
import scash.warhorse.rpc.RpcUtil
import scash.warhorse.TestUtil._

import zio.test.DefaultRunnableSpec
import zio.test._

object BlockchainRPCTest extends DefaultRunnableSpec {
  val spec = suite("BlockchainRPC")(
    testM("getBestBlockHash") {
      assertM(rpc.getBestBlockHash)(successful[DoubleSha256B])
    }
  ).provideCustomLayerShared(RpcUtil.instance) @@ TestAspect.sequential

}
