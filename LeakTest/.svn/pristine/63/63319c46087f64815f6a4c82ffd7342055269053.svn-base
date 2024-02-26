package com.cnlaunch.physics.downloadbin.util;

import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.Tools;

public class MyFactory {
	private static final String TAG = MyFactory.class.getSimpleName();
	public static OrderMontage creatorForOrderMontage() {
		MLog.d(TAG, "creatorForOrderMontage enter.");
		if (Tools.isTruck() && !Tools.isCarAndHeavyduty()) {
			return OrderMontageForHD.getInstance();
		}
		else {
			return OrderMontageForCar.getInstance();
		}
	}
	public static Analysis creatorForAnalysis() {
		MLog.d(TAG, "creatorForAnalysis enter.");
		if (Tools.isTruck() && !Tools.isCarAndHeavyduty()) {
			return AnalysisForHD.getInstance();
		}
		else {
			return AnalysisForCar.getInstance();
		}
	}
	
}
