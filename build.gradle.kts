import com.novoda.gradle.release.PublishExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin")
        classpath("com.novoda:bintray-release:+")
    }
}


group = "tech.standalonetc"
version = "0.0.1"

repositories {
    mavenCentral()
    jcenter()
}

plugins {
    kotlin("jvm") version "1.3.21"
    id("org.jetbrains.dokka") version "0.9.17"
}

apply {
    plugin("com.novoda.bintray-release")
}

task<Jar>("javadocJar") {
    group = "build"
    classifier = "javadoc"
    from("$buildDir/javadoc")
}

task<Jar>("sourcesJar") {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

tasks.withType<DokkaTask> {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}

tasks["javadoc"].dependsOn("dokka")
tasks["jar"].dependsOn("sourcesJar")
tasks["jar"].dependsOn("javadocJar")

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("tech.standalonetc", "protocol", "0.2.4")


    implementation("org.mechdancer:dataflow-jvm:0.2.0-dev-5")
    implementation("org.mechdancer:common-concurrent:v0.1.0-1")
    implementation("org.mechdancer:common-extension-log4j:v0.1.0-1")

    testCompile("junit", "junit", "4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

configure<PublishExtension> {
    userOrg = "standalonetc"
    groupId = "tech.standalonetc"
    artifactId = "host"
    publishVersion = version.toString()
    desc = "host of standalone"
    website = "https://github.com/StandaloneTC/host"
    issueTracker = "https://github.com/StandaloneTC/host/issues"
    repository = "https://github.com/StandaloneTC/host.git"
}
