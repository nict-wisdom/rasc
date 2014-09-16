package jp.go.nict.wisdom.daemonizer.command;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


public abstract class CommandPool<I, O> {

	private static Logger logger = Logger.getLogger(CommandPool.class.getName());

	protected final AtomicInteger numCmd = new AtomicInteger();

	protected BlockingQueue<Command<I, O>> cmdPool = new LinkedBlockingQueue<>();
	protected String cmdLine;
	protected List<String> cmdArray;

	private String directory = null;
	private String delimiterIn;
	private String delimiterOut;
	private boolean useEnvLineSeparator;
	private boolean delLastNewline = false;
	private boolean includeDelim = false;

	protected int timeOut = 60000;
	protected int startWait = 1000;
	protected int restartWait = 1000;
	protected int bufSize = 2000000;
	protected int pollTimeOut = 3000;

	protected int poolSize = 10;
	protected int initPoolSize = 1;

	public CommandPool(String cmdLine, List<String> cmdArray, String directory, 
			String delimiterIn, String delimiterOut, boolean useEnvLineSeparator,
			boolean delLastNewline, boolean includeDelim, int timeOut, int startWait, int restartWait,
			int bufSize, int pollTimeOut, int poolSize, int initPoolSize) {
		logger.info("new CommandPool()");

		this.cmdLine = cmdLine;
		this.cmdArray = cmdArray;
		this.directory = directory;
		this.delimiterIn = delimiterIn;
		this.delimiterOut = delimiterOut;
		this.useEnvLineSeparator = useEnvLineSeparator;
		this.delLastNewline = delLastNewline;
		this.includeDelim = includeDelim;
		this.timeOut = timeOut;
		this.startWait = startWait;
		this.restartWait = restartWait;
		this.bufSize = bufSize;
		this.pollTimeOut = pollTimeOut;
		this.poolSize = poolSize;
		this.initPoolSize = initPoolSize;
	}

	public int getPooledSize() {
		return numCmd.get() > poolSize ? poolSize : numCmd.get();
	}

	public int getMaxPoolSize() {
		return poolSize;
	}

	public int getPoolingSize() {
		return cmdPool.size();
	}

	public abstract Command<I, O> getInstance() throws IOException, InterruptedException;

	public Command<I, O> getInstance(Class<? extends Command<I, O>> cmdClass) throws IOException, InterruptedException {
		logger.info("getInstance(...)");
		if(cmdLine != null && cmdArray != null){
			String msg = "Both cmdLine and cmdArray are set. Set only one of them.";
			logger.severe(msg);
			throw new IOException(msg);
		}
		String[] exeCmd;
		if(cmdLine != null)
			exeCmd = cmdLine.split(" ");
		else
			exeCmd = cmdArray.toArray(new String[0]);

		Command<I, O> cmd = null;
		if (cmdPool.isEmpty() && numCmd.getAndIncrement() < poolSize) {
			int num = numCmd.get();
			// Create new process
			try {
				Class<?>[] argType = {String[].class, String.class, String.class, String.class, 
						boolean.class, boolean.class, boolean.class, int.class, int.class, int.class, int.class};
				Constructor<? extends Command<I, O>> constructor;
				constructor = cmdClass.getConstructor(argType);
				cmd = constructor.newInstance(exeCmd, directory, delimiterIn, delimiterOut, 
						useEnvLineSeparator, delLastNewline,
						includeDelim, timeOut, startWait, restartWait, bufSize);
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

	public void returnInstance(Command<I, O> cmd) {
		cmdPool.add(cmd);
		logger.finest("Retuned a process to pool.");
	}

	public void init() {
		int size = initPoolSize > poolSize ? 1 : initPoolSize;
		logger.info("init() initSize = " + size);
		init(size);
	}

	private void init(int size) {
		logger.info("init() size = " + size);
		List<Command<I, O>> list = new ArrayList<Command<I, O>>();
		try {
			for(int i = 0; i < size; i++){
				Command<I, O> cmd = getInstance();
				list.add(cmd);
			}
			for(Command<I, O> cmd : list){
				returnInstance(cmd);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
