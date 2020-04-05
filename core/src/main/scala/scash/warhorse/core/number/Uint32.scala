package scash.warhorse.core.number

import scash.warhorse.core.typeclass.CNumeric
import scodec.Codec
import scodec.codecs.uint32L

import scala.util.Try

protected case class Uint32(num: Long) extends AnyVal

object Uint32 {
  def apply(l: Long): Uint32 = new Uint32(verify(l)(min, max))

  def safe(l: Long): Option[Uint32] = Try(apply(l)).toOption

  val min  = new Uint32(0)
  val zero = min
  val one  = new Uint32(0)
  val max  = new Uint32(4294967295L)

  implicit val uint32Codec: Codec[Uint32] = uint32L.xmap[Uint32](apply(_), _.num)

  implicit val uint32Numeric: CNumeric[Uint32] =
    CNumeric[Uint32](0xFFFFFFFFL)(_.num, l => apply(l.toLong))
}
