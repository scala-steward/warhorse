package scash.warhorse

import java.math.BigInteger

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

  implicit class BigIntOps(n: BigInt) {
    def toUnsignedByteVector = {
      val bytes = ByteVector(n.toByteArray)
      if (bytes.length <= 32) bytes.padLeft(32)
      else bytes.tail
    }

    def toHex: String = toUnsignedByteVector.toHex
  }

  implicit class BigIntegerOps(n: BigInteger) {
    def unSignedArrayLength = (n.bitLength() + 7) / 8

    def toUnsignedByteVector = {
      val bytes = ByteVector(n.toByteArray)
      if (bytes.length <= 32) bytes.padLeft(32)
      else bytes.tail
    }
  }

  implicit class ArrayByteOps(array: Array[Byte]) {
    def toByteVector = ByteVector.view(array)
  }
}
