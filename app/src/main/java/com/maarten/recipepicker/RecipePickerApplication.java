package com.maarten.recipepicker;

import android.app.Application;
import android.content.Context;

/**
 * The function of this class is to get the context of the application which you can use in static methods
 */
public class RecipePickerApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        RecipePickerApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return RecipePickerApplication.context;
    }
}
