package scash.warhorse.core.crypto.hash

import org.bouncycastle.crypto.Digest

import scash.warhorse.core.typeclass.{ Hasher, Serde }
import scash.warhorse.core._

import scodec.bits.ByteVector
import scodec.codecs.bytes

case class Hash160(b: ByteVector)

object Hash160 {

  implicit val hash160Serde: Serde[Hash160] = Serde(bytes(20).as[Hash160])

  implicit val hash160hash = new Hasher[Hash160] {
    def cons(bytes: ByteVector): Hash160 = Hash160(bytes.hash[RipeMd160].b)
    def hasher: Digest                   = Hasher[Sha256].hasher
  }
}
