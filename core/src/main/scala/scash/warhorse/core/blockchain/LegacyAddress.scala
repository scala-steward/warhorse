package scash.warhorse.core.blockchain

import scash.warhorse.{ Err, Result }
import scash.warhorse.core.blockchain.Address._
import scash.warhorse.core.crypto.PublicKey
import scash.warhorse.core.crypto.hash.{ DoubleSha256, Hash160, Hasher }
import scodec.bits.ByteVector

object LegacyAddress {

  val P2PKHMainNet = 0x00.toByte
  val P2PKHTestNet = 0x6f.toByte

  val P2SHMainNet = 0x05.toByte
  val P2SHTestNet = 0xc4.toByte

  def p2pkh(net: Net, publicKey: PublicKey): P2PKH =
    p2pkh(net, Hasher[Hash160].hash(publicKey.bytes))

  def p2pkh(net: Net, hash: Hash160): P2PKH          =
    P2PKH(cons(net, P2PKHMainNet, P2PKHTestNet, hash))

  //TODO: redeemScript
  def p2sh(net: Net, redeemScript: ByteVector): P2SH =
    p2sh(net, Hasher[Hash160].hash(redeemScript))

  def p2sh(net: Net, hash: Hash160): P2SH =
    P2SH(cons(net, P2SHMainNet, P2SHTestNet, hash))

  def fromByteVector(bytes: ByteVector): Result[Address] =
    Result.fromOption(
      bytes.headOption.flatMap {
        case P2PKHMainNet | P2PKHTestNet => Some(P2PKH(bytes.toBase58))
        case P2SHMainNet | P2SHTestNet   => Some(P2SH(bytes.toBase58))
        case _                           => None
      },
      Err(s"bytes: $bytes is not a legacy address")
    )

  private def cons(net: Net, mainNetByte: Byte, testNetByte: Byte, hash: Hash160) = {
    def genBase58(prefix: Byte): String = {
      val bytes    = prefix +: hash.bytes
      val checksum = Hasher[DoubleSha256].hash(bytes).bytes.take(4)
      (bytes ++ checksum).toBase58
    }
    net match {
      case MainNet           => genBase58(mainNetByte)
      case TestNet | RegTest => genBase58(testNetByte)
    }
  }
}
