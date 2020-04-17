package scash.warhorse.rpc.client

import scash.warhorse.Result
import scash.warhorse.core.crypto.DoubleSha256._
import scash.warhorse.core.crypto.DoubleSha256B
import scash.warhorse.rpc.responses._
import zio.RIO
import io.circe.syntax._
import io.circe.Decoder
import scodec.bits.ByteVector

trait BlockchainRPC {
  def getBestBlockHash: RIO[RpcClient, Result[DoubleSha256B]] =
    RIO.accessM[RpcClient](_.get.bitcoindCall[DoubleSha256B]("getbestblockhash"))

  def getBestBlock: RIO[RpcClient, Result[GetBlock]] =
    for {
      best  <- getBestBlockHash
      block <- getBlock(best.require)
    } yield block

  /**
   * Returns a `GetBlockResult` from the block <hash>, with a Vector of tx hashes inside the block
   * Equivalent to bitcoin-cli getblock <hash> 0`
   */
  def getBlockRaw(headerHash: DoubleSha256B): RIO[RpcClient, Result[ByteVector]] =
    getBlock[ByteVector](headerHash.hex, 0)

  /**
   * Returns a `GetBlockResult` from the block <hash>, with a Vector of tx hashes inside the block
   * Equivalent to bitcoin-cli getblock <hash> 1`
   */
  def getBlock(headerHash: DoubleSha256B): RIO[RpcClient, Result[GetBlock]] =
    getBlock[GetBlock](headerHash.hex, 1)

  /**
   * Returns a `GetBlockWithTransactionsResult` from the block <hash>, with a Vector of tx `RpcTransaction`
   * Equivalent to bitcoin-cli getblock <hash> 2
   */
  def getBlockWithTransactions(headerHash: DoubleSha256B): RIO[RpcClient, Result[GetBlockWithTransactions]] =
    getBlock[GetBlockWithTransactions](headerHash.hex, 2)

  def getBlockChainInfo: RIO[RpcClient, Result[GetBlockChainInfo]] =
    RIO.accessM[RpcClient](_.get.bitcoindCall[GetBlockChainInfo]("getblockchaininfo"))

  def getBlockCount: RIO[RpcClient, Result[Int]] =
    RIO.accessM[RpcClient](_.get.bitcoindCall[Int]("getblockcount"))

  def getBlockHash(height: Int): RIO[RpcClient, Result[DoubleSha256B]] =
    RIO.accessM[RpcClient](_.get.bitcoindCall[DoubleSha256B]("getblockhash", List(height.asJson)))

  def getBlockHeader(headerHash: DoubleSha256B): RIO[RpcClient, Result[GetBlockHeader]] =
    getBlockHeader[GetBlockHeader](headerHash, true)

  def getBlockHeaderRaw(headerHash: DoubleSha256B): RIO[RpcClient, Result[ByteVector]] =
    getBlockHeader[ByteVector](headerHash, false)

  def getChainTips: RIO[RpcClient, Result[Vector[ChainTip]]] =
    RIO.accessM[RpcClient](_.get.bitcoindCall[Vector[ChainTip]]("getchaintips"))

  private def getBlockHeader[A: Decoder](headerHash: DoubleSha256B, verbosity: Boolean): RIO[RpcClient, Result[A]] =
    RIO.accessM[RpcClient](
      _.get.bitcoindCall[A]("getblockheader", List(headerHash.hex.asJson, verbosity.asJson))
    )

  private def getBlock[A: Decoder](headerHash: String, verbosity: Int) =
    RIO.accessM[RpcClient](
      _.get.bitcoindCall[A]("getblock", List(headerHash.asJson, verbosity.asJson))
    )
}
