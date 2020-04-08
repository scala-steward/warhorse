package scash.warhorse.core

import scash.warhorse.core.typeclass.CNumeric
import scodec.Codec
import zio.test.Assertion.Render.param
import zio.test.Assertion.{ equalTo, isNone, isTrue }
import zio.test.{ assert, Assertion }

import scala.util.Try

object CNumericUtil {
  def equalTo_[A: CNumeric](expected: A): Assertion[A] =
    Assertion.assertion("equalTo")(param(expected))(_ === expected)

  def shiftL[A: CNumeric](u: A, i: Int) = {
    val Cnum     = CNumeric[A]
    val expected = (Cnum.num(u) << i) & Cnum.andMask
    if (Cnum.max > expected) assert(u << i)(equalTo(Cnum.lift(expected)))
    else assert(Try(u << i).isFailure)(isTrue)
  }

  def shiftR[A: CNumeric](u: A, i: Int, size: Int) = {
    val Cnum = CNumeric[A]
    val r    = u >> i
    val expected =
      if (i >= size) Cnum.min
      else Cnum.lift(Cnum.num(u) >> i)
    assert(r)(equalTo_(expected))
  }

  def sum[A: CNumeric](u1: A, u2: A) = {
    val expected = CNumeric[A].num(u1) + CNumeric[A].num(u2)
    if (expected > CNumeric[A].num(CNumeric[A].max)) {
      if (Try(u1 + u2).isSuccess) Predef.println(s"u1 $u1 u2 $u2 sum ${u1 + u2}")
      assert(Try(u1 + u2).isFailure)(isTrue)
    } else assert(u1 + u2)(equalTo_(CNumeric[A].lift(expected)))
  }

  def substract[A: CNumeric](u1: A, u2: A) = {
    val expected = CNumeric[A].num(u1) - CNumeric[A].num(u2)
    if (expected < CNumeric[A].num(CNumeric[A].min)) assert(Try(u1 - u2).isFailure)(isTrue)
    else assert(u1 - u2)(equalTo_(CNumeric[A].lift(expected)))
  }

  def mult[A: CNumeric](u1: A, u2: A) = {
    val expected = CNumeric[A].num(u1) * CNumeric[A].num(u2)
    if (expected > CNumeric[A].num(CNumeric[A].max)) assert(Try(u1 * u2).isFailure)(isTrue)
    else assert(u1 * u2)(equalTo_(CNumeric[A].lift(expected)))
  }

  def bitwiseInclusive[A: CNumeric](u1: A, u2: A) =
    assert(u1 | u2)(equalTo_(CNumeric[A].lift(CNumeric[A].num(u1) | CNumeric[A].num(u2))))

  def bitwiseExclusive[A: CNumeric](u1: A, u2: A) =
    assert(u1 ^ u2)(equalTo_(CNumeric[A].lift(CNumeric[A].num(u1) ^ CNumeric[A].num(u2))))

  def bitwiseAnd[A: CNumeric](u1: A, u2: A) =
    assert(u1 & u2)(equalTo_(CNumeric[A].lift(CNumeric[A].num(u1) & CNumeric[A].num(u2))))

  def symmetry[A: Codec: CNumeric](u: A) = assert(u.bytes.decode[A])(equalTo_(u))

  def outofBounds[A: CNumeric] =
    assert(Try(CNumeric[A].max + CNumeric[A].lift(BigInt(1))).toOption)(isNone) &&
      assert(Try(CNumeric[A].min - CNumeric[A].lift(BigInt(1))).toOption)(isNone)

  def testBounds[A: CNumeric](min: BigInt, max: BigInt) =
    assert(CNumeric[A].num(CNumeric[A].min))(equalTo(min)) &&
      assert(CNumeric[A].num(CNumeric[A].max))(equalTo(max))

}
