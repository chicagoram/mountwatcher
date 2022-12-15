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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class FileListener extends BaseListener implements IFileListener {

	private static final Logger logger = Logger.getLogger(FileListener.class);
	private static final Object counterLock = new Object();

	public void onStart(Object monitoredResource) {
		// On startup
		if (monitoredResource instanceof File) {
			File resource = (File) monitoredResource;
			if (resource.isDirectory()) {

				logger.info("Start to monitor the resource........................ "
						+ resource.getAbsolutePath());

			}
		}
	}

	/**
	 * Connstructor
	 */
	public FileListener() {
		super();
	}

	public void onStop(Object notMonitoredResource) {

	}

	public void onAdd(Object newResource) throws Exception {
		if (newResource instanceof File) {
			File file = (File) newResource;
			if (file.isFile()) {
				boolean fileIsReady = false;
				while (!fileIsReady) {
					try {

						file.canWrite();
						fileIsReady = true;
					} catch (Exception e) {

					}
				}
				processFile(file);

			}
		}
	}

	private void processFile(final File fr) throws Exception {

		try {
			new AutoFileCloser() {
				@Override
				protected void doWork() throws Throwable {
					logger.info("\n");

					logger.info("file just being processed............................" + fr.getName());
					             					

					// move the file to in process folder

					boolean success = moveFiletoNewDestination(fr,
							Util.getInProcessDest());

					logger.info("\n");

					if (success) {
						String inProcessFile = Util.getInProcessDest() + "\\"
								+ fr.getName();
						// read the file from inprocess folder
						BufferedReader fs = autoClose(new BufferedReader(
								autoClose(new FileReader(inProcessFile))));

						String c;
						String[] tokens = null;
						while ((c = fs.readLine()) != null) {

							tokens = c.split(",");

							String file = Util.extractFileName(tokens[0]);
							String newFile = tokens[1] + "\\" + file;

							File targetFile = new File(newFile);
							if (targetFile.exists()) {
								logger.info("Art Work........." + tokens[0] + " already exists");
								continue;

							} else {
								Util.setTotRecords(1);
							}
							InputStream istream = autoClose(new FileInputStream(
									new File(tokens[0])));

							ReadableByteChannel source = Channels
									.newChannel(istream);
							WritableByteChannel channel = autoClose(new FileOutputStream(
									newFile).getChannel());

							ByteBuffer buffer = ByteBuffer
									.allocateDirect(102400);

							logger.info("\n");
							logger.info("Processing record ......................." + tokens[0]);
							             

							while (source.read(buffer) != -1) {
								buffer.flip();
								while (buffer.hasRemaining()) {
									channel.write(buffer);
								}
								buffer.clear();
							}

							logger.info(" Record.........................." + tokens[0]	+ "  processed successfully");
							              
							// move to processed Destination
						}

						logger.info("\n");

						fs.close();

						boolean moved = moveFiletoNewDestination(new File(
								inProcessFile), Util.getProcessedDest());

						if (!moved) {
							throw new Exception("error moving processed file");
						}
					}
				}
			};
		} catch (Exception e) {

			throw e;
		} finally {

			synchronized (counterLock) {

				logger.info("Total Records Processed.............................." + Util.totRecords);
				logger.info("\n");
				logger.info("Processing Ends@....................................."
						+ new SimpleDateFormat("MM/DD/YYYY HH:mm:ss")
								.format(new Date()));
				logger.info("\n");

			}

		}

	}

	public boolean moveFiletoNewDestination(File afile, String dest)
			throws Exception {

		boolean success = true;

		logger.info("Moving file...................... " + afile.getName() + " to destination... " + dest);
		try {

			File newFile = new File(dest + "\\" + afile.getName());
			if (newFile.exists()) {
				logger.info("File " + newFile.getName()
						+ " already exists in Target " + dest);
				return !success;
			}
			success = afile.renameTo(new File(dest + "\\" + afile.getName()));

		} catch (Exception e) {
			success = false;
			e.printStackTrace();
			throw e;
		}

		logger.info("File........................... " + afile.getName() + " moved successfully to " + dest 	+ " folder!");
		

		return success;
	}
}
