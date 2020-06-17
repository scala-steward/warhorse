package scash.warhorse.core.blockchain

import scash.warhorse.Result.Failure
import scash.warhorse.{ Err, Result }
import scash.warhorse.core.crypto.hash.{ DoubleSha256, Hash160 }
import scash.warhorse.core._
import scash.warhorse.core.typeclass.Show
import scodec.bits.ByteVector

sealed trait LegacyAddr

object LegacyAddr {

  val P2PKHMainNet = 0x00.toByte
  val P2PKHTestNet = 0x6f.toByte

  val P2SHMainNet = 0x05.toByte
  val P2SHTestNet = 0xc4.toByte

  val addrShow = new Show[Address] {
    private def cons(byte: Byte, h160: Hash160): String = {
      val bytes    = byte +: h160.bytes
      val checksum = bytes.hash[DoubleSha256].bytes.take(4)
      (bytes ++ checksum).toBase58
    }

    def encode(address: Address): String =
      address match {
        case P2PKH(MainNet, pubKeyHash)      => cons(P2PKHMainNet, pubKeyHash)
        case P2PKH(_, pubKeyHash)            => cons(P2PKHTestNet, pubKeyHash)
        case P2SH(MainNet, redeemScriptHash) => cons(P2SHMainNet, redeemScriptHash)
        case P2SH(_, redeemScriptHash)       => cons(P2SHTestNet, redeemScriptHash)
      }

    def decode(addr: String): Result[Address] =
      if (addr.length < 26 || addr.length > 35)
        Failure(Err.BoundsError("Address", "26 <= addr.length <= 35 s", addr.length.toString))
      else
        Result.fromOption(
          for {
            bytes <- ByteVector.fromBase58(addr)
            head  <- bytes.headOption
            payload = bytes.tail.dropRight(4)
            ans <-
              if (bytes.takeRight(4) != bytes.dropRight(4).hash[DoubleSha256].bytes.take(4)) None
              else
                head match {
                  case P2PKHMainNet => Some(P2PKH(MainNet, payload.decode_[Hash160]))
                  case P2PKHTestNet => Some(P2PKH(TestNet, payload.decode_[Hash160]))
                  case P2SHMainNet  => Some(P2SH(MainNet, payload.decode_[Hash160]))
                  case P2SHTestNet  => Some(P2SH(TestNet, payload.decode_[Hash160]))
                  case _            => None
                }
          } yield ans,
          Err(s"Address $addr is an invalid Legacy Address")
        )
  }
}
