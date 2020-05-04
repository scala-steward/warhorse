import sbt._

object Deps {
  object V {
    val bouncyCastle = "1.65"
    val circe        = "0.13.0"
    val scodec       = "1.11.7"
    val scodecbits   = "1.1.14"
    val sttp         = "2.0.9"
    val zio          = "1.0.0-RC18-2+223-ae857d4f-SNAPSHOT"
    val ziosecp256k1 = "0.1.5"
  }

  object Libs {
    val bouncycastle = "org.bouncycastle"             % "bcprov-jdk15on"                 % V.bouncyCastle
    val circeCore    = "io.circe"                     %% "circe-core"                    % V.circe
    val circeGen     = "io.circe"                     %% "circe-generic"                 % V.circe
    val scodecbits   = "org.scodec"                   %% "scodec-bits"                   % V.scodecbits withSources () withJavadoc ()
    val scodec       = "org.scodec"                   %% "scodec-core"                   % V.scodec withSources () withJavadoc ()
    val sttp         = "com.softwaremill.sttp.client" %% "core"                          % V.sttp
    val sttpCirce    = "com.softwaremill.sttp.client" %% "circe"                         % V.sttp
    val sttpZio      = "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % V.sttp
    val zio          = "dev.zio"                      %% "zio"                           % V.zio withSources () withJavadoc ()
    val zioTest      = "dev.zio"                      %% "zio-test"                      % V.zio withSources () withJavadoc ()
    val zioTestsbt   = "dev.zio"                      %% "zio-test-sbt"                  % V.zio
    val ziosecp256k1 = "org.scash"                    %% "zio-secp256k1"                 % V.ziosecp256k1
  }

  val core = List(
    Libs.bouncycastle,
    Libs.scodec,
    Libs.scodecbits,
    Libs.zio
  )

  val testkit = List(
    Libs.scodec,
    Libs.scodecbits,
    Libs.zioTest      % "compile,test,it",
    Libs.zioTestsbt   % "test,it",
    Libs.ziosecp256k1 % "test"
  )

  val rpc = List(
    Libs.zio,
    Libs.circeCore,
    Libs.circeGen,
    Libs.sttp,
    Libs.sttpCirce,
    Libs.sttpZio
  )
}
