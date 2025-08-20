# 保留 Kotlin 相关
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Hilt
-keep,allowobfuscation @dagger.hilt.android.HiltAndroidApp class * extends android.app.Application
-keep,allowobfuscation @dagger.hilt.android.internal.managers.ApplicationComponentManager class * extends android.app.Application
-keep,allowobfuscation @dagger.hilt.android.internal.managers.FragmentComponentManager class * extends androidx.fragment.app.Fragment

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.runtime.** { *; }

# WebView
-keep class android.webkit.** { *; }

# 保留应用程序的类
-keep class com.shellplusplus.** { *; }
-keepclassmembers class com.shellplusplus.** { *; }

# GSON
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# 保留 JavaScript 接口
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# 保留枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留 Parcelable
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# 保留 Serializable
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
