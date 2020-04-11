package scash.warhorse.core.typeclass

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scodec.{ Codec, DecodeResult }
import scodec.bits.{ BitVector, ByteVector }

trait Serde[A] {
  def codec: Codec[A]

  def xmap[B](fa: A => B, fb: B => A): Serde[B] =
    Serde(
      Codec[B](
        (b: B) => codec.encode(fb(b)),
        (bits: BitVector) => codec.decode(bits).map(_.map(fa))
      )
    )

  def decodeValue(byteVector: ByteVector): Result[A] =
    Result.fromAttempt(codec.decodeValue(byteVector.bits))

  def decode(byteVector: ByteVector): Result[DecodeResult[A]] =
    Result.fromAttempt(codec.decode(byteVector.bits))

  def encode(a: A): Result[ByteVector] =
    Result.fromAttempt(codec.encode(a).map(_.toByteVector))
}

object Serde {
  //implicit def toCodec[A](a: Serde[A]): Codec[A] = a.codec

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

  def apply[A](c: Codec[A]): Serde[A] = new Serde[A] {
    def codec: Codec[A] = c
  }
}

trait SerdeSyntax {
  implicit class SerdeSyntaxOps[A: Serde](a: A) {
    def bytes: ByteVector = Serde[A].encode(a).require
    def hex: String       = bytes.toHex
  }

  implicit class ByteVectorOps(byteVector: ByteVector) {
    def validDecode[A: Serde]: A    = decode.require
    def decode[A: Serde]: Result[A] = Serde[A].decodeValue(byteVector)

    def decodeExactly[A: Serde]: Result[A] = Serde[A].decode(byteVector).flatMap { a =>
      if (a.remainder.isEmpty) Successful(a.value)
      else Failure(Err(s"Decoding left remainder bits: ${a.remainder.toByteVector.size}"))
    }

    def validDecodeExactly[A: Serde]: A = decodeExactly.require
  }
}
