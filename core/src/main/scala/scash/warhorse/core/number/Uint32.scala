package scash.warhorse.core.number

import scash.warhorse.core.typeclass.CNumeric
import scodec.Codec
import scodec.codecs.uint32L

import scala.util.Try

protected case class Uint32(num: Long) extends AnyVal

object Uint32 {
  def apply(long: Long): Uint32 = new Uint32(verify(long)(min, max))

  def safe(long: Long): Option[Uint32] = Try(apply(long)).toOption

  val min  = new Uint32(0)
  val zero = min
  val one  = new Uint32(1)
  val max  = new Uint32(4294967295L)

  implicit val uint32Codec: Codec[Uint32] = uint32L.xmap[Uint32](apply(_), _.num)

  implicit val uint32Numeric: CNumeric[Uint32] =
    CNumeric[Uint32](0xFFFFFFFFL, min, max)(_.num, l => apply(l.toLong))
}
