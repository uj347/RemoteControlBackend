
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.js.inline.clean.removeDuplicateImports
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
    implementation("org.jmdns:jmdns:3.5.7")
    implementation("io.netty:netty-all:4.1.68.Final")
    implementation ("com.google.dagger:dagger:2.39.1")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")
    implementation("com.squareup.moshi:moshi:1.12.0")
    kapt ("com.google.dagger:dagger-compiler:2.39.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("commons-io:commons-io:2.9.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.6.1")
    implementation("org.apache.logging.log4j:log4j-core:2.6.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.2")
    implementation("io.github.hakky54:sslcontext-kickstart:7.0.2")
    implementation("io.github.hakky54:sslcontext-kickstart-for-pem:7.0.2")
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("org.mockito:mockito-core:4.1.0")


}

tasks.test {
    useJUnit()
}


tasks.processResources {
    duplicatesStrategy=org.gradle.api.file.DuplicatesStrategy.INCLUDE
}

sourceSets {
    main {
        resources {
            srcDir ("/src/main/resources")
        }
    }
}



tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}