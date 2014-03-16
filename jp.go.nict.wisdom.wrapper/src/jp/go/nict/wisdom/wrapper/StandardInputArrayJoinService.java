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
import java.util.List;
import java.util.logging.Logger;

import jp.go.nict.wisdom.daemonizer.command.StandardInputCommand;
import jp.go.nict.wisdom.daemonizer.command.StringIOCommand;

public class StandardInputArrayJoinService extends AbstractTextAnalysisService {
	private static Logger logger = Logger.getLogger(StandardInputArrayJoinService.class.getName());

	private String cmdLine;
	private List<String> cmdArray;
	private String delimiterIn;
	private String delimiterOut;
	private String internalDelimiterIn = "";
	private String internalDelimiterOut;
	private boolean delLastNewline = false;
	private boolean includeDelim = false;
	private boolean includeInternalDelim = false;
	private int pollTimeOut = 3000;
	private int poolSize = 20;
	private int initPoolSize = 1;
	private int timeOut = 60000;
	private int startWait = 1000;
	private int restartWait = 1000;
	private int bufSize = 2000000;

	public StandardInputArrayJoinService() {
		logger.info("new StandardInputArrayJoinService()");
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
		if(internalDelimiterOut == null){
			throw new Exception("internalDelimiterOut is null.");
		}

		String str = "";
		final String lineSep = System.getProperty("line.separator");
		internalDelimiterIn = internalDelimiterIn.replaceAll("\\\\n", lineSep);

		for(String s : text){
			if(delLastNewline){
				while(s.endsWith(lineSep)){
					s = s.substring(0, s.length() - lineSep.length());
				}
			}

			if(!s.endsWith(internalDelimiterIn)){
				boolean add = false;
				for(int i = 1; i < internalDelimiterIn.length(); i++){
					if(s.endsWith(internalDelimiterIn.substring(0, i))){
						s += internalDelimiterIn.substring(i, internalDelimiterIn.length());
						add = true;
						break;
					}
				}
				if(!add){
					s += internalDelimiterIn;
				}
			}
			str = str + s;
		}

		String ret = super.analyze(str);

		//処理はすっきりするが速度的に問題あるかも。
		String[] retArray = ret.split(internalDelimiterOut);
		if(retArray.length != text.length){
			throw new Exception("Size of the input and output is different.");
		}

		if(includeInternalDelim){
			for(int i = 0; i < retArray.length; i++){
				retArray[i] = retArray[i] + internalDelimiterOut;
			}
		}


		return retArray;
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

	public String getInternalDelimiterIn() {
		return internalDelimiterIn;
	}

	public void setInternalDelimiterIn(String internalDelimiterIn) {
		this.internalDelimiterIn = internalDelimiterIn;
	}

	public String getInternalDelimiterOut() {
		return internalDelimiterOut;
	}

	public void setInternalDelimiterOut(String internalDelimiterOut) {
		this.internalDelimiterOut = internalDelimiterOut;
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

	public boolean isIncludeInternalDelim() {
		return includeInternalDelim;
	}

	public void setIncludeInternalDelim(boolean includeInternalDelim) {
		this.includeInternalDelim = includeInternalDelim;
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
