name := "scrimage"

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

useGpg := true

pomIncludeRepository := { _ => false}

pomExtra := {
  <url>https://github.com/sksamuel/scrimage</url>
    <licenses>
      <license>
        <name>The Apache Software License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:git@github.com:sksamuel/scrimage.git</connection>
      <developerConnection>scm:git:git@github.com:sksamuel/scrimage.git</developerConnection>
      <url>git@github.com:sksamuel/scrimage.git</url>
    </scm>
    <developers>
      <developer>
        <name>Stephen Samuel</name>
        <email>sam@sksamuel.com</email>
        <timezone>GMT</timezone>
      </developer>
    </developers>
}
