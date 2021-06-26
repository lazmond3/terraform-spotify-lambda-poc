import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.0/userguide/building_java_projects.html
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.4.31"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    kotlin("kapt") version "1.5.20"
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

noArg {
    annotation("terraform.spotify.lambda.poc.annotation.NoArgsConstructor")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-lambda-java-events:3.1.0")
    runtimeOnly("com.amazonaws:aws-lambda-java-log4j2:1.2.0")

    // bom
    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.11.1020"))
    implementation("com.amazonaws:aws-java-sdk-dynamodb")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.squareup.okhttp3:mockwebserver:4.9.0")

    // line bot
    implementation("com.linecorp.bot:line-bot-api-client:4.3.0")

    // dagger2
    implementation("com.google.dagger:dagger:2.37")
    kapt("com.google.dagger:dagger-compiler:2.37")
}

application {
    // Define the main class for the application.
    mainClass.set("terraform.spotify.lambda.poc.AppKt")
}

tasks {
//    register<Zip> ("buildZip2") {
//        println("hello world")
//        println("hello world")
//        from(compileKotlin)
//        from(processResources)
//        into("bbbbbbbbbbb") {
//            from(configurations.runtime)
//        }
//    }
//    create<Zip> ("buildZip") {
//        from(compileKotlin)
//        from(processResources)
//        into("bbbbbbbbbbb") {
//            from(configurations.runtime)
//        }
//    }
    named("build") {
        dependsOn("shadowJar")
    }

}
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
//tasks.withType<Test> {
//    useJUnitPlatform()
//}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        javaParameters = true
        freeCompilerArgs = listOf(
            "-Xjsr305=strict"
        )
    }
}