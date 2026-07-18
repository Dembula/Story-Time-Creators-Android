# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class online.storytime.creators.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class online.storytime.creators.**$$serializer { *; }
-keepclassmembers class online.storytime.creators.** {
    *** Companion;
}
