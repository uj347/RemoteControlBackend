
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt


plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("kapt") version "1.5.31"
}

group = "me.uj347"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation("org.jmdns:jmdns:3.5.7")
    implementation("io.netty:netty-all:4.1.68.Final")
    implementation ("com.google.dagger:dagger:2.38.1")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")
    implementation("com.squareup.moshi:moshi:1.12.0")
    kapt ("com.google.dagger:dagger-compiler:2.38.1")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}