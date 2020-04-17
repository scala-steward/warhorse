package scash.warhorse

import scash.warhorse.Result.{ Failure, Successful }
import zio.Managed
import zio.test.Assertion
import zio.test.Assertion.Render.param
import zio.test.Assertion.isSubtype

import scala.io.{ BufferedSource, Source }

object util {
  def success[A](expected: A): Assertion[Result[A]] =
    Assertion.assertion("success")(param(expected))(_ == Successful(expected))

  def successResult[A](expected: Result[A]) = success[A](expected.require)

  def failure = isSubtype[Failure](Assertion.anything)

  def successful[A] = isSubtype[Successful[A]](Assertion.anything)

  def openFile(fileName: String): Managed[Throwable, BufferedSource] =
    Managed.makeEffect(Source.fromResource(fileName))(_.close())
}
