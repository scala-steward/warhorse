package scash.warhorse.core.number

import scash.warhorse.core.typeclass.CNumeric
import scash.warhorse.core._
import scodec.Codec
import scodec.codecs.uint8L

import scala.util.Try

protected case class Uint8(num: Int) extends AnyVal

object Uint8 {

  def apply(i: Int): Uint8 = new Uint8(verify(i)(min, max))

  def apply(n: BigInt): Uint8 = new Uint8(verify(n)(min, max).toInt)

  def safe(i: Int): Option[Uint8] = Try(apply(i)).toOption

  val min = new Uint8(0)
  val one = new Uint8(1)
  val max = new Uint8(255)

  implicit val uint8Codec: Codec[Uint8] = uint8L.xmap(apply(_), _.num)

  implicit val uint8Numeric: CNumeric[Uint8] =
    CNumeric[Uint8](0xFF, min, max)(_.num, apply(_))

}
