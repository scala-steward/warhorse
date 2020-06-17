package scash.warhorse.core.blockchain

import scash.warhorse.{ Err, Result }
import scash.warhorse.Result.{ Failure, Successful }
import scash.warhorse.core.crypto.hash.Hash160
import scash.warhorse.core._
import scash.warhorse.core.typeclass.Show
import scodec.bits.ByteVector

import scala.Predef._

sealed trait CashAddr

object CashAddr {

  val P2KHbyte = 0x00.toByte
  val P2SHbyte = 0x08.toByte

  val cashAddrShowNet = new Show[Net] {
    def decode(s: String): Result[Net] =
      s match {
        case "bitcoincash" => Successful(MainNet)
        case "bchtest"     => Successful(TestNet)
        case "bchreg"      => Successful(RegTest)
        case _             => Failure(Err(s"$s is not a valid net"))
      }

    def encode(a: Net): String =
      a match {
        case MainNet => "bitcoincash"
        case TestNet => "bchtest"
        case RegTest => "bchreg"
      }
  }

  val addrShow = new Show[Address] {
    def decode(addr: String): Result[Address] =
      for {
        netpay  <- split(addr)
        net     <- cashAddrShowNet.decode(netpay._1)
        payload <- Base32.fromBase32(netpay._1, netpay._2)
        ans     <- toAddress(net, payload)
      } yield ans

    def encode(a: Address): String =
      a match {
        case P2PKH(net, pkHash) => Base32.toBase32(cashAddrShowNet.encode(net), P2KHbyte, pkHash.bytes)
        case P2SH(net, rsHash)  => Base32.toBase32(cashAddrShowNet.encode(net), P2SHbyte, rsHash.bytes)
      }
  }

  private def split(str: String): Result[(String, String)] = {
    val split = str.split(":")
    if (str.toUpperCase != str && str.toLowerCase != str) Failure(Err(s"$str mixed upper and lower case not allowed"))
    else if (split.size != 2) Failure(Err(s"$str has invalid cashaddr format"))
    else Successful((split(0).toLowerCase, split(1).toLowerCase))
  }

  private def toAddress(net: Net, payLoad: ByteVector): Result[Address] =
    payLoad.head match {
      case P2KHbyte => Successful(P2PKH(net, payLoad.tail.decode_[Hash160]))
      case P2SHbyte => Successful(P2SH(net, payLoad.tail.decode_[Hash160]))
      case _        => Failure(Err(s"Invalid version byte for $payLoad"))
    }
}
