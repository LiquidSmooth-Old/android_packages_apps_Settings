/*
 * Copyright (C) 2013-2014 neighbors28 for Dokdo Project
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

package com.neighbors28.dokdo.smartfilter;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainService extends Service {

	LinearLayout mView;
	SharedMemory shared;
	
	public static int STATE;
	
	public static final int INACTIVE=0;
	public static final int ACTIVE=0;	
	
	static{
		STATE=INACTIVE;
	}
	
	@Override
	public IBinder onBind(Intent i) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		shared=new SharedMemory(this);
		mView = new LinearLayout(this);   
		mView.setBackgroundColor(shared.getColor());
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				0 | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
				PixelFormat.TRANSLUCENT);
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		wm.addView(mView, params);
	}
	
	@Override
	public void onDestroy() {			
		super.onDestroy();
		if(mView!=null){
			WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
			wm.removeView(mView);
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		mView.setBackgroundColor(shared.getColor());
		return super.onStartCommand(intent, flags, startId);
	}
}
