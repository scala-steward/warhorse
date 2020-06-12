package scash.warhorse.core

import scash.warhorse.core.number.{ Uint5, Uint64 }
import scodec.bits.ByteVector

import Predef._

/**
https://github.com/bitcoincashorg/bitcoincash.org/blob/master/spec/cashaddr.md
 */
object BCH32 {
  private val gen = List(
    (0x01, 0x98f2bc8e61L),
    (0x02, 0x79b76d99e2L),
    (0x04, 0xf33e5fb3c4L),
    (0x08, 0xae2eabe2a8L),
    (0x10, 0x1e4f43e470L)
  )

  def polyMod(data: Vector[Uint5]): Uint64 = {
    var c = Uint64.one
    data.foreach { d =>
      val c0 = c >> 35
      c = ((c & 0x07ffffffffL) << 5) ^ d
      gen.foreach { case (bit, gen) => if (c0 hasBit bit) c ^= gen }
    }
    c ^ 1
  }

  /*
  def calculateCheckSum(prefix: String, payLoadBits: BitVector): BitVector = {
    val prefixBits = prefix.map(s => Uint5.cast(s.toByte)).toVector
    val sepBits    = Vector(Uint5.zero)
    val fraction   = payLoadBits.size % 5

    val padding = if (fraction == 0) payLoadBits else payLoadBits.padRight(payLoadBits.size + 5 - fraction)

    val uint5Chunks = padding
      .grouped(5)
      .map(_.decode_[Uint5])

    val checkSumTemplate = Vector.fill(8)(Uint5.zero)
    val poly             = polyMod(prefixBits ++ sepBits ++ uint5Chunks ++ checkSumTemplate)

    (0 until 8)
      .map(i => Uint8((poly >> 5 * (7 - i) & 0x1f).num.toByte).bytes)
      .reduce(_ ++ _)
      .toBitVector
  }
   */

  def valid(str: String): Boolean = {
    val strs = str.split(":")
    if (strs.size != 2) false
    else {
      val (prefix, payload) = (strs(0), strs(1))
      val payLoadBytes      = payload.map(Charset.toIndex _ andThen Uint5.apply).toVector
      val prefixBits        = prefix.map(c => Uint5.cast(c.toByte)).toVector
      val sepBits           = Vector(Uint5.zero)

      polyMod(prefixBits ++ sepBits ++ payLoadBytes) == Uint64.zero
    }
  }

  def toBase32(bytes: ByteVector): String = bytes.toArray.map(b => Charset.toChar(b.toInt)).mkString

  private object Charset {
    // format: off
    private val Chars = Array(
      'q', 'p', 'z', 'r', 'y', '9', 'x', '8',
      'g', 'f', '2', 't', 'v', 'd', 'w', '0',
      's', '3', 'j', 'n', '5', '4', 'k', 'h',
      'c', 'e', '6', 'm', 'u', 'a', '7', 'l'
      )
    // format: on

    def toChar(i: Int) = Chars(i)

    def toIndex(c: Char): Byte = {
      val idx = Chars.indexOf(c)
      if (idx >= 0) idx.toByte
      else throw new IllegalArgumentException
    }
  }
}
