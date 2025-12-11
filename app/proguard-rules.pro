# Snake Game için özel ProGuard kuralları
-keep class com.example.snakegame.** { *; }
-keepclassmembers class com.example.snakegame.** {
    *;
}

# DataStore sınıflarını koru
-keep class androidx.datastore.** { *; }

# Compose bileşenlerini koru
-keep class androidx.compose.** { *; }

# Navigation bileşenlerini koru
-keep class androidx.navigation.** { *; }