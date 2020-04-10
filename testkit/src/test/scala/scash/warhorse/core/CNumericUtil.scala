package scash.warhorse.core

import scash.warhorse.core.typeclass.CNumeric
import scodec.Codec
import scodec.bits.ByteVector
import zio.test.Assertion.Render.param
import zio.test.Assertion.{ equalTo, isFalse, isNone, isTrue }
import zio.test.{ assert, Assertion }

import scala.util.Try

object CNumericUtil {
  def equalTo_[A: CNumeric](expected: A): Assertion[A] =
    Assertion.assertion("equalTo")(param(expected))(_ === expected)

  def inRange[A: CNumeric](u: BigInt): Boolean = CNumeric.inRange[A](u)

  def safe[A: CNumeric](f: BigInt => Option[A])(u: BigInt) =
    f(u) match {
      case Some(a) => assert(CNumeric[A].num(a))(equalTo_(u))
      case None    => assert(inRange[A](u))(isFalse)
    }

  def shiftL[A: CNumeric](u: A, i: Int) = {
    val Cnum     = CNumeric[A]
    val expected = (Cnum.num(u) << i) & Cnum.andMask
    if (inRange[A](expected)) assert(u << i)(equalTo(Cnum.lift(expected)))
    else assert(Try(u << i).isFailure)(isTrue)
  }

  def shiftR[A: CNumeric](u: A, i: Int) = {
    val Cnum     = CNumeric[A]
    val r        = u >> i
    val expected = Cnum.lift(Cnum.num(u) >> i)
    assert(r)(equalTo_(expected))
  }

  def sum[A: CNumeric](u1: A, u2: A) = {
    val expected = CNumeric[A].num(u1) + CNumeric[A].num(u2)
    if (inRange[A](expected)) assert(u1 + u2)(equalTo_(CNumeric[A].lift(expected)))
    else assert(Try(u1 + u2).isFailure)(isTrue)
  }

  def substract[A: CNumeric](u1: A, u2: A) = {
    val expected = CNumeric[A].num(u1) - CNumeric[A].num(u2)
    if (inRange[A](expected)) assert(u1 - u2)(equalTo_(CNumeric[A].lift(expected)))
    else assert(Try(u1 - u2).isFailure)(isTrue)
  }

  def mult[A: CNumeric](u1: A, u2: A) = {
    val expected = CNumeric[A].num(u1) * CNumeric[A].num(u2)
    if (inRange[A](expected)) assert(u1 * u2)(equalTo_(CNumeric[A].lift(expected)))
    else assert(Try(u1 * u2).isFailure)(isTrue)
  }

  def bitwiseInclusive[A: CNumeric](u1: A, u2: A) =
    assert(u1 | u2)(equalTo_(CNumeric[A].lift(CNumeric[A].num(u1) | CNumeric[A].num(u2))))

  def bitwiseExclusive[A: CNumeric](u1: A, u2: A) =
    assert(u1 ^ u2)(equalTo_(CNumeric[A].lift(CNumeric[A].num(u1) ^ CNumeric[A].num(u2))))

  def bitwiseAnd[A: CNumeric](u1: A, u2: A) =
    assert(u1 & u2)(equalTo_(CNumeric[A].lift(CNumeric[A].num(u1) & CNumeric[A].num(u2))))

  def symmetry[A: Codec: CNumeric](u: A) = assert(u.bytes.decode[A])(equalTo_(u))

  def symmetryHex[A: Codec: CNumeric](u: A) =
    assert(ByteVector.fromValidHex(u.hex).decode[A])(equalTo_(u))

  def outofBounds[A: CNumeric] =
    assert(Try(CNumeric[A].max + CNumeric[A].lift(BigInt(1))).toOption)(isNone) &&
      assert(Try(CNumeric[A].min - CNumeric[A].lift(BigInt(1))).toOption)(isNone)

  def testBounds[A: CNumeric](min: BigInt, max: BigInt) =
    assert(CNumeric[A].num(CNumeric[A].min))(equalTo(min)) &&
      assert(CNumeric[A].num(CNumeric[A].max))(equalTo(max))

}
