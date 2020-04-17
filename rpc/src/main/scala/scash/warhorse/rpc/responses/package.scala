package scash.warhorse.rpc

import io.circe.Decoder
import scash.warhorse.core.crypto.{ DoubleSha256, DoubleSha256B }
import scash.warhorse.core.number.{ Int32, Uint32 }
import scash.warhorse.core._
import scodec.bits.ByteVector

package object responses extends Blockchain with RpcTransactionDecoders {
  implicit val byteVectorDecoder: Decoder[ByteVector] =
    Decoder.decodeString
      .emap(s => ByteVector.fromHex(s).toRight(s"Unable to parse byteVector: $s is not valid hex"))

  implicit val uint32Decoder: Decoder[Uint32] =
    Decoder.decodeLong
      .ensure(n => Uint32.min <= n && Uint32.max >= n, "Value not in valid Range")
      .map(Uint32(_))

  implicit val int32Decoder: Decoder[Int32] =
    Decoder.decodeInt.map(Int32(_))

  implicit val doubleSha256Decoder: Decoder[DoubleSha256] =
    byteVectorDecoder
      .ensure(_.size <= 32, "ByteVector length is not 32 bytes")
      .map(DoubleSha256(_))

  implicit val doubleSha256BDecoder: Decoder[DoubleSha256B] =
    doubleSha256Decoder.map(DoubleSha256.toBigEndian)

}
