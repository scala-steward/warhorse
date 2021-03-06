package scash.warhorse.core.number

import scash.warhorse.core.CNumericUtil._
import scash.warhorse.util._
import scash.warhorse.core._
import scash.warhorse.gen

import scodec.bits.ByteVector

import zio.test.Assertion.equalTo
import zio.test._

object Int32Spec extends DefaultRunnableSpec {
  val spec = suite("Int32")(
    suite("CNumeric")(
      testM("shiftL")(check(gen.int32, Gen.int(0, 32))(shiftL)),
      testM("shiftR")(check(gen.int32, Gen.int(0, 100))(shiftR)),
      testM("sum")(check(gen.int32, gen.int32)(sum)),
      testM("substract")(check(gen.int32, gen.int32)(substract)),
      testM("multiply")(check(gen.int32, gen.int32)(mult)),
      testM("negation")(check(gen.int32)(i => assert(-i)(equalTo_(Int32(-i.num))))),
      testM("bitwiseInclusive |")(check(gen.int32, gen.int32)(bitwiseInclusive)),
      testM("bitwiseExclusive ^")(check(gen.int32, gen.int32)(bitwiseExclusive)),
      testM("bitwiseAnd &")(check(gen.int32, gen.int32)(bitwiseAnd)),
      testM("test safe")(check(gen.bigInts)(safe[Int32](Int32.safe))),
      test("out of bounds")(outofBounds[Int32]),
      test("test bounds")(testBounds[Int32](BigInt(-2147483648), BigInt(2147483647)))
    ),
    suite("Serde")(
      test("0")(assert(ByteVector.low(4).decodeExact[Int32])(success(Int32.zero))),
      test("1")(assert((1.toByte +: ByteVector.low(3)).decodeExact[Int32])(success(Int32.one))),
      test("-1")(assert(ByteVector.fill(4)(0xff).decodeExact[Int32])(success(-Int32.one))),
      test("max to hex")(assert(Int32.max.hex)(equalTo("ffffff7f"))),
      test("min to hex")(assert(Int32.min.hex)(equalTo("00000080"))),
      test("0xffffff7f == Int32.max")(
        assert((ByteVector.fill(3)(0xff) :+ 0x7f.toByte).decodeExact[Int32])(success(Int32.max))
      ),
      test("0x00000080 == Int32.min")(
        assert((ByteVector.low(3) :+ 0x80.toByte).decodeExact[Int32])(success(Int32.min))
      ),
      test("too large bytevector 0")(assert(ByteVector.low(5).decodeExact[Int32])(failure)),
      test("too large bytevector 1")(assert(ByteVector.high(5).decodeExact[Int32])(failure))
    )
  )
}
