# The rules from AOSP are located in proguard.flags file, we can just maintain Lawnchair related rules here.

# Optimization options.
-allowaccessmodification
-dontusemixedcaseclassnames
-allowaccessmodification
-keepattributes InnerClasses, *Annotation*, Signature, SourceFile, LineNumberTable

# Remove some Kotlin overhead
-processkotlinnullchecks remove

# Common rules.
-keep,allowshrinking,allowoptimization class android.window.** { *; }
-keep,allowshrinking,allowoptimization class android.view.** { *; }
-keep,allowshrinking,allowoptimization class com.android.systemui.** { *; }
-keep,allowshrinking,allowoptimization class com.android.wm.shell.** { *; }

-keepclassmembers class * implements android.os.Parcelable {
  public static final ** CREATOR;
}

# Lawnchair specific rules.
-keep,allowshrinking,allowoptimization class app.lawnchair.LawnchairProto$* { *; }
-keep,allowshrinking,allowoptimization class app.lawnchair.LawnchairApp { *; }
-keep,allowshrinking,allowoptimization class app.lawnchair.LawnchairLauncher { *; }
-keep,allowshrinking,allowoptimization class app.lawnchair.compatlib.** { *; }

-keep,allowshrinking,allowoptimization class com.google.protobuf.Timestamp { *; }
