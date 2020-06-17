package scash.warhorse.core.blockchain

import scash.warhorse.Result
import scash.warhorse.core.crypto.PublicKey
import scash.warhorse.core.crypto.hash.{ Hash160, Hasher }

import scodec.bits.ByteVector

sealed trait Address extends Product with Serializable { self =>
  def toCashAddr: String   = CashAddr.addrShow.encode(self)
  def toLegacyAddr: String = LegacyAddr.addrShow.encode(self)
}

case class P2PKH(net: Net, pubKeyHash: Hash160)      extends Address
case class P2SH(net: Net, redeemScriptHash: Hash160) extends Address

object Address {

  def p2pkh(net: Net, pubKey: PublicKey): P2PKH =
    P2PKH(net, Hasher[Hash160].hash(pubKey))

  def p2sh(net: Net, redeemScript: ByteVector): P2SH =
    P2SH(net, Hasher[Hash160].hash(redeemScript))

  def apply(addr: String): Result[Address] =
    LegacyAddr.addrShow.decode(addr) orElse CashAddr.addrShow.decode(addr)

  def cashAddrToLegacy(addr: String): Result[String] =
    CashAddr.addrShow
      .decode(addr)
      .map(LegacyAddr.addrShow.encode)

  def legacyToCashAddr(addr: String): Result[String] =
    LegacyAddr.addrShow
      .decode(addr)
      .map(CashAddr.addrShow.encode)
}
