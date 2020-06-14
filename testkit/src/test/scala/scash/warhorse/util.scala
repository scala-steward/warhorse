package scash.warhorse

import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.Err.ParseError

import scala.io.{ BufferedSource, Source }

import io.circe.parser._
import io.circe._

import zio.{ Managed, Task, UIO, ZIO }
import zio.test.{ Assertion, BoolAlgebra, TestResult }
import zio.test.Assertion.Render.param
import zio.test.Assertion.isSubtype

object util {
  def success[A](expected: A): Assertion[Result[A]] =
    Assertion.assertion("success")(param(expected))(_ == Successful(expected))

  def successResult[A](expected: Result[A]) = success[A](expected.require)

  def failure = isSubtype[Failure](Assertion.anything)

  def success[A]() = isSubtype[Successful[A]](Assertion.anything)

  def parseJson[A: Decoder](js: String): Result[A] =
    decode[A](js).fold(e => Failure(ParseError("JsonParser", e.getMessage)), Successful(_))

  def jsonFromFile[A: Decoder](fileName: String): Task[Result[A]] =
    openFile(fileName)
      .map(b => parseJson[A](b.getLines().mkString))
      .use(ZIO.succeed(_))

  def jsonFromCSV[A: Decoder](fileName: String)(f: A => TestResult) =
    jsonFromCSVM[A](fileName)(a => ZIO.succeed(f(a)))

  def jsonFromCSVM[A: Decoder](fileName: String)(f: A => UIO[TestResult]) =
    openFile(fileName)
      .map(b => parseJson[List[A]](b.getLines().mkString))
      .use(r => ZIO.foreach(r.require)(f))
      .map(BoolAlgebra.collectAll(_).get)

  def rowCoder[A](f: List[String] => A): Decoder[A] = Decoder.decodeList[String].map(f)

  implicit def csvDecoder[A: Decoder]: Decoder[List[A]] = Decoder.decodeList[A]

  private def openFile(fileName: String): Managed[Throwable, BufferedSource] =
    Managed.makeEffect(Source.fromResource(fileName))(_.close())
}
