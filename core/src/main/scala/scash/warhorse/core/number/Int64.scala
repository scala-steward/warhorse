package scash.warhorse.core.number

import scash.warhorse.core.typeclass.{ CNumeric, Serde }
import scodec.codecs.int64L

import scala.util.Try

protected[warhorse] case class Int64(num: Long) extends AnyVal

object Int64 {
  def apply(n: Long): Int64 = new Int64(n)

  def apply(n: BigInt): Int64 = new Int64(verify(n)(min, max).toLong)

  def safe(bigInt: BigInt): Option[Int64] = Try(apply(bigInt)).toOption

  val min  = new Int64(-9223372036854775808L)
  val zero = new Int64(0)
  val one  = new Int64(1)
  val max  = new Int64(9223372036854775807L)

  implicit val int64Serde: Serde[Int64] = Serde[Int64](int64L.as[Int64])

  implicit val int64Numeric: CNumeric[Int64] =
    CNumeric[Int64](0xffffffffffffffffL, min, max)(_.num, apply(_))
}
