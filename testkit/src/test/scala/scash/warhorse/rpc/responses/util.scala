package scash.warhorse.rpc.responses

import scash.warhorse._
import io.circe.Decoder
import io.circe.parser.parse
import scash.warhorse.Result.{ Failure, Successful }

object util {
  def parseJson[A: Decoder](js: String): Result[A] =
    for {
      json <- parse(js).fold(e => Failure(Err(e.message)), Successful(_))
      res  <- Result.fromOption(json.as[A].toOption, Err(s"Fail to cast $json into Type"))
    } yield res
}
