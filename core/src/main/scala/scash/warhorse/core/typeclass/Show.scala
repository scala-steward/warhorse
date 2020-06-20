package scash.warhorse.core.typeclass

import scash.warhorse.Result

trait Show[A] {
  def show(a: A): String

  def parse(s: String): Result[A]
}

object Show {
  def apply[A](implicit c: Show[A]): Show[A] = c
}

trait ShowSyntax {
  implicit class ShowSyntaxOps[A: Show](a: A) {
    def show: String = Show[A].show(a)
  }
}
