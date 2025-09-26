import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("plugin.compose")
  id("com.google.devtools.ksp")
  id("androidx.room")
}

android {
  namespace = "com.robinwersich.todue"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.robinwersich.todue"
    minSdk = 24
    targetSdk = 35
    versionCode = 2
    versionName = "0.2.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }
  }

  buildTypes {
    getByName("release") {
      isDebuggable = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    getByName("debug") { isDebuggable = true }
  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  buildFeatures { compose = true }
  packaging { resources { excludes.add("/META-INF/{AL2.0,LGPL2.1}") } }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_1_8
  }
}

composeCompiler {
  stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose-stability.conf"))
  reportsDestination = project.layout.buildDirectory.dir("reports")
}

room { schemaDirectory(project.layout.projectDirectory.dir("schemas").toString()) }

ksp { arg("room.generateKotlin", "true") }

dependencies {
  // Core Android Libraries
  implementation("androidx.core:core-ktx:1.17.0")
  implementation("androidx.activity:activity-compose:1.11.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

  // Compose
  val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
  implementation(composeBom)
  testImplementation(composeBom)
  androidTestImplementation(composeBom)

  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.material3:material3")

  // Room
  val roomVersion = "2.8.1"
  implementation("androidx.room:room-runtime:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")

  // Paging
  val pagingVersion = "3.3.6"
  implementation("androidx.paging:paging-runtime:$pagingVersion")
  implementation("androidx.paging:paging-compose:$pagingVersion")

  // Utilities
  implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")
  implementation("org.threeten:threeten-extra:1.8.0")

  // Tests
  testImplementation("junit:junit:4.13.2")
  testImplementation("com.google.truth:truth:1.4.5")
  testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.0")
  androidTestImplementation("androidx.test.ext:junit:1.3.0")
  androidTestImplementation("com.google.truth:truth:1.4.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")

  // Desugaring
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
