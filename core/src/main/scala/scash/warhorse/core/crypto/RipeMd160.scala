package scash.warhorse.core.crypto

import scash.warhorse.Err
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.typeclass.Serde
import scodec.DecodeResult
import scodec.bits.ByteVector

object RipeMd160 {
  protected[warhorse] case class RipeMd160(private[crypto] val b: ByteVector)
  protected[warhorse] case class RipeMd160B(private[crypto] val b: ByteVector)
  protected[warhorse] case class RipeMd160Sha256(private[crypto] val b: ByteVector)
  protected[warhorse] case class RipeMd160Sha256B(private[crypto] val b: ByteVector)

  implicit val ripeMd160Serde: Serde[RipeMd160] = Serde[RipeMd160](
    (a: RipeMd160) => Successful(a.b),
    (bytes: ByteVector) =>
      if (bytes.size >= bsize) Successful(DecodeResult(RipeMd160(bytes.take(bsize)), bytes.drop(bsize).bits))
      else Failure(Err.BoundsError("RipeMD160", s"needs $bsize bytes", s"${bytes.size} bytes"))
  )

  implicit val ripeMd160BSerde: Serde[RipeMd160B] =
    ripeMd160Serde.xmap(b => RipeMd160B(b.b.reverse), a => RipeMd160(a.b.reverse))

  implicit val ripeMd160Sha256Serde: Serde[RipeMd160Sha256] =
    ripeMd160Serde.xmap(b => RipeMd160Sha256(b.b), a => RipeMd160(a.b))

  implicit val ripeMd160Sha256BSerde: Serde[RipeMd160Sha256B] =
    ripeMd160Sha256Serde.xmap(b => RipeMd160Sha256B(b.b.reverse), a => RipeMd160Sha256(a.b.reverse))

  private val bsize = 20L
}
