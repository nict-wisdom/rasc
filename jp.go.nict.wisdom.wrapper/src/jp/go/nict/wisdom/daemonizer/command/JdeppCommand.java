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
 * This class is a wrapper for jdep
 *
 * @author mtanaka
 */
public class JdeppCommand extends StringIOCommand{

	private static Logger logger = Logger.getLogger(JdeppCommand.class.getName());
	private static final String lineSep = System.getProperty("line.separator");
	private static final String DELIMITER = "EOS" + System.getProperty("line.separator");

	public JdeppCommand(String[] cmd, String dir, String delimiterIn, String delimiterOut, boolean delLastNewline, boolean includeDelim, int timeOut, int startWait, int restartwait, int bufSize) {
		super(cmd, dir, timeOut, startWait, restartwait, bufSize);
	}

	public void put(String input) throws IOException {
		logger.finest("Received input: " + input);

		while(input.endsWith(lineSep)){
			input = input.substring(0, input.length() - lineSep.length());
		}

		if(input.contains(lineSep)){
			throw new IOException("Delimiter is included in the input data.");
		}
		super.doPut(input + lineSep);
	}

	public String getNextResult() throws InterruptedException, IOException {
		return super.doGetNextResult(true);
	}

	@Override
	protected String getDelimiter() {
		return DELIMITER;
	}
}
