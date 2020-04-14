package scash.warhorse.rpc.client

import scash.warhorse.core.crypto.DoubleSha256B
import scash.warhorse.rpc
import scash.warhorse.rpc.RpcUtil
import scash.warhorse.TestUtil._
import scash.warhorse.rpc.responses.GetBlockResult
import zio.test.DefaultRunnableSpec
import zio.test._

object BlockchainRPCTest extends DefaultRunnableSpec {
  val spec = suite("BlockchainRPC")(
    testM("getBestBlockHash")(
      assertM(rpc.getBestBlockHash)(successful[DoubleSha256B])
    ),
    testM("getBlock")(
      assertM(rpc.getBlock(RpcUtil.genesisBlockHashB.require))(successful[GetBlockResult])
    )
  ).provideCustomLayerShared(RpcUtil.instance) @@ TestAspect.sequential

}
