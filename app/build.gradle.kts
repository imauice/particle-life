plugins { id("com.android.application") }
android { namespace = "com.particlelife.wallpaper"; compileSdk = 35
 defaultConfig { applicationId = "com.particlelife.wallpaper"; minSdk = 26; targetSdk = 35; versionCode = 3; versionName = "1.2-bio" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
