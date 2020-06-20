package scash.warhorse.core.typeclass

import zio.test._
import zio.test.laws.Laws

import scash.warhorse.core._
import scash.warhorse.core.blockchain.CashAddr
import scash.warhorse.gen
import scash.warhorse.util._

object ShowSpec extends DefaultRunnableSpec {
  val symmetryLaw = new Laws.Law1[Show]("symmetryLaw") {
    def apply[A: Show](a: A): TestResult =
      assert(a.show.parse[A])(success(a))
  }

  val laws: Laws[Show] = symmetryLaw

  val spec = suite("ShowSpec")(
    testM("cashaddr")(laws.run(gen.address)(CashAddr.addrShow))
  )

}
