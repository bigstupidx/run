package com.run;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/*
 * 用来管理Activity
 */
public class ActivityCollector {

	public static List<Activity> activitycollector = new ArrayList<Activity>();
	
	public static void addActivity(Activity activity) {
		activitycollector.add(activity);
	}
	
	public static void removeActivity(Activity activity) {
		activitycollector.remove(activity);
	}
	
	public static void finishAll() {
		for (Activity activity : activitycollector) {
			if(!activity.isFinishing()) {
				activity.finish();
			}
		}
	}
}
