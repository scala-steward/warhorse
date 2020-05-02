package scash.warhorse.rpc.client

import scash.warhorse.core.crypto.hash.DoubleSha256B
import scash.warhorse.rpc
import scash.warhorse.util._
import scash.warhorse.rpc.client.util._
import scash.warhorse.rpc.responses.{
  ChainTip,
  GetBlock,
  GetBlockChainInfo,
  GetBlockHeader,
  GetBlockWithTransactions,
  GetChainTxStats,
  RescanBlockChain
}
import scodec.bits.ByteVector
import zio.test.DefaultRunnableSpec
import zio.test._

object BlockchainRPCTest extends DefaultRunnableSpec {
  val spec = suite("BlockchainRPC")(
    testM("getBestBlockHash")(assertM(rpc.getBestBlockHash)(success[DoubleSha256B])),
    testM("getBlock")(assertM(rpc.getBlock(test2Hash.require))(success[GetBlock])),
    testM("getBlockRaw")(assertM(rpc.getBlockRaw(test2Hash.require))(success[DoubleSha256B])),
    testM("getBlockWithTransactions")(
      assertM(rpc.getBlockWithTransactions(test2Hash.require))(success[GetBlockWithTransactions])
    ),
    testM("getBlockchainInfo")(assertM(rpc.getBlockChainInfo)(success[GetBlockChainInfo])),
    testM("getBlockCount")(assertM(rpc.getBlockCount)(success[Int])),
    testM("getBlockHash")(assertM(rpc.getBlockHash(1))(successResult(genesisBlockHashB))),
    testM("getBlockHeader")(assertM(rpc.getBlockHeader(genesisBlockHashB.require))(success[GetBlockHeader])),
    testM("getBlockHeaderRaw")(
      assertM(rpc.getBlockHeaderRaw(genesisBlockHashB.require))(success[ByteVector])
    ),
    testM("getChainTips")(assertM(rpc.getChainTips)(success[Vector[ChainTip]])),
    testM("getChainTxStats")(assertM(rpc.getChainTxStats)(success[GetChainTxStats])),
    testM("getDifficulty")(assertM(rpc.getDifficulty)(success[Int])),
    testM("pruneBlockChain")(assertM(rpc.pruneBlockChain(10))(failure)),
    testM("rescanBlockChain")(assertM(rpc.rescanBlockChain(1, 2))(success[RescanBlockChain])),
    testM("preciousBlock")(assertM(rpc.preciousBlock(test2Hash.require))(success[Unit])),
    testM("verifyLastBlock")(assertM(rpc.verifyLastBlock())(success(true))),
    testM("verifyChain")(assertM(rpc.verifyChain())(success(true)))
  ).provideCustomLayerShared(instance) @@ TestAspect.sequential
}
