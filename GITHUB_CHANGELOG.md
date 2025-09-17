Lawnchair 16 pE Development 1 is here! Contributors are encouraged to target this branch instead of 
older (i.e., Lawnchair `15-dev`).

### 🏗️ Development 2 (Draft)

#### Features

* Enable All Apps Blur Flags on Phone (oops, forgot about the allAppsSheetForHandheld flag)
* Make Safe Mode check more reliable
* Smartspace Battery now reports battery charging status of Fast (more than 90% of 20 W) and Slow (less than 90% of 5 W) charging
* ~~Enabling GSF flags (Testing)~~ _To be rework_
* Show pseudonym version to Settings
* Resizing workspace should calculate items position more accurately
* Link Popup's Arrow Theme to Lawnchair
* Update Lawnchair default grid size to 4×7 (or 4×6 with smartspace widget)
* Reimplement Hotseat background customisation
* Make haptic on a locked workspace use MSDL vibration
* Make Launcher3 colour more accurate to upstream Android 16
* ProvideComposeSheetHandler now have expressive blur
* Lawnchair Settings now uses Material 3 Expressive

#### Fixes

* Fix unable to access preview for icon style
* Widget should open normally after a workaround (C7evQZDJ)
* Fix (1) Search bar and Dock, (2) Folders and App Drawer settings didn't open due to init problems
* Lawnchair should hopefully remember what grid they should be using
* Most if not all of Lawnchair settings should be usable without crashes
* Correct Baseline Profile from old `market` to `play` variant, and now should calculate profile for `nightly`
* Fix Private Space crash when Lawnchair is set as Launcher due to flags only available on A16
* Fix crash on a device with strict export receiver requirements on A14
* Interactable widget crashing due to App Transition Manager being null (C7evQZDJ)

#### Known Bugs
* Preview can't show device wallpaper -> (lIxkAYGg)
* IDP Preview doesn't refresh on settings change -> workaround is to hit apply and re-open the preview -> (ZbLX3438)
* Workspace theme doesn't refresh until restart -> (ZbLX3438)
* Lawnchair Colour can't handle restart causing default colour to be used instead -> Fixed?
* (Investigating) Work profile switch on widget selector *may* have reverted to Lawnchair 15 style
* Full lists: https://trello.com/b/8IdvO81K/pe-lawnchair

### 🥞 Development 1

First development milestone! Basic launcher functionality should be stable enough.

* Make Lawnchair Launcher launchable in Android 12.1, 13, 14, 15, 16
* Remove two deprecated features (Use Material U Popup, and Use dot pagination)
* Add pseudonym version in debug settings
* Adapt Lawnchair code to Launcher3 16
* Make basic features of Launcher work (App Drawer, Home Screen, Search, Folders, Widgets)
* Enable Material Expressive Flags (Try swiping through launcher page)
* Enable All Apps Blur Flags (Try opening All Apps on supported devices)
* Enable MSDL Haptics Feedback Flags (Try gliding widget or icons across the homescreen)
* Make Predictive Back Gesture work on Android 13, 14, 15, 16 (Try swiping left or right on gesture-based navigational)
* Programmatically set Safe Mode status

#### Known Bugs

* App Icon may sometimes render with less than 0 in height/width causing blank icon to be rendered and crashing ISE on customising icons -> (31lLEflf)
* Any Lawnchair settings using IDP will crash the launcher -> Fixed in Lawnchair 16 pE Development 2
* Icon pack isn't usable -> (DXo69Qzd)
* Dynamic icons will not be themed by launcher
* Full lists: https://trello.com/b/8IdvO81K/pe-lawnchair

### Snapshot 6 

This is a developer-focused change log:

This snapshot marks the first time Lawnchair 16 is able to compile and build an APK!

* Fix all issues with Java files in both `lawn` and `src`
* Make Lawnchair compilable (with instant crash)
* Move to KSP for Dagger code generation

### Snapshot 5

This is a developer-focused change log:

This snapshot now able to compile all sources (Kotlin files only)

* Fix MORE MORE MORE `lawn` issues
* Use Gradle Version Catalog for consistent dependency version across all modules (Full implementation @ LawnchairLauncher/Lawnchair#5753)
* Magically fix ASM Instrumentation issues (I didn't do anything, it just works now)
* Fix ALL the issues in kotlin stage (`compileLawnWithQuickstepNightlyDebugKotlin`)
* Reintroduce some features from Lawnchair
* Add compatibility checks and workarounds for them
* Fix most issues with Java files in both `lawn` and `src`

### Snapshot 4

This is a developer-focused change log:

This snapshot marks the first time Lawnchair 16 is able to compile all Launcher3 sources!

* Add `MSDLLib` to `platform_frameworks_libs_systemui` 
* Add `contextualeducationlib` to `platform_frameworks_libs_systemui`
* Fix issues in both `lawn` and `src` modules
* Fix AIDL sources
* Resolve Lawnchair/LC-TODO lists
* Merge `wmshell.shared` res with res from `wmshell`
* Consistent build reproducibility by specifying dependencies in `build.gradle`
* Some ASM Instrumentation issues (and re-add some…)
* Update documentations

### Snapshot 3

This is a developer-focused change log:

Not a lot of errors left to go!

* Finish correctly implementing all Dagger functions (?)
* Merge Lawnchair 15 Beta 1 into Bubble Tea
  * Support for 16-kb page size devices
* Repository rebased and dropped commit
  * Switch back from turbine-combined variant to javac variant for prebuilt SystemUI-core-16 because issues with LFS
    * MORE MORE fixes regarding turbine-combined to javac
* Publish `platform_frameworks_libs_systemui` to pe 16-dev branch
* ATLEAST check to almost every launcher3 source file
* `Utils` module (stripped)
* Fix Dagger duplicated classes (because of Dagger dependency ksp/kapt mixing)
* Build reproducibility improvements by specifying dependencies in `build.gradle` files
* Fix some of the issues in both `lawn` and `src` modules

### Snapshot 2

This is a developer-focused change log:

This snapshot milestone marked the first time Lawnchair now able to compile all supplementary 
modules, `src` + `lawn` will be in Snapshot 5 or Development 1 milestone.

* Merge flags
* Fix some issues with launcher3 sources.
* A temporary workaround with framworks.jar not adding in anim module.
* Shared not having access to animationlib.
* **Switch from javac variant to turbine-combined variant for prebuilt SystemUI-core-16**.

### From Initial snapshot 0 and 1

This is a developer-focused change log:
* Prebuilt updated to Android 16-0.0_r2 (Android 16.0.0 Release 2)
* Submodule have also been refreshed to A16r2
* Baklava Compatlib (QuickSwitch compatibility not guaranteed)
* Refreshed internal documentation like prebuilt, systemUI
