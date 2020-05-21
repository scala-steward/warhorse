package scash.warhorse.core.typeclass

import scash.warhorse.core._
import scash.warhorse.core.CNumericUtil._
import scash.warhorse._

import zio.test.Assertion._
import zio.test._
import zio.test.laws._

object CNumericSpec extends DefaultRunnableSpec {
  val consistencyLaw = new Laws.Law1[CNumeric]("consistencyLaw") {
    def apply[A: CNumeric](a: A): TestResult =
      assert(CNumeric[A].lift(CNumeric[A].num(a)))(equalTo_(a))
  }

  val rangeLaw = new Laws.Law1[CNumeric]("rangeLaw") {
    def apply[A: CNumeric](a: A): TestResult =
      assert(
        CNumeric[A].min <= CNumeric[A].max &&
          (a > CNumeric[A].min || a === CNumeric[A].min) &&
          (a < CNumeric[A].max || a === CNumeric[A].max)
      )(isTrue)
  }

  val reflexiveLaw = new Laws.Law1[CNumeric]("reflexiveLaw") {
    def apply[A: CNumeric](a: A): TestResult =
      assert(
        (CNumeric[A].num(a) == CNumeric[A].num(a)) && (a === a)
      )(isTrue)
  }

  val additiveIdentityLaw = new Laws.Law1[CNumeric]("additiveIdentityLaw") {
    def apply[A: CNumeric](a: A): TestResult =
      assert(a + CNumeric[A].lift(BigInt(0)))(equalTo_(a))
  }

  val subtractiveIdentityLaw = new Laws.Law1[CNumeric]("subtractiveIdentityLaw") {
    def apply[A: CNumeric](a: A): TestResult =
      assert(a - CNumeric[A].lift(BigInt(0)))(equalTo_(a))
  }

  val multiplicativeIdentityLaw = new Laws.Law1[CNumeric]("multiplicativeIdentityLaw") {
    def apply[A: CNumeric](a: A): TestResult =
      assert(a * CNumeric[A].lift(BigInt(1)))(equalTo_(a))
  }

  val underylingIdentityLaw = new Laws.Law2[CNumeric]("underylingIdentityLaw") {
    def apply[A: CNumeric](a1: A, a2: A): TestResult =
      assert(
        (a1 !== a2) ||
          ((a1 === a2) || (CNumeric[A].num(a1) == CNumeric[A].num(a2)))
      )(isTrue)
  }

  val symmetryLaw = new Laws.Law2[CNumeric]("symmetryLaw") {
    def apply[A: CNumeric](a1: A, a2: A): TestResult =
      assert(
        (a1 !== a2) ||
          ((a1 === a2) || (CNumeric[A].num(a1) == CNumeric[A].num(a2)) &&
            (a2 === a1) || (CNumeric[A].num(a2) == CNumeric[A].num(a1)))
      )(isTrue)
  }

  val transitivityLaw = new Laws.Law3[CNumeric]("transitivityLaw") {
    def apply[A: CNumeric](a1: A, a2: A, a3: A): TestResult =
      assert(!(a1 === a2 && a2 === a3) || (a1 === a3))(isTrue)
  }

  val laws: Laws[CNumeric] =
    consistencyLaw +
      rangeLaw +
      reflexiveLaw +
      additiveIdentityLaw +
      subtractiveIdentityLaw +
      multiplicativeIdentityLaw +
      underylingIdentityLaw +
      symmetryLaw +
      transitivityLaw

  val spec = suite("CNumericSpec")(
    testM("uint8")(laws.run(gen.uint8)),
    testM("uint32")(laws.run(gen.uint32)),
    testM("uint64")(laws.run(gen.uint64)),
    testM("int32")(laws.run(gen.int32)),
    testM("int64")(laws.run(gen.int64))
  )
}
