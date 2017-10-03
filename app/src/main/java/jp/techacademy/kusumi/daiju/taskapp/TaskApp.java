package jp.techacademy.kusumi.daiju.taskapp;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by USER on 2017/09/13.
 */

public class TaskApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
