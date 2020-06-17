package scash.warhorse.core.crypto.hash

import org.bouncycastle.crypto.Digest

import scash.warhorse.core.crypto.hash.Sha256.sha256Serde
import scash.warhorse.core.typeclass.Serde

import scodec.bits.ByteVector

case class DoubleSha256(private[crypto] val b: ByteVector)
case class DoubleSha256B(private[crypto] val b: ByteVector)

object DoubleSha256B {
  def toLittleEndian(dsha256: DoubleSha256B): DoubleSha256 = DoubleSha256(dsha256.b.reverse)

  implicit val doubleSha256Serde: Serde[DoubleSha256B] =
    DoubleSha256.doubleSha256Serde.xmap(d => DoubleSha256B(d.b), d => DoubleSha256(d.b))
}

object DoubleSha256 {
  def toBigEndian(dsha256: DoubleSha256): DoubleSha256B = DoubleSha256B(dsha256.b.reverse)

  implicit val doubleSha256Serde: Serde[DoubleSha256] =
    sha256Serde.xmap(b => DoubleSha256(b.b), a => Sha256(a.b))

  implicit val doubleSha256hash = new Hasher[DoubleSha256] {
    def cons(b: ByteVector): DoubleSha256 = DoubleSha256(Hasher[Sha256].hash(b).b)
    def hasher: Digest                    = Hasher[Sha256].hasher
  }
}
