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
import java.util.logging.Logger;

import jp.go.nict.wisdom.daemonizer.command.StringIOCommand;
import jp.go.nict.wisdom.daemonizer.command.SvmClassifyCommand;

public class SvmClassifyService extends AbstractTextAnalysisService {
	private static Logger logger = Logger.getLogger(SvmClassifyService.class.getName());

	private String cmdLine;
	private int pollTimeOut;
	private int poolSize;
	private int timeOut;
	private int startWait;
	private int restartWait;
	private int bufSize;

	public SvmClassifyService() {
		logger.info("new SvmClassifyService()");
	}

	@Override
	protected StringIOCommand getInstance() throws IOException, InterruptedException {
		return super.getInstance(SvmClassifyCommand.class, cmdLine,
				pollTimeOut, poolSize,
				timeOut, startWait, restartWait, bufSize
		);
	}

	public String getCmdLine() {
		return cmdLine;
	}

	public void setCmdLine(String cmdLine) {
		this.cmdLine = cmdLine;
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
