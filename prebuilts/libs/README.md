# Lawnchair Prebuilt JARs

Launcher3 has some dependencies on internal AOSP modules. 
To build Lawnchair, you have to build AOSP and obtain these JARs.

| File                       | Path                                                                                                                              |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| framework-16.jar           | ./soong/.intermediates/frameworks/base/framework/android_common/turbine-combined/framework.jar                                    |
| SystemUI-statsd-16.jar     | ./soong/.intermediates/frameworks/base/packages/SystemUI/shared/SystemUI-statsd/android_common/javac/SystemUI-statsd.jar          |
| WindowManager-Shell-16.jar | ./soong/.intermediates/frameworks/base/libs/WindowManager/Shell/WindowManager-Shell/android_common/javac/WindowManager-Shell.jar |

## Usage

Lawnchair rely on these JAR:

`aosp_cf_x86_64_only_phone-aosp_current-userdebug`

| File                       | Command                 | Android tag       |
|----------------------------|-------------------------|-------------------|
| framework-16.jar           | `m framework`           | android-16.0.0_r2 |
| SystemUI-statsd-16.jar     | `m SystemUI-statsd`     | android-16.0.0_r2 |
| WindowManager-Shell-16.jar | `m WindowManager-Shell` | android-16.0.0_r2 |
| SystemUI-core-16.jar       | `m SystemUI-core`       | android-16.0.0_r2 |
| framework-15.jar           | `m framework`           |                   |
| framework-14.jar           | `m framework`           |                   |
| framework-13.jar           | `m framework`           |                   |
| framework-12l.jar          | `m framework`           |                   |
| framework-12.jar           | `m framework`           |                   |
| framework-11.jar           | `m framework`           |                   |
| framework-10.jar           | `m framework`           |                   |
