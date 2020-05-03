package scash.warhorse

import scash.warhorse.core.typeclass.{ CNumeric, CNumericSyntax, SerdeSyntax }
import scodec.bits.ByteVector

package object core extends SerdeSyntax with CNumericSyntax {
  import scala.{ Predef => P }

  implicit val bigInt64Numeric: CNumeric[BigInt] =
    CNumeric[BigInt](
      0xFFFFFFFFFFFFFFFFL,
      BigInt(Long.MinValue),
      BigInt(Long.MaxValue)
    )(P.identity, P.identity)

  implicit val longNumeric: CNumeric[Long] =
    CNumeric[Long](
      0xFFFFFFFFFFFFFFFFL,
      Long.MinValue,
      Long.MaxValue
    )(P.identity[Long], _.toLong)

  implicit val intNumeric: CNumeric[Int] =
    CNumeric[Int](
      0xFFFFFFFF,
      Int.MinValue,
      Int.MaxValue
    )(P.identity[Int], _.toInt)

  implicit class BigIntOps(bigInt: BigInt) {
    def toByteVector = bigInt.toByteArray.toByteVector

    def toHex: String = toByteVector.toHex
  }

  implicit class ArrayByteOps(array: Array[Byte]) {
    def toByteVector = ByteVector(array)
  }
}
