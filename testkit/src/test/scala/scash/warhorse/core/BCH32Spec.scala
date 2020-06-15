package scash.warhorse.core

import scash.warhorse.core.number.{ Uint5, Uint64 }
import scash.warhorse.util._

import scodec.bits.ByteVector

import zio.ZIO
import zio.test.DefaultRunnableSpec
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.Assertion._

import Predef.{ augmentString, intWrapper }

object BCH32Spec extends DefaultRunnableSpec {
  case class BCH32Test(payloadSizeBits: Int, vtype: Int, addr: String, payLoadHex: ByteVector)

  implicit val bch32Decoder =
    rowCoder(str => BCH32Test(str(0).toInt * 8, str(1).toInt, str(2), ByteVector.fromValidHex(str(3))))

  private def testCheckSum(str: String): Boolean = {
    val strs = str.split(":")
    BCH32.verifyCheckSum(strs(0), strs(1))
  }

  private def testPolyMod(n: Int)(pad: Int, expected: Uint64) =
    assert(BCH32.polyMod(Vector(Uint5(n.toByte)) ++ Vector.fill(pad)(Uint5.zero)))(equalTo(expected))

  val spec = suite("BCH32Spec")(
    testM("decode")(
      jsonFromCSV[BCH32Test]("bch32.json") { test =>
        val split    = test.addr.split(":")
        val typeByte = (test.vtype.toByte << 3).toByte
        val bch      = BCH32.genBch32(split(0), typeByte, test.payLoadHex)
        assert(bch.toString)(equalTo(test.addr))
      }
    ),
    testM("fromString") {
      jsonFromCSV[BCH32Test]("bch32.json") { test =>
        val split = test.addr.split(":")
        val bch   = BCH32.fromString(split(0), split(1))
        assert(bch.map(_.toString))(success(test.addr))
      }
    },
    test("invalid versionbyte")(
      assert(BCH32.fromString("bitcoincash", "0pm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a"))(failure)
    ),
    test("invalid mixed case")(
      assert(BCH32.fromString("bitcoincash", "qpm2qsznhks23z7629Mms6s4cwef74vcwvy22gdx6a"))(failure)
    ),
    // format: off
    suite("polyMod")(
      test("empty")(assert(BCH32.polyMod(Vector.empty))(equalTo(Uint64.zero))),
      testM("all Uint5")(
        ZIO.foreach(1 to 31) { n =>
          val polyVec = testPolyMod(n) _
          ZIO.succeed(
            polyVec(0, Uint64(0x21 ^ n)) &&
              polyVec(1, Uint64(0x401 ^ (n << 5))) &&
              polyVec(2, Uint64(0x8001 ^ (n << 10))) &&
              polyVec(3, Uint64(0x100001 ^ (n << 15))) &&
              polyVec(4, Uint64(0x2000001 ^ (n << 20))) &&
              polyVec(5, Uint64(0x40000001 ^ (n << 25))) &&
              polyVec(6, Uint64(0x800000001L) ^ (Uint64(n) << 30)) &&
              polyVec(7, Uint64(0x98f2bc8e60L) ^ (Uint64(n) << 35))
            )}
          .map(BoolAlgebra.collectAll(_).get)
        ),
      test("array") {
        val poly = (Vector.fill(8)(0) ++ (0 to 31).toVector ++ Vector.fill(8)(0)).map(i => Uint5(i.toByte))
        assert(BCH32.polyMod(poly))(equalTo(Uint64(0x724afe7af2L)))
      }
    ),
    suite("checkSum")(
      test("prefix")(assert(testCheckSum("prefix:x64nx6hz"))(isTrue)),
      test("p")(assert(testCheckSum("p:gpf8m4h7"))(isTrue)),
      test("bitcoincash2")(assert(testCheckSum("bitcoincash:qr6m7j9njldwwzlg9v7v53unlr4jkmx6eylep8ekg2"))(isTrue)),
      test("bitcoincash")(assert(testCheckSum("bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a"))(isTrue)),
      test("bchtest")(assert(testCheckSum("bchtest:testnetaddress4d6njnut"))(isTrue)),
      test("bchreg")(assert(testCheckSum("bchreg:555555555555555555555555555555555555555555555udxmlmrz"))(isTrue))
    ),
    // format: on
  )

}
