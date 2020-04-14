package scash.warhorse.rpc.client

import io.circe.Json

sealed trait JParam[A] {
  def toJson(a: A): Json
}

object JParam {
  def apply[A](implicit j: JParam[A]): JParam[A] = j

  implicit val stringJParam = new JParam[String] {
    def toJson(a: String): Json = Json.fromString(a)
  }
  implicit val intJParam = new JParam[Int] {
    def toJson(a: Int): Json = Json.fromInt(a)
  }
  implicit val boolJParam = new JParam[Boolean] {
    def toJson(a: Boolean): Json = Json.fromBoolean(a)
  }
}

trait JParamSyntax {
  implicit class JParamOps[A: JParam](a: A) {
    def toJson = JParam[A].toJson(a)
  }
}
