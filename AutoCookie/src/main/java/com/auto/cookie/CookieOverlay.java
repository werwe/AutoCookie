package com.auto.cookie;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;

/**
 * Created by werwe on 13. 10. 22..
 */
public class CookieOverlay {

    public static final String AUTO_COOKIE = "AUTO_COOKIE";
    View rootView;
    private final WindowManager wm;
    public static TextView countView;
    Bus bus = BusProvider.getInstance();
    public CookieOverlay(Context context)
    {
    //    bus.register(this);
        rootView = CreateLayout(context);

        wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams overlayParams = new WindowManager.LayoutParams();
        overlayParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        overlayParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        overlayParams.alpha = 60;
        overlayParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
               // | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        overlayParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        overlayParams.format = PixelFormat.TRANSLUCENT;
        overlayParams.gravity = Gravity.LEFT | Gravity.TOP;
        wm.addView(rootView, overlayParams);
        Log.d(AUTO_COOKIE, "Cookie overlay");
    }

    private View CreateLayout(final Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate( R.layout.overlay_layout, null);
        //v.setOnTouchListener(outsideTouchListener);
        int cnt = AutoCookieService.pref.getRepeatCount();
        countView = (TextView) v.findViewById(R.id.RepeatCountText);
        countView.setText(""+cnt);
        final Button btnLoop = (Button) v.findViewById(R.id.LoopStart);
        btnLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bus.post(produceLoopStartBtnEvent(btnLoop));
                //context.stopService(new Intent(context,AutoCookieService.class ));
            }
        });
        Button btn = (Button) v.findViewById(R.id.SuspendAutoCookie);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bus.post(produceStopBtnEvent());
                //context.stopService(new Intent(context,AutoCookieService.class ));
            }
        });
        return v;
    }

    public void destroyView() {
        if(wm !=null)
            wm.removeView(rootView);
      //  bus.unregister(this);
    }

    private View.OnTouchListener outsideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.d("AUTO_COOKIE","action:"+motionEvent.getAction());
            return false;
        }
    };

    public static class LoopStartBtnEvent{
        public Button button;
        public LoopStartBtnEvent(Button btn)
        {
            button = btn;
        }
    }
    public static class StopAutoBtnEvent{}

    @Produce
    public StopAutoBtnEvent produceStopBtnEvent(){return new StopAutoBtnEvent(); }

    @Produce
    public LoopStartBtnEvent produceLoopStartBtnEvent(Button btn){ return new LoopStartBtnEvent(btn); }
}
