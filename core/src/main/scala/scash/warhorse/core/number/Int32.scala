package scash.warhorse.core.number

import scash.warhorse.core.typeclass.CNumeric
import scodec.Codec
import scodec.codecs.int32L

protected case class Int32(num: Int) extends AnyVal

object Int32 {
  def apply(n: Int): Int32 = new Int32(n)

  def apply(n: BigInt): Int32 = new Int32(verify(n)(min, max).toInt)

  def safe(bigInt: BigInt): Option[Int32] = CNumeric.safe[Int32](bigInt)

  val min  = new Int32(-2147483648)
  val zero = new Int32(0)
  val one  = new Int32(1)
  val max  = new Int32(2147483647)

  /** Generates a random Int32 */
  implicit val int32LCodec: Codec[Int32] = int32L.xmap(apply(_), _.num)

  implicit val int32Numeric: CNumeric[Int32] =
    CNumeric[Int32](0xffffffff, min, max)(_.num, apply(_))
}
