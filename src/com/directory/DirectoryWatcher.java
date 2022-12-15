package com.directory;

import java.io.File;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class DirectoryWatcher extends AbstractResourceWatcher {

	/**
	 * Logger instance
	 * 
	 * 
	 */

	/** The Constant logger. */
	private static final Logger logger = Logger
			.getLogger(DirectoryWatcher.class);

	/**
	 * The current map of files and their timestamps (String fileName => Long
	 * lastMod)
	 */
	private Map<String, Long> currentFiles = new ConcurrentHashMap<String, Long>();

	/**
	 * The directory to watch.
	 */
	private String directory;

	/**
	 * The map of last recorded files and their timestamps (String fileName =>
	 * Long lastMod)
	 */
	// private Map<String, Long> prevFiles = new HashMap<String, Long>();

	/**
	 * Constructor that takes the directory to watch.
	 * 
	 * @param directoryPath
	 *            the directory to watch
	 * @param intervalSeconds
	 *            The interval to use when monitoring this directory. I.e., ever
	 *            x seconds, check this directory to see what has changed.
	 * @throws IllegalArgumentException
	 *             if the argument does not map to a valid directory
	 */
	public DirectoryWatcher(String directoryPath, int intervalSeconds)
			throws IllegalArgumentException {

		// Get the common thread interval stuff set up.
		super(intervalSeconds, directoryPath + " interval watcher.");

		// log4j configuration

		// Check that it is indeed a directory.
		File theDirectory = new File(directoryPath);

		if (theDirectory != null && !theDirectory.isDirectory()) {

			// This is bad, so let the caller know
			String message = "The path " + directory
					+ " does not represent a valid directory.";
			throw new IllegalArgumentException(message);

		}

		// Else all is well so set this directory and the interval
		this.directory = directoryPath;

	}

	/**
	 * For testing only.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// log4j conf

		PropertyConfigurator.configure("log4j.properties");

		// Monitor WATCH_DIR every 30 seconds
		logger.info("Listener starting @.................................." + new SimpleDateFormat("MM/DD/yy HH:mm:ss").format(new Date()));

		if (args == null || args.length == 0 || args[0] == null
				|| args[1] == null || args[2] == null || args[3] == null) {
			System.out
					.println("Please set the environment before starting the application System exiting...");
			logger.error("Please set the environment before starting the application");
			System.exit(0);
		}
		Util.setupEnv(args[0], args[1], args[2],
				new Integer(args[3]).intValue());

		DirectoryWatcher dw = new DirectoryWatcher(Util.getWatchDir(),
				Util.getTimeDelay());
		dw.addListener(new FileListener());
		dw.start();

	}

	/**
	 * Start the monitoring of this directory.
	 */
	@Override
	public void start() throws Exception {

		// Since we're going to start monitoring, we want to take a snapshot of
		// the
		// current directory to we have something to refer to when stuff
		// changes.
		takeSnapshot();

		// And start the thread on the given interval
		super.start();

		// And notify the listeners that monitoring has started
		File theDirectory = new File(directory);
		monitoringStarted(theDirectory);
	}

	/**
	 * Stop the monitoring of this directory.
	 */
	@Override
	public void stop() throws Exception {

		// And start the thread on the given interval
		super.stop();

		// And notify the listeners that monitoring has started
		File theDirectory = new File(directory);
		monitoringStopped(theDirectory);
	}

	/**
	 * Store the file names and the last modified timestamps of all the files
	 * and directories that exist in the directory at this moment.
	 */
	private void takeSnapshot() {

		// Set the last recorded snap shot to be the current list
		// prevFiles.clear();
		// prevFiles.putAll(currentFiles);

		// And get a new current state with all the files and directories
		currentFiles.clear();

		File theDirectory = new File(directory);
		File[] children = theDirectory.listFiles();

		// if children empty set

		if (children == null || children.length == 0) {
			children = null;
			theDirectory = null;
		} else {
			// Store all the current files and their timestamps
			for (int i = 0; i < children.length; i++) {

				File file = children[i];
				currentFiles.put(file.getAbsolutePath(),
						new Long(file.lastModified()));

			}

		}
	}

	/**
	 * Check this directory for any changes and fire the proper events.
	 */
	@Override
	protected void doInterval() throws Exception {

		// Take a snapshot of the current state of the dir for comparisons

		// Thread starting

		System.out.println("Listener invocation ..."
				+ Thread.currentThread().getName()
				+ " time = "
				+ new SimpleDateFormat("yy/MM/dd HH:mm:ss")
						.format(new Date()));
		Iterator<String> currentIt = null;

		try {

			takeSnapshot();

			// Iterate through the map of current files and compare
			// them for differences etc...

			if (currentFiles != null && currentFiles.keySet().size() > 0) {
				currentIt = currentFiles.keySet().iterator();

				while (currentIt.hasNext()) {

					String fileName = (String) currentIt.next();
					// If this file did not exist before, but it does now, then
					/*
					 * it's been added - im going to process this folder in
					 * variably - in case reqmt arises for comparing new and old
					 * content we'll un cooment this
					 */
					// if (!prevFiles.containsKey(fileName)) {
					// DirectorySnapshot.addFile(fileName);

					if (fileName != null && !new File(fileName).isDirectory()) {
					String fileNameWithOutExt = new File(fileName).getName().replaceFirst("[.][^.]+$", "");
					if (!(fileNameWithOutExt.equalsIgnoreCase(Util
							.getIgnoreFiles()))) {
						logger.info("Adding file...................."
								+ fileName + "  for processing");

						resourceAdded(new File(fileName));
					}
				  }

				}
			}
		} catch (Exception e) {
			logger.error("Error processing the file due to the reason ........."
					+ e.getMessage());

			throw e;
		} finally {
			currentIt = null;
		}
	}

}
