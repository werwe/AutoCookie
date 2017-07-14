package com.auto.cookie;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.stericson.RootTools.execution.CommandCapture;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by werwe on 13. 10. 22..
 */
public class AutoCookieService extends Service {

    CookieOverlay overlay;
    TimeLine timeLine;
    public static final String AUTO_COOKIE = "AUTO_COOKIE";


    Bus bus = BusProvider.getInstance();
    Handler handler = new Handler();

    public static CookiePreference pref;
    PowerManager.WakeLock lock;

    @Override
    public void onCreate() {

        Log.d(AUTO_COOKIE, "onCreate service");
        pref = new CookiePreference(getApplicationContext());
        bus.register(this);
        StartAuto();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
            Log.d("AUTO_COOKIE", "start Foreground is exist");
            return;
        } catch (NoSuchMethodException e) {
            mStartForeground = mStopForeground = null;
        }
        try {
            mSetForeground = getClass().getMethod("setForeground", mSetForegroundSignature);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
        }
    }

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.d("AUTO_COOKIE", "service restart");
        }
        Notification notification;
        Notification.Builder notiBuilder = new Notification.Builder(getApplicationContext());
        notiBuilder.setContentTitle("오토 쿠키");
        notiBuilder.setContentText("오토 쿠키가 실행중입니다.");
        notification = notiBuilder.build();


        // The PendingIntent to launch our activity if the user selects this notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, LocalServiceActivities.Controller.class), 0);

        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
//                text, contentIntent);

        startForegroundCompat(10101010, notification);
        return START_STICKY;
    }

    private void SetUpWakeLock() {
        Context context = getApplicationContext();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        lock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "AUTO_COOKIE_WAKE_LOCK");
        lock.acquire();
    }

    private void StopWakeLock() {
        if (lock != null) {
            lock.release();
            lock = null;
        }
    }

    private void StartAuto() {
        overlay = new CookieOverlay(getApplicationContext());
//        LaunchCookieRun(getApplicationContext());
//        //item 선택
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                StartCookieTimeLine();
//            }
//        },20000);
    }

    boolean loopStarted = false;

    @Subscribe
    public void RecieveLoppStartEvent(CookieOverlay.LoopStartBtnEvent event) {
        Log.d("AUTO_COOKIE", "Recieve Loop Start Event");
        if (!loopStarted) {
            SetUpWakeLock();
            StartCookieTimeLine();
            event.button.setText("Loop Stop");
            loopStarted = true;
            pref.setRepeatCount(0);

        } else {
            StopWakeLock();
            StopCookieTimeLine();
            event.button.setText("Loop Start");
            loopStarted = false;
        }

    }

    @Subscribe
    public void RecieveStopAutoCookieEvent(CookieOverlay.StopAutoBtnEvent event) {
        Log.d("AUTO_COOKIE", "Recieve Stop Auto Cookie Event");
        stopSelf();
    }

    @SuppressLint("NewApi")
    public void StartCookieTimeLine() {
        Display display = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;
        final int height = size.y;

        Log.d("test", "widthPixels=" + width);
        Log.d("test", "heightPixels=" + height);

        List<TimeLineEvent> lists = new ArrayList<TimeLineEvent>();
        //lists.add(new TimeLineEvent(10, TapCommand(width / 2, height / 3, width, height)));
        lists.add(new TimeLineEvent(50, sendEvent(width / 2, height / 3, width, height)));
        //lists.add(new TimeLineEvent(2000,TapCommand(863,603,width,height)));
        //lists.add(new TimeLineEvent(175000,TapCommand(412,644,width,height)));
        if (timeLine != null)
            timeLine.Stop();

        timeLine = new TimeLine(lists);
        timeLine.loop = true;
        timeLine.play();
    }

    private void StopCookieTimeLine() {
        timeLine.Stop();
    }

    private CommandCapture TapCommand(int x, int y) {
        Log.d("AUTO_COOKIE", "x:" + x + "/y:" + y);
        return new CommandCapture(0, "input tap " + x + " " + y);
    }

    private CommandCapture TapCommand(int x, int y, int screenW, int screenH) {
        //1280 X 720 base
        return TapCommand((int) (x / 1184f * screenW), (int) (y / 720f * screenH));
    }

    private static int tracking_id = 200;

    private CommandCapture sendEvent(int x, int y, int screenW, int screenH) {
        String DEVICE_NAME = "/dev/input/event1";
        String EV_ABS = "3";
        String EV_SYN = "0";
        MessageFormat mf = new MessageFormat(
                "sendevent {0} {1} {2} {3}" +
                "sendevent {0} {1} {2} {3}" +
                "sendevent {0} {1} {2} {3}" +
                "sendevent {0} {1} {2} {3}" +
                "sendevent {0} {1} {2} {3}"
        );


        String msg = "" +
                "sendevent /dev/input/event1 3 57 "+ (tracking_id++) +";" +
                "sendevent /dev/input/event1 3 53 570;" +
                "sendevent /dev/input/event1 3 54 500;" +
                "sendevent /dev/input/event1 3 58 54;" +
                "sendevent /dev/input/event1 0 0 0;" +
                "sendevent /dev/input/event1 3 57 4294967295;" +
                "sendevent /dev/input/event1 0 0 0;";


        return new CommandCapture(0, msg);
    }

    public IBinder onBind(Intent intent) {
        Log.d(AUTO_COOKIE, "ON BIND service");
        return null;
    }

    @Override
    public void onDestroy() {

        Log.d("AUTO_COOKIE", "Destroy Cookie");
        if (lock != null)
            lock.release();
        bus.unregister(this);
        if (timeLine != null)
            timeLine.Stop();
        overlay.destroyView();
        stopForegroundCompat(R.string.foreground_service_started);
    }

    private void LaunchCookieRun(Context context) {
        Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage("com.devsisters.CookieRunForKakao");

        LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(AUTO_COOKIE, LaunchIntent.getPackage());
        context.startActivity(LaunchIntent);
    }


    private static final Class<?>[] mSetForegroundSignature = new Class[]{
            boolean.class};
    private static final Class<?>[] mStartForegroundSignature = new Class[]{
            int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[]{
            boolean.class};

    private NotificationManager mNM;
    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];


    void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(this, args);
        } catch (InvocationTargetException e) {
            // Should not happen.
            Log.w("ApiDemos", "Unable to invoke method", e);
        } catch (IllegalAccessException e) {
            // Should not happen.
            Log.w("ApiDemos", "Unable to invoke method", e);
        }
    }

    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            invokeMethod(mStartForeground, mStartForegroundArgs);
            return;
        }

        // Fall back on the old API.
        mSetForegroundArgs[0] = Boolean.TRUE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
        mNM.notify(id, notification);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            invokeMethod(mStopForeground, mStopForegroundArgs);
            return;
        }

        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        mSetForegroundArgs[0] = Boolean.FALSE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }

}
