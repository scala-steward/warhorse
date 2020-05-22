package scash.warhorse.rpc.client

import scash.warhorse.Result
import scash.warhorse.rpc.responses._
import zio.RIO
import io.circe.syntax._
import io.circe.Decoder
import scash.warhorse.core.crypto.hash.DoubleSha256B
import scodec.bits.ByteVector

trait BlockchainRPC {
  def getBestBlockHash: RIO[RpcClient, Result[DoubleSha256B]] =
    call[DoubleSha256B]("getbestblockhash")

  def getBestBlock: RIO[RpcClient, Result[GetBlock]]                                                        =
    for {
      best  <- getBestBlockHash
      block <- getBlock(best.require)
    } yield block

  /**
   * Returns a `GetBlockResult` from the block <hash>, with a Vector of tx hashes inside the block
   * Equivalent to bitcoin-cli getblock <hash> 0`
   */
  def getBlockRaw(headerHash: DoubleSha256B): RIO[RpcClient, Result[ByteVector]]                            =
    getBlock[ByteVector](headerHash.hex, 0)

  /**
   * Returns a `GetBlockResult` from the block <hash>, with a Vector of tx hashes inside the block
   * Equivalent to bitcoin-cli getblock <hash> 1`
   */
  def getBlock(headerHash: DoubleSha256B): RIO[RpcClient, Result[GetBlock]]                                 =
    getBlock[GetBlock](headerHash.hex, 1)

  /**
   * Returns a `GetBlockWithTransactionsResult` from the block <hash>, with a Vector of tx `RpcTransaction`
   * Equivalent to bitcoin-cli getblock <hash> 2
   */
  def getBlockWithTransactions(headerHash: DoubleSha256B): RIO[RpcClient, Result[GetBlockWithTransactions]] =
    getBlock[GetBlockWithTransactions](headerHash.hex, 2)

  def getBlockChainInfo: RIO[RpcClient, Result[GetBlockChainInfo]] =
    call[GetBlockChainInfo]("getblockchaininfo")

  def getBlockCount: RIO[RpcClient, Result[Int]] =
    call[Int]("getblockcount")

  def getBlockHash(height: Int): RIO[RpcClient, Result[DoubleSha256B]] =
    call[DoubleSha256B]("getblockhash", List(height.asJson))

  def getBlockHeader(headerHash: DoubleSha256B): RIO[RpcClient, Result[GetBlockHeader]] =
    getBlockHeader[GetBlockHeader](headerHash, true)

  def getBlockHeaderRaw(headerHash: DoubleSha256B): RIO[RpcClient, Result[ByteVector]] =
    getBlockHeader[ByteVector](headerHash, false)

  def getChainTips: RIO[RpcClient, Result[Vector[ChainTip]]] =
    call[Vector[ChainTip]]("getchaintips")

  def getChainTxStats: RIO[RpcClient, Result[GetChainTxStats]] = getChainTxStats(None, None)

  def getChainTxStats(blocks: Int): RIO[RpcClient, Result[GetChainTxStats]] =
    getChainTxStats(Some(blocks), None)

  def getChainTxStats(blocks: Int, blockHash: DoubleSha256B): RIO[RpcClient, Result[GetChainTxStats]] =
    getChainTxStats(Some(blocks), Some(blockHash))

  def getDifficulty: RIO[RpcClient, Result[BigDecimal]] =
    call[BigDecimal]("getdifficulty")

  def invalidateBlock(blockHash: DoubleSha256B): RIO[RpcClient, Result[Unit]] =
    call[Unit]("invalidateblock", List(blockHash.asJson))

  def pruneBlockChain(height: Int): RIO[RpcClient, Result[Int]] =
    call[Int]("pruneblockchain", List(height.asJson))

  def rescanBlockChain(start: Int, stop: Int): RIO[RpcClient, Result[RescanBlockChain]] =
    rescanBlockChain(Some(start), Some(stop))

  def rescanBlockChain(start: Int): RIO[RpcClient, Result[RescanBlockChain]] =
    rescanBlockChain(Some(start), None)

  def rescanBlockChain: RIO[RpcClient, Result[RescanBlockChain]] =
    rescanBlockChain(None, None)

  def preciousBlock(headerHash: DoubleSha256B): RIO[RpcClient, Result[Unit]] =
    call[Unit]("preciousblock", List(headerHash.asJson))

  def verifyLastBlock(level: Int = 3): RIO[RpcClient, Result[Boolean]] =
    verifyChain(level, 1)

  def verifyEntireChain(level: Int = 3): RIO[RpcClient, Result[Boolean]] =
    verifyChain(level, 0)

  def verifyChain(level: Int = 3, blocks: Int = 6): RIO[RpcClient, Result[Boolean]] =
    call[Boolean]("verifychain", List(level, blocks).map(_.asJson))

  private def rescanBlockChain(start: Option[Int], stop: Option[Int]) =
    call[RescanBlockChain]("rescanblockchain", List(start, stop).flatten.map(_.asJson))

  private def getChainTxStats(
    blocks: Option[Int],
    blockHash: Option[DoubleSha256B]
  ): RIO[RpcClient, Result[GetChainTxStats]] =
    call[GetChainTxStats](
      "getchaintxstats",
      List(blocks.map(_.asJson), blockHash.map(_.asJson)).flatten
    )

  private def getBlockHeader[A: Decoder](headerHash: DoubleSha256B, verbosity: Boolean): RIO[RpcClient, Result[A]] =
    call[A]("getblockheader", List(headerHash.hex.asJson, verbosity.asJson))

  private def getBlock[A: Decoder](headerHash: String, verbosity: Int) =
    call[A]("getblock", List(headerHash.asJson, verbosity.asJson))
}
