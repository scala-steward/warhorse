package scash.warhorse.core.typeclass

import scala.util.Try

trait CNumeric[A] {
  def andMask: BigInt
  def num: A => BigInt
  def lift: BigInt => A
  def min: A
  def max: A

  def sum[A1: CNumeric](a: A, a1: A1): A = lift(num(a) + CNumeric[A1].num(a1))
  def sub[A1: CNumeric](a: A, a1: A1): A = lift(num(a) - CNumeric[A1].num(a1))
  def mul[A1: CNumeric](a: A, a1: A1): A = lift(num(a) * CNumeric[A1].num(a1))

  def gt[A1: CNumeric](a: A, a1: A1): Boolean  = num(a) > CNumeric[A1].num(a1)
  def gte[A1: CNumeric](a: A, a1: A1): Boolean = num(a) >= CNumeric[A1].num(a1)
  def lt[A1: CNumeric](a: A, a1: A1): Boolean  = num(a) < CNumeric[A1].num(a1)
  def lte[A1: CNumeric](a: A, a1: A1): Boolean = num(a) <= CNumeric[A1].num(a1)
  def eq[A1: CNumeric](a: A, a1: A1): Boolean  = num(a) == CNumeric[A1].num(a1)
  def neq[A1: CNumeric](a: A, a1: A1): Boolean = num(a) != CNumeric[A1].num(a1)

  def or[A1: CNumeric](a: A, a1: A1): A  = lift(num(a) | CNumeric[A1].num(a1))
  def and[A1: CNumeric](a: A, a1: A1): A = lift(num(a) & CNumeric[A1].num(a1))
  def xor[A1: CNumeric](a: A, a1: A1): A = lift(num(a) ^ CNumeric[A1].num(a1))

  def negative(a: A): A = lift(-num(a))

  def shiftL(a: A, a1: Int): A = lift((num(a) << a1) & andMask)

  def shiftR(a: A, a1: Int): A = lift(num(a) >> a1)
}

object CNumeric   {

  def apply[A](implicit n: CNumeric[A]): CNumeric[A] = n

  def apply[A](mask: BigInt, minimum: A, maximum: A)(n: A => BigInt, app: BigInt => A): CNumeric[A] =
    new CNumeric[A] {
      def andMask = mask
      def num     = n
      def lift    = app
      def min     = minimum
      def max     = maximum
    }

  def inRange[A: CNumeric](u: BigInt): Boolean = {
    val C = CNumeric[A]
    C.num(C.min) <= u && u <= C.num(C.max)
  }

  def safe[A: CNumeric](u: BigInt): Option[A] = Try(CNumeric[A].lift(u)).toOption
}

trait CNumericSyntax {
  implicit class CNumericSyntaxOps[A: CNumeric](a: A) {
    def +[A1: CNumeric](num: A1): A         = CNumeric[A].sum(a, num)
    def -[A1: CNumeric](num: A1): A         = CNumeric[A].sub(a, num)
    def *[A1: CNumeric](num: A1): A         = CNumeric[A].mul(a, num)
    def >[A1: CNumeric](num: A1): Boolean   = CNumeric[A].gt(a, num)
    def >=[A1: CNumeric](num: A1): Boolean  = CNumeric[A].gte(a, num)
    def <[A1: CNumeric](num: A1): Boolean   = CNumeric[A].lt(a, num)
    def <=[A1: CNumeric](num: A1): Boolean  = CNumeric[A].lte(a, num)
    def ===[A1: CNumeric](num: A1): Boolean = CNumeric[A].eq(a, num)
    def !==[A1: CNumeric](num: A1): Boolean = CNumeric[A].neq(a, num)
    def <<(num: Int): A                     = CNumeric[A].shiftL(a, num)
    def >>(num: Int): A                     = CNumeric[A].shiftR(a, num)

    def |(num: A): A = CNumeric[A].or(a, num)
    def &(num: A): A = CNumeric[A].and(a, num)
    def ^(num: A): A = CNumeric[A].xor(a, num)
    def unary_- : A  = CNumeric[A].negative(a)
  }
}
