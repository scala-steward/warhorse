package scash.warhorse.core.number

import scash.warhorse.gen
import scash.warhorse.core.CNumericUtil._
import scash.warhorse.util._
import scash.warhorse.core._

import scodec.bits.ByteVector

import zio.test.Assertion._
import zio.test._

object Uint32Spec extends DefaultRunnableSpec {
  val spec = suite("Uint32")(
    suite("CNumeric")(
      testM("shiftL")(check(gen.uint32, Gen.int(0, 32))(shiftL)),
      testM("shiftR")(check(gen.uint32, Gen.int(0, 100))(shiftR)),
      testM("sum")(check(gen.uint32, gen.uint32)(sum)),
      testM("substract")(check(gen.uint32, gen.uint32)(substract)),
      testM("multiply")(check(gen.uint32, gen.uint32)(mult)),
      testM("bitwiseInclusive |")(check(gen.uint32, gen.uint32)(bitwiseInclusive)),
      testM("bitwiseExclusive ^")(check(gen.uint32, gen.uint32)(bitwiseExclusive)),
      testM("bitwiseAnd &")(check(gen.uint32, gen.uint32)(bitwiseAnd)),
      testM("test safe")(check(gen.positiveBigInts)(safe[Uint32](i => Uint32.safe(i.toLong)))),
      test("out of bounds")(outofBounds[Uint32]),
      test("test bounds")(testBounds[Uint32](BigInt(0), BigInt(4294967295L)))
    ),
    suite("Serde")(
      test("0")(assert(ByteVector(0, 0, 0, 0).decode[Uint32])(success(Uint32.min))),
      test("1")(assert(ByteVector(1, 0, 0, 0).decode[Uint32])(success(Uint32.one))),
      test("16777216")(assert(ByteVector(0, 0, 0, 1).decode[Uint32])(success(Uint32(16777216)))),
      test("65536")(assert(ByteVector(0, 0, 1, 0).decode[Uint32])(success(Uint32(65536)))),
      test("0xFF")(assert(ByteVector(0xff, 0, 0, 0).decode[Uint32])(success(Uint32(255)))),
      test("max to hex")(assert(Uint32.max.hex)(equalTo("ffffffff"))),
      test("min to hex")(assert(Uint32.min.hex)(equalTo("00000000"))),
      test("0xffffffff == Uint32.max")(
        assert(ByteVector(0xff, 0xff, 0xff, 0xff).decode[Uint32])(success(Uint32.max))
      ),
      test("too large bytevector 0")(
        assert(ByteVector(0, 0, 0, 0, 0).decodeExact[Uint32])(failure)
      ),
      test("too large bytevector 1")(assert(ByteVector(1, 1, 1, 1, 1).decodeExact[Uint32])(failure))
    )
  )
}
