# App-specific R8 rules.
# Keep reflection-based serialization metadata intact.
-keepattributes Signature,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class **$$serializer { *; }
