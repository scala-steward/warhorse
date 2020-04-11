package scash.warhorse.core.crypto

import scash.warhorse.Err
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.typeclass.Serde
import scodec.DecodeResult
import scodec.bits.ByteVector

object Sha256 {
  protected[warhorse] case class Sha256(private[crypto] val b: ByteVector)
  protected[warhorse] case class Sha256B(private[crypto] val b: ByteVector)
  protected[warhorse] case class DoubleSha256(private[crypto] val b: ByteVector)
  protected[warhorse] case class DoubleSha256B(private[crypto] val b: ByteVector)

  implicit val sha256Serde: Serde[Sha256] = Serde[Sha256](
    (a: Sha256) => Successful(a.b),
    (b: ByteVector) =>
      if (b.size >= bsize) Successful(DecodeResult(Sha256(b.take(bsize)), b.drop(bsize).bits))
      else Failure(Err.BoundsError("sha256", s"needs $bsize bytes", s"${b.size} bytes"))
  )
  private val bsize = 32L

  implicit val sha256SerdeB: Serde[Sha256B] =
    sha256Serde.xmap(b => Sha256B(b.b.reverse), a => Sha256(a.b.reverse))

  implicit val doubleSha256Serde: Serde[DoubleSha256] =
    sha256Serde.xmap(b => DoubleSha256(b.b), a => Sha256(a.b))

  implicit val doubleSha256SerdeB: Serde[DoubleSha256B] =
    doubleSha256Serde.xmap(b => DoubleSha256B(b.b.reverse), a => DoubleSha256(a.b.reverse))
}
