import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.20"
}

group = "tech.standalonetc"
version = "0.1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("tech.standalonetc", "protocol", "0.2.3")

    implementation("org.mechdancer:dataflow-jvm:0.2.0-dev-4")
    implementation("org.mechdancer:common-concurrent:v0.1.0-1")
    implementation("org.mechdancer:common-extension-log4j:v0.1.0-1")

    testCompile("junit", "junit", "4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions{
        jvmTarget = "1.8"
//        freeCompilerArgs = listOf("-Xuse-experimental=kotlin.contracts.ExperimentalContracts")
    }
}