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
import android.content.Context;
import android.content.SharedPreferences;

/**
* This class is used to store preferences and acts as shared memory
* between the activity and the service
* @author Hathibelagal
*/
public class SharedMemory {
    SharedPreferences prefs;

    SharedMemory(Context ctx){
        prefs=ctx.getSharedPreferences("SCREEN_SETTINGS", Context.MODE_PRIVATE);
    }

    void setAlpha(int value){
        prefs.edit().putInt("alpha", value).commit();
    }

    void setRed(int value){
        prefs.edit().putInt("red", value).commit();
    }

    void setGreen(int value){
        prefs.edit().putInt("green", value).commit();
    }

    void setBlue(int value){
        prefs.edit().putInt("blue", value).commit();
    }

    int getBlue(){
        return prefs.getInt("blue", 0x00);
    }

    int getGreen(){
        return prefs.getInt("green", 0x00);
    }

    int getRed(){
        return prefs.getInt("red", 0x00);
    }

    int getAlpha(){
        return prefs.getInt("alpha", 0x33);
    }

    public static int getColor(int alpha, int red, int green, int blue){
        String hex = String.format("%02x%02x%02x%02x", alpha, red, green, blue);
        int color=(int)Long.parseLong(hex,16);
        return color;
    }

    public int getColor(){
        String hex = String.format("%02x%02x%02x%02x", getAlpha(), getRed(), getGreen(), getBlue());
        int color=(int)Long.parseLong(hex,16);
        return color;
    }
}
