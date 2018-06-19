package xyz.imxqd.appshare;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

import java.util.List;

public class PackageUtil {
    public static String getAppName(String packageName) throws PackageManager.NameNotFoundException {
        ApplicationInfo info = App.get().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        return App.get().getPackageManager().getApplicationLabel(info).toString();
    }

    public static String getPath(String packageName) throws PackageManager.NameNotFoundException {
        ApplicationInfo info = App.get().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        return info.sourceDir;
    }

    public static List<PackageInfo> getInstalledPackages() {
        return App.get().getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
    }

    public static int getPackageVersionCode(String packageName) {
        PackageInfo info = null;
        try {
            info = App.get().getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public static String getPackageVersionName(String packageName) {
        PackageInfo info = null;
        try {
            info = App.get().getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }




    private static final String SCHEME = "package";

    public static void showInstalledAppDetails(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts(SCHEME, packageName, null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.get().startActivity(intent);
    }

}
