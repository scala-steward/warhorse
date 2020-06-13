package scash.warhorse.core.number

import scash.warhorse.core.typeclass.{ CNumeric, Serde }
import scodec.bits.BitVector
import scodec.codecs._

protected[warhorse] case class Uint5(num: Byte) extends AnyVal

object Uint5 {
  def apply(i: Byte): Uint5 = new Uint5(verify(i)(zero, max))

  def cast(i: Char): Uint5 = apply((i & 0x1f).toByte)

  val zero = new Uint5(0)
  val one  = new Uint5(1)
  val max  = new Uint5(31)

  implicit val uint5Serde: Serde[Uint5] =
    Serde[Uint5](
      bits(5).xmap[Uint5](b => apply(b.toByte(false)), u => BitVector(u.num).drop(3))
    )

  implicit val vecUint5Serde = Serde(vector(uint5Serde.codec))

  implicit val uint5Numeric: CNumeric[Uint5] =
    CNumeric[Uint5](0x1f, zero, max)(u => BigInt(u.num.toInt), b => apply(b.toByte))

}
