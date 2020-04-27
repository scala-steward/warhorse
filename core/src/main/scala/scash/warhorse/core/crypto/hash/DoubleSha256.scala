package scash.warhorse.core.crypto.hash

import scash.warhorse.Result
import scash.warhorse.core._
import scash.warhorse.core.crypto.hash.Sha256.sha256Serde
import scash.warhorse.core.typeclass.Serde
import scodec.bits.ByteVector

protected[warhorse] case class DoubleSha256(private[crypto] val b: ByteVector)
protected[warhorse] case class DoubleSha256B(private[crypto] val b: ByteVector)

object DoubleSha256 {
  def toBigEndian(dsha256: DoubleSha256): DoubleSha256B    = DoubleSha256B(dsha256.b.reverse)
  def toLittleEndian(dsha256: DoubleSha256B): DoubleSha256 = DoubleSha256(dsha256.b.reverse)

  def validBigEndianHex(hex: String): Result[DoubleSha256B] =
    ByteVector.fromValidHex(hex).decode[DoubleSha256B]

  implicit val doubleSha256Serde: Serde[DoubleSha256] =
    sha256Serde.xmap(b => DoubleSha256(b.b), a => Sha256.Sha256(a.b))

  implicit val doubleSha256SerdeB: Serde[DoubleSha256B] =
    doubleSha256Serde.xmap(b => DoubleSha256B(b.b.reverse), a => DoubleSha256(a.b.reverse))
}
