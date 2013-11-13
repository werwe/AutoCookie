package com.auto.cookie;

import android.content.SharedPreferences;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;

import org.apache.http.cookie.Cookie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by werwe on 13. 10. 27.
 */

//60frame per seconds 16.6666f miliseconds;
    //
public class TimeLine {

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            Log.d("AUTO_COOKIE","Event Handled:"+msg.what);
            switch (msg.what)
            {
                case 1: //restart time line;
                    play();
                    break;
            }
        }
    };

    long startTime;
    long currentTime;
    long elapsedTime;

    boolean endOfTimeLine = true;

    boolean loop = true;

    List<TimeLineEvent> eventList;
    Thread timeThread;
    private int currentPosition = 0;

    int loopCnt = 0;

    public void TimeLine()
    {
        eventList = new ArrayList<TimeLineEvent>();
    }

    public TimeLine(List<TimeLineEvent> events)
    {
        eventList = events;
    }

    public void setTimeLineEvents(List<TimeLineEvent> events)
    {
        eventList = events;
    }

    public void play()
    {
       timeThread = new Thread(runTime);
       endOfTimeLine = false;
       currentPosition = 0;
       timeThread.start();
       loopCnt++;

        AutoCookieService.pref.setRepeatCount(loopCnt);
       if(CookieOverlay.countView != null)
        CookieOverlay.countView.setText(""+loopCnt);
    }

    public Runnable runTime = new Runnable() {
        @Override
        public void run() {

            startTime = System.currentTimeMillis();

            while (!endOfTimeLine)
            {
                currentTime = System.currentTimeMillis();
                elapsedTime = currentTime - startTime;
                try {
                    Thread.sleep(16);
                    processEvent();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            if(loop)
            {
               endOfTimeLine = true;
               handler.sendEmptyMessageDelayed(1,3000);
            }
        }
    };

    public synchronized void Stop()
    {
        endOfTimeLine = true;
        loop = false;
        loopCnt = 0;
        if(CookieOverlay.countView != null)
            CookieOverlay.countView.setText("0");
    }

    TimeLineEvent pickedEvent;
    private void processEvent() {
        if(pickedEvent == null)
            pickedEvent = getEvent();
        if(pickedEvent == null)
        {
            endOfTimeLine = true;
            return;
        }
        if(elapsedTime >= pickedEvent.eventTime)
        {
            try {
                RootTools.getShell(true).add(pickedEvent.command);
                pickedEvent = getEvent();
            } catch (IOException e) {
                Log.d("AUTO_COOKIE","exception",e);
            } catch (TimeoutException e) {
                Log.d("AUTO_COOKIE","exception",e);
            } catch (RootDeniedException e) {
                Log.d("AUTO_COOKIE","exception",e);
            }

        }
    }

    private TimeLineEvent getEvent() {
        TimeLineEvent event = null;
        if(eventList.size() == 0)
        {
            return null;
        }
        else
        {
            if(currentPosition >= eventList.size())
                return null;

            event = eventList.get(currentPosition);
            currentPosition++;
        }
        return event;
    }

}
