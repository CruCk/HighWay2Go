package app.com.example.cruck.h2g;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by cruck on 01-06-2016.
 */
public class ToDoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
    }

}