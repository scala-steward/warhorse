package scash.warhorse.rpc.responses

import scash.warhorse.rpc.responses.util._
import scash.warhorse.util._

import zio.test.{ suite, DefaultRunnableSpec }
import zio.test._

object BlockchainSpec extends DefaultRunnableSpec {
  val spec = suite("BlockchainSpec")(
    testM("GetBlock")(
      assertM(parseJsonfromFile[GetBlock]("rpcblock.json"))(successful[GetBlock])
    ),
    testM("GetBlockWithTransactions")(
      assertM(parseJsonfromFile[GetBlockWithTransactions]("rpcblocktx.json"))(successful[GetBlockWithTransactions])
    ),
    testM("GetBlockChainInfo")(
      assertM(parseJsonfromFile[GetBlockChainInfo]("blockchaininfo.json"))(successful[GetBlockChainInfo])
    ),
    test("GetBlockheader")(assert(parseJson[GetBlockHeader](blockheader))(successful[GetBlockHeader])),
    test("ChainTips")(assert(parseJson[Vector[ChainTip]](chaintips))(successful[Vector[ChainTip]]))
  )

  val chaintips = """[
  {
    "height": 631141,
    "hash": "00000000000000000204d2bf270760cfeee06f02010f832e5872bfab843dfa76",
    "branchlen": 0,
    "status": "active"
  },
  {
    "height": 625151,
    "hash": "000000000000000000043bfc5b707897ed6bac4c71f2d5797808940b43c73df1",
    "branchlen": 1,
    "status": "valid-headers"
  },
  {
    "height": 560697,
    "hash": "000000000000000000b49253f36544c13343a98607009e213a6eb9152d28c4ca",
    "branchlen": 3931,
    "status": "headers-only"
  },
  {
    "height": 556767,
    "hash": "00000000000000000067d19fc7aba2667b4676a4c84c2fb0d4da7043469601ed",
    "branchlen": 1,
    "status": "invalid"
  }
  ]
  """

  val blockheader = """
      {
        "hash": "00000000000000000001d13ecd23ca98967abed37285fe70986345df01f2cb6a",
        "confirmations": 1,
        "height": 631142,
        "version": 541065216,
        "versionHex": "20400000",
        "merkleroot": "071b75a30107881b7fd10bdf447ac23576cb68cb279c7109286945df397031e7",
        "time": 1587078884,
        "mediantime": 1587071335,
        "nonce": 1685371160,
        "bits": "18045caa",
        "difficulty": 252063884919.1585,
        "chainwork": "0000000000000000000000000000000000000000012d86c519fb206aea200201",
        "nTx": 1422,
        "previousblockhash": "00000000000000000204d2bf270760cfeee06f02010f832e5872bfab843dfa76"
      }
      """
}
