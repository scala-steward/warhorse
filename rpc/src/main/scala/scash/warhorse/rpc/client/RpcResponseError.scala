package scash.warhorse.rpc.client

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import scala.collection.immutable.Map

private[client] case class RpcResponseError(
  code: Int,
  message: String
)

private[client] object RpcResponseError {
  implicit val rpcResponseError: Decoder[RpcResponseError] = deriveDecoder

  def error(code: Int): String = errors.getOrElse(code, s"UnknownError: code $code")

  private val errors: Map[Int, String] = Map(
    (-1, "MiscError"),
    (-2, "ForbiddenBySafeMode"),
    (-3, "TypeError"),
    (-4, "WalletError"),
    (-5, "InvalidAddressOrKey"),
    (-6, "InsufficientFunds"),
    (-7, "OutOfMemory"),
    (-8, "InvalidParameter"),
    (-9, "NotConnected"),
    (-10, "InInitialDownload"),
    (-11, "InvalidLabelName"),
    (-12, "KeypoolRanOut"),
    (-13, "UnlockNeeded"),
    (-14, "PassphraseIncorrect"),
    (-15, "WrongEncState"),
    (-16, "EncryptionFailed"),
    (-17, "AlreadyUnlocked"),
    (-18, "NotFound"),
    (-19, "NotSpecified"),
    (-20, "DatabaseError"),
    (-22, "DeserializationError"),
    (-23, "NodeAlreadyAdded"),
    (-24, "NodeNotAdded"),
    (-25, "VerifyError"),
    (-26, "VerifyRejected"),
    (-27, "VerifyAlreadyInChain"),
    (-28, "InWarmUp"),
    (-29, "NodeNotConnected"),
    (-30, "InvalidIpOrSubnet"),
    (-31, "P2PDisabled"),
    (-32, "MethodDeprecated")
  )
}
