package scash.warhorse.core.number

import zio.test.DefaultRunnableSpec
import zio.test.Assertion._
import zio.test._

import scash.warhorse.core._

object UintSpec extends DefaultRunnableSpec {
  val spec = suite("Uint8Spec")(
    test("symmetry")(assert(Uint8(8).bytes.decode[Uint8])(equalTo(Uint8(8)))),
    test("substraction")(assert(Uint8(3) - Uint8(3))(equalTo(Uint8.min))) /*,
    test("symmetry")(assert(Uint8.safe(1) * Uint8.safe(1))(equalTo(Uint8.one))),
    test("symmetry")(assert(Uint8(4) >= Uint8(3))(isTrue)),
    test("symmetry")(assert(Uint8(4) > Uint8(3))(isTrue)),
    test("symmetry")(assert(Uint8(4) <= Uint8(3))(isFalse)),
    test("symmetry")(assert(Uint8(4) < Uint8(3))(isFalse))*/
  )
}
