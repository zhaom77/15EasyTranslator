
// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
}

buildscript {
    apply(from = "repositories.gradle.kts")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        val kotlinVersion = rootProject.extra["kotlinVersion"].toString()
        classpath(rootProject.extra["androidPlugin"].toString())
        classpath(kotlin("gradle-plugin", kotlinVersion))
//        classpath("com.google.android.gms:oss-licenses-plugin:0.10.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.5.30")
//        classpath("org.mozilla.rust-android-gradle:plugin:0.9.0")

        //生成垃圾文件
        classpath("com.github.qq549631030:android-junk-code:1.2.1")

        //字符串加密
        classpath("com.github.megatronking.stringfog:gradle-plugin:4.0.1")
        // 选用加解密算法库，默认实现了xor算法，也可以使用自己的加解密库。
        classpath("com.github.megatronking.stringfog:xor:4.0.1")
    }
}

allprojects {
    apply(from = "${rootProject.projectDir}/repositories.gradle.kts")
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

// skip uploading the mapping to Crashlytics
subprojects {
    tasks.whenTaskAdded {
        if (name.contains("uploadCrashlyticsMappingFile")) enabled = false
    }
}
