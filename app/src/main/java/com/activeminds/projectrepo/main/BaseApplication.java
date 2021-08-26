package com.activeminds.projectrepo.main;


import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.sendbird.android.SendBird;
import com.activeminds.projectrepo.fcm.MyFirebaseMessagingService;
import com.activeminds.projectrepo.utils.PreferenceUtils;
import com.activeminds.projectrepo.utils.PushUtils;

public class BaseApplication extends Application {

    private static final String APP_ID = "1DB46B08-3C62-4461-8A61-B3CF87E18251"; // Location: US Oregon

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestore.setLoggingEnabled(true);
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        PreferenceUtils.init(getApplicationContext());

        SendBird.init(APP_ID, getApplicationContext());

        PushUtils.registerPushHandler(new MyFirebaseMessagingService());
    }
}
