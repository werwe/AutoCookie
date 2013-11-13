package com.auto.cookie;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.List;

/**
 * Created by werwe on 13. 10. 21..
 */
public class AutoCookieProvider extends AppWidgetProvider {

    public static final String AUTO_COOKIE = "AUTO_COOKIE";

    public enum COOKIES  {
        ZOMBIE
    };

    public static final int BTN_START_CODE = 101;
    public static final int BTN_COOKIE_SELECT = 102;
    public static final int BTN_COOKIE_SETTING = 103;

    public static final String AUTO_TOGGLE_ACTION = "com.auto.cookie.AUTO_TOGGLE_ACTION";
    public static final String AUTO_SELECT_COOKIE_ACTION = "com.auto.cookie.SELECT_COOKIE_ACTION";
    public static final String AUTO_COOKIE_SETTING_ACTION = "com.auto.cookie.SETTING_ACTION";



    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(AUTO_COOKIE,"onUpdate");
        RemoteViews remoteView = CreateRemoteView(context);
        appWidgetManager.updateAppWidget(appWidgetIds,remoteView);
    }

    private RemoteViews CreateRemoteView(Context context) {

        RemoteViews views = new RemoteViews("com.auto.cookie",R.layout.widget_auto_cookie);
        SharedPreferences pref = context.getSharedPreferences("com.auto.cookie", Context.MODE_PRIVATE);

        SetAutoStartButton(context,pref, views);
        SetCookieSelectButton(context, pref, views);
        SetCookieSettingButton(context,pref,views);

        return views;
    }

    private void SetCookieSettingButton(Context context, SharedPreferences pref, RemoteViews views) {
        Intent settingIntent = new Intent(AUTO_COOKIE_SETTING_ACTION);
        views.setOnClickPendingIntent(R.id.cookie_settings, PendingIntent.getBroadcast(context, BTN_COOKIE_SETTING, settingIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private void SetCookieSelectButton(Context context, SharedPreferences pref, RemoteViews views) {
        String cookie = pref.getString("SELECTED_COOKIE",null);
        views.setTextViewText(R.id.cookie_select, cookie);

        Intent cookieSelectIntent = new Intent(AUTO_SELECT_COOKIE_ACTION);
        views.setOnClickPendingIntent(
                R.id.cookie_select,
                PendingIntent.getBroadcast(context, BTN_COOKIE_SETTING, cookieSelectIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        );

    }

    private void SetAutoStartButton(Context context, SharedPreferences pref, RemoteViews views) {
        Log.d(AUTO_COOKIE,"SetAutoStartButton");
        boolean start = pref.getBoolean("AUTO_START", false);
        if(start)
            views.setTextViewText(R.id.auto_start,"OFF");
        else
            views.setTextViewText(R.id.auto_start,"START");

        views.setOnClickPendingIntent(
                R.id.auto_start,
                PendingIntent.getBroadcast(context,0,new Intent(AUTO_TOGGLE_ACTION),PendingIntent.FLAG_UPDATE_CURRENT)
        );
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        try {
            Process root = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(AUTO_COOKIE,"onReceive");
        Log.d(AUTO_COOKIE,intent.getAction());

        if(intent == null)
            return;

        String action = intent.getAction();
        if(action == AUTO_TOGGLE_ACTION)
        {
            //Log.d(AUTO_COOKIE, "check toogle pass");
            //if(checkRunningServices(context,AutoCookieService.class))
            //{
               // Log.d(AUTO_COOKIE,"check services pass");
                StartAutoService(context);
            //}
        }
        else if(action == AUTO_SELECT_COOKIE_ACTION)
        {

        }
        else if(action == AUTO_COOKIE_SETTING_ACTION)
        {

        }
    }

    private void StopAutoService(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getRunningServices(100);
        context.stopService(new Intent(context, AutoCookieService.class));
    }

    private void StartAutoService(Context context) {
        context.startService(new Intent(context,AutoCookieService.class));
    }

    private boolean checkRunningServices(Context context,Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(100);
        for(ActivityManager.RunningServiceInfo info : services)
        {
            Log.d(AUTO_COOKIE,info.clientPackage);
            Log.d(AUTO_COOKIE,info.service.getClassName());
            if (serviceClass.getName().equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }




}