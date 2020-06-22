package scash.warhorse.core.crypto.hash

import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.RIPEMD160Digest

import scash.warhorse.core.typeclass.{ Hasher, Serde }

import scodec.codecs.bytes
import scodec.bits.ByteVector

protected[warhorse] case class RipeMd160(private[crypto] val b: ByteVector)

object RipeMd160 {

  implicit val ripeMd160Serde: Serde[RipeMd160] =
    Serde[RipeMd160](bytes(20).as[RipeMd160])

  implicit val ripemd160hash = new Hasher[RipeMd160] {
    def cons(b: ByteVector): RipeMd160 = RipeMd160(b)
    def hasher: Digest                 = new RIPEMD160Digest
  }

}
