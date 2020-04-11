package scash.warhorse.core.number

import scash.warhorse.core.typeclass.{ CNumeric, Serde }
import scodec.codecs.int64L

import scala.util.Try

protected case class Uint64(num: BigInt) extends AnyVal

object Uint64 {
  def apply(bigInt: BigInt): Uint64 = new Uint64(verify(bigInt)(min, max))

  def safe(bigInt: BigInt): Option[Uint64] = Try(apply(bigInt)).toOption

  val min  = new Uint64(BigInt(0))
  val zero = min
  val one  = new Uint64(BigInt(1))
  val max  = new Uint64(BigInt("18446744073709551615"))

  implicit val uint64Serde: Serde[Uint64] =
    Serde[Uint64](
      int64L.xmap[Uint64](
        l => apply(uLongtoBigInt(l)),
        n => bigIntToLong(n.num)
      )
    )

  implicit val uint64Numeric: CNumeric[Uint64] =
    CNumeric[Uint64](0xFFFFFFFFFFFFFFFFL, min, max)(_.num, apply(_))
}
