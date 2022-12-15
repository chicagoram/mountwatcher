package com.mlxod.mw.listener;

import java.io.File;


import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.mlxod.mw.listener.ExecLogHangler;
import com.mlxod.mw.listener.ScriptExecutor;
import com.mlxod.mw.util.Util;

public class ScriptExecutor {

	private static final Logger logger = Logger.getLogger(ScriptExecutor.class
			.getName());

	private static final Logger debugLogger = Logger.getLogger("debuglog");
	private static final Logger errorLogger = Logger.getLogger("errorlog");

	private class ExecuteScriptResultHandler extends
			DefaultExecuteResultHandler {

		private ExecuteWatchdog watchdog;

		public ExecuteScriptResultHandler(ExecuteWatchdog watchdog) {
			this.watchdog = watchdog;
		}

		public ExecuteScriptResultHandler(int exitValue) {
			super.onProcessComplete(exitValue);
		}

		@Override
		public void onProcessComplete(int exitValue) {
			super.onProcessComplete(exitValue);

		}

		@Override
		public void onProcessFailed(ExecuteException e) {
			super.onProcessFailed(e);
			errorLogger.error("process failed" + e.getMessage());
			if (watchdog != null && watchdog.killedProcess()) {
				errorLogger
						.error("[resultHandler] The execute script process timed out");
			} else {
				errorLogger
						.error("[resultHandler] The execute script process failed to do : "
								+ e.getMessage());

			}
		}
	}

	/** simulates a PDF execute job */

	public static void main(String[] args) throws Exception {

		// ScriptExecutor execScript = new ScriptExecutor("C://test.bat");

	}

	public ScriptExecutor() {

	}

	/**
	 * @param file
	 *            the file to execute
	 * @param executeJobTimeout
	 *            the executeJobTimeout (ms) before the watchdog terminates the
	 *            execute process
	 * @param executeInBackground
	 *            executing done in the background or blocking
	 * @return a execute result handler (implementing a future)
	 * @throws IOException
	 *             upon failures
	 */
	public ExecuteScriptResultHandler execute(File file, String ip,
			String mountPoint, long executeJobTimeout,
			boolean executeInBackground) throws IOException {

		int exitValue;
		ExecuteWatchdog watchdog = null;
		ExecuteScriptResultHandler resultHandler = null;

		// build up the command line to using a 'java.io.File'
		Map<String, File> map1 = new HashMap<String, File>();
		map1.put("file", file);

		CommandLine commandLine = new CommandLine("/bin/bash");
		commandLine.addArgument("${file}");
		commandLine.addArgument(ip, false);
		commandLine.addArgument(mountPoint, false);
		
		commandLine.setSubstitutionMap(map1);

		// create the executor and consider the exitValue '1' as success
		Executor executor = new DefaultExecutor();
		executor.setExitValue(0);
		PumpStreamHandler psh = new PumpStreamHandler(new ExecLogHangler(
				debugLogger, Level.DEBUG), new ExecLogHangler(errorLogger,
				Level.ERROR));
		executor.setStreamHandler(psh);

		// create a watchdog if requested
		if (executeJobTimeout > 0) {
			watchdog = new ExecuteWatchdog(executeJobTimeout);
			executor.setWatchdog(watchdog);
		}

		debugLogger.debug("commandLine first= " + Arrays.toString(commandLine.toStrings()));

		debugLogger.debug("[execute] Executing blocking execute job  ...");
		exitValue = executor.execute(commandLine);
		resultHandler = new ExecuteScriptResultHandler(exitValue);

		return resultHandler;
	}

	public static int executeCommand(CommandLine command, Logger log)
			throws ExecuteException, IOException {
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(0);

		PumpStreamHandler psh = new PumpStreamHandler(new ExecLogHangler(log,
				Level.DEBUG), new ExecLogHangler(log, Level.ERROR));
		executor.setStreamHandler(psh);

		return executor.execute(command);
	}

	public int executeScript(String ip, String mountPoint) throws Exception {

		long executeJobTimeout = 900000;
		boolean executeInBackground = false;
		File scriptFile = new File(Util.SCRIPT);
		int returnCode = Util.ERROR;

		ExecuteScriptResultHandler executeResult = null;

		// executeing takes around 10 seconds
		debugLogger.debug("[main] Preparing execute job ..." + scriptFile.getName());
		executeResult = execute(scriptFile, ip, mountPoint, executeJobTimeout,
				executeInBackground);
		debugLogger.debug("[main] Successfully sent the execute job ...");

		// come back to check the execute result
		debugLogger
				.debug("[main] Test is exiting but waiting for the execute job"
						+ scriptFile + " to finish...");
		executeResult.waitFor();
		debugLogger.debug("[main] The execute job has finished ...");

		if (executeResult != null) {
			returnCode = executeResult.getExitValue();
			debugLogger.debug("Return code for script " + " " + returnCode);
		}

		return returnCode;

	}

}

class ExecLogHangler extends LogOutputStream {
	private Logger log;

	public ExecLogHangler(Logger log, Level level) {
		super(level.toInt());
		this.log = log;
	}

	protected void processLine(String line, int level) {
		log.log(Level.toLevel(level), line);
		Util.setErrorFromScript(line);
		Util.setErrorFromScript("\n");
	}
}
