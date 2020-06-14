package scash.warhorse.core.blockchain

import scash.warhorse.Result.Failure
import scash.warhorse.{ Err, Result }
import scash.warhorse.core.crypto.hash.{ DoubleSha256, Hash160, Hasher }
import scash.warhorse.core.typeclass.Serde
import scodec.DecodeResult
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

  def fromString(addr: String): Result[Address] =
    if (addr.length < 26 || addr.length > 35)
      Failure(Err.BoundsError("Address", "26 <= addr.length <= 35 s", addr.length.toString))
    else
      Result
        .fromOption(ByteVector.fromBase58(addr), Err(s"addr: $addr is not a legacy address"))
        .flatMap(fromByteVector)

  def fromByteVector(bytes: ByteVector): Result[Address] =
    Result.fromOption(
      bytes.headOption.flatMap {
        case P2PKHMainNet | P2PKHTestNet => Some(P2PKH(bytes.toBase58))
        case P2SHMainNet | P2SHTestNet   => Some(P2SH(bytes.toBase58))
        case _                           => None
      },
      Err(s"bytes: $bytes is not a legacy address")
    )

  val serde: Serde[Address] =
    Serde(
      (a: Address) => Result.fromOption(ByteVector.fromBase58(a.value), Err("Is not Legacy Address")),
      (bytes: ByteVector) => fromByteVector(bytes.take(25)).map(DecodeResult(_, bytes.drop(25).toBitVector))
    )

  implicit val legacyAddr = new Addr[LegacyAddr] {
    def p2pkh(net: Net, hash: Hash160): P2PKH =
      P2PKH(cons(net, P2PKHMainNet, P2PKHTestNet, hash))

    def p2sh(net: Net, hash: Hash160): P2SH =
      P2SH(cons(net, P2SHMainNet, P2SHTestNet, hash))
  }

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
