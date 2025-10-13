import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.kotlin.android)
  alias(libs.plugins.jetbrains.kotlin.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.androidx.room)
}

android {
  namespace = "com.robinwersich.todue"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.robinwersich.todue"
    minSdk = 24
    targetSdk = 35
    versionCode = 4
    versionName = "0.2.2"

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

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_1_8 } }

composeCompiler {
  stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose-stability.conf"))
  reportsDestination = project.layout.buildDirectory.dir("reports")
}

room { schemaDirectory(project.layout.projectDirectory.dir("schemas").toString()) }

ksp { arg("room.generateKotlin", "true") }

dependencies {
  // Android
  coreLibraryDesugaring(libs.desugar.jdk)
  implementation(libs.core.ktx)
  implementation(libs.activity.compose)
  implementation(libs.lifecycle.runtime.ktx)
  implementation(libs.lifecycle.viewmodel.compose)
  implementation(libs.paging.runtime)
  implementation(libs.paging.compose)
  implementation(libs.room.runtime)
  ksp(libs.room.compiler)
  implementation(libs.room.ktx)

  // Compose
  val composeBom = platform(libs.compose.bom)
  implementation(composeBom)
  testImplementation(composeBom)
  androidTestImplementation(composeBom)

  implementation(libs.compose.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)
  debugImplementation(libs.compose.ui.test.manifest)
  implementation(libs.compose.foundation)
  implementation(libs.compose.material3)

  // Utilities
  implementation(libs.kotlinx.collections.immutable)
  implementation(libs.threeTenExtra)

  // Tests
  testImplementation(libs.junit)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.truth)
  androidTestImplementation(libs.compose.ui.test.junit4)
  androidTestImplementation(libs.espresso.core)
  androidTestImplementation(libs.junit.android.ext)
  androidTestImplementation(libs.truth)
}
