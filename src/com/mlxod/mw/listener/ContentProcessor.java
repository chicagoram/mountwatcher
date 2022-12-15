package com.mlxod.mw.listener;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import com.mlxod.mw.listener.ContentProcessor;
import com.mlxod.mw.listener.ScriptExecutor;
import com.mlxod.mw.util.Util;

public class ContentProcessor {

	private static final Logger logger = Logger
			.getLogger(ContentProcessor.class.getName());
	private static final Logger debugLogger = Logger.getLogger("debuglog");
	private static final Logger errorLogger = Logger.getLogger("errorlog");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private Path source = null;

	public ContentProcessor() {

	}

	public String moveFiletoNewDestination(Path afile, String dest)
			throws Exception {

		logger.debug("Method Access moveFiletoNewDestination   file   = "
				+ afile.getFileName());
		debugLogger.debug("Moving file...................... "
				+ afile.getFileName() + " to destination... " + dest);
		String filename = afile.getFileName().toString() +  '.' + System.currentTimeMillis() ;
		//Path target = FileSystems.getDefault().getPath(dest, filename);
		//Files.move(afile, target, ATOMIC_MOVE);
		afile.toFile().renameTo(new File(dest + File.separator + filename));

		debugLogger.debug("File........................... "
				+ afile.getFileName() + " moved successfully to " + dest);

		return dest;
	}

	public int processContentFile(Path sourceFile) throws Exception {

		this.source = sourceFile;
		int returnCode = readFile(source);
		return returnCode;

	}

	private static void sendFileToNewDestination(Path sourceFile)
			throws Exception {

		debugLogger.debug("sending file to new destination  target File = "
				+ sourceFile.getFileName());
		String newFileName = Util.getProcessedDest() + File.separator
				+ sourceFile.getFileName() + '.' + System.currentTimeMillis();
		try (InputStream istream = new FileInputStream(sourceFile.toFile());

				WritableByteChannel channel = new FileOutputStream(new File(
						newFileName)).getChannel()) {

			ReadableByteChannel source = Channels.newChannel(istream);

			ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

			while (source.read(buffer) != -1) {
				buffer.flip();
				while (buffer.hasRemaining()) {
					channel.write(buffer);
				}
				buffer.clear();
			}
		}
	}

	public int readFile(Path sourceFile) throws Exception {

		String ip = null;
		String mountPoint = null;
		try (BufferedReader fs = new BufferedReader(new FileReader(
				sourceFile.toFile()));) {

			String c;
			String[] tokens = null;
			while ((c = fs.readLine()) != null) {

				errorLogger.error("LOC007:- File Content " + c);
				tokens = c.split(",");

				ip = Util.extractFileName(tokens[0]);
				mountPoint = Util.extractFileName(tokens[1]);

			}
		} catch (Exception e) {
			errorLogger.error("LOC008:- File processing error " + e.getMessage()
					+ " ");
			throw e;
		}
		logger.debug("\n");
		int returnCode = 0;

			
			ScriptExecutor se = new ScriptExecutor();
			returnCode = se.executeScript(ip, mountPoint);
		

		String target = moveFiletoNewDestination(sourceFile,
				Util.getProcessedDest());
		logger.debug(" Script execution status for script " + sourceFile
				+ " status = " + returnCode);
		return returnCode;
	}

}
