package scash.warhorse.core.blockchain

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.crypto.hash.Hash160
import scash.warhorse.core._
import scash.warhorse.core.typeclass.Serde

import scodec.bits._

import scala.Predef._

sealed trait CashAddr

object CashAddr {

  val P2KHbyte = 0x00.toByte
  val P2SHbyte = 0x08.toByte
  val P2KHchar = 'q'
  val P2SHchar = 'p'

  def fromLegacyAddr(addr: Address): Address = {
    val payload = ByteVector.fromValidBase58(addr.value).tail.dropRight(4)
    val net     = LegacyAddr.net(addr)

    addr match {
      case _: P2PKH => cashAddr.p2pkh(net, Hash160(payload))
      case _: P2SH  => cashAddr.p2sh(net, Hash160(payload))
    }
  }

  lazy val serde: Serde[Address] = BCH32.bch32Serde.xmap[Address](
    bch => toAddress(bch).require,
    addr => toBCH32(addr.value).require
  )

  def fromString(str: String): Result[Address] =
    for {
      bch <- toBCH32(str)
      ans <- toAddress(bch)
    } yield ans

  implicit val cashAddr = new Addr[CashAddr] {
    def p2pkh(net: Net, hash: Hash160): P2PKH =
      P2PKH(cons(net, P2KHbyte, hash))

    def p2sh(net: Net, hash: Hash160): P2SH =
      P2SH(cons(net, P2SHbyte, hash))
  }

  private def toBCH32(str: String): Result[BCH32] = {
    val split = str.split(":")
    if (split.size != 2) Failure(Err(s"$str has invalid cashaddr format"))
    else if (!List("bitcoincash", "bchtest", "bchreg").contains(split(0)))
      Failure(Err(s"Not a valid prefix: ${split(0)}"))
    else BCH32.fromString(split(0), split(1))
  }

  private def toAddress(bch: BCH32): Result[Address] =
    bch.payload.head match {
      case P2KHchar => Successful(P2PKH(bch.toString))
      case P2SHchar => Successful(P2SH(bch.toString))
      case _        => Failure(Err(s"Invalid version byte for $bch"))
    }

  private def cons(net: Net, addrByte: Byte, h160: Hash160): String = {
    val prefix = net match {
      case MainNet => "bitcoincash"
      case TestNet => "bchtest"
      case RegTest => "bchreg"
    }
    BCH32.genBch32(prefix, addrByte, h160.bytes).toString
  }
}
