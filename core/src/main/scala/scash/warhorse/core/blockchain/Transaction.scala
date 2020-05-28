package scash.warhorse.core.blockchain

import scash.warhorse.core.crypto.hash.{ DoubleSha256, Hasher }
import scash.warhorse.core.number.{ CompactSize, Int32, Uint32 }
import scash.warhorse.core.typeclass.Serde
import scodec.Codec

case class Transaction(
  version: Int32,
  inputs: List[TransactionInput],
  outputs: List[TransactionOutput],
  lockTime: Uint32
) { self =>

  def txId: DoubleSha256 =
    Transaction.transactionSerde
      .encode(self)
      .map(Hasher[DoubleSha256].hash)
      .require
}

object Transaction {
  lazy val version  = Int32.one
  lazy val lockTime = Uint32.zero
  lazy val sequence = Uint32(4294967295L)

  def apply(
    inputs: List[TransactionInput],
    outputs: List[TransactionOutput]
  ): Transaction =
    Transaction(version, inputs, outputs, lockTime)

  implicit val transactionSerde: Serde[Transaction] = Serde(
    (
      Codec[Int32] ::
        CompactSize.listOfN(Serde[TransactionInput]) ::
        CompactSize.listOfN(Serde[TransactionOutput]) ::
        Codec[Uint32]
    ).as[Transaction]
  )
}
