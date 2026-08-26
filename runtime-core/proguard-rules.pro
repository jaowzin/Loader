# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class dev.jaowzin.carromloader.runtime.** {*; }
-keep class top.niunaijun.jnihook.** {*; }
-keep class mirror.** {*; }
-keep class android.** {*; }
-keep class com.android.** {*; }

-keep class dev.jaowzin.carromloader.bridge.** {*; }
-keep @dev.jaowzin.carromloader.bridge.annotation.BClass class * {*;}
-keep @dev.jaowzin.carromloader.bridge.annotation.BClassName class * {*;}
-keep @dev.jaowzin.carromloader.bridge.annotation.BClassNameNotProcess class * {*;}
-keepclasseswithmembernames class * {
    @dev.jaowzin.carromloader.bridge.annotation.BField.* <methods>;
    @dev.jaowzin.carromloader.bridge.annotation.BFieldNotProcess.* <methods>;
    @dev.jaowzin.carromloader.bridge.annotation.BFieldSetNotProcess.* <methods>;
    @dev.jaowzin.carromloader.bridge.annotation.BFieldCheckNotProcess.* <methods>;
    @dev.jaowzin.carromloader.bridge.annotation.BMethod.* <methods>;
    @dev.jaowzin.carromloader.bridge.annotation.BStaticField.* <methods>;
    @dev.jaowzin.carromloader.bridge.annotation.BStaticMethod.* <methods>;
    @dev.jaowzin.carromloader.bridge.annotation.BMethodCheckNotProcess.* <methods>;
    @dev.jaowzin.carromloader.bridge.annotation.BConstructor.* <methods>;
    @dev.jaowzin.carromloader.bridge.annotation.BConstructorNotProcess.* <methods>;
}