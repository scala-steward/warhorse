package scash.warhorse.rpc.client

import scash.warhorse.Result
import scash.warhorse.core.crypto.DoubleSha256._
import scash.warhorse.core.crypto.DoubleSha256B
import scash.warhorse.rpc.responses._
import zio.RIO

import io.circe.syntax._
import io.circe.Decoder

trait BlockchainRPC {
  def getBestBlockHash: RIO[RpcClient, Result[DoubleSha256B]] =
    RIO.accessM[RpcClient](_.get.bitcoindCall[DoubleSha256B]("getbestblockhash"))

  /**
   * Returns a `GetBlockResult` from the block <hash>, with a Vector of tx hashes inside the block
   * Equivalent to bitcoin-cli getblock <hash> 0`
   */
  def getBlockRaw(headerHash: DoubleSha256B): RIO[RpcClient, Result[DoubleSha256B]] =
    getBlock[DoubleSha256B](headerHash.hex, 0)

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

  private def getBlock[A: Decoder](headerHash: String, verbosity: Int) =
    RIO.accessM[RpcClient](
      _.get.bitcoindCall[A]("getblock", List(headerHash.asJson, verbosity.asJson))
    )
}
