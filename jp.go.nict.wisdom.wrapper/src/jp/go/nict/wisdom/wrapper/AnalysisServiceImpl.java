package jp.go.nict.wisdom.wrapper;

import java.util.List;
import java.util.logging.Logger;

import jp.go.nict.wisdom.daemonizer.command.CommandPool;

public class AnalysisServiceImpl<I, O> {
	private static Logger logger = Logger.getLogger(AnalysisServiceImpl.class.getName());

	protected CommandPool<I, O> cmdPool;
	
	protected String cmdLine;
	protected List<String> cmdArray;

	public AnalysisServiceImpl(CommandPool<I, O> cmdPool) {
		logger.info("new AnalysisServiceImpl()");
		
		this.cmdPool = cmdPool;
	}

	public String getStatus() {
		String ret = "Command line: " + cmdLine + System.getProperty("line.separator");
		if(cmdPool.getSize() < cmdPool.getSize()){
			ret = "Pooled processes: " + ret + cmdPool.getSize()  + " / " + cmdPool.getSize();
		}else{
			ret = "Pooled processes: " +  ret + cmdPool.getSize() + " / " + cmdPool.getSize();
		}

		return ret + System.getProperty("line.separator");
	}

	public int getPooledSize() {
		return cmdPool.getSize();
	}
}
