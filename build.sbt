ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "3.5.2"
ThisBuild / idePackagePrefix := Some("com.siriusxm.cart")

lazy val root = (project in file(".")).settings(
  name := "cats-effect-3-quick-start",
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % "3.6.3",
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % "3.6.3",
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % "3.6.3",
    // JSON Support
    "io.circe" %% "circe-core" % "0.14.15",
    "io.circe" %% "circe-generic" % "0.14.15",
    "io.circe" %% "circe-parser" % "0.14.15",
    // http client

    // HTTP client
    "org.http4s" %% "http4s-ember-client" % "0.23.29",
    // (optional, handy if you want http4s <-> Circe EntityDecoder/Encoder helpers)
    "org.http4s" %% "http4s-circe" % "0.23.29",
    // better monadic for compiler plugin as suggested by documentation
    //    compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
    "org.http4s" %% "http4s-dsl" % "0.23.33" % Test

  )
)

