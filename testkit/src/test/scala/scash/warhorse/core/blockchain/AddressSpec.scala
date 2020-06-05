package scash.warhorse.core.blockchain

import scash.warhorse._
import scash.warhorse.core._
import scash.warhorse.util._

import zio.test.DefaultRunnableSpec
import zio.test._

object AddressSpec extends DefaultRunnableSpec {
  val spec = suite("AddressSpec")(
    suite("symmetry")(
      testM("p2pkh")(check(gen.p2pkh)(addr => assert(addr.bytes.decode[Address])(success(addr)))),
      testM("ps2h")(check(gen.p2sh)(addr => assert(addr.bytes.decode[Address])(success(addr))))
    )
  )
}
