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
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * This class manages a process of the target command, keeping it running.<br/>
 * The process starts when start() is invoked, and closes when close() is invoked.
 *
 * @author mtanaka
 */
public abstract class StringIOCommand extends DaemonizedCommand {

	private static Logger logger = Logger.getLogger(StringIOCommand.class.getName());

	private LinkedList<String> results = new LinkedList<String>();

	public StringIOCommand(String[] cmd, String dir, int timeOut, int startWait, int restartwait, int bufSize) {
		super(cmd, dir, timeOut, startWait, restartwait, bufSize);
	}

	public abstract String getNextResult() throws InterruptedException, IOException;

	public void put(String input) throws IOException{
		logger.finest("Received input: " + input);
		super.doPut(input);
	}

	public String doGetNextResult(boolean includeDelim) throws InterruptedException, IOException {
		int indexDelim, useBufLen;
		int delimLen = getDelimiter().length();
		String serchStr;
		StringBuffer buf = new StringBuffer();
		while (results.isEmpty()) {
			String retCmd = super.doGetNextResult();
			if(buf.length() < delimLen){
				serchStr = buf + retCmd;
				useBufLen = buf.length();
			}else{
				useBufLen = delimLen;
				serchStr = buf.substring(buf.length() - delimLen) + retCmd;
			}
			buf.append(retCmd);

			if ((indexDelim = serchStr.indexOf(getDelimiter())) >= 0) {
				int resultLength = buf.length() - retCmd.length();
				if (includeDelim) {
					resultLength += indexDelim - useBufLen + getDelimiter().length();
				} else {
					resultLength += indexDelim - useBufLen;
				}
				String retStr = buf.substring(0, resultLength);
				results.add(retStr);
			}
		}
		String result = results.getFirst();
		results.removeFirst();
		logger.finest("Returning output: " + result);

		return result;
	}

	protected abstract String getDelimiter();
}
