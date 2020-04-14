package scash.warhorse.rpc.responses

import scash.warhorse.core.crypto.DoubleSha256B
import scash.warhorse.core.number._
import io.circe.Decoder
import io.circe.generic.semiauto._
import scodec.bits.ByteVector

case class GetBlockResult(
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

object GetBlockResult {
  implicit val getBlockResultDecoder: Decoder[GetBlockResult] = deriveDecoder
}
