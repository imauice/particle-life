# Particle Life — Bond Lab Live Wallpaper

Native Android Live Wallpaper using `WallpaperService` and Canvas. Reimplements the original Bond Lab particle/bond physics (not a WebView). No network permissions.

## Build
1. Open this folder in Android Studio (JDK 17, Android SDK 35).
2. Let Gradle sync (Android Gradle Plugin 8.7.3).
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Install `app/build/outputs/apk/debug/app-debug.apk` on your phone.
5. Launch **Particle Life Bond Lab**, adjust settings, tap **Set Live Wallpaper**, preview and apply.

Settings: particles (100–450), bond probability, bond strength, random motion, FPS 10–60. Touch wallpaper to add particles. Stops updating when hidden.

Note: This is a source project, not a prebuilt APK. Internet access may be needed on the build computer for Gradle dependencies. The simulation is a stylized model, not physical molecular dynamics.
