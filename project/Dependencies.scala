import sbt._
import Keys._
import Versions._

object Dependencies {
  def circe(artifact: String): ModuleID     = "io.circe"          %% s"circe-$artifact"     % circeVersion


  object Cats {
    val cats       = "org.typelevel" %% "cats-core"   % catsVersion
    val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
  }
  object Refined {
    val refinedCore      = "eu.timepit" %% "refined"           % refinedVersion
  }
  object Circe {
    val circeCore    = circe("core")
    val circeGeneric = circe("generic")
    val circeParser  = circe("parser")
    val circeRefined = circe("refined")
  }
  val squants = "org.typelevel" %% "squants"  % squantsVersion
  val monocleCore = "dev.optics"      %% "monocle-core" % monocleVersion
  val shapeless = "org.typelevel" %% "shapeless3-deriving" % shapelessVersion

  // Runtime
  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime

  val commonDependencies: Seq[ModuleID] = Seq(Cats.cats, Cats.catsEffect)

  val tradeioDependencies: Seq[ModuleID] = 
    commonDependencies ++ Seq(squants, shapeless) ++
      Seq(Refined.refinedCore) ++
        Seq(Circe.circeCore, Circe.circeGeneric, Circe.circeParser, Circe.circeRefined) ++ Seq(monocleCore)


}
