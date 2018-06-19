package xyz.imxqd.appshare;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import net.freeutils.httpserver.HTTPServer;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileServer extends HTTPServer {
    private static final String TAG = "FileServer";
    private static final String REGEX_PACKAGE = "^/(([a-zA-Z0-9_]+\\.{1})+[a-zA-Z0-9_]+)";
    private static final Pattern PACKAGE_PATTERN = Pattern.compile(REGEX_PACKAGE);

    public FileServer() {
        super(8088);
        setSocketTimeout(15000);
        setExecutor(Executors.newCachedThreadPool());
        HTTPServer.VirtualHost host = getVirtualHost(null); // default host
        host.setAllowGeneratedIndex(true); // with directory index pages
    }

    @Override
    protected void serve(Request req, Response resp) throws IOException {
        String reqPath = req.getPath();
        Log.d(TAG, "method : " + req.getMethod());
        Log.d(TAG, "path : " + req.getPath());
        Matcher matcher = PACKAGE_PATTERN.matcher(reqPath);
        if (matcher.find()) {
            try {
                String packageName = matcher.group(1);
                String filePath = PackageUtil.getPath(packageName);
                StringBuilder str = new StringBuilder();
                str.append(String.format("<a href=\"download\">%s</a><br />", App.get().getString(R.string.download)));
                str.append(PackageUtil.getAppName(packageName)).append("\t<br />");
                str.append(App.get().getString(R.string.app_info));
                str.append("<lu>");
                PackageInfo info = App.get().getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
                str.append("<li>");
                str.append(App.get().getString(R.string.package_name) + info.packageName);
                str.append("</li>");
                str.append("<li>");
                str.append(App.get().getString(R.string.version_name) + info.versionName);
                str.append("</li>");
                str.append("<li>");
                str.append(App.get().getString(R.string.version_code) + info.versionCode);
                str.append("</li>");
                str.append("<li>");
                str.append(App.get().getString(R.string.first_install_time) + DateFormat.getInstance().format(new Date(info.firstInstallTime)));
                str.append("</li>");
                str.append("<li>");
                str.append(App.get().getString(R.string.last_update_time) + DateFormat.getInstance().format(new Date(info.lastUpdateTime)));
                str.append("</li>");
                str.append("</lu>");
                if (reqPath.endsWith("/download")) {
                    resp.getHeaders().add("content-disposition", "attachment; filename=\"" + packageName + ".apk\"");
                    serveFileContent(new File(filePath), req, resp);
                    return;
                } else {
                    resp.send(200, str.toString());
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("/".equals(reqPath) || TextUtils.isEmpty(reqPath)) {
            List<PackageInfo> packageInfos = PackageUtil.getInstalledPackages();
            StringBuilder str = new StringBuilder();
            str.append("<lu>");
            for (PackageInfo info : packageInfos) {
                str.append("<li>");
                try {
                    str.append(PackageUtil.getAppName(info.packageName) + "\t" + info.versionName + "\t");
                    str.append(String.format("<a href=\"%s/download\">%s</a>", info.packageName, App.get().getString(R.string.download)));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                str.append("</li>");
            }
            str.append("</lu>");
            resp.send(200, str.toString());
        } else {
            super.serve(req, resp);
        }
    }
}
