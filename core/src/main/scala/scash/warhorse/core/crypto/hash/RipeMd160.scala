package scash.warhorse.core.crypto.hash

import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import scash.warhorse.Err
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.typeclass.{ Hasher, Serde }
import scodec.DecodeResult
import scodec.bits.ByteVector

protected[warhorse] case class RipeMd160(private[crypto] val b: ByteVector)

object RipeMd160 {

  implicit val ripeMd160Serde: Serde[RipeMd160] = Serde[RipeMd160](
    (a: RipeMd160) => Successful(a.b),
    (bytes: ByteVector) =>
      if (bytes.size == bsize) Successful(DecodeResult(RipeMd160(bytes.take(bsize)), bytes.drop(bsize).bits))
      else Failure(Err.BoundsError("RipeMD160", s"needs $bsize bytes", s"${bytes.size} bytes"))
  )

  implicit val ripemd160hash = new Hasher[RipeMd160] {
    def cons(b: ByteVector): RipeMd160 = RipeMd160(b)
    def hasher: Digest                 = new RIPEMD160Digest
  }

  private val bsize = 20L
}
