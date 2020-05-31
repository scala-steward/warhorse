package scash.warhorse.core.blockchain

import scash.warhorse.core.number.Int64
import scash.warhorse.core.typeclass.Serde

import scodec.bits.ByteVector

case class TransactionOutput(
  value: Int64, //TODO: change the value to Satoshis
  scriptPubKey: ByteVector
)

object TransactionOutput {

  implicit val transactionOutputSerde = Serde[TransactionOutput](
    (Serde[Int64].codec :: CompactSizeUint.bytes)
      .as[TransactionOutput]
  )
}
