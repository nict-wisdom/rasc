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
	private String directory = null;
	private String cmdLine;
	private List<String> cmdArray;
	private String delimiterIn;
	private String delimiterOut;
	private boolean delLastNewline = false;
	private boolean includeDelim = false;
	private int pollTimeOut = 3000;
	private int poolSize = 10;
	private int initPoolSize = 1;
	private int timeOut = 60000;
	private int startWait = 1000;
	private int restartWait = 1000;
	private int bufSize = 2000000;

	public AbstractTextAnalysisService() {
	}

	protected abstract StringIOCommand getInstance() throws IOException, InterruptedException;

	protected StringIOCommand getInstance(Class<? extends StringIOCommand> cmdClass) throws IOException, InterruptedException {
		logger.info("getInstance(...)");
		if(cmdLine != null && cmdArray != null){
			logger.severe("Both cmdLine and CmdArray is set. Set either one.");
			throw new IOException("Both cmdLine and CmdArray is set. Set either one.");
		}
		String[] exeCmd;
		if(cmdLine != null)
			exeCmd = cmdLine.split(" ");
		else
			exeCmd = cmdArray.toArray(new String[0]);

		StringIOCommand cmd = null;
		if (cmdPool.isEmpty() && numCmd.getAndIncrement() < poolSize) {
			int num = numCmd.get();
			// Create new process
			try {
				Class<?>[] argType = {String[].class, String.class, String.class, String.class, boolean.class, boolean.class, int.class, int.class, int.class, int.class};
				Constructor<? extends StringIOCommand> constructor;
				constructor = cmdClass.getConstructor(argType);
				cmd = constructor.newInstance(exeCmd, directory, delimiterIn, delimiterOut, delLastNewline, includeDelim, timeOut, startWait, restartWait, bufSize);
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
		int size = initPoolSize > poolSize ? 1 : initPoolSize;
		logger.info("init() initSize = " + size);
		init(size);
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

	public String getCmdLine() {
		return cmdLine;
	}

	public void setCmdLine(String cmdLine) {
		this.cmdLine = cmdLine;
	}

	public List<String> getCmdArray() {
		return cmdArray;
	}

	public void setCmdArray(List<String> cmdArray) {
		this.cmdArray = cmdArray;
	}

	public String getDelimiterIn() {
		return delimiterIn;
	}

	public void setDelimiterIn(String delimiterIn) {
		this.delimiterIn = delimiterIn;
	}

	public String getDelimiterOut() {
		return delimiterOut;
	}

	public void setDelimiterOut(String delimiterOut) {
		this.delimiterOut = delimiterOut;
	}

	public boolean isDelLastNewline() {
		return delLastNewline;
	}

	public void setDelLastNewline(boolean delLastNewline) {
		this.delLastNewline = delLastNewline;
	}

	public boolean isIncludeDelim() {
		return includeDelim;
	}

	public void setIncludeDelim(boolean includeDelim) {
		this.includeDelim = includeDelim;
	}

	public int getPollTimeOut() {
		return pollTimeOut;
	}

	public void setPollTimeOut(int pollTimeOut) {
		this.pollTimeOut = pollTimeOut;
	}

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public int getInitPoolSize() {
		return initPoolSize;
	}

	public void setInitPoolSize(int initPoolSize) {
		this.initPoolSize = initPoolSize;
	}

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public int getStartWait() {
		return startWait;
	}

	public void setStartWait(int startWait) {
		this.startWait = startWait;
	}

	public int getRestartWait() {
		return restartWait;
	}

	public void setRestartWait(int restartWait) {
		this.restartWait = restartWait;
	}

	public int getBufSize() {
		return bufSize;
	}

	public void setBufSize(int bufSize) {
		this.bufSize = bufSize;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
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
