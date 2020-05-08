package scash.warhorse.core.number

import scash.warhorse.core.typeclass.{ CNumeric, Serde }
import scodec.codecs.uint32L

import scala.util.Try

protected[warhorse] case class Uint32(num: Long) extends AnyVal

object Uint32 {
  def apply(n: Long): Uint32 = new Uint32(verify(n)(min, max))

  def apply(n: Int): Uint32 = apply(n.toLong)

  def apply(n: BigInt): Uint32 = new Uint32(verify(n)(min, max).toLong)

  def safe(n: Long): Option[Uint32] = Try(apply(n)).toOption

  val min  = new Uint32(0)
  val zero = min
  val one  = new Uint32(1)
  val max  = new Uint32(4294967295L)

  implicit val uint32Serde: Serde[Uint32] =
    Serde[Uint32](uint32L.xmap[Uint32](apply(_), _.num))

  implicit val uint32Numeric: CNumeric[Uint32] =
    CNumeric[Uint32](0xffffffffL, min, max)(_.num, apply(_))
}
