package scash.warhorse.core.blockchain

import scash.warhorse.Result.Failure
import scash.warhorse.{ Err, Result }
import scash.warhorse.core.crypto.hash.{ DoubleSha256, Hash160, Hasher }

import scodec.bits.ByteVector

sealed trait LegacyAddr

object LegacyAddr {

  val P2PKHMainNet = 0x00.toByte
  val P2PKHTestNet = 0x6f.toByte

  val P2SHMainNet = 0x05.toByte
  val P2SHTestNet = 0xc4.toByte

  def net(addr: Address): Net =
    ByteVector.fromValidBase58(addr.value)(0) match {
      case P2PKHMainNet | P2SHMainNet => MainNet
      case P2PKHTestNet | P2SHTestNet => TestNet
    }

  implicit val legacyAddr = new Addr[LegacyAddr] {
    def p2pkh(net: Net, hash: Hash160): P2PKH =
      P2PKH(cons(net, P2PKHMainNet, P2PKHTestNet, hash))

    def p2sh(net: Net, hash: Hash160): P2SH =
      P2SH(cons(net, P2SHMainNet, P2SHTestNet, hash))

    def decode(addr: String): Result[Address] =
      if (addr.length < 26 || addr.length > 35)
        Failure(Err.BoundsError("Address", "26 <= addr.length <= 35 s", addr.length.toString))
      else
        Result.fromOption(
          for {
            bytes <- ByteVector.fromBase58(addr)
            head  <- bytes.headOption
            ans   <- if (!verifyCheckSum(bytes)) None
                   else
                     head match {
                       case P2PKHMainNet | P2PKHTestNet => Some(P2PKH(addr))
                       case P2SHMainNet | P2SHTestNet   => Some(P2SH(addr))
                       case _                           => None
                     }
          } yield ans,
          Err(s"Address $addr is an invalid Legacy Address")
        )
  }

  private def verifyCheckSum(bytes: ByteVector): Boolean =
    bytes.takeRight(4) === Hasher[DoubleSha256].hash(bytes.dropRight(4)).bytes.take(4)

  private def cons(net: Net, mainNetByte: Byte, testNetByte: Byte, h160: Hash160): String = {
    def genBase58(prefix: Byte): String = {
      val bytes    = prefix +: h160.bytes
      val checksum = Hasher[DoubleSha256].hash(bytes).bytes.take(4)
      (bytes ++ checksum).toBase58
    }
    net match {
      case MainNet           => genBase58(mainNetByte)
      case TestNet | RegTest => genBase58(testNetByte)
    }
  }
}
