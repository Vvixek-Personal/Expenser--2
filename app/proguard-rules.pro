# Readable crash reports
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# Moshi reflection (KotlinJsonAdapterFactory) needs Kotlin metadata
-keep class kotlin.Metadata { *; }

# Backup/recovery snapshots (LocalRecoveryManager) serialize these through
# reflection, so R8 must not rename or strip them
-keep class com.example.data.** { *; }

# Exchange-rate JSON models
-keep class com.example.api.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Background worker is created by class name
-keep class com.example.ui.RecurringTransactionWorker { *; }
