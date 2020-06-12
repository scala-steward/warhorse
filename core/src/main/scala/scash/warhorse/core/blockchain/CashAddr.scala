/*
package scash.warhorse.core.blockchain

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.crypto.hash.Hash160
import scash.warhorse.core.number.{ Uint5, Uint64 }
import scodec.bits.BitVector
import scodec.bits._
import scash.warhorse.core._
import scodec.bits.Bases.Base32Alphabet

import scala.Predef._

sealed trait CashAddr

object CashAddr {

  val P2KHbits = bin"0000"
  val P2SHbits = bin"0001"

  private val gen = List(
    (0x01, 0x98f2bc8e61L),
    (0x02, 0x79b76d99e2L),
    (0x04, 0xf33e5fb3c4L),
    (0x08, 0xae2eabe2a8L),
    (0x10, 0x1e4f43e470L)
  )

  object
  /**
  https://github.com/bitcoincashorg/bitcoincash.org/blob/master/spec/cashaddr.md#payload
 */
  object BCH32 extends Base32Alphabet {
    // format: off
    private val Chars    = Array(
      'q', 'p', 'z', 'r', 'y', '9', 'x', '8',
      'g', 'f', '2', 't', 'v', 'd', 'w', '0',
      's', '3', 'j', 'n', '5', '4', 'k', 'h',
      'c', 'e', '6', 'm', 'u', 'a', '7', 'l'
    )
    // format: on
    val pad              = 'X' //this should never return in our case
    def toChar(i: Int)   = Chars(i)
    def toIndex(c: Char) = {
      val idx = Chars.indexOf(c)
      if (idx >= 0) idx
      else throw new IllegalArgumentException
    }

    def ignore(c: Char) = c.isWhitespace
  }

  implicit val cashAddr = new Addr[CashAddr] {
    def p2pkh(net: Net, hash: Hash160): P2PKH =
      P2PKH(cons(net, P2KHbits, hash))

    def p2sh(net: Net, hash: Hash160): P2SH =
      P2SH(cons(net, P2SHbits, hash))
  }

  def fromLegacyAddr(addr: Address): Address = {
    val payload = ByteVector.fromValidBase58(addr.value).tail.dropRight(4)
    val net     = LegacyAddr.net(addr)

    addr match {
      case _: P2PKH => cashAddr.p2pkh(net, Hash160(payload))
      case _: P2SH  => cashAddr.p2sh(net, Hash160(payload))
    }
  }

  private def versionByte(addrType: BitVector, hashSizeBits: Long): Result[BitVector] = {
    val sizeBits = Vector(160, 192, 224, 256, 320, 384, 448, 512).indexOf(hashSizeBits)
    if (sizeBits == -1) Failure(Err(s"Invalid hashSize $hashSizeBits"))
    else Successful(BitVector.zero ++ addrType ++ BitVector(sizeBits.toByte).takeRight(3))
  }

  private def polyMod(data: Vector[Uint5]): Uint64 = {
    var c = Uint64.one
    data.foreach { d =>
      val c0 = c >> 35
      c = ((c & 0x07ffffffffL) << 5) ^ d.num
      gen.foreach { case (bit, gen) => if (c0 hasBit bit) c ^= gen }
    }
    c ^ 1
  }

  def calculateCheckSum(prefix: String, payLoadBits: BitVector): ByteVector = {
    val prefixBits = prefix.map(s => Uint5.cast(s.toByte)).toVector
    val sepBits    = Vector(Uint5.zero)
    val fraction   = payLoadBits.size % 5

    val padding = if (fraction == 0) payLoadBits else payLoadBits.padRight(payLoadBits.size + 5 - fraction)

    val uint5Chunks = padding
      .grouped(5)
      .map(_.decode_[Uint5])

    val checkSumTemplate = Vector.fill(8)(Uint5.zero)
    val poly             = polyMod(prefixBits ++ sepBits ++ uint5Chunks ++ checkSumTemplate)
    (0 to 7)
      .map(i => Uint5((poly >> 5 * (7 - i) & 0x1f).num.toByte).bytes)
      .reduce(_ ++ _)
  }

  def valid(str: String): Boolean = {
    val strs = str.split(":")
    val res  = if (strs.size == 2) Some((strs(0), strs(1))) else None
    val ans  = for {
      (prefix, payload) <- res
      payLoadBits       <- BitVector.fromBase32(payload, BCH32)
      prefixBits         = prefix.map(s => Uint5.cast(s.toByte)).toVector
      sepBits            = Vector(Uint5.zero)
      payLoad            = payLoadBits.grouped(5).toArray.map(bits => Uint5(bits.toByte(false)))
    } yield polyMod(prefixBits ++ sepBits ++ payLoad) == Uint64.zero

    ans.getOrElse(false)
  }

  private def cons(net: Net, addrType: BitVector, h160: Hash160): String = {
    val prefix      = net match {
      case MainNet => "bitcoincash"
      case TestNet => "bchtest"
      case RegTest => "bchreg"
    }
    val versionBits = versionByte(addrType, h160.bits.size).require
    Predef.println(versionBits.toByte(false))
    val payloadBits = versionBits ++ h160.bits
    val checkSum    = calculateCheckSum(prefix, payloadBits)
    Predef.println(checkSum)
    s"$prefix:${(payloadBits ++ checkSum.toBitVector).toBase32(BCH32)}"
  }
}
 */

package scash.warhorse.core.blockchain

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.crypto.hash.Hash160

import scodec.bits.BitVector
import scodec.bits._
import scash.warhorse.core._

import scala.Predef._

sealed trait CashAddr

object CashAddr {

  val P2KHbits = bin"0000"
  val P2SHbits = bin"0001"

  implicit val cashAddr = new Addr[CashAddr] {
    def p2pkh(net: Net, hash: Hash160): P2PKH =
      P2PKH(cons(net, P2KHbits, hash))

    def p2sh(net: Net, hash: Hash160): P2SH =
      P2SH(cons(net, P2SHbits, hash))
  }

  def fromLegacyAddr(addr: Address): Address = {
    val payload = ByteVector.fromValidBase58(addr.value).tail.dropRight(4)
    val net     = LegacyAddr.net(addr)

    addr match {
      case _: P2PKH => cashAddr.p2pkh(net, Hash160(payload))
      case _: P2SH  => cashAddr.p2sh(net, Hash160(payload))
    }
  }

  private def versionByte(addrType: BitVector, hashSizeBits: Long): Result[BitVector] = {
    val sizeBits = Vector(160, 192, 224, 256, 320, 384, 448, 512).indexOf(hashSizeBits)
    if (sizeBits == -1) Failure(Err(s"Invalid hashSize $hashSizeBits"))
    else Successful(BitVector.zero ++ addrType ++ BitVector(sizeBits.toByte).takeRight(3))
  }

  private def cons(net: Net, addrType: BitVector, h160: Hash160): String = {
    val prefix      = net match {
      case MainNet => "bitcoincash"
      case TestNet => "bchtest"
      case RegTest => "bchreg"
    }
    val versionBits = versionByte(addrType, h160.bits.size).require

    val payloadBits = versionBits ++ h160.bits
    //val checkSum    = BCH32.calculateCheckSum(prefix, payloadBits)

    s"$prefix:${BCH32.toBase32((payloadBits ++ BitVector.empty).toByteVector)}"
  }
}
