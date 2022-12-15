package com.directory;

import java.util.HashMap;
import java.util.Map;

public class DirectorySnapshot {

	private static Map<String, String> files = new HashMap<String, String>();

	public static void addFile(String fileName) {
		files.put(fileName, fileName);
	}

	public static void removeFile(String fileName) {
		files.remove(fileName);
	}

	public static boolean containsFile(String fileName) {
		return files.containsKey(fileName);
	}
}
