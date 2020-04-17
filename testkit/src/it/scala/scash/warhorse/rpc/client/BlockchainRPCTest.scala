package scash.warhorse.rpc.client

import scash.warhorse.core.crypto.DoubleSha256B
import scash.warhorse.rpc
import scash.warhorse.util._
import scash.warhorse.rpc.client.util._
import scash.warhorse.rpc.responses.{ ChainTip, GetBlock, GetBlockChainInfo, GetBlockHeader, GetBlockWithTransactions }
import scodec.bits.ByteVector
import zio.test.DefaultRunnableSpec
import zio.test._

object BlockchainRPCTest extends DefaultRunnableSpec {
  val spec = suite("BlockchainRPC")(
    testM("getBestBlockHash")(assertM(rpc.getBestBlockHash)(successful[DoubleSha256B])),
    testM("getBlock")(assertM(rpc.getBlock(test2Hash.require))(successful[GetBlock])),
    testM("getBlockRaw")(assertM(rpc.getBlockRaw(test2Hash.require))(successful[DoubleSha256B])),
    testM("getBlockWithTransactions")(
      assertM(rpc.getBlockWithTransactions(test2Hash.require))(successful[GetBlockWithTransactions])
    ),
    testM("getBlockchainInfo")(assertM(rpc.getBlockChainInfo)(successful[GetBlockChainInfo])),
    testM("getBlockCount")(assertM(rpc.getBlockCount)(successful[Int])),
    testM("getBlockHash")(assertM(rpc.getBlockHash(1))(successResult(genesisBlockHashB))),
    testM("getBlockHeader")(assertM(rpc.getBlockHeader(genesisBlockHashB.require))(successful[GetBlockHeader])),
    testM("getBlockHeaderRaw")(
      assertM(rpc.getBlockHeaderRaw(genesisBlockHashB.require))(successful[ByteVector])
    ),
    testM("getChainTips")(assertM(rpc.getChainTips)(successful[Vector[ChainTip]]))
  ).provideCustomLayerShared(instance) @@ TestAspect.sequential
}
