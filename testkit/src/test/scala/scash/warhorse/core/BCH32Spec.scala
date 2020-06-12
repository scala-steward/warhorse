package scash.warhorse.core

import scash.warhorse.core.number.{ Uint5, Uint64 }
import zio.ZIO

import zio.test._
import zio.test.Assertion._

import Predef.intWrapper

object BCH32Spec extends DefaultRunnableSpec {

  // format: off
  val spec = suite("BCH32Spec")(
    suite("polyMod")(
      test("empty")(assert(BCH32.polyMod(Vector.empty))(equalTo(Uint64.zero))),
      testM("all Uint5")(
        ZIO.foreach(1 to 31) { n =>
          def polyVec(pad: Int, expected: Uint64) =
            assert(BCH32.polyMod(Vector(Uint5(n.toByte)) ++ Vector.fill(pad)(Uint5.zero)))(equalTo(expected))
          ZIO.succeed(
            polyVec(0, Uint64(0x21 ^ n)) &&
              polyVec(1, Uint64(0x401 ^ (n << 5))) &&
              polyVec(2, Uint64(0x8001 ^ (n << 10))) &&
              polyVec(3, Uint64(0x100001 ^ (n << 15))) &&
              polyVec(4, Uint64(0x2000001 ^ (n << 20))) &&
              polyVec(5, Uint64(0x40000001 ^ (n << 25))) &&
              polyVec(6, Uint64(0x800000001L) ^ (Uint64(n) << 30)) &&
              polyVec(7, Uint64(0x98f2bc8e60L) ^ (Uint64(n) << 35))
            )}
          .map(BoolAlgebra.collectAll(_).get)
        ),
      test("array") {
        val poly = (Vector.fill(8)(0) ++ (0 to 31).toVector ++ Vector.fill(8)(0)).map(i => Uint5(i.toByte))
        assert(BCH32.polyMod(poly))(equalTo(Uint64(0x724afe7af2L)))
      }
    ),
    suite("checkSum")(
      test("prefix")(assert(BCH32.valid("prefix:x64nx6hz"))(isTrue)),
      test("p")(assert(BCH32.valid("p:gpf8m4h7"))(isTrue)),
      test("bitcoincash")(assert(BCH32.valid("bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a"))(isTrue)),
      test("bchtest")(assert(BCH32.valid("bchtest:testnetaddress4d6njnut"))(isTrue)),
      test("bchreg")(assert(BCH32.valid("bchreg:555555555555555555555555555555555555555555555udxmlmrz"))(isTrue))
    )
  )
  // format: off
}
