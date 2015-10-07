name := "authentication-service-example"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion = "2.3.9"
  val sprayVersion = "1.3.2"
  val specs2Version = "3.6.4"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayVersion,
    "io.spray"            %%  "spray-routing" % sprayVersion,
    "io.spray"            %%  "spray-json"    % sprayVersion,
    "io.spray"            %%  "spray-client"  % sprayVersion,
    "com.typesafe.akka"   %%  "akka-actor"    % akkaVersion,
    "io.spray"            %%  "spray-testkit" % sprayVersion  % "test",
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaVersion   % "test",
    "org.specs2"          %%  "specs2-core"   % specs2Version % "test"
  )
}