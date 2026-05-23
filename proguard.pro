# The rules from AOSP are located in proguard.flags file, we can just maintain Lawnchair related rules here.

# Optimization options.
-allowaccessmodification
-dontusemixedcaseclassnames
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

# Common rules.
-keep class android.window.** { *; }
-keep class android.view.** { *; }
-keep class com.android.systemui.** { *; }
-keep class com.android.wm.shell.** { *; }

-keepclassmembers class * implements android.os.Parcelable {
  public static final ** CREATOR;
}

# Lawnchair specific rules.
-keep class app.lawnchair.LawnchairProto$* { *; }
-keep class app.lawnchair.LawnchairApp { *; }
-keep class app.lawnchair.LawnchairLauncher { *; }
-keep class app.lawnchair.compatlib.** { *; }

-keep class com.google.protobuf.Timestamp { *; }

# TODO: Remove this after the change in https://github.com/ChickenHook/RestrictionBypass/pull/9 has been released.
-keep class org.chickenhook.restrictionbypass.** { *; }
