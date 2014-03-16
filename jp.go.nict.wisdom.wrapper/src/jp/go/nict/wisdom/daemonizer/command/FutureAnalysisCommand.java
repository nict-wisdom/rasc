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

import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class manages a process of the target command, keeping it running.<br/>
 * The process starts when start() is invoked, and closes when close() is invoked.
 *
 * @author mtanaka
 */
public class FutureAnalysisCommand extends StringIOCommand{

	private static Logger logger = Logger.getLogger(FutureAnalysisCommand.class.getName());
	private static final String DELIMITER = "EOS" + System.getProperty("line.separator");

	public FutureAnalysisCommand(String cmd, int timeOut, int startWait, int restartwait, int bufSize) {
		super(cmd, timeOut, startWait, restartwait, bufSize);
	}

	public void put(String input) {
		logger.finest("Received input: " + input);
		super.doPut(input);
	}

	public String getNextResult() throws InterruptedException, IOException {
		return super.doGetNextResult(false);
	}

	@Override
	protected String getDelimiter() {
		return DELIMITER;
	}
}
