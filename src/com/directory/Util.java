package com.directory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class Util {

	private static String watchDir   = null;
	private static String inProcessDest = null;
	private static String processedDest = null;
	private static int timeDelay = 0;
	public static volatile int totRecords = 0;
	private static String ignoreFiles = "tst-cnnct";
	
	public static int getTotRecords() {
		return totRecords;
	}

	public static void setTotRecords(int records) {
		Util.totRecords = totRecords + records;
	}

	public static String getIgnoreFiles() {
		return ignoreFiles;
	}

	public static void setIgnoreFiles(String ignoreFiles) {
		Util.ignoreFiles = ignoreFiles;
	}

	public static void setupEnv(String watch, String inprocess, String processed,  int delay)
	{
		inProcessDest = inprocess;
		processedDest = processed;
		watchDir = watch;
		timeDelay = delay;
		
	}

	public static String getWatchDir() {
		return watchDir;
	}

	public static void setWatchDir(String watchDir) {
		Util.watchDir = watchDir;
	}

	public static String getInProcessDest() {
		return inProcessDest;
	}

	public static void setInProcessDest(String inProcessDest) {
		Util.inProcessDest = inProcessDest;
	}

	public static String getProcessedDest() {
		return processedDest;
	}

	public static void setProcessedDest(String processedDest) {
		Util.processedDest = processedDest;
	}

	public static int getTimeDelay() {
		return timeDelay;
	}

	public static void setTimeDelay(int timeDelay) {
		Util.timeDelay = timeDelay;
	}

	public static String extractFileName(String filePathName) {
		if (filePathName == null)
			return null;

		int slashPos = filePathName.lastIndexOf('\\');

		if (slashPos == -1)
			return filePathName;

		return filePathName.substring(slashPos + 1, filePathName.length());
	}

	public static void main(String[] args) throws Exception {
		
		
		File theDirectory = new File(System.getenv("41056_WATCH_DIR"));

		if (theDirectory != null && !theDirectory.isDirectory()) {

			// This is bad, so let the caller know
			String message = "The path does not represent a valid directory.";
			throw new IllegalArgumentException(message);

		}
		File fr = new File("C:\\Watch\\test.txt");

		File newloc = moveFiletoNewDestination(fr, inProcessDest);

		BufferedReader fs = new BufferedReader(new FileReader(newloc));

		String c;
		String[] tokens = null;
		while ((c = fs.readLine()) != null) {

			tokens = c.split(",");

			// BufferedReader fs = new BufferedReader(new FileReader(fr));

			String file = extractFileName(tokens[0]);
			String newFile = tokens[1] + "\\" + file;

			File targetFile = new File(newFile);
			if (targetFile.exists()) {
				System.out.println("File" + newFile + " already exists");
				continue;

			}
			InputStream istream = new FileInputStream(new File(tokens[0]));

			ReadableByteChannel source = Channels.newChannel(istream);
			WritableByteChannel channel = new FileOutputStream(newFile)
					.getChannel();

			ByteBuffer buffer = ByteBuffer.allocateDirect(102400);

			while (source.read(buffer) != -1) {
				buffer.flip();
				while (buffer.hasRemaining()) {
					channel.write(buffer);
				}
				buffer.clear();
			}

			source.close();
			channel.close();

			// move to processed Destination
		}
		File processedloc = Util
				.moveFiletoNewDestination(newloc, processedDest);

		if (processedloc == null) {
			throw new Exception("error moving processed file");
		}

	}

	public static File moveFiletoNewDestination(File afile, String dest)
			throws Exception {

		System.out.println("Moving file" + afile.getName() + "to destination "
				+ dest);

		File bfile = new File(dest + "\\" + afile.getName());

		FileReader fr = new FileReader(afile);

		BufferedReader br = new BufferedReader(fr);
		StringBuilder contents = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			contents.append(line);
			contents.append(System.getProperty("line.separator"));
		}

		// use buffering
		Writer output = new BufferedWriter(new FileWriter(bfile));

		// FileWriter always assumes default encoding is OK!
		output.write(contents.toString());

		br.close();
		output.close();

		// delete the original file
		afile.delete();

		System.out
				.println("File moved successfully and deleted from source folder!");

		return bfile;

	}
	
	
	
	
}
