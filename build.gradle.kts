import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50" apply (true)
    id("org.jetbrains.dokka") version "0.10.0"
    `build-scan`
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    publishAlways()
}

group = "org.mechdancer"
version = "0.0.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.2")

    implementation("tech.standalonetc", "protocol", "0.2.4")

    implementation("org.mechdancer", "common-extension", "0.1.0-3")
    implementation("org.mechdancer", "common-concurrent", "0.1.0-3")
    implementation("org.mechdancer", "common-extension-log4j", "0.1.0-3") {
        exclude("log4j")
    }
    implementation("org.mechdancer", "remote", "0.2.1-dev-13") {
        exclude("org.slf4j")
    }
    implementation("org.mechdancer", "dependency", "0.1.0-rc-3")
    implementation("org.mechdancer", "dataflow-jvm", "0.2.0-dev-6")
    implementation("org.slf4j", "slf4j-log4j12", "1.7.29")

    testImplementation("junit", "junit", "4.12")
    testImplementation(kotlin("test-junit"))
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:1.3.50")
        force("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.dokka {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}

val doc = tasks.register<Jar>("javadocJar") {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
}

val sources = tasks.register<Jar>("sourcesJar") {
    group = JavaBasePlugin.BUILD_TASK_NAME
    description = "Creates sources jar"
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val fat = tasks.register<Jar>("fatJar") {
    group = JavaBasePlugin.BUILD_TASK_NAME
    description = "Packs binary output with dependencies"
    archiveClassifier.set("all")
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.register("allJars") {
    group = JavaBasePlugin.BUILD_TASK_NAME
    description = "Assembles all jars in one task"
    dependsOn(doc, sources, fat, tasks.jar)
}

artifacts {
    add("archives", tasks.jar)
    add("archives", fat)
    add("archives", sources)
    add("archives", doc)
}
