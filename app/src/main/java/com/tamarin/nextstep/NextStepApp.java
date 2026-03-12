package com.tamarin.nextstep;

import android.app.Application;

public class NextStepApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(this);
    }
}