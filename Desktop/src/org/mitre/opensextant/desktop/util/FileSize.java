package org.mitre.opensextant.desktop.util;

import java.math.BigInteger;

import org.apache.commons.io.FileUtils;

public class FileSize {

	// this is originally from commons-io FileUtils v2.1. however, more
	// precision was desired per: http://jira.mitre.org/browse/OPENSEXTANT-273
	public static String byteCountToDisplaySize(long size) {
		String displaySize;

		if (size / FileUtils.ONE_GB > 0) {
			displaySize = String.format("%.4f", (size / (float)FileUtils.ONE_GB)) + " GB";
		} else if (size / FileUtils.ONE_MB > 0) {
			displaySize = String.format("%.3f", (size / (float)FileUtils.ONE_MB)) + " MB";
		} else if (size / FileUtils.ONE_KB > 0) {
			displaySize = String.format("%.2f", (size / (float)FileUtils.ONE_KB)) + " KB";
		} else {
			displaySize = String.valueOf(size) + " bytes";
		}
		return displaySize;
	}
}
