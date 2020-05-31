package scash.warhorse.core.blockchain

import zio.test.DefaultRunnableSpec
import zio.test._

import scash.warhorse._
import scash.warhorse.core._
import scash.warhorse.util._

import scodec.bits._

object CompactSizeUintSpec extends DefaultRunnableSpec {

  val spec = suite("CompactSizeUintSpec")(
    testM("from Uint8")(
      check(gen.uint8)(c => assert(CompactSizeUint(c).bytes.decode[CompactSizeUint])(success(CompactSizeUint(c))))
    ),
    testM("from Uint32")(
      check(gen.uint32)(c => assert(CompactSizeUint(c).bytes.decode[CompactSizeUint])(success(CompactSizeUint(c))))
    ),
    testM("from Type")(check(gen.compactSizeUints)(c => assert(c.bytes.decode[CompactSizeUint])(success(c)))),
    test("bytes")(
      assert(hex"8b".decode[CompactSizeUint].map(_.bytes))(success(hex"8b")) &&
        assert(hex"00".decode[CompactSizeUint].map(_.bytes))(success(hex"00")) &&
        assert(hex"fdff00".decode[CompactSizeUint].map(_.bytes))(success(hex"fdff00")) &&
        assert(hex"fd0302".decode[CompactSizeUint].map(_.bytes))(success(hex"fd0302")) &&
        assert(hex"fe20a10700".decode[CompactSizeUint].map(_.bytes))(success(hex"fe20a10700")) &&
        assert(hex"feffffffff".decode[CompactSizeUint].map(_.bytes))(success(hex"feffffffff")) &&
        assert(hex"ffffff008bff0001a1".decode[CompactSizeUint].map(_.bytes))(success(hex"ffffff008bff0001a1")) &&
        assert(hex"ffffffffffffffffff".decode[CompactSizeUint].map(_.bytes))(success(hex"ffffffffffffffffff"))
    )
  )

}
