# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Timber
-keep class timber.log.** { *; }
-keep class org.jetbrains.annotations.** { *; }
-keep class kotlin.** { *; }

# Keep game classes
-keep class com.example.snakegame.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep generic signatures for better reflection
-keepattributes Signature

# Keep annotations
-keepattributes *Annotation*

# For navigation and safe args
-keep class androidx.navigation.** { *; }

# For view binding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(android.view.LayoutInflater);
    public static * inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

# Remove logging in release builds
-assumenosideeffects class timber.log.Timber {
    public static void d(...);
    public static void i(...);
    public static void v(...);
    public static void w(...);
}