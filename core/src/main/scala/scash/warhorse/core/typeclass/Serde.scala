package scash.warhorse.core.typeclass

import java.math.BigInteger

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scodec.{ Codec, DecodeResult }
import scodec.bits.{ BitVector, ByteVector }
import scodec.codecs._

trait Serde[A]    {
  def codec: Codec[A]

  def xmap[B](fa: A => B, fb: B => A): Serde[B] = Serde(codec.xmap(fa, fb))

  def narrow[B](fa: A => Result[B], fb: B => A): Serde[B] =
    Serde(codec.narrow(a => Result.toAttempt(fa(a)), fb))

  def decodeValue(byteVector: ByteVector): Result[A] =
    Result.fromAttempt(codec.decodeValue(byteVector.bits))

  def decode(byteVector: ByteVector): Result[DecodeResult[A]] =
    Result.fromAttempt(codec.decode(byteVector.bits))

  def encodeBits(a: A): Result[BitVector] = Result.fromAttempt(codec.encode(a))

  def encode(a: A): Result[ByteVector] = encodeBits(a).map(_.toByteVector)

  def orElse(or: Serde[A]): Serde[A] = Serde(choice(codec, or.codec))

  def ||(or: Serde[A]): Serde[A] = orElse(or)
}

object Serde      {
  def apply[A](implicit c: Serde[A]): Serde[A] = c

  def apply[A](
    encode: A => Result[ByteVector],
    decode: ByteVector => Result[DecodeResult[A]]
  ): Serde[A] =
    apply(
      Codec[A](
        (a: A) => Result.toAttempt(encode(a).map(_.bits)),
        (b: BitVector) => Result.toAttempt(decode(b.toByteVector))
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
    def bytesB: ByteVector       = bytes.reverse
    def bits: BitVector          = Serde[A].encodeBits(a).require
    def hex: String              = bytes.toHex
    def toArray: Array[Byte]     = bytes.toArray
    def toBigInteger: BigInteger = bytes.toBigInteger
  }

  implicit class BitVectorOps(bitVector: BitVector) {
    def decode[A: Serde]: Result[A] = Result.fromAttempt(Serde[A].codec.decodeValue(bitVector))
    def decode_[A: Serde]: A        = decode.require
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
