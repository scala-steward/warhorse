package scash.warhorse

import scash.warhorse.core.typeclass.{ CNumeric, CNumericSyntax, SerdeSyntax }

package object core extends SerdeSyntax with CNumericSyntax {
  import scala.{ Predef => P }

  implicit val bigIntNumeric: CNumeric[BigInt] =
    CNumeric[BigInt](0xFFFFFFFFFFFFFFFFL)(P.identity, P.identity)

  implicit val longNumeric: CNumeric[Long] =
    CNumeric[Long](0xFFFFFFFFFFFFFFFFL)(P.identity[Long], _.toLong)

  implicit val intNumeric: CNumeric[Int] =
    CNumeric[Int](0xFFFFFFFF)(P.identity[Int], _.toInt)

}
