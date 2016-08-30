package com.example.Util;

public class TimeUtil {

	public static String timeFont(int time) {
		StringBuffer buffer = new StringBuffer();
		int min = time / 60;
		int secon = time % 60;

		if (min < 10) {
			buffer.append("0" + min + ":");
		} else {

			buffer.append(min + ":");
		}
		if (secon < 10) {
			buffer.append("0" + secon);
		} else {

			buffer.append(secon);
		}
		return buffer.toString();
	}
}
