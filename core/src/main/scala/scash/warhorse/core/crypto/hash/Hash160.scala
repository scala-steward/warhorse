package scash.warhorse.core.crypto.hash

import org.bouncycastle.crypto.Digest

import scash.warhorse.Err
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.typeclass.{ Hasher, Serde }
import scash.warhorse.core._

import scodec.DecodeResult
import scodec.bits.ByteVector

case class Hash160(b: ByteVector)

object Hash160 {

  implicit val hash160Serde: Serde[Hash160] = Serde(
    (a: Hash160) => Successful(a.b),
    (bytes: ByteVector) =>
      if (bytes.size == bsize) Successful(DecodeResult(Hash160(bytes.take(bsize)), bytes.drop(bsize).bits))
      else Failure(Err.BoundsError("Hash160", s"needs $bsize bytes", s"${bytes.size} bytes"))
  )

  implicit val hash160hash = new Hasher[Hash160] {
    def cons(bytes: ByteVector): Hash160 = Hash160(bytes.hash[RipeMd160].b)
    def hasher: Digest                   = Hasher[Sha256].hasher
  }

  private val bsize = 20L
}
