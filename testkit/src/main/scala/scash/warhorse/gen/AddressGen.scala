package scash.warhorse.gen

import scash.warhorse.core.blockchain.Address
import zio.random.Random
import zio.test.Gen

trait AddressGen {

  def p2pkh: Gen[Random, Address] =
    genp2pkhLoad.map { case (net, pubkey) => Address.p2pkh(net, pubkey) }

  def p2sh: Gen[Random, Address]  =
    genp2shLoad.map { case (net, redeemScript) => Address.p2sh(net, redeemScript) }

  private def genp2pkhLoad        =
    for {
      net  <- netGenerator
      pkey <- pubkey
    } yield (net, pkey)

  private def genp2shLoad =
    for {
      net     <- netGenerator
      spubkey <- scriptPubKey
    } yield (net, spubkey)

  def address = Gen.oneOf(p2pkh, p2sh)
}
