package scash.warhorse.core.typeclass

import scash.warhorse._
import scash.warhorse.core._

import zio.test.Assertion._
import zio.test._
import zio.test.laws._

import scodec.bits._

object SerdeSpec extends DefaultRunnableSpec {
  val symmetryLaw = new Laws.Law1[Serde]("symmetryLaw") {
    def apply[A: Serde](a: A): TestResult =
      assert(a.bytes.decode_[A])(equalTo(a))
  }

  val symmetryHexLaw = new Laws.Law1[Serde]("symmetryHexLaw") {
    def apply[A: Serde](a: A): TestResult =
      assert(ByteVector.fromValidHex(a.hex).decode_[A])(equalTo(a))
  }

  val laws: Laws[Serde] = symmetryLaw + symmetryHexLaw

  val spec = suite("SerdeSpec")(
    suite("number")(
      testM("uint8")(laws.run(gen.uint8)),
      testM("uint32")(laws.run(gen.uint32)),
      testM("uint64")(laws.run(gen.uint64)),
      testM("int32")(laws.run(gen.int32)),
      testM("int64")(laws.run(gen.int64))
    ),
    suite("crypto")(
      testM("privkey")(laws.run(gen.privKey)),
      testM("pubkey")(laws.run(gen.pubkey)),
      testM("sha256")(laws.run(gen.sha256)),
      testM("hash160")(laws.run(gen.hash160))
    )
  )
}
