package xyz.imxqd.appshare;

import android.app.Application;

public class App extends Application {

    private static App mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
    }

    public static App get() {
        return mApp;
    }
}
