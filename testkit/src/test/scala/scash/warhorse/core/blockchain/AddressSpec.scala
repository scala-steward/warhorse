package scash.warhorse.core.blockchain

import scash.warhorse._
import scash.warhorse.core._
import scash.warhorse.util._

import zio.test.DefaultRunnableSpec
import zio.test._

object AddressSpec extends DefaultRunnableSpec {
  val spec = suite("AddressSpec")(
    testM("symmetry")(check(gen.legacyAddress)(addr => assert(addr.bytes.decode[Address])(success(addr))))
  )
}
