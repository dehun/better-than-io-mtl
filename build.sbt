name := "better-than-io"

version := "0.1"

scalaVersion := "2.12.7"

mainClass in (Compile, run) := Some("space.dehun.BetterThanIoMain")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")

scalacOptions ++= Seq("-encoding", "UTF-8")

libraryDependencies ++= Seq(
	"org.scalactic" %% "scalactic" % "3.0.5",
	"org.scalatest" %% "scalatest" % "3.0.5" % "test",
	"org.typelevel" %% "cats-core" % "1.4.0",
	"org.typelevel" %% "cats-mtl-core" % "0.4.0",
	"org.typelevel" %% "cats-effect" % "1.0.0"
)