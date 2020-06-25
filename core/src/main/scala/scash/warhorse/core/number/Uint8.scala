package scash.warhorse.core.number

import scash.warhorse.core.typeclass.{ CNumeric, Serde }
import scash.warhorse.core._
import scodec.codecs.uint8L

import scala.util.Try

protected[warhorse] case class Uint8(num: Int) extends AnyVal

object Uint8 {

  def apply(i: Int): Uint8 = new Uint8(verify(i)(min, max))

  def apply(n: BigInt): Uint8 = new Uint8(verify(n)(min, max).toInt)

  def safe(i: Int): Option[Uint8] = Try(apply(i)).toOption

  val min = new Uint8(0)
  val one = new Uint8(1)
  val max = new Uint8(255)

  implicit val uint8Serde: Serde[Uint8] =
    Serde[Uint8](uint8L.as[Uint8])

  implicit val uint8Numeric: CNumeric[Uint8] =
    CNumeric[Uint8](0xff, min, max)(_.num, apply(_))

}
