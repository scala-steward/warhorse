package scash.warhorse.core.number

import scash.warhorse.core.typeclass.CNumeric
import scodec.Codec
import scodec.codecs.int64L

import scala.util.Try

protected case class Int64(num: Long) extends AnyVal

object Int64 {
  def apply(n: Long): Int64 = new Int64(n)

  def apply(n: BigInt): Int64 = new Int64(verify(n)(min, max).toLong)

  def safe(bigInt: BigInt): Option[Int64] = Try(apply(bigInt)).toOption

  val min  = new Int64(-9223372036854775808L)
  val zero = new Int64(0)
  val one  = new Int64(1)
  val max  = new Int64(9223372036854775807L)

  /** Generates a random Int64 */
  implicit val int64LCodec: Codec[Int64] = int64L.xmap(apply(_), _.num)

  implicit val int64Numeric: CNumeric[Int64] =
    CNumeric[Int64](0xFFFFFFFFFFFFFFFFL, min, max)(_.num, apply(_))
}
