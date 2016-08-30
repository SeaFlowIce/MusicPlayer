package com.example.Util;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;

public class MyApplication extends Application {
	ArrayList<Activity> list = new ArrayList<Activity>();

	@Override
	public void onCreate() {
		// 初始化
		super.onCreate();
	}

	// 添加Activity
	public void addAcivity(Activity activity) {
		list.add(activity);
	}

	// 移除Activity
	public void removeAcivity(Activity activity) {
		list.remove(activity);
	}

	// 关闭所有启动的Activity
	public void closeAllAcivity() {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).finish();
		}
		list.clear();
	}

}
