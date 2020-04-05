package scash.warhorse

import scash.warhorse.core.typeclass.{ CNumeric, CNumericSyntax, SerdeSyntax }

package object core extends SerdeSyntax with CNumericSyntax {

  implicit val bigIntNumeric: CNumeric[BigInt] =
    CNumeric[BigInt](0xFFFFFFFFFFFFFFFFL)(identity, identity)

  implicit val longNumeric: CNumeric[Long] =
    CNumeric[Long](0xFFFFFFFFFFFFFFFFL)(identity[Long], _.toLong)

  implicit val intNumeric: CNumeric[Int] =
    CNumeric[Int](0xFFFFFFFF)(identity[Int], _.toInt)

}
