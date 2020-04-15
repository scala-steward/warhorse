package scash.warhorse

import scash.warhorse.Result.{ Failure, Successful }
import zio.test.Assertion
import zio.test.Assertion.Render.param
import zio.test.Assertion.isSubtype

object util {
  def success[A](expected: A): Assertion[Result[A]] =
    Assertion.assertion("success")(param(expected))(_ == Successful(expected))

  def failure = isSubtype[Failure](Assertion.anything)

  def successful[A] = isSubtype[Successful[A]](Assertion.anything)
}
