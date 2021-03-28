package com.example.hw1;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class Toaster {

    private static Toaster toasterInstance;
    private static Context context;

    public static Toaster getInstance() {
        return toasterInstance;
    }

    private Toaster(Context context) {
        this.context = context;
    }

    public static Toaster init(Context context) {
        if (toasterInstance == null)
            toasterInstance = new Toaster(context);
        return toasterInstance;
    }

    public void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
