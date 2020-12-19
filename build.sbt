ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.0.1"
ThisBuild / organization := "com.github.tzarouali"

val Http4sVersion        = "0.21.7"
val CatsVersion          = "2.1.1"
val CatsEffectVersion    = "2.1.4"
val Fs2Version           = "2.4.3"
val ScalaNewtypeVersion  = "0.4.4"
val CirceVersion         = "0.13.0"
val LogbackVersion       = "1.2.3"
val kindProjectorVersion = "0.11.0"
val BetterMonadicVersion = "0.3.1"
val CatsRetryVersion     = "1.1.1"

lazy val root = (project in file("."))
  .settings(
    name := "aylien-challenge",
    scalacOptions.in(Compile) ~= (opts => opts.filterNot(_ == "-Xlint:package-object-classes")),
    scalacOptions.in(Compile) ++= Seq("-Ymacro-annotations"),
    libraryDependencies ++= Seq(
      //
      // Http requests
      //
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      //
      // :=)
      //
      "org.typelevel"    %% "cats-core"   % CatsVersion,
      "org.typelevel"    %% "cats-effect" % CatsEffectVersion,
      "co.fs2"           %% "fs2-core"    % Fs2Version,
      "com.github.cb372" %% "cats-retry"  % CatsRetryVersion,
      //
      // Newtypes
      //
      "io.estatico" %% "newtype" % ScalaNewtypeVersion,
      //
      // JSON
      //
      "io.circe" %% "circe-core"    % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      //
      // Logging
      //
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    ),
    addCompilerPlugin(("org.typelevel" %% "kind-projector" % kindProjectorVersion).cross(CrossVersion.full)),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % BetterMonadicVersion)
  )
