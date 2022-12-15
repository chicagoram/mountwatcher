package com.mlxod.mw.listener;

/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.mlxod.mw.listener.ContentProcessor;
import com.mlxod.mw.listener.WatchDirDriver;
import com.mlxod.mw.util.Emailer;
import com.mlxod.mw.util.Util;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDirDriver {

	private static final Logger debugLogger = Logger.getLogger("debuglog");
	private static final Logger errorLogger = Logger.getLogger("errorlog");
	private static final Logger logger = Logger.getLogger(WatchDirDriver.class
			.getName());
	@SuppressWarnings("unchecked")
	public static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}
	public static void main(String[] args) {

		// parse arguments

		// Initialize log4j
		 

		try ( 
				java.io.InputStream inputStream = WatchDirDriver.class.getClassLoader().getResourceAsStream("log4j.properties")
			){
			

			

			System.out.println("loading log4j props..." + inputStream);
			Properties props = new Properties();
			props.load(inputStream);
			PropertyConfigurator.configure(props);

			

			if (args.length > 3 || args.length == 0)
				usage();
			boolean recursive = true;
			int dirArg = 0;
			if (args[0].equals("-r")) {
				if (args.length < 3)
					usage();
				recursive = true;
				dirArg++;
			}

			if (args[0] != null && args[0].equals("-r")) {
				Util.setupEnv(args[1], args[2]);
			} else {
				Util.setupEnv(args[0], args[1]);
			}

			// register directory and process its events
			Path dir = Paths.get(args[dirArg]);
			new WatchDirDriver(dir, recursive).processEvents();

		} catch (Exception e) {
			errorLogger.error("error processing " + e.getMessage());
			e.printStackTrace();
		}
	}
	static void usage() {
		errorLogger.error("usage: java WatchDir [-r] dir");
		System.exit(-1);
	}
	private final Map<WatchKey, Path> keys;

	private final boolean recursive;

	private boolean trace = false;

	private final WatchService watcher;

	/**
	 * Creates a WatchService and registers the given directory
	 */
	WatchDirDriver(Path dir, boolean recursive) throws Exception {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		this.recursive = recursive;

		

		if (recursive) {
			
			registerAll(dir);
			
		} else {
			register(dir);
		}

		// enable trace after initial registration

	}
	
	private void processAdd(Path child) {


		String fileNameWithOutExt = child.toFile().getName()
				.replaceFirst("[.][^.]+$", "");
		int returnCode = 0;

		try {

			ContentProcessor cp = new ContentProcessor();
			returnCode = cp.processContentFile(child);
			
			if (returnCode != 0) {
				
				issueSystemAlert(fileNameWithOutExt,returnCode,child);
			} else {
				
				String mount = "/mnt/41/Towers/" + fileNameWithOutExt;
				logger.info("Executed the script -001- Tower mounted to " + mount + " successfully" + "Return Code " + returnCode);
				Emailer.sendEmail("AV_SYSTEM_ALERT TOWER_MOUNT_PROCESS success - mount point " + mount + " Successfully " 
						, " Status : Success");
			}

		} catch (Exception e) {
			e.printStackTrace();
			errorLogger.error("Error Processing the script ---- error is ---"
					+ e.getMessage() + " script to execute " + child + " Return Code " + returnCode);
			issueSystemAlert(fileNameWithOutExt,returnCode,child);
			/* Emailer.sendEmail("Mount Watch Dog - Script " + child
					+ " execution status: failure" + " return code = " +  e.getMessage(), Util.getErrorFromScript().toString()); */
			//Util.setErrorFromScript("No Error");
	
		}
	}

	private void issueSystemAlert(String towerInfo, int returnCode, Path child){
		StringBuffer messageBuf = new StringBuffer();
		messageBuf.append("Error mounting the tower " + towerInfo + " with returnCode = "
				+ returnCode + " script file executed = " + child);
		messageBuf.append(System.getProperty("line.separator"));
		messageBuf.append(" Possible causes for failure could be: \n");
		messageBuf.append("1. Target share may not exists \n");
		messageBuf.append("2. Target share may not have sufficient permissions \n"  );
		messageBuf.append("3. Target tower could be down \n");
		messageBuf.append("4. Check if Mount point " + child + "Exists" );
		messageBuf.append("4. System message from script execution is \n" );
		messageBuf.append(" Error Code from the process = " + Util.getErrorFromScript().toString());
		errorLogger.error(messageBuf.toString());
		Emailer.sendEmail("AV_SYSTEM_ALERT TOWER_MOUNT_PROCESS failed on AVSTOR01 mounting to " + child
				, messageBuf.toString());


	}
	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE,ENTRY_MODIFY);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				errorLogger.error("No Directories Registered: %s\n" + dir);
			} else {
				if (!dir.equals(prev)) {
					errorLogger.error("update: %s -> %s\n" + "prev " + prev
							+ "current " + dir);
				}
			}
		}
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	void processEvents() throws Exception {
		for (;;) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.poll(10, TimeUnit.MILLISECONDS);
				key = watcher.take();
				
			} catch (InterruptedException x) {
				errorLogger.error("event processing exception " + x.getMessage());
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				errorLogger.error("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				Kind<?> kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					errorLogger.error("Event Overflow!!");
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				// print out event
				if (child != null)
				debugLogger.debug("event kind name %s: %s\n " + event.kind().name()
						+ "child is " + child.toFile().getName());

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if (recursive
						&& kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
					try {
						if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
							registerAll(child);
						}
					} catch (IOException x) {
						errorLogger.error("Error creating file in the directory"
								+ x.getMessage());
					}
				}
				
				if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
		            logger.info("Begin Creating file : " + child);
		            Thread.sleep(5000);

		            boolean isGrowing = false;
		            Long initialWeight = new Long(0);
		            Long finalWeight = new Long(0);

		            do {
		                initialWeight = child.toFile().length();
		                //Thread.sleep(3000);
		                finalWeight = child.toFile().length();
		                isGrowing = initialWeight < finalWeight;

		            } while(isGrowing);

		            logger.info("Finished creating file " + child);
		            
		            String extension = "";
		            File f = child.toFile();
		            String fname = f.getName();
		    		int i = fname.lastIndexOf('.');
		    		if (i > 0) {
		    		    extension = fname.substring(i+1);
		    		}
		    		if (extension.equalsIgnoreCase("txt"))
		            processAdd(child);
		        }
				if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
					logger.info("ENTRY ADDED " + child);
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				logger.error("Directory no longer available ");
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}
}
