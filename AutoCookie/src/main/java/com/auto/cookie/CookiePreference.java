package com.auto.cookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import java.text.DateFormat;

/**
 * Created by werwe on 2013. 11. 7..
 */
public class CookiePreference {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public CookiePreference(Context context)
    {
        pref = context.getSharedPreferences("cookie.pref",Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setRepeatCount(int count)
    {
        editor.putInt("reapeat",count);
        editor.commit();
    }

    public int getRepeatCount()
    {
        return pref.getInt("reapeat",0);
    }

    public void setElapsedTime(long time)
    {
        String elapsedTime = DateUtils.formatElapsedTime(time);
        editor.putString("elapsedTime",elapsedTime);
    }

    public String getElapsedTime()
    {
        return pref.getString("elapsedTime",null);
    }
}
