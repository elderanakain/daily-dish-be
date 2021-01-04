import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

val logback_version: String by project
val ktor_version = "1.5.0"
val kotlin_version: String by project
val exposed_version = "0.28.1"

plugins {
  application
  kotlin("jvm") version "1.4.10"
  kotlin("plugin.serialization") version "1.4.10"
}

group = "io.krugosvet.dailydish"
version = "0.2.2-SNAPSHOT"

application {
  mainClassName = "io.ktor.server.netty.EngineMain"
}

repositories {
  mavenLocal()
  jcenter()
  maven { url = uri("https://kotlin.bintray.com/ktor") }
}

dependencies {

  // Ktor
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
  implementation("io.ktor:ktor-server-netty:$ktor_version")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  implementation("io.ktor:ktor-server-core:$ktor_version")
  implementation("io.ktor:ktor-serialization:$ktor_version")
  testImplementation("io.ktor:ktor-server-tests:$ktor_version")

  // DB
  implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-jodatime:$exposed_version")
  implementation("org.postgresql:postgresql:42.2.2")

  implementation("org.koin:koin-ktor:2.2.1")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

tasks {
  test {
    testLogging {
      events = setOf(TestLogEvent.FAILED)
      exceptionFormat = TestExceptionFormat.FULL
    }
  }

  create("stage") {
    dependsOn("installDist")
  }
}
