# kotlinx.serialization (Supabase-DTOs)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class de.paarfinanz.**$$serializer { *; }
-keepclassmembers class de.paarfinanz.** { *** Companion; }
-keepclasseswithmembers class de.paarfinanz.** { kotlinx.serialization.KSerializer serializer(...); }

# Ktor / OkHttp
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
