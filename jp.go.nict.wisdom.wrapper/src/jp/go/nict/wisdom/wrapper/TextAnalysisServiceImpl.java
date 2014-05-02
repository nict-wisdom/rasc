package jp.go.nict.wisdom.wrapper;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Logger;

import jp.go.nict.wisdom.daemonizer.command.Command;
import jp.go.nict.wisdom.daemonizer.command.CommandPool;

public class TextAnalysisServiceImpl extends AnalysisServiceImpl<String, String> {
	
	public TextAnalysisServiceImpl(CommandPool<String, String> cmdPool) {
		super(cmdPool);
	}

	private static Logger logger = Logger.getLogger(TextAnalysisServiceImpl.class.getName());

	public String analyze(String text) throws Exception {
		long startMillis = System.currentTimeMillis();
		Command<String, String> cmd = cmdPool.getInstance();
		String result = null;
		try {
			cmd.put(text);
			result = cmd.getNextResult();
		} catch (InterruptedException | IOException e) {
			logger.severe(e.getMessage());
			throw e;
		} finally {
			cmdPool.returnInstance(cmd);
			logger.config("Time : [" + (System.currentTimeMillis() - startMillis) + "ms]");
		}
		return result;
	}

	public String[] analyzeArray(String[] text) throws Exception {
		long startMillis = System.currentTimeMillis();
		Command<String, String> cmd = cmdPool.getInstance();
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
			cmdPool.returnInstance(cmd);
		}

		logger.config("Time : [" + (System.currentTimeMillis() - startMillis) + "ms]");
		return result;
	}

	private class InputWorker implements Runnable {

		private Command<String, String> cmd;
		private String[] text;

		public InputWorker(Command<String, String> cmd, String[] text) {
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

	class CatchException implements UncaughtExceptionHandler {
		public void uncaughtException(Thread t, Throwable e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
