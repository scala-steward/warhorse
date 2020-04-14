package scash.warhorse.rpc

import zio.Has

package object client extends JParamSyntax {
  type RpcClient = Has[RpcClient.Service]
}
