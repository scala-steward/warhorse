package scash.warhorse.core.crypto.hash

import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.SHA256Digest

import scash.warhorse.core.typeclass.{ Hasher, Serde }

import scodec.codecs.bytes
import scodec.bits.ByteVector

case class Sha256(private[crypto] val b: ByteVector)

case class Sha256B(private[crypto] val b: ByteVector)

object Sha256 {
  implicit val sha256Serde: Serde[Sha256] = Serde[Sha256](bytes(32).as[Sha256])

  implicit val sha256SerdeB: Serde[Sha256B] =
    sha256Serde.xmap(b => Sha256B(b.b.reverse), a => Sha256(a.b.reverse))

  implicit val sha256hash = new Hasher[Sha256] {
    def cons(b: ByteVector): Sha256 = Sha256(b)
    def hasher: Digest              = new SHA256Digest
  }
}
