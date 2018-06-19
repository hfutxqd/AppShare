package xyz.imxqd.appshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.glxn.qrgen.android.QRCode;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {

    ListView mAppList;
    ProgressBar mProgressBar;

    FileServer mServer;

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (isDestroyed() || isFinishing()) {
                return;
            }
            mProgressBar.setVisibility(View.GONE);
            mAppList.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        }
    };
    List<AppInfo> mAppInfoList = new ArrayList<>();
    AppListAdapter mAdapter = new AppListAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMembers();
        findViews();
        initViews();
        loadInfos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share_all) {
            String ip = Utils.getIPAddress(true);
            if (TextUtils.isEmpty(ip)) {
                Toast.makeText(this, R.string.can_not_get_ip, Toast.LENGTH_LONG).show();
                return true;
            }
            String downloadUrl = "http://" + ip + ":8088/";
            showQRCode(downloadUrl);
        } else if (item.getItemId() == R.id.action_about) {
            showAbout();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServer();
    }

    private void showAbout() {
        View v = getLayoutInflater().inflate(R.layout.about_layout, null);
        new AlertDialog.Builder(this)
                .setView(v)
                .setNeutralButton(R.string.rate, (dialog, which) -> {
                    launchMarket();
                })
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void loadInfos() {
        mProgressBar.setVisibility(View.VISIBLE);
        mAppList.setVisibility(View.GONE);
        new Thread(() -> {
            final PackageManager pm = getPackageManager();
            List<AppInfo> infos = new ArrayList<>();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo applicationInfo : packages) {
                AppInfo appInfo = new AppInfo();
                appInfo.name = applicationInfo.loadLabel(pm).toString();
                appInfo.icon = applicationInfo.loadIcon(pm);
                appInfo.packageName = applicationInfo.packageName;
                Intent intent = pm.getLaunchIntentForPackage(appInfo.packageName);
                if (intent != null) {
                    infos.add(appInfo);
                }
            }
            Comparator<AppInfo> cmp = (o1, o2) -> Collator.getInstance(Locale.getDefault()).compare(o1.name, o2.name);
            Collections.sort(infos, cmp);
            mAppInfoList = infos;
            mHandler.sendEmptyMessage(0);

        }).start();
    }
    private void initMembers() {
        mServer = new FileServer();
        startServer();
    }

    private void findViews() {
        mProgressBar = findViewById(R.id.app_loading);
        mAppList = findViewById(R.id.app_list);
    }

    private void initViews() {
        mAppList.setAdapter(mAdapter);
        mAppList.setFastScrollEnabled(true);
        mAppList.setOnItemClickListener((parent, view, position, id) -> {
            AppInfo info = mAppInfoList.get(position);
            showAppDownload(info);
        });
    }

    private void showAppDownload(AppInfo info) {

        String ip = Utils.getIPAddress(true);
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(this, R.string.can_not_get_ip, Toast.LENGTH_LONG).show();
            return;
        }
        String downloadUrl = "http://" + ip + ":8088/" + info.packageName + "/download";
        showQRCode(downloadUrl);
    }

    private void showQRCode(String url) {
        View qrcodeInfoLayout = getLayoutInflater().inflate(R.layout.download_info_layout, null);
        ImageView ivQrcode = qrcodeInfoLayout.findViewById(R.id.download_qrcode);
        TextView tvDUrl = qrcodeInfoLayout.findViewById(R.id.download_url);

        Bitmap bitmap = QRCode.from(url).withSize(500, 500).bitmap();
        ivQrcode.setImageBitmap(bitmap);
        tvDUrl.setText(url);
        new AlertDialog.Builder(this)
                .setView(qrcodeInfoLayout)
                .show();
    }

    private void startServer() {
        try {
            mServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopServer() {
        mServer.stop();
    }


    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }


    public class AppListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAppInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mAppInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_app, parent, false);
                convertView.setTag(new AppViewHolder(convertView));
            }
            AppViewHolder holder = (AppViewHolder) convertView.getTag();
            AppInfo info = mAppInfoList.get(position);
            holder.name.setText(info.name);
            holder.packageName.setText(info.packageName);
            holder.icon.setImageDrawable(info.icon);
            return convertView;
        }
    }

    public class AppViewHolder {

        ImageView icon;
        TextView name;
        TextView packageName;

        public AppViewHolder(View itemView) {
            icon = itemView.findViewById(R.id.app_icon);
            name = itemView.findViewById(R.id.app_name);
            packageName = itemView.findViewById(R.id.app_package);
        }
    }

    public static class AppInfo {
        public String name;
        public Drawable icon;
        public String packageName;
    }
}
