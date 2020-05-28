package scash.warhorse.core.blockchain

import scash.warhorse.core.number.{ CompactSize, Uint32 }
import scash.warhorse.core.typeclass.Serde

import scodec.bits.ByteVector

case class TransactionInput(
  previousOutput: TransactionOutPoint,
  sigScript: ByteVector, //TODO: change when implenting Script
  sequence: Uint32
)

object TransactionInput {

  implicit val transactionInputSerde: Serde[TransactionInput] = Serde(
    (
      Serde[TransactionOutPoint].codec ::
        CompactSize.bytes ::
        Serde[Uint32].codec
    ).as[TransactionInput]
  )

  def apply(prevOutput: TransactionOutPoint, sigScript: ByteVector): TransactionInput =
    TransactionInput(prevOutput, sigScript, Transaction.sequence)
}
