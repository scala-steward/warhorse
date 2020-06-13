package scash.warhorse.core

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.number.{ Uint5, Uint64 }
import scash.warhorse.core.typeclass.Serde

import scodec.Codec
import scodec.bits.{ BitVector, ByteVector }
import scodec.codecs.{ bits, _ }

import Predef._
import scala.util.Try

/**
https://github.com/bitcoincashorg/bitcoincash.org/blob/master/spec/cashaddr.md
 */

case class BCH32(prefix: String, payload: String) {
  override def toString: String = s"$prefix:$payload"
}

object BCH32                                      {
  private val gen = List(
    (0x01, 0x98f2bc8e61L),
    (0x02, 0x79b76d99e2L),
    (0x04, 0xf33e5fb3c4L),
    (0x08, 0xae2eabe2a8L),
    (0x10, 0x1e4f43e470L)
  )

  private val hashSizeMap =
    Vector(160, 192, 224, 256, 320, 384, 448, 512)

  def polyMod(data: Vector[Uint5]): Uint64 = {
    var c = Uint64.one
    data.foreach { d =>
      val c0 = c >> 35
      c = ((c & 0x07ffffffffL) << 5) ^ d
      gen.foreach { case (bit, gen) => if (c0 hasBit bit) c ^= gen }
    }
    c ^ 1
  }

  def verifyCheckSum(prefix: String, payload: String): Boolean = {
    val payLoadVec = payload.map(Charset.index _ andThen Uint5.apply).toVector
    val prefixVec  = prefix.map(Uint5.cast).toVector
    val sepVec     = Vector(Uint5.zero)

    polyMod(prefixVec ++ sepVec ++ payLoadVec) === Uint64.zero
  }

  def calculateCheckSum(prefix: String, payloadVec: Vector[Uint5]): Vector[Uint5] = {
    val prefixVec         = prefix.map(s => Uint5((s & 0x1f).toByte)).toVector
    val sepVec            = Vector(Uint5.zero)
    val chkSumTemplateVec = Vector.fill(8)(Uint5.zero)
    val poly              = polyMod(prefixVec ++ sepVec ++ payloadVec ++ chkSumTemplateVec)

    (0 until 8).toVector
      .map(i => Uint5((poly >> 5 * (7 - i) & 0x1f).num.toByte))
  }

  lazy val bch32Serde: Serde[BCH32] = Serde(Codecs.bchCodec)

  def fromString(prefix: String, payLoad: String): Result[BCH32] =
    for {
      prefixBytes <- Charset.fromBase32(prefix)
      payLoad     <- Charset.fromBase32(payLoad)
      ans         <- bch32Serde.decodeValue(prefixBytes ++ payLoad)
    } yield ans

  def genBch32(prefix: String, vtype: Byte, payload: ByteVector): BCH32 = {
    val versionByte   = (vtype | hashSizeMap.indexOf(payload.size * 8)).toByte
    val vec           = bytestoUint5(versionByte +: payload)
    val checkSum      = calculateCheckSum(prefix, vec)
    val base32Payload = Charset.toBase32(vec ++ checkSum)
    BCH32(prefix, base32Payload)
  }

  private def bytestoUint5(bytes: ByteVector): Vector[Uint5] = {
    val bits     = bytes.bits
    val fraction = bits.size % 5
    val padding  =
      if (fraction == 0) bits
      else bits.padRight(bits.size + 5 - fraction)

    padding
      .grouped(5)
      .map(_.decode_[Uint5])
      .toVector
  }

  private object Codecs {

    val hashBytesCodec = bits(3).exmap[Int](
      b => {
        val idx = b.toByte(false)
        if (idx >= hashSizeMap.length && idx < 0) Failure(Err(s"hashbits size failed: idx $idx is invalid")).toAttempt
        else Successful(hashSizeMap(b.toByte(false).toInt) / 8).toAttempt
      },
      i => {
        val bits = hashSizeMap.indexOf(i)
        if (bits == -1) Failure(Err(s"hashbits failed: $i is invalid size")).toAttempt
        else Successful(BitVector(bits)).toAttempt
      }
    )

    val typeAddrCodec =
      (bits(5) ~ peek(bits(3)))
        .exmap[Byte](
          bb =>
            if (BitVector.bit(bb._1.head) != BitVector.zero)
              Failure(Err(s"Versionbit must be 0. it is: ${bb._1(0)}")).toAttempt
            else Successful((bb._1 ++ bb._2).toByte(false)).toAttempt,
          b => Successful(BitVector(b).splitAt(5)).toAttempt
        )

    val payLoadCodec: Codec[String] =
      (typeAddrCodec ~ byteVectorCodec(hashBytesCodec) ~ byteVectorCodec(provide(5)))
        .exmap[String](
          bb => Successful(Charset.toBase32((bb._1._1 +: bb._1._2) ++ bb._2)).toAttempt,
          str =>
            Charset
              .fromBase32(str)
              .map { b =>
                val (verPayload, chkSum) = b.splitAt(b.size - 5)
                ((verPayload.head, verPayload.tail), chkSum)
              }
              .toAttempt
        )

    val prefixCodec =
      variableSizeDelimited(constant(':'.toByte), bytes)
        .exmap[String](
          b => Successful(Charset.toBase32(b)).toAttempt,
          str => Charset.fromBase32(str).toAttempt
        )

    val bchCodec =
      (prefixCodec ~ payLoadCodec)
        .exmap[BCH32](
          str =>
            if (verifyCheckSum(str._1, str._2)) Successful(BCH32(str._1, str._2)).toAttempt
            else Failure(Err(s"addr ${(str._1, str._2)} doesnt have valid payload")).toAttempt,
          bch32 => Successful((bch32.prefix, bch32.payload)).toAttempt
        )

    def byteVectorCodec(size: Codec[Int]) = vectorOfN(size, byte).xmap[ByteVector](ByteVector(_), _.toArray.toVector)

  }

  private object Charset {
    // format: off
    private val Chars = Array(
      'q', 'p', 'z', 'r', 'y', '9', 'x', '8',
      'g', 'f', '2', 't', 'v', 'd', 'w', '0',
      's', '3', 'j', 'n', '5', '4', 'k', 'h',
      'c', 'e', '6', 'm', 'u', 'a', '7', 'l'
      )
    // format: on

    def fromBase32(str: String): Result[ByteVector] =
      Result.fromTry(Try(ByteVector(str.map(index))))

    def toBase32(bb: ByteVector): String = (bytestoUint5 _ andThen toBase32)(bb)

    def toBase32(bb: Vector[Uint5]): String = {
      val str = new StringBuffer
      bb.foreach(b => str.append(char(b.num.toInt)))
      str.toString
    }

    def char(i: Int): Char = Chars(i)

    def index(c: Char): Byte = {
      val idx = Chars.indexOf(c)
      if (idx >= 0) idx.toByte
      else throw new IllegalArgumentException
    }
  }

}
