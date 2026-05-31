# The rules from AOSP are located in proguard.flags file, we can just maintain Lawnchair related rules here.

# Optimization options.
-allowaccessmodification
-dontusemixedcaseclassnames
-allowaccessmodification
-keepattributes InnerClasses, *Annotation*, Signature, SourceFile, LineNumberTable


# This is generated automatically by the Android Gradle plugin.
-dontwarn android.appwidget.AppWidgetHost$AppWidgetHostListener
-dontwarn android.util.StatsEvent$Builder
-dontwarn android.util.StatsEvent
-dontwarn androidx.window.extensions.**
-dontwarn androidx.window.sidecar.**
-dontwarn com.android.org.conscrypt.TrustManagerImpl
-dontwarn com.android.wm.shell.**
-dontwarn com.skydoves.balloon.**
-dontwarn dalvik.system.CloseGuard
-dontwarn lineageos.providers.LineageSettings$System
-dontwarn androidx.compose.runtime.PrimitiveSnapshotStateKt
-dontwarn androidx.renderscript.Allocation
-dontwarn androidx.renderscript.BaseObj
-dontwarn androidx.renderscript.Element
-dontwarn androidx.renderscript.FieldPacker
-dontwarn androidx.renderscript.RSRuntimeException
-dontwarn androidx.renderscript.RenderScript
-dontwarn androidx.renderscript.Script$LaunchOptions
-dontwarn androidx.renderscript.ScriptC
-dontwarn androidx.renderscript.ScriptIntrinsicBlur
-dontwarn androidx.renderscript.Type

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
-keep,allowshrinking,allowoptimization class android.view.** { *; }

-keep,allowshrinking,allowoptimization class com.google.protobuf.Timestamp { *; }
