package scash.warhorse.rpc.responses

import scash.warhorse.rpc.responses.util._
import scash.warhorse.util._

import zio.test.{ suite, DefaultRunnableSpec }
import zio.test._

object RpcTransactionCodecSpec extends DefaultRunnableSpec {
  val jsonScriptPubKey =
    """
    {
      "asm": "0496b538e853519c726a2c91e61ec11600ae1390813a627c66fb8be7947be63c52da7589379515d4e0a604f8141781e62294721166bf621e73a82cbf2342c858ee OP_CHECKSIG",
      "hex": "410496b538e853519c726a2c91e61ec11600ae1390813a627c66fb8be7947be63c52da7589379515d4e0a604f8141781e62294721166bf621e73a82cbf2342c858eeac",
      "reqSigs": 1,
      "type": "pubkey",
      "addresses": ["bitcoincash:qqgekzvw96vq5g57zwdfa5q6g609rrn0ycp33uc325"]
    }
  """

  val scriptPubkey = parseJson[RpcScriptPubKey](jsonScriptPubKey)
  val spec = suite("RpcTransactionCodecSpec")(
    test("scriptPubKey")(
      assert(scriptPubkey)(successful[RpcScriptPubKey])
    )
  )
}
