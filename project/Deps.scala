import sbt._

object Deps {
  object V {
    val scodecv     = "1.11.7"
    val scodecbitsv = "1.1.14"
    val zioV        = "1.0.0-RC18-2"
  }

  object Libs {
    val scodecbits = "org.scodec" %% "scodec-bits"  % V.scodecbitsv
    val scodec     = "org.scodec" %% "scodec-core"  % V.scodecv
    val zio        = "dev.zio"    %% "zio"          % V.zioV withSources () withJavadoc ()
    val zioTest    = "dev.zio"    %% "zio-test"     % V.zioV
    val zioTestsbt = "dev.zio"    %% "zio-test-sbt" % V.zioV % "test"
  }

  val core = List(
    Libs.scodec,
    Libs.scodecbits,
    Libs.zio
  )

  val testkit = List(
    Libs.scodec,
    Libs.scodecbits,
    Libs.zioTest,
    Libs.zioTestsbt
  )
}
