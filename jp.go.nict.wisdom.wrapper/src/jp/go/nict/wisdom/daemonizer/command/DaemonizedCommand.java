/*
* Copyright (C) 2014 Information Analysis Laboratory, NICT
*
* RaSC is free software: you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 2.1 of the License, or (at
* your option) any later version.
*
* RaSC is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
* General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package jp.go.nict.wisdom.daemonizer.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This class manages a process of the target command, keeping it running.<br/>
 * The process starts when start() is invoked, and closes when close() is invoked.
 *
 * @author mtanaka
 */
public class DaemonizedCommand {
	private static Logger logger = Logger.getLogger(DaemonizedCommand.class.getName());

	private static final String lineSep = System.getProperty("line.separator");

	private String[] cmd;
	private String dir;
	private int timeOut;
	private int startWait;
	private int restartWait;
	private int bufSize;

	private BlockingQueue<String> inputQueue;
	private BlockingQueue<String> outputQueue;

	private ExecutorService exInput;
	private ExecutorService exOutput;
	private ExecutorService exError;

	private Process process;

	static {
	}

	public DaemonizedCommand(String[] cmd, String dir, int timeOut, int startWait, int restartwait, int bufSize) {
		this.cmd = cmd;
		this.dir = dir;
		this.timeOut = timeOut;
		this.startWait = startWait;
		this.restartWait = restartwait;
		this.bufSize = bufSize;
		logger.info("DaemonizedCommand(String[] cmd, String dir, int timeOut, int startWait, int restartwait, int bufSize)");
		String cmdStr = "";
		for(String s : cmd){
			cmdStr = cmdStr + " " + s;
		}
		logger.info(cmdStr);
		logger.info(timeOut + " : " + startWait + " : " + restartwait + " : " + bufSize);
	}

	protected String doGetNextResult() throws InterruptedException, IOException {
		String result = outputQueue.poll(timeOut, TimeUnit.MILLISECONDS);
		if (result == null) {
			logger.info("Timeout occured.");
			try {
				close();
				Thread.sleep(restartWait);
				start();
			} catch (IOException e) {
				throw new IOException("Timeout occured. The wrapped program may stall or die. Restart failed.");
			}

			throw new InterruptedException("Timeout occured. The wrapped program may stall or die.");
		}

		return result;
	}

	/**
	 * Write input to stdin of the target.
	 *
	 * @param input Input string
	 */
	protected void doPut(String input) {

		inputQueue.offer(input);
	}


	/**
	 * Invoke a process,and initialize thread pools.
	 *
	 * @throws IOException Target command not found / Failed to start a process
	 */
	public void start() throws IOException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		if(dir != null){
			pb.directory(new File(dir));
		}
		process = pb.start();

		try {
			Thread.sleep(startWait);
		} catch (InterruptedException e) {
			logger.severe("Sleep after invocation is interrupted.");
		}

		boolean running = false;
		try {
			process.exitValue();
		} catch (IllegalThreadStateException e) {
			running = true;
		}
		if (!running) {
			String msg = "No process running after the attempt of invocation.";
			logger.severe(msg);
			throw new IOException(msg);
		}

		inputQueue = new LinkedBlockingQueue<String>();
		outputQueue = new LinkedBlockingQueue<String>();

		exInput = Executors.newSingleThreadExecutor();
		exOutput = Executors.newSingleThreadExecutor();
		exError = Executors.newSingleThreadExecutor();

		exInput.submit(new InputWorker());
		exOutput.submit(new OutputWorker());
		exError.submit(new ErrorWorker());

		clear();
	}

	/**
	 * Close the process.
	 *
	 * @throws IOException Failed to close
	 */
	public void close() throws IOException {

		exInput.shutdownNow();
		exOutput.shutdownNow();
		exError.shutdownNow();

		process.destroy();

		clear();
	}

	public void clear() {
		inputQueue.clear();
		outputQueue.clear();
	}

	/**
	 * Worker thread writing input to the stdin of the program.
	 *
	 * @author mtanaka
	 */
	private class InputWorker implements Runnable {
		@Override
		public void run() {
			logger.config("InputWorker : " + Thread.currentThread().getName() + " Start");
			while (true) {
				try {
					String input = inputQueue.take();
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
					bw.write(input);
					bw.flush();
				} catch (InterruptedException e) {
					break;
				} catch (IOException e) {
					logger.warning("Failed to write input to the process");
				}
			}
			logger.config("InputWorker : " + Thread.currentThread().getName() + " Stop");
			return;
		}
	}

	/**
	 * Worker thread reading from stdout of the program.
	 *
	 * @author mtanaka
	 */
	private class OutputWorker implements Runnable {

		@Override
		public void run() {
			logger.config("OutputWorker : " + Thread.currentThread().getName() + " Start");
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					outputQueue.offer(line + lineSep);
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			logger.config("OutputWorker : " + Thread.currentThread().getName() + " Stop");
		}
	}

	/**
	 * Worker thread reading from stderr of the program.
	 *
	 * @author mtanaka
	 */
	private class ErrorWorker implements Runnable {

		@Override
		public void run() {
			logger.config("ErrorWorker : " + Thread.currentThread().getName() + " Start");
			byte[] buf = new byte[bufSize];
			int readSize = 1;
			try {
					while (true) {
						readSize = process.getErrorStream().read(buf);
						if(readSize > 0){
							logger.fine(new String(buf, 0, readSize, "UTF-8"));
						}
					}
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			logger.config("ErrorWorker : " + Thread.currentThread().getName() + " Stop");
		}
	}

}
