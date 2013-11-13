package com.auto.cookie;

import android.app.Instrumentation;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;

import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;

/**
 * Created by werwe on 2013. 10. 29..
 */
public class TimeLineEvent {
    long eventTime;
    CommandCapture command;

    public TimeLineEvent(long eventTime,CommandCapture command)
    {
        this.eventTime = eventTime;
        this.command = command;
    }
}
