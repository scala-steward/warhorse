package scash.warhorse.core.number

import scash.warhorse.core._
import scash.warhorse.core.CNumericUtil._
import scash.warhorse.core.SerdeUtil._
import scash.warhorse.util._
import scash.warhorse.gen

import scodec.bits.ByteVector
import zio.test.Assertion.equalTo
import zio.test._

object Uint64Spec extends DefaultRunnableSpec {
  val spec = suite("Uint64")(
    suite("CNumeric")(
      testM("shiftL")(check(gen.uint64, Gen.int(0, 64))(shiftL)),
      testM("shiftR")(check(gen.uint64, Gen.int(0, 100))(shiftR)),
      testM("sum")(check(gen.uint64, gen.uint64)(sum)),
      testM("substract")(check(gen.uint64, gen.uint64)(substract)),
      testM("multiply")(check(gen.uint64, gen.uint64)(mult)),
      testM("bitwiseInclusive |")(check(gen.uint64, gen.uint64)(bitwiseInclusive)),
      testM("bitwiseExclusive ^")(check(gen.uint64, gen.uint64)(bitwiseExclusive)),
      testM("bitwiseAnd &")(check(gen.uint64, gen.uint64)(bitwiseAnd)),
      testM("test safe")(check(gen.positiveBigInts)(safe[Uint64](Uint64.safe))),
      test("out of bounds")(outofBounds[Uint64]),
      test("test bounds")(testBounds[Uint64](BigInt(0), BigInt("18446744073709551615")))
    ),
    suite("Serde")(
      testM("symmetry")(check(gen.uint64)(symmetry)),
      testM("symmetryHex")(check(gen.uint64)(symmetryHex)),
      test("sym min")(symmetry(Uint64.min)),
      test("sym max")(symmetry(Uint64.max)),
      test("0")(assert(ByteVector.fill(8)(0).decode[Uint64])(success(Uint64.min))),
      test("1")(assert((1.toByte +: ByteVector.fill(7)(0)).decode[Uint64])(success(Uint64.one))),
      test("Uint32.max + 1")(
        assert(((ByteVector.fill(4)(0) :+ 1.toByte) ++ ByteVector.fill(3)(0)).decode[Uint64])(
          success(Uint64(4294967296L))
        )
      ),
      test("0xFFFFFFFF  == Uint32.max")(
        assert((ByteVector.fill(4)(0xFF) ++ ByteVector.fill(4)(0)).decode[Uint64])(success(Uint64(4294967295L)))
      ),
      test("0xFF == Uint8.max")(
        assert((0xFF.toByte +: ByteVector.fill(7)(0)).decode[Uint64])(success(Uint64(255)))
      ),
      test("max to hex")(assert(Uint64.max.hex)(equalTo("ffffffffffffffff"))),
      test("min to hex")(assert(Uint64.min.hex)(equalTo("0000000000000000"))),
      test("0xffffffffffffffff == Uint64.max")(
        assert(ByteVector.fill(8)(0xFF).decode[Uint64])(success(Uint64.max))
      ),
      test("too large bytevector 0")(assert(ByteVector.fill(9)(0).decodeExact[Uint64])(failure)),
      test("too large bytevector 1")(assert(ByteVector.fill(9)(1).decodeExact[Uint64])(failure))
    )
  )
}
