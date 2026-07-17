# Proguard Rules for Jmoney Wear OS

# Keep Network DTOs for safe serialization/deserialization
-keep class com.jayanth.jmoney.wear.data.network.dto.** { *; }

# Keep Room entities and database models
-keep class com.jayanth.jmoney.wear.data.database.** { *; }

# General Kotlinx Serialization support
-keepclassmembers class * {
    *** Companion;
    *** $serializer;
}

# Suppress R8 warning for missing Guava/J2ObjC dependency
-dontwarn com.google.j2objc.annotations.RetainedWith
