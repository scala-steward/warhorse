package scash.warhorse.rpc.responses

import io.circe.Decoder
import io.circe.parser._

import scash.warhorse.Err.ParseError
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.util._
import scash.warhorse.Result

import zio.{ Task, ZIO }

object util {
  def parseJson[A: Decoder](js: String): Result[A] =
    decode[A](js).fold(e => Failure(ParseError("JsonParser", e.getMessage)), Successful(_))

  def parseJsonfromFile[A: Decoder](fileName: String): Task[Result[A]] =
    openFile(fileName)
      .map(b => parseJson[A](b.getLines().mkString))
      .use(ZIO.succeed(_))
}
