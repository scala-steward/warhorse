package scash.warhorse.core.typeclass

import scodec.Codec
import scodec.bits.ByteVector

object Serde {
  def apply[A](implicit c: Codec[A]): Codec[A] = c
}

trait SerdeSyntax {
  implicit class SerdeSyntaxOps[A: Codec](a: A) {
    def bytes: ByteVector = Serde[A].encode(a).require.toByteVector
    def hex: String       = Serde[A].encode(a).require.toHex
  }

  implicit class ByteVectorOps(byteVector: ByteVector) {
    def decode[A: Codec]: A = Serde[A].decode(byteVector.bits).require.value
  }
}
