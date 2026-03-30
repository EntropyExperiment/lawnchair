package app.lawnchair.ui.preferences.navigation

const val URI = "lawnchair://settings"

enum class PreferenceDeepLink(val basePath: String) {
    General("$URI/general"),
    GeneralIconPack("$URI/iconpack"),
    GeneralCustomIconShapeCreator("$URI/general-icon-shape-creator"),
    HomeScreen("$URI/home-screen"),
    HomeScreenGrid("$URI/home-screen-grid"),
    HomeScreenPopupEditor("$URI/home-screen-popup-editor"),
    Dock("$URI/dock"),
    DockSearchProvider("$URI/dock-search-provider"),
    Smartspace("$URI/smartspace"),
    AppDrawer("$URI/app-drawer"),
    AppDrawerHiddenApps("$URI/app-drawer-hidden-apps"),
    AppDrawerFolder("$URI/app-drawer-folder"),
    Search("$URI/search"),
    SearchProviderPreference("$URI/search-provider"),
    Folders("$URI/folders"),
    Gestures("$URI/gestures"),
    Quickstep("$URI/quickstep"),
    BackupAndRestore("$URI/backup-restore"),
    About("$URI/about"),
    AboutLicenses("$URI/about-licenses"),
    ExperimentalFeatures("$URI/experimental-features"),
    CreateBackup("$URI/create-backup"),
}
