package scash.warhorse.core.number

import scash.warhorse.gen
import scash.warhorse.core.CNumericUtil._
import scash.warhorse.core._

import scodec.bits.ByteVector

import scala.util.Try

import zio.test.Assertion._
import zio.test._
import zio.test.Assertion.equalTo

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
      test("out of bounds")(outofBounds[Uint32]),
      test("test bounds")(testBounds[Uint32](BigInt(0), BigInt(4294967295L)))
    ),
    suite("Serde")(
      testM("symmetry")(check(gen.uint32)(symmetry)),
      test("0")(assert(ByteVector(0, 0, 0, 0).decode[Uint32])(equalTo_(Uint32.min))),
      test("1")(assert(ByteVector(1, 0, 0, 0).decode[Uint32])(equalTo_(Uint32.one))),
      test("16777216")(assert(ByteVector(0, 0, 0, 1).decode[Uint32])(equalTo_(Uint32(16777216)))),
      test("65536")(assert(ByteVector(0, 0, 1, 0).decode[Uint32])(equalTo_(Uint32(65536)))),
      test("0xFF")(assert(ByteVector(0xFF, 0, 0, 0).decode[Uint32])(equalTo_(Uint32(255)))),
      test("max to hex")(assert(Uint32.max.hex)(equalTo("ffffffff"))),
      test("min to hex")(assert(Uint32.min.hex)(equalTo("00000000"))),
      test("0xffffffff == Uint32.max")(assert(ByteVector(0xff, 0xff, 0xff, 0xff).decode[Uint32])(equalTo_(Uint32.max))),
      test("too large bytevector 0")(assert(Try(ByteVector(0, 0, 0, 0, 0).decodeExactly[Uint32]).toOption)(isNone)),
      test("too large bytevector 1")(assert(Try(ByteVector(1, 1, 1, 1, 1).decodeExactly[Uint32]).toOption)(isNone))
    )
  )
}
