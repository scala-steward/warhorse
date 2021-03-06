package scash.warhorse.core.number

import scash.warhorse.core._
import scash.warhorse.core.CNumericUtil._
import scash.warhorse.util._
import scash.warhorse.gen

import scodec.bits.ByteVector

import zio.test.DefaultRunnableSpec
import zio.test.Assertion._
import zio.test._

object Uint8Spec extends DefaultRunnableSpec {
  val spec = suite("Uint8")(
    suite("CNumeric")(
      testM("shiftL")(check(gen.uint8, Gen.int(0, 8))(shiftL)),
      testM("shiftR")(check(gen.uint8, Gen.int(0, 100))(shiftR)),
      testM("sum")(check(gen.uint8, gen.uint8)(sum)),
      testM("substract")(check(gen.uint8, gen.uint8)(substract)),
      testM("multiply")(check(gen.uint8, gen.uint8)(mult)),
      testM("bitwiseInclusive |")(check(gen.uint8, gen.uint8)(bitwiseInclusive)),
      testM("bitwiseExclusive ^")(check(gen.uint8, gen.uint8)(bitwiseExclusive)),
      testM("bitwiseAnd &")(check(gen.uint8, gen.uint8)(bitwiseAnd)),
      testM("test safe")(check(gen.positiveBigInts)(safe[Uint8](i => Uint8.safe(i.toInt)))),
      test("out of bounds")(outofBounds[Uint8]),
      test("test bounds")(testBounds[Uint8](BigInt(0), BigInt(0xff)))
    ),
    suite("Serde")(
      test("0")(assert(ByteVector(0.toByte).decode[Uint8])(success(Uint8.min))),
      test("1")(assert(ByteVector(1.toByte).decode[Uint8])(success(Uint8.one))),
      test("0xFF")(assert(ByteVector(0xff.toByte).decode[Uint8])(success(Uint8.max))),
      test("255")(assert(ByteVector(255.toByte).decode[Uint8])(success(Uint8.max))),
      test("max to hex")(assert(Uint8.max.hex)(equalTo("ff"))),
      test("min to hex")(assert(Uint8.min.hex)(equalTo("00"))),
      test("too large bytevector 0")(assert(ByteVector(0, 0).decodeExact[Uint8])(failure)),
      test("too large bytevector 1")(assert(ByteVector(1, 1).decodeExact[Uint8])(failure))
    )
  )
}
