/*
 * Copyright (C) 2013 hathibelagal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.liquid.smartfilter;

import com.android.settings.R;
import android.os.Bundle;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
* This class has the controls to set the color of the filter
* @author Hathibelagal
*/
public class MainActivity extends Activity implements OnSeekBarChangeListener {

    SharedMemory shared;

    SeekBar alphaSeek;
    SeekBar redSeek;
    SeekBar greenSeek;
    SeekBar blueSeek;

    int alpha,red,green,blue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }

    private void initialize(){

        stopServiceIfActive();

        shared=new SharedMemory(this);
        alphaSeek=(SeekBar)findViewById(R.id.alphaControl);
        redSeek=(SeekBar)findViewById(R.id.redControl);
        greenSeek=(SeekBar)findViewById(R.id.greenControl);
        blueSeek=(SeekBar)findViewById(R.id.blueControl);

        alphaSeek.setOnSeekBarChangeListener(this);
        redSeek.setOnSeekBarChangeListener(this);
        greenSeek.setOnSeekBarChangeListener(this);
        blueSeek.setOnSeekBarChangeListener(this);

        alpha=shared.getAlpha();
        red=shared.getRed();
        green=shared.getGreen();
        blue=shared.getBlue();

        alphaSeek.setProgress(alpha);
        redSeek.setProgress(red);
        greenSeek.setProgress(green);
        blueSeek.setProgress(blue);

        updateColor();
    }

    private void stopServiceIfActive(){
        if(MainService.STATE == MainService.ACTIVE){
            Intent i=new Intent(MainActivity.this, MainService.class);
            stopService(i);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        if(seekBar==alphaSeek){
            alpha=seekBar.getProgress();
        }
        if(seekBar==redSeek){
            red=seekBar.getProgress();
        }
        if(seekBar==greenSeek){
            green=seekBar.getProgress();
        }
        if(seekBar==blueSeek){
            blue=seekBar.getProgress();
        }
        updateColor();
    }

    private void updateColor(){
        int color=SharedMemory.getColor(alpha, red, green, blue);
        ColorDrawable cd=new ColorDrawable(color);
        getWindow().setBackgroundDrawable(cd);
    }

    @Override
    public void onStartTrackingTouch(SeekBar sb) {}

    @Override
    public void onStopTrackingTouch(SeekBar sb) {}

    public void cancelClick(View v){
        finish();
    }

    public void applyClick(View v){
        shared.setAlpha(alpha);
        shared.setRed(red);
        shared.setGreen(green);
        shared.setBlue(blue);

        Intent i=new Intent(MainActivity.this, MainService.class);
        startService(i);

        makeNotification();
        finish();
    }

    private void makeNotification(){
        NotificationCompat.Builder nb=new NotificationCompat.Builder(this);
        nb.setSmallIcon(R.drawable.ic_launcher);
        nb.setContentTitle(getText(R.string.notification_title));
        nb.setContentText(getText(R.string.notification_message));
        nb.setAutoCancel(true);
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
        nb.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0x355, nb.build());
    }
}
