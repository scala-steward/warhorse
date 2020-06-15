package scash.warhorse.core.number

import scash.warhorse.core.CNumericUtil._
import scash.warhorse.gen
import scash.warhorse.core._
import zio.test.DefaultRunnableSpec
import zio.test._
import zio.test.Assertion._
import scodec.bits._

object Uint5Spec extends DefaultRunnableSpec {
  val spec = suite("Uint5Spec")(
    suite("CNumeric")(
      testM("shiftL")(check(gen.uint5, Gen.int(0, 8))(shiftL)),
      testM("shiftR")(check(gen.uint5, Gen.int(0, 100))(shiftR)),
      testM("sum")(check(gen.uint5, gen.uint5)(sum)),
      testM("substract")(check(gen.uint5, gen.uint5)(substract)),
      testM("multiply")(check(gen.uint5, gen.uint5)(mult)),
      testM("bitwiseInclusive |")(check(gen.uint5, gen.uint5)(bitwiseInclusive)),
      testM("bitwiseExclusive ^")(check(gen.uint5, gen.uint5)(bitwiseExclusive)),
      testM("bitwiseAnd &")(check(gen.uint5, gen.uint5)(bitwiseAnd)),
      test("out of bounds")(outofBounds[Uint5]),
      test("test bounds")(testBounds[Uint5](BigInt(0), BigInt(0x1f)))
    ),
    test("cast b")(assert(Uint5.cast('b'))(equalTo(Uint5(2.toByte)))),
    test("cast i")(assert(Uint5.cast('i'))(equalTo(Uint5(9.toByte)))),
    test("cast t")(assert(Uint5.cast('t'))(equalTo(Uint5(20.toByte)))),
    suite("serde")(
      test("toBin Max")(assert(Uint5.max.bits)(equalTo(bin"11111"))),
      test("toBin Zero")(assert(Uint5.zero.bits)(equalTo(bin"00000"))),
      testM("symmetry")(check(gen.uint5)(u => assert(u.bits.decode_[Uint5])(equalTo(u))))
    )
  )

}
