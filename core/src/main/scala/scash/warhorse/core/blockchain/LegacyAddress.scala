package scash.warhorse.core.blockchain

import scash.warhorse.{ Err, Result }
import scash.warhorse.core.blockchain.Address._
import scash.warhorse.core.crypto.hash.{ DoubleSha256, Hash160, Hasher }

import scodec.bits.ByteVector

object LegacyAddress {

  val P2PKHMainNet = 0x00.toByte
  val P2PKHTestNet = 0x6f.toByte

  val P2SHMainNet = 0x05.toByte
  val P2SHTestNet = 0xc4.toByte

  def p2pkh(net: Net, hash: Hash160): P2PKH          =
    net match {
      case MainNet           => P2PKH(genBase58(P2PKHMainNet, hash.bytes))
      case TestNet | RegTest => P2PKH(genBase58(P2PKHTestNet, hash.bytes))
    }

  //TODO: ScriptPubkey
  def p2sh(net: Net, redeemScript: ByteVector): P2SH = {
    val hash = Hasher[Hash160].hash(redeemScript)
    net match {
      case MainNet           => P2SH(genBase58(P2SHMainNet, hash.bytes))
      case TestNet | RegTest => P2SH(genBase58(P2SHTestNet, hash.bytes))
    }
  }

  private def genBase58(prefix: Byte, data: ByteVector): String = {
    val bytes    = prefix +: data
    val checksum = Hasher[DoubleSha256].hash(bytes).bytes.take(4)
    (bytes ++ checksum).toBase58
  }

  def fromByteVector(bytes: ByteVector): Result[Address] =
    Result.fromOption(
      bytes.headOption.flatMap {
        case P2PKHMainNet | P2PKHTestNet => Some(P2PKH(bytes.toBase58))
        case P2SHMainNet | P2SHTestNet   => Some(P2SH(bytes.toBase58))
        case _                           => None
      },
      Err(s"bytes: $bytes is not a legacy address")
    )
}
