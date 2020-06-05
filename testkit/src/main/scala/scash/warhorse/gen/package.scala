package scash.warhorse

import scash.warhorse.core.blockchain.{ MainNet, RegTest, TestNet }
import scash.warhorse.gen.crypto.{ CryptoGen, IntGen, Secp256k1Gen, UintGen }
import zio.test.Gen

package object gen
    extends NumGen
    with IntGen
    with UintGen
    with ByteVectorGen
    with Secp256k1Gen
    with CryptoGen
    with ScriptGen
    with TransactionGen
    with AddressGen {
  def randomMessage = Gen.string1(Gen.anyChar)

  def netGenerator = Gen.elements(MainNet, TestNet, RegTest)
}
