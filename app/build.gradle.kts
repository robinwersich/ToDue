plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("plugin.compose")
  id("com.google.devtools.ksp")
  id("androidx.room")
}

android {
  namespace = "com.robinwersich.todue"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.robinwersich.todue"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "0.1.1"

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
  kotlinOptions { jvmTarget = "1.8" }
  buildFeatures { compose = true }
  packaging { resources { excludes.add("/META-INF/{AL2.0,LGPL2.1}") } }
}

composeCompiler {
  enableStrongSkippingMode = true
  stabilityConfigurationFile = project.layout.projectDirectory.file("compose-stability.conf")
}

room { schemaDirectory(project.layout.projectDirectory.dir("schemas").toString()) }

ksp { arg("room.generateKotlin", "true") }

dependencies {
  // Core Android Libraries
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.activity:activity-compose:1.9.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

  // Compose
  val composeUiVersion = "1.6.8"
  implementation("androidx.compose.ui:ui:$composeUiVersion")
  implementation("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")
  debugImplementation("androidx.compose.ui:ui-tooling:$composeUiVersion")
  debugImplementation("androidx.compose.ui:ui-test-manifest:$composeUiVersion")
  implementation("androidx.compose.material3:material3:1.2.1")

  // Room
  val roomVersion = "2.6.1"
  implementation("androidx.room:room-runtime:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")

  // Paging
  val pagingVersion = "3.3.0"
  implementation("androidx.paging:paging-runtime:$pagingVersion")
  implementation("androidx.paging:paging-compose:$pagingVersion")

  // Utilities
  implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
  implementation("org.threeten:threeten-extra:1.7.2")

  // Tests
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeUiVersion")

  // Desugaring
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
