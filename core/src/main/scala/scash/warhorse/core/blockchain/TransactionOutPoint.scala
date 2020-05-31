package scash.warhorse.core.blockchain

import scash.warhorse.core.crypto.hash.DoubleSha256
import scash.warhorse.core.number.Uint32
import scash.warhorse.core.typeclass.Serde

case class TransactionOutPoint(
  txId: DoubleSha256,
  vout: Uint32
)

object TransactionOutPoint {
  implicit val transactionOutPointSerde = Serde(
    (Serde[DoubleSha256].codec :: Serde[Uint32].codec)
      .as[TransactionOutPoint]
  )
}
