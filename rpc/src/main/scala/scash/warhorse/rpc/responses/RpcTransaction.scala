package scash.warhorse.rpc.responses

import scash.warhorse.core.number.Uint32
import cats.syntax.functor._
import io.circe.generic.semiauto.deriveDecoder
import io.circe.Decoder
import io.circe.generic.auto._
import scash.warhorse.core.crypto.hash.DoubleSha256B

case class RpcTransaction(
  txid: DoubleSha256B,
  hash: DoubleSha256B,
  version: Int,
  size: Int,
  locktime: Uint32,
  vin: Vector[RpcTransactionInput],
  vout: Vector[RpcTransactionOutput],
  hex: Option[String]
)

sealed trait RpcTransactionInput

case class RpcCoinbaseInput(
  coinbase: String,
  sequence: Uint32
) extends RpcTransactionInput

case class RpcTInput(
  txid: DoubleSha256B,
  vout: Int,
  scriptSig: RpcScriptSig,
  sequence: Uint32
) extends RpcTransactionInput

case class RpcScriptSig(
  asm: String,
  hex: String
)

case class RpcTransactionOutput(
  value: BigDecimal,
  n: Int,
  scriptPubKey: RpcScriptPubKey
)

case class RpcScriptPubKey(
  asm: String,
  hex: String,
  reqSigs: Option[Int],
  `type`: String,                   //TODO: add type
  addresses: Option[Vector[String]] //TODO: cant support cashaddr yet store as string for now
)

trait RpcTransactionDecoders {
  implicit val rpcScriptPubKeyDecoder: Decoder[RpcScriptPubKey]           = deriveDecoder
  implicit val rpcTransactionOutputDecoder: Decoder[RpcTransactionOutput] = deriveDecoder
  implicit val rpcScriptSigDecoder: Decoder[RpcScriptSig]                 = deriveDecoder
  implicit val rpcTransactionInput: Decoder[RpcTransactionInput]          =
    Decoder[RpcTInput].widen or Decoder[RpcCoinbaseInput].widen
  implicit val rpcTransaction: Decoder[RpcTransaction]                    = deriveDecoder
}
