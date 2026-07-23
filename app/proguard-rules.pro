# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.storytime.creators.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.storytime.creators.**$$serializer { *; }
-keepclassmembers class com.storytime.creators.** {
    *** Companion;
}
