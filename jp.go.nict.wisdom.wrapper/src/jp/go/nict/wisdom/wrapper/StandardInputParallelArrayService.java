package jp.go.nict.wisdom.wrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import jp.go.nict.wisdom.daemonizer.command.StandardInputCommand;
import jp.go.nict.wisdom.daemonizer.command.StringIOCommand;

public class StandardInputParallelArrayService extends AbstractTextAnalysisService {
	private static Logger logger = Logger.getLogger(StandardInputParallelArrayService.class.getName());

	private String cmdLine;
	private List<String> cmdArray;
	private String delimiterIn;
	private String delimiterOut;
	private boolean delLastNewline = false;
	private boolean includeDelim = false;
	private int pollTimeOut = 3000;
	private int poolSize = 20;
	private int initPoolSize = 1;
	private int timeOut = 60000;
	private int startWait = 1000;
	private int restartWait = 1000;
	private int bufSize = 2000000;

	public StandardInputParallelArrayService() {
		logger.info("new StandardInputArrayParallelService()");
	}

	@Override
	protected StringIOCommand getInstance() throws IOException, InterruptedException {
		if(cmdLine != null && cmdArray != null){
			logger.severe("Both cmdLine and CmdArray is set. Set either one.");
			throw new IOException("Both cmdLine and CmdArray is set. Set either one.");
		}

		String[] cmd;
		if(cmdLine != null)
			cmd = cmdLine.split(" ");
		else
			cmd = cmdArray.toArray(new String[0]);

		return super.getInstance(StandardInputCommand.class, cmd,
				delimiterIn, delimiterOut,
				delLastNewline, includeDelim,
				pollTimeOut, poolSize,
				timeOut, startWait, restartWait, bufSize
		);
	}

	@Override
	public void init() {
		int size = initPoolSize > poolSize ? 1 : initPoolSize;
		logger.info("init() initSize = " + size);
		super.init(size);
	}

	@Override
	public String[] analyzeArray(String[] text) throws Exception {
		int pooledSize = getPooledSize();
		int initSize = text.length < poolSize ? text.length : poolSize;
		initSize = initSize < pooledSize ? 0 : initSize - pooledSize;

		//実行予定スレッド数より初期化済みスレッドが少ない場合は初期化処理実行
		if(initSize > 0){
			ExecutorService exec = Executors.newFixedThreadPool(initSize);
			Future<?>[] ft = new Future<?>[initSize];
			for(int i = 0; i < initSize; i++){
				ft[i] = exec.submit(new Runnable() {
					@Override
					public void run() {
						try {
							StringIOCommand cmd = getInstance();
							returnInstance(cmd);
						} catch (IOException e) {
							logger.info("Already been initialized.");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
			}
			//初期化待ち
			for(int i = 0; i < initSize; i++){
				ft[i].get();
			}
		}

		int execSize = text.length / poolSize;
		if(text.length % poolSize != 0){
			execSize++;
		}

		int execNum = text.length < poolSize ? text.length : poolSize;
		ExecutorService exec = Executors.newFixedThreadPool(execNum);
		Future<?>[] ft = new Future<?>[execNum];

		//実行
		for(int i = 0; i < execNum; i++){
			int from = i * execSize;
			int to = from + execSize;
			to = to < text.length ? to : text.length;
			final String[] str = Arrays.copyOfRange(text, from, to);
			ft[i] = exec.submit(new Callable<String[]>() {
				@Override
				public String[] call() {
					while(true){
						try {
							String[] ret = doAnalyzeArray(str);
							return ret;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}

		String[] retText = new String[text.length];
		for(int i = 0; i < execNum; i++){
			String[] ret = (String[])ft[i].get();
			System.arraycopy(ret, 0, retText, i * execSize, ret.length);
		}

		return retText;
	}

	private String[] doAnalyzeArray(String[] text) throws Exception {
		return super.analyzeArray(text);
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
}
