package scash.warhorse.rpc.responses

import scash.warhorse.core.crypto.DoubleSha256B
import scash.warhorse.core.number._
import io.circe.Decoder
import io.circe.generic.semiauto._
import scodec.bits.ByteVector

import scala.Predef.Map

case class GetBlock(
  hash: DoubleSha256B,
  confirmations: Int,
  size: Int,
  height: Int,
  version: Int,
  versionHex: ByteVector,
  merkleroot: DoubleSha256B,
  tx: Vector[DoubleSha256B],
  time: Uint32,
  mediantime: Uint32,
  nonce: Uint32,
  bits: ByteVector,
  difficulty: BigDecimal,
  chainwork: String,
  nTx: Int,
  previousblockhash: Option[DoubleSha256B],
  nextblockhash: Option[DoubleSha256B]
)

case class GetBlockWithTransactions(
  hash: DoubleSha256B,
  confirmations: Int,
  size: Int,
  height: Int,
  version: Int,
  versionHex: ByteVector,
  merkleroot: DoubleSha256B,
  tx: Vector[RpcTransaction],
  time: Uint32,
  mediantime: Uint32,
  nonce: Uint32,
  bits: ByteVector,
  difficulty: BigDecimal,
  chainwork: String,
  nTx: Int,
  previousblockhash: Option[DoubleSha256B],
  nextblockhash: Option[DoubleSha256B]
)

case class GetBlockChainInfo(
  chain: String, //TODO as NetworkParameters
  blocks: Int,
  headers: Int,
  bestblockhash: DoubleSha256B,
  difficulty: BigDecimal,
  mediantime: Int,
  verificationprogress: BigDecimal,
  initialblockdownload: Boolean,
  chainwork: String, // How should this be handled?
  size_on_disk: Long,
  pruned: Boolean,
  pruneheight: Option[Int],
  softforks: Map[String, Softfork],
  warnings: String
)

case class Softfork(`type`: String, bip9: Bip9Softfork, active: Boolean)
case class Bip9Softfork(status: String, start_time: Int, timeout: BigInt, since: Int)

case class GetBlockHeader(
  hash: DoubleSha256B,
  confirmations: Int,
  height: Int,
  version: Int,
  versionHex: ByteVector,
  merkleroot: DoubleSha256B,
  time: Uint32,
  mediantime: Uint32,
  nonce: Uint32,
  bits: ByteVector,
  difficulty: BigDecimal,
  chainwork: String,
  previousblockhash: Option[DoubleSha256B],
  nextblockhash: Option[DoubleSha256B]
)

case class ChainTip(height: Int, hash: DoubleSha256B, branchlen: Int, status: String)

trait Blockchain {
  implicit val getBlockDecoder: Decoder[GetBlock]                                 = deriveDecoder
  implicit val getBlockWithTransactionsDecoder: Decoder[GetBlockWithTransactions] = deriveDecoder
  implicit val getBlockChainInfoDecoder: Decoder[GetBlockChainInfo]               = deriveDecoder
  implicit val softforkDecoder: Decoder[Softfork]                                 = deriveDecoder
  implicit val bip9Softfork: Decoder[Bip9Softfork]                                = deriveDecoder
  implicit val getBlockHeaderDecoder: Decoder[GetBlockHeader]                     = deriveDecoder
  implicit val chainTip: Decoder[ChainTip]                                        = deriveDecoder
}
