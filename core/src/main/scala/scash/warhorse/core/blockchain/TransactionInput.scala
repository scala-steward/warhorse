package scash.warhorse.core.blockchain

import scash.warhorse.core.crypto.hash.DoubleSha256B
import scash.warhorse.core.number.{ CompactSizeUint, Uint32 }
import scash.warhorse.core.typeclass.Serde

import scodec.bits._
import scodec.codecs._

sealed trait TransactionInput

case class BaseTransactionInput(
  previousOutput: TransactionOutPoint,
  sigScript: ByteVector, //TODO: change when implenting Script
  sequence: Uint32
) extends TransactionInput

case class CoinbaseInput(
  height: Uint32,
  coinbase: ByteVector,
  sequence: Uint32
) extends TransactionInput

object TransactionInput {
  implicit val coinbaseInputSerde: Serde[CoinbaseInput]               = Serde(
    (constant(hex"0000000000000000000000000000000000000000000000000000000000000000") ::
      constant(ByteVector(0xffffffff)) ::
      Serde[Uint32].codec).as[CoinbaseInput]
  )
  implicit val basetransactionInputSerde: Serde[BaseTransactionInput] = Serde(
    (
      Serde[TransactionOutPoint].codec ::
        CompactSizeUint.bytes ::
        Serde[Uint32].codec
    ).as[TransactionInput]
  )

  implicit val transactionInputSerde: Serde[TransactionInput]                                  =
    coinbaseInputSerde.codec.<~()
  implicit def apply(prevOutput: TransactionOutPoint, sigScript: ByteVector): TransactionInput =
    TransactionInput(prevOutput, sigScript, Transaction.sequence)
}
