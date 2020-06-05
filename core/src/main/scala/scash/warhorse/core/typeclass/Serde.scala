package scash.warhorse.core.typeclass

import java.math.BigInteger

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scodec.{ Codec, DecodeResult }
import scodec.bits.{ BitVector, ByteVector }

trait Serde[A]    {
  def codec: Codec[A]

  def xmap[B](fa: A => B, fb: B => A): Serde[B] = Serde(codec.xmap(fa, fb))

  def narrow[B](fa: A => Result[B], fb: B => A): Serde[B] =
    Serde(codec.narrow(a => Result.toAttempt(fa(a)), fb))

  def decodeValue(byteVector: ByteVector): Result[A] =
    Result.fromAttempt(codec.decodeValue(byteVector.bits))

  def decode(byteVector: ByteVector): Result[DecodeResult[A]] =
    Result.fromAttempt(codec.decode(byteVector.bits))

  def encode(a: A): Result[ByteVector] =
    Result.fromAttempt(codec.encode(a).map(_.toByteVector))
}

object Serde      {
  def apply[A](implicit c: Serde[A]): Serde[A] = c

  def apply[A](
    decode: A => Result[ByteVector],
    encode: ByteVector => Result[DecodeResult[A]]
  ): Serde[A] =
    apply(
      Codec[A](
        (a: A) => Result.toAttempt(decode(a).map(_.bits)),
        (b: BitVector) => Result.toAttempt(encode(b.toByteVector))
      )
    )

  def apply[A](c: Codec[A]): Serde[A] =
    new Serde[A] {
      def codec: Codec[A] = c
    }
}

trait SerdeSyntax {
  implicit class SerdeSyntaxOps[A: Serde](a: A) {
    def bytes: ByteVector        = Serde[A].encode(a).require
    def bits: BitVector          = bytes.toBitVector
    def hex: String              = bytes.toHex
    def toArray: Array[Byte]     = bytes.toArray
    def toBigInteger: BigInteger = bytes.toBigInteger
  }

  implicit class ByteVectorOps(byteVector: ByteVector) {

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
    def decodeExact_[A: Serde]: A        = decodeExact.require

    def toBigInt: BigInt = BigInt(1, byteVector.toArray)

    def toBigInteger: BigInteger = new BigInteger(1, byteVector.toArray)

  }
}
