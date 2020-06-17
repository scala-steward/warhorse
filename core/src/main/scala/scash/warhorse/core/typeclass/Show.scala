package scash.warhorse.core.typeclass

import scash.warhorse.Result

trait Show[A] {
  def encode(a: A): String

  def decode(s: String): Result[A]
}

object Show {
  def apply[A](implicit c: Show[A]) = c
}
