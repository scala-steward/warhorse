package scash.warhorse

import java.math.BigInteger

import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.typeclass._
import scodec.bits.ByteVector

package object core extends SerdeSyntax with CNumericSyntax {
  import scala.{ Predef => P }

  implicit val bigInt64Numeric: CNumeric[BigInt] =
    CNumeric[BigInt](
      0xffffffffffffffffL,
      BigInt(Long.MinValue),
      BigInt(Long.MaxValue)
    )(P.identity, P.identity)

  implicit val longNumeric: CNumeric[Long] =
    CNumeric[Long](
      0xffffffffffffffffL,
      Long.MinValue,
      Long.MaxValue
    )(P.identity[Long], _.toLong)

  implicit val intNumeric: CNumeric[Int] =
    CNumeric[Int](
      0xffffffff,
      Int.MinValue,
      Int.MaxValue
    )(P.identity[Int], _.toInt)

  implicit val byteNumeric: CNumeric[Byte] =
    CNumeric[Byte](
      0xff,
      Byte.MinValue,
      Byte.MaxValue
    )(b => BigInt(b.toInt), _.toByte)

  implicit class BigIntOps(n: BigInt) {
    def toUnsignedByteVector = {
      val bytes = ByteVector(n.toByteArray)
      if (bytes.length <= 32) bytes.padLeft(32)
      else bytes.tail
    }

    def toHex: String = toUnsignedByteVector.toHex
  }

  implicit class BigIntegerOps(n: BigInteger) {
    def toUnsignedByteVector = {
      val bytes = ByteVector(n.toByteArray)
      if (bytes.length <= 32) bytes.padLeft(32)
      else bytes.tail
    }
  }

  implicit class ArrayByteOps(array: Array[Byte]) {
    def toByteVector = ByteVector.view(array)
  }

  implicit class StringOps(str: String) {
    def hash[A: Hasher]: A = Hasher[A].hash(str)
  }

  implicit class ByteVectorOps(byteVector: ByteVector) {

    def hash[A: Hasher]: A = Hasher[A].hash(byteVector)

    /** Returns the successful value if present; otherwise throws an `IllegalArgumentException`. */
    def decode_[A: Serde]: A = decode.require

    /** Returns the Successful[A] if decoding was successful. otherwise `Failure(cause)` */
    def decode[A: Serde]: Result[A] = Serde[A].decodeValue(byteVector)

    /** Decode Bytevector into type `Result[A]`. if there are remainder bytes it will return Failure */
    def decodeExact[A: Serde]: Result[A] =
      Serde[A].decode(byteVector).flatMap { a =>
        if (a.remainder.isEmpty) Successful(a.value)
        else Failure(Err(s"Decoding left remainder bits: ${a.remainder.toByteVector.size}"))
      }

    /** Returns the successful value if present; otherwise throws an `IllegalArgumentException` if decodeExact fails. */
    def decodeExact_[A: Serde]: A = decodeExact.require

    def toBigInt: BigInt = BigInt(1, byteVector.toArray)

    def toBigInteger: BigInteger = new BigInteger(1, byteVector.toArray)

  }

}
