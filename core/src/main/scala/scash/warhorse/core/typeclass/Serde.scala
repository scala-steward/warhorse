package scash.warhorse.core.typeclass

import java.math.BigInteger

import scash.warhorse.Result
import scash.warhorse.core._
import scodec.{ Codec, DecodeResult }
import scodec.bits.{ BitVector, ByteVector }
import scodec.codecs._

trait Serde[A] {
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

object Serde {
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
    def bits: BitVector          = Serde[A].encodeBits(a).require
    def hex: String              = bytes.toHex
    def toArray: Array[Byte]     = bytes.toArray
    def toBigInteger: BigInteger = bytes.toBigInteger
    def hash[A: Hasher]          = Hasher[A].hash(a)
  }

  implicit class BitVectorOps(bitVector: BitVector) {
    def decode[A: Serde]: Result[A] = Result.fromAttempt(Serde[A].codec.decodeValue(bitVector))
    def decode_[A: Serde]: A        = decode.require
  }
}
