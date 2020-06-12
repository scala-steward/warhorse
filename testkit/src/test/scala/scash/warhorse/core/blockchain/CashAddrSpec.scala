package scash.warhorse.core.blockchain

import zio.test.DefaultRunnableSpec
import zio.test._
import zio.test.Assertion._

object CashAddrSpec extends DefaultRunnableSpec {

  val spec = suite("CashAddrSpec")(
    test("fromLegacyAddr") {
      val laddr = LegacyAddr.fromString("1BpEi6DfDAUFd7GtittLSdBeYJvcoaVggu")
      val exp   = "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a"
      assert(CashAddr.fromLegacyAddr(laddr.require).value)(equalTo(exp))
    }
  )
  /*
    suite("symmetry")(
      testM("p2pkh")(check(gen.addrp2pkh)(addr => assert(addr.bytes.decode[Address])(success(addr)))),
      testM("ps2h")(check(gen.addrp2sh)(addr => assert(addr.bytes.decode[Address])(success(addr))))
    )
   */
}
