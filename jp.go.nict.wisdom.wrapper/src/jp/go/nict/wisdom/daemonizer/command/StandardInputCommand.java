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
 * This class is a wrapper of sentence extraction
 *
 * @author mtanaka
 */
public class StandardInputCommand extends StringIOCommand{

	private static Logger logger = Logger.getLogger(StandardInputCommand.class.getName());

	private static final String lineSep = System.getProperty("line.separator");
	private String delimiterIn = "";
	private String delimiterOut = "";
	private boolean delLastNewline = false;
	private boolean includeDelim = false;

	public StandardInputCommand(String[] cmd, String dir, String delimiterIn, String delimiterOut, boolean delLastNewline, boolean includeDelim, int timeOut, int startWait, int restartwait, int bufSize) {
		super(cmd, dir, timeOut, startWait, restartwait, bufSize);
		if(delimiterIn != null)
			this.delimiterIn = delimiterIn.replaceAll("\\\\n", lineSep);
		if(delimiterOut != null)
			this.delimiterOut = delimiterOut.replaceAll("\\\\n", lineSep);
		this.delLastNewline = delLastNewline;
		this.includeDelim = includeDelim;
	}

	public void put(String input) throws IOException {
		logger.finest("Received input: " + input);

		if(delLastNewline){
			while(input.endsWith(lineSep)){
				input = input.substring(0, input.length() - lineSep.length());
			}
		}

		if(input.contains(delimiterIn)){
			throw new IOException("Delimiter is included in the input data.");
		}

		super.doPut(input + delimiterIn);
	}


	public String getNextResult() throws InterruptedException, IOException {
		return super.doGetNextResult(includeDelim);
	}

	@Override
	protected String getDelimiter() {
		return delimiterOut;
	}
}
