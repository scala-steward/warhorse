package scash.warhorse.gen

import scash.warhorse.core.blockchain.{ Addr, Address, CashAddr, LegacyAddr, MainNet }
import zio.random.Random
import zio.test.{ Gen, Sized }

trait AddressGen {

  def addrp2pkh: Gen[Random, Address] =
    genp2pkhLoad.map { case (_, pubkey) => Addr[CashAddr].p2pkh(MainNet, pubkey) }

  def addrp2sh: Gen[Random, Address]  =
    genp2shLoad.map { case (net, spubkey) => Addr[CashAddr].p2sh(net, spubkey) }

  def p2pkh: Gen[Random, Address]     =
    genp2pkhLoad.map { case (net, pubkey) => Addr[LegacyAddr].p2pkh(net, pubkey) }

  def p2sh: Gen[Random, Address]      =
    genp2shLoad.map { case (net, spubkey) => Addr[LegacyAddr].p2sh(net, spubkey) }

  private def genp2pkhLoad            =
    for {
      net  <- netGenerator
      pkey <- pubkey
    } yield (net, pkey)

  private def genp2shLoad =
    for {
      net     <- netGenerator
      spubkey <- scriptPubKey
    } yield (net, spubkey)

  def legacyAddress: Gen[Random with Sized, Address] = Gen.oneOf(p2pkh, p2sh)
}
