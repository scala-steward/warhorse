package scash.warhorse.core.number

import scash.warhorse.core._
import scash.warhorse.gen
import zio.test.DefaultRunnableSpec
import zio.test.Assertion._
import zio.test._

import scala.util.Try
import scodec.bits.ByteVector

object UintSpec extends DefaultRunnableSpec {
  val spec = suite("Uint8Spec")(
    testM("symmetry")(check(gen.uint8)(u => assert(u.bytes.decode[Uint8])(equalTo(u)))),
    testM("<<")(
      check(gen.uint8, Gen.int(0, 8)) {
        case (u, i) =>
          val expected = (u.num.toLong << i) & 0xFFL
          if (Uint8.max > expected) assert(u << i)(equalTo(Uint8(expected.toInt)))
          else assert(Try(u << i).isFailure)(isTrue)
      }
    ),
    testM(">>")(check(gen.uint8, Gen.int(0, 100)) {
      case (u, i) =>
        val r        = u >> i
        val expected = if (i > 31) Uint8.min else Uint8((u.num.toLong >> i).toInt)
        assert(r)(equalTo(expected))
    }),
    suite("serdes")(
      test("0")(assert(ByteVector(0.toByte).decode[Uint8])(equalTo(Uint8.min))),
      test("1")(assert(ByteVector(1.toByte).decode[Uint8])(equalTo(Uint8.one))),
      test("255")(assert(ByteVector(255.toByte).decode[Uint8])(equalTo(Uint8.max)))
    ),
    suite("edge cases")(
      test("255 + 1")(assert(Try(Uint8.max + Uint8.one).toOption)(isNone)),
      test("0 - 1")(assert(Try(Uint8.min - Uint8.one).toOption)(isNone)),
      test("-1")(assert(Try(Uint8(-1)).toOption)(isNone)),
      test("too large bytevector")(assert(Try(ByteVector(1, 1, 1).decodeExactly[Uint8]).toOption)(isNone))
    )
  )
}
