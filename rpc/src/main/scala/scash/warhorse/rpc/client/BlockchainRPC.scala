package scash.warhorse.rpc.client

import scash.warhorse.Result
import scash.warhorse.core.crypto.DoubleSha256B
import scash.warhorse.rpc.responses._
import zio.RIO

trait BlockchainRPC {
  def getBestBlockHash: RIO[RpcClient, Result[DoubleSha256B]] =
    RIO.accessM[RpcClient](_.get.bitcoindCall[DoubleSha256B]("getbestblockhash"))
  /*
  /**
 * Returns a `GetBlockResult` from the block <hash>, with a Vector of tx hashes inside the block
 * Equivalent to bitcoin-cli getblock <hash> 1`
 */
  def getBlock(headerHash: DoubleSha256B): RIO[RpcClient, Result[GetBlockResult]] =
    RIO.accessM[RpcClient](
      _.get.bitcoindCall[GetBlockResult]("getblock", List(headerHash.hex, 1.toString))
    )

  /**
 * Same as [[getBlock]] but takes a Little Endian DoubleSha256 Hex instead of Big endian (BE)
 */
  def getBlock(headerHash: DoubleSha256): RIO[RpcClient, Result[GetBlockResult]] =
    getBlock(Sha256.flip(headerHash))
 */
}
