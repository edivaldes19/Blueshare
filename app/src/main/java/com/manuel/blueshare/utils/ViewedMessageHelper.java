package com.manuel.blueshare.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import com.manuel.blueshare.providers.AuthProvider;
import com.manuel.blueshare.providers.UsersProvider;

import java.util.List;

public class ViewedMessageHelper {
    public static void updateOnline(boolean status, final Context context) {
        UsersProvider usersProvider = new UsersProvider();
        AuthProvider authProvider = new AuthProvider();
        if (authProvider.getUid() != null) {
            if (isApplicationSentToBackground(context)) {
                usersProvider.updateOnline(authProvider.getUid(), status);
            } else if (status) {
                usersProvider.updateOnline(authProvider.getUid(), true);
            }
        }
    }

    public static boolean isApplicationSentToBackground(final Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            return !topActivity.getPackageName().equals(context.getPackageName());
        }
        return false;
    }
}