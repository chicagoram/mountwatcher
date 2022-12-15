package com.mlxod.mw.util;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.mlxod.mw.util.ApplicationPropertyDigester;
import com.mlxod.mw.util.Util;

public class Util {

	/** The Constant APPLICATION_PROPERTIES_FILE. */
	public static final String APPLICATION_PROPERTIES_FILE = "ApplicationProperties.xml";
	/** The Constant DEBUG. */
	public static final int DEBUG = 0;
	/** The Constant EMAIL_FROM. */
	public static final String EMAIL_FROM = "";
	/** The Constant EMAIL_HOST. */
	public static final String EMAIL_HOST = "EMAIL_HOST";
	/** The Constant ERROR. */
	public static final int ERROR = 1;
	/** The Constant INFO. */
	public static final int INFO = 1;
	/** The Constant LOCAL. */
	public static final String LOCAL = "Windows 2000";
	
	/** script to execute */
	  	 
	public static final String SCRIPT = "/array2/watch/mnt_watcher/mnt.sh";
 

	/** The Constant RUNNING_ENV. */
	public static final String RUNNING_ENV = System.getProperty("os.name");

	public static volatile int totRecords = 0;

	/** The Constant WARNING. */
	public static final int WARNING = 2;

	/** The Application property table. */
	private static Hashtable<String, String> ApplicationPropertyTable = null;

	private static String ignoreFiles = "tst-cnnct";

	private static String inProcessDest = null;

	/** The log. */
	private static Logger log = Logger.getLogger(Util.class.getName());

	private static String processedDest = null;

	private static int timeDelay = 0;

	private static String watchDir = null;
	
	private static StringBuilder errorFromScript = new StringBuilder();

	public static StringBuilder getErrorFromScript() {
		return errorFromScript;
	}

	public static void setErrorFromScript(String errormessage) {
		Util.errorFromScript.append(errormessage);
	}

	public static String extractFileName(String filePathName) {
		if (filePathName == null)
			return null;

		int slashPos = filePathName.lastIndexOf('\\');

		if (slashPos == -1)
			return filePathName;

		return filePathName.substring(slashPos + 1, filePathName.length());
	}

	public static String getIgnoreFiles() {
		return ignoreFiles;
	}

	public static String getInProcessDest() {
		return inProcessDest;
	}

	public static String getProcessedDest() {
		return processedDest;
	}

	/**
	 * Gets the property.
	 * 
	 * @param propertyName
	 *            the property name
	 * 
	 * @return the property
	 * 
	 * @throws UnRecoverableException
	 *             the un recoverable exception
	 */
	public static String getProperty(String propertyName) throws Exception {
		String propertyValue = "";
		try {
			if (Util.ApplicationPropertyTable == null) {
				Util.ApplicationPropertyTable = ApplicationPropertyDigester
						.getProperties(Util.APPLICATION_PROPERTIES_FILE);

			}
			propertyValue = Util.ApplicationPropertyTable.get(propertyName);
		} catch (final Exception e) {
			throw new Exception(" Error getting property values"
					+ e.getMessage());
		}
		Util.log.debug("query returned " + propertyValue);
		return propertyValue;
	}

	public static int getTimeDelay() {
		return timeDelay;
	}

	public static int getTotRecords() {
		return totRecords;
	}

	public static String getWatchDir() {
		return watchDir;
	}

	public static void main2(String[] args) throws Exception {

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

	public static void setIgnoreFiles(String ignoreFiles) {
		Util.ignoreFiles = ignoreFiles;
	}

	public static void setInProcessDest(String inProcessDest) {
		Util.inProcessDest = inProcessDest;
	}

	public static void setProcessedDest(String processedDest) {
		Util.processedDest = processedDest;
	}

	public static void setTimeDelay(int timeDelay) {
		Util.timeDelay = timeDelay;
	}

	public static void setTotRecords(int records) {
		Util.totRecords = totRecords + records;
	}

	public static void setupEnv(String watch, String processed) {
		processedDest = processed;
		watchDir = watch;

	}

	public static void setWatchDir(String watchDir) {
		Util.watchDir = watchDir;
	}

	public static void main(String[] args) throws Exception {

		String inProcessFile = null;
		String newFile = null;

		try (

		BufferedReader fs = new BufferedReader(new FileReader(inProcessFile));) {

			Path fr = Paths.get("C:/41056/test.txt");

			// move the file to in process folder

			inProcessFile = moveFiletoNewDestination(fr,
					Util.getInProcessDest());

			String c;
			String[] tokens = null;
			while ((c = fs.readLine()) != null) {

				tokens = c.split(",");

				String file = Util.extractFileName(tokens[0]);
				newFile = tokens[1] + "\\" + file;

				File targetFile = new File(newFile);
				if (targetFile.exists()) {
					continue;

				} else {
					Util.setTotRecords(1);
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

				// move to processed Destination

			}
			fs.close();

			String target = moveFiletoNewDestination(Paths.get(inProcessFile),
					Util.getProcessedDest());

		} catch (Exception e) {

			throw e;
		}
	}

	public static String moveFiletoNewDestination(Path afile, String dest)
			throws Exception {

		String filename = afile.getFileName().toString();
		Path target = FileSystems.getDefault().getPath(dest, filename);
		Files.move(afile, target, REPLACE_EXISTING);

		return target.toString();
	}

}
