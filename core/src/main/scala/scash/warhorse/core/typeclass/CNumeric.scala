package scash.warhorse.core.typeclass

import scash.warhorse.core._

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

  def or[A1: CNumeric](a: A, a1: A1): A  = lift(num(a) | CNumeric[A1].num(a1))
  def and[A1: CNumeric](a: A, a1: A1): A = lift(num(a) & CNumeric[A1].num(a1))
  def xor[A1: CNumeric](a: A, a1: A1): A = lift(num(a) ^ CNumeric[A1].num(a1))

  def negative(a: A): A = lift(-num(a))

  def shiftL(a: A, a1: Int): A = lift((num(a) << a1) & andMask)

  def shiftR(a: A, a1: Int): A = lift(num(a) >> a1)
}

object CNumeric {

  def consistencyLaw[A: CNumeric](a: A): Boolean =
    CNumeric[A].lift(CNumeric[A].num(a)) === a

  def rangeLaw[A: CNumeric](a: A): Boolean =
    CNumeric[A].min <= CNumeric[A].max &&
      a >= CNumeric[A].min && a <= CNumeric[A].max

  def reflexiveLaw[A: CNumeric](a: A): Boolean =
    (CNumeric[A].num(a) == CNumeric[A].num(a)) && (a === a)

  def symmetryLaw[A: CNumeric, A1: CNumeric](a1: A, a2: A1) =
    underylingIdentityLaw(a1, a2) && underylingIdentityLaw(a2, a1)

  def additiveIdentityLaw[A: CNumeric](a: A): Boolean =
    a + CNumeric[A].lift(BigInt(0)) === a

  def subtractiveIdentityLaw[A: CNumeric](a: A): Boolean =
    a - CNumeric[A].lift(BigInt(0)) === a

  def multiplicativeIdentityLaw[A: CNumeric](a: A): Boolean =
    a * CNumeric[A].lift(BigInt(1)) === a

  def underylingIdentityLaw[A: CNumeric, A1: CNumeric](a1: A, a2: A1): Boolean =
    !(a1 === a2) || (CNumeric[A].num(a1) == CNumeric[A1].num(a2))

  def transitivityLaw[A1: CNumeric, A2: CNumeric, A3: CNumeric](a1: A1, a2: A2, a3: A3): Boolean =
    !(a1 === a2 && a2 === a3) || (a1 === a3)

  def apply[A](implicit n: CNumeric[A]): CNumeric[A] = n

  def apply[A](mask: BigInt, minimum: A, maximum: A)(n: A => BigInt, app: BigInt => A): CNumeric[A] =
    new CNumeric[A] {
      def andMask = mask
      def num     = n
      def lift    = app
      def min     = minimum
      def max     = maximum
    }

}

trait CNumericSyntax {
  implicit class CNumericSyntaxOps[A: CNumeric](a: A) {
    def +[A1: CNumeric](num: A1): A         = CNumeric[A].sum[A1](a, num)
    def -[A1: CNumeric](num: A1): A         = CNumeric[A].sub(a, num)
    def *[A1: CNumeric](num: A1): A         = CNumeric[A].mul(a, num)
    def >[A1: CNumeric](num: A1): Boolean   = CNumeric[A].gt(a, num)
    def >=[A1: CNumeric](num: A1): Boolean  = CNumeric[A].gte(a, num)
    def <[A1: CNumeric](num: A1): Boolean   = CNumeric[A].lt(a, num)
    def <=[A1: CNumeric](num: A1): Boolean  = CNumeric[A].lte(a, num)
    def ===[A1: CNumeric](num: A1): Boolean = CNumeric[A].eq(a, num)
    def !==[A1: CNumeric](num: A1): Boolean = CNumeric[A].eq(a, num)
    def <<(num: Int): A                     = CNumeric[A].shiftL(a, num)
    def >>(num: Int): A                     = CNumeric[A].shiftR(a, num)

    def |(num: A): A = CNumeric[A].or(a, num)
    def &(num: A): A = CNumeric[A].and(a, num)
    def ^(num: A): A = CNumeric[A].xor(a, num)
    def unary_- : A  = CNumeric[A].negative(a)
  }
}
