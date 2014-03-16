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

package jp.go.nict.wisdom.wrapper;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import jp.go.nict.wisdom.daemonizer.command.StringIOCommand;
import jp.go.nict.wisdom.wrapper.api.TextAnalysisService;
import jp.go.nict.wisdom.wrapper.status.ServiceInitialize;
import jp.go.nict.wisdom.wrapper.status.ServiceStatus;

public abstract class AbstractTextAnalysisService implements TextAnalysisService, ServiceStatus, ServiceInitialize {
	private static Logger logger = Logger.getLogger(AbstractTextAnalysisService.class.getName());

	protected static final AtomicInteger numCmd = new AtomicInteger();

	protected static BlockingQueue<StringIOCommand> cmdPool = new LinkedBlockingQueue<>();
	private String cmdLine = null;
	private int poolSize = 0;

	public AbstractTextAnalysisService() {
	}

	protected abstract StringIOCommand getInstance() throws IOException, InterruptedException;

	protected StringIOCommand getInstance(
			Class<? extends StringIOCommand> cmdClass, String cmdLine,
			int pollTimeOut, int poolSize,
			int timeOut, int startWait, int restartWait, int bufSize
			)
			throws IOException, InterruptedException {

		logger.info("getInstance(...)");
		this.cmdLine = cmdLine;
		this.poolSize = poolSize;
		StringIOCommand cmd = null;
		if (cmdPool.isEmpty() && numCmd.getAndIncrement() < poolSize) {
			int num = numCmd.get();
			// Create new process
			try {
				Class<?>[] argType = {String.class, int.class, int.class, int.class, int.class};
				Constructor<? extends StringIOCommand> constructor;
				constructor = cmdClass.getConstructor(argType);
				cmd = constructor.newInstance(cmdLine, timeOut, startWait, restartWait, bufSize);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				logger.severe(e.getMessage());
				throw new RuntimeException(e);
			}
			cmd.start();

			logger.info("Created a new process. Pooled processes : " + num);
		} else {
			int num = numCmd.get();
			if(num >= Integer.MAX_VALUE - poolSize){
				numCmd.set(poolSize);
				logger.config("numCmd reset");
			}
			// Reuse running process
			try {
				cmd = cmdPool.poll(pollTimeOut, TimeUnit.MILLISECONDS);
				if(cmd == null){
					String msg = "All running processes are occupied. Timeout occured.";
					logger.info(msg);
					throw new IOException(msg);
				}
				cmd.clear();
				logger.finest("Reused a process");
			} catch (InterruptedException e) {
				logger.info("Polling of pooled process was interrupted.");
				throw e;
			}
		}
		return cmd;
	}

	protected StringIOCommand getInstance(
			Class<? extends StringIOCommand> cmdClass, String[] cmdLine,
			String delimiterIn, String delimiterOut,
			boolean delLastNewline, boolean includeDelim,
			int pollTimeOut, int poolSize,
			int timeOut, int startWait, int restartWait, int bufSize
			)
			throws IOException, InterruptedException {

		logger.info("getInstance(...)");
		if(this.cmdLine == null){
			int i = 0;
			for(i = 0; i < cmdLine.length; i++){
				if(i == 0)
					this.cmdLine = cmdLine[i];
				else
					this.cmdLine = this.cmdLine + " " + cmdLine[i];
			}
		}
		this.poolSize = poolSize;

		StringIOCommand cmd = null;
		if (cmdPool.isEmpty() && numCmd.getAndIncrement() < poolSize) {
			int num = numCmd.get();
			// Create new process
			try {
				Class<?>[] argType = {String[].class, String.class, String.class, boolean.class, boolean.class, int.class, int.class, int.class, int.class};
				Constructor<? extends StringIOCommand> constructor;
				constructor = cmdClass.getConstructor(argType);
				cmd = constructor.newInstance(cmdLine, delimiterIn, delimiterOut, delLastNewline, includeDelim, timeOut, startWait, restartWait, bufSize);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				logger.severe(e.getMessage());
				throw new RuntimeException(e);
			}
			cmd.start();

			logger.info("Created a new process. Pooled processes : " + num);
		} else {
			int num = numCmd.get();
			if(num >= Integer.MAX_VALUE - poolSize){
				numCmd.set(poolSize);
				logger.config("numCmd reset");
			}
			// Reuse running process
			try {
				cmd = cmdPool.poll(pollTimeOut, TimeUnit.MILLISECONDS);
				if(cmd == null){
					String msg = "All running processes are occupied. Timeout occured.";
					logger.info(msg);
					throw new IOException(msg);
				}
				cmd.clear();
				logger.finest("Reused a process");
			} catch (InterruptedException e) {
				logger.info("Polling of pooled process was interrupted.");
				throw e;
			}
		}
		return cmd;
	}

	protected void returnInstance(StringIOCommand cmd) {
		cmdPool.add(cmd);
		logger.finest("Retuned a process to pool.");
	}

	@Override
	public String analyze(String text) throws Exception {
		long startMillis = System.currentTimeMillis();
		StringIOCommand cmd = getInstance();
		String result = null;
		try {
			cmd.put(text);
			result = cmd.getNextResult();
		} catch (InterruptedException | IOException e) {
			logger.severe(e.getMessage());
			throw e;
		} finally {
			returnInstance(cmd);
			logger.config("Time : [" + (System.currentTimeMillis() - startMillis) + "ms]");
		}
		return result;
	}

	@Override
	public String[] analyzeArray(String[] text) throws Exception {
		long startMillis = System.currentTimeMillis();
		StringIOCommand cmd = getInstance();
		CatchException ce = new CatchException();

		Thread inputWorker = new Thread(new InputWorker(
				cmd, text));
		inputWorker.setUncaughtExceptionHandler(ce);
		inputWorker.start();

		String[] result = new String[text.length];
		int count = 0;
		try {
			while (count < text.length) {
				result[count++] = cmd.getNextResult();
			}
		} catch (InterruptedException | IOException e) {
			logger.severe(e.getMessage());
			throw e;
		} finally {
			returnInstance(cmd);
		}

		logger.config("Time : [" + (System.currentTimeMillis() - startMillis) + "ms]");
		return result;
	}

	@Override
	public String getStatus() {
		String ret = "Command line: " + cmdLine + System.getProperty("line.separator");
		if(numCmd.get() < poolSize){
			ret = "Pooled processes: " + ret + numCmd.get() + " / " + poolSize;
		}else{
			ret = "Pooled processes: " +  ret + poolSize + " / " + poolSize;
		}

		return ret + System.getProperty("line.separator");
	}

	@Override
	public void init() {
		init(1);
	}

	public void init(int size) {
		logger.info("init() size = " + size);
		List<StringIOCommand> list = new ArrayList<StringIOCommand>();
		try {
			for(int i = 0; i < size; i++){
				StringIOCommand cmd = getInstance();
				list.add(cmd);
			}
			for(StringIOCommand cmd : list){
				returnInstance(cmd);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int getPooledSize() {
		return numCmd.get();
	}

	class CatchException implements UncaughtExceptionHandler {
		public void uncaughtException(Thread t, Throwable e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private class InputWorker implements Runnable {

		private StringIOCommand cmd;
		private String[] text;

		public InputWorker(StringIOCommand cmd, String[] text) {
			this.cmd = cmd;
			this.text = text;
		}

		@Override
		public void run() {
			for (String t : text) {
				try {
					cmd.put(t);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
