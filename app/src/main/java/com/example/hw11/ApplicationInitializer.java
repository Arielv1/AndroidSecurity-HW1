package com.example.hw11;

import android.app.Application;

public class ApplicationInitializer extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Toaster.init(this);
    }
}
