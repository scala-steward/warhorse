package scash.warhorse.rpc.client

import scash.warhorse.core.crypto.DoubleSha256B
import scash.warhorse.rpc
import scash.warhorse.util._
import scash.warhorse.rpc.client.util._
import scash.warhorse.rpc.responses.{ GetBlock, GetBlockWithTransactions }
import zio.test.DefaultRunnableSpec
import zio.test._

object BlockchainRPCTest extends DefaultRunnableSpec {
  val spec = suite("BlockchainRPC")(
    testM("getBestBlockHash")(assertM(rpc.getBestBlockHash)(successful[DoubleSha256B])),
    testM("getBlock")(assertM(rpc.getBlock(genesisBlockHashB.require))(successful[GetBlock])),
    testM("getBlockWithTransactions")(
      assertM(rpc.getBlockWithTransactions(genesisBlockHashB.require))(successful[GetBlockWithTransactions])
    )
  ).provideCustomLayerShared(instance) @@ TestAspect.sequential

}
