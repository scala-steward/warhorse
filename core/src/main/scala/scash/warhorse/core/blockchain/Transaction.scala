package scash.warhorse.core.blockchain

import scash.warhorse.core.crypto.hash.{ DoubleSha256, DoubleSha256B, Hasher }
import scash.warhorse.core.number.{ Int32, Uint32 }
import scash.warhorse.core.typeclass.Serde

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

  def txIdB: DoubleSha256B = DoubleSha256.toBigEndian(txId)
}

object Transaction {
  lazy val version  = Int32.one
  lazy val lockTime = Uint32.zero

  /**
    https://github.com/bitcoin/bips/blob/master/bip-0068.mediawiki
      Setting nSequence to this value for every input in a transaction
      disables nLockTime
   */
  lazy val nSequence = Uint32.max

  implicit val transactionSerde: Serde[Transaction] = Serde(
    (
      Serde[Int32].codec ::
        CompactSizeUint.listOfN(Serde[TransactionInput]) ::
        CompactSizeUint.listOfN(Serde[TransactionOutput]) ::
        Serde[Uint32].codec
    ).as[Transaction]
  )
}
