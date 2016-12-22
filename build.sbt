scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
    "io.grpc" % "grpc-netty" % "1.0.1",
    "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion,
    "com.websudos" %% "phantom-dsl" % "1.29.6"
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

// If you need scalapb/scalapb.proto or anything from google/protobuf/*.proto
// libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"