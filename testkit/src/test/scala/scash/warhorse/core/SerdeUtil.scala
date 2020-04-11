package scash.warhorse.core

import scash.warhorse.Result
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.typeclass.{ CNumeric, Serde }
import scodec.bits.ByteVector
import zio.test.Assertion.Render.param
import zio.test.Assertion.isSubtype
import zio.test.{ assert, Assertion }

object SerdeUtil {
  def success[A](expected: A): Assertion[Result[A]] =
    Assertion.assertion("success")(param(expected))(_ == Successful(expected))

  def failure = isSubtype[Failure](Assertion.anything)

  def symmetry[A: Serde: CNumeric](u: A) =
    assert(u.bytes.decode[A])(success[A](u))

  def symmetryHex[A: Serde: CNumeric](u: A) =
    assert(ByteVector.fromValidHex(u.hex).decode[A])(success(u))

}
