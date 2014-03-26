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

import jp.go.nict.wisdom.daemonizer.command.StandardInputCommand;
import jp.go.nict.wisdom.daemonizer.command.StringIOCommand;

public class StandardInputArrayJoinService extends AbstractTextAnalysisService {
	private static Logger logger = Logger.getLogger(StandardInputArrayJoinService.class.getName());

	private String internalDelimiterIn = "";
	private String internalDelimiterOut;
	private boolean includeInternalDelim = false;

	public StandardInputArrayJoinService() {
		logger.info("new StandardInputArrayJoinService()");
	}

	@Override
	protected StringIOCommand getInstance() throws IOException, InterruptedException {
		return super.getInstance(StandardInputCommand.class);
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
			if(isDelLastNewline()){
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

	public boolean isIncludeInternalDelim() {
		return includeInternalDelim;
	}

	public void setIncludeInternalDelim(boolean includeInternalDelim) {
		this.includeInternalDelim = includeInternalDelim;
	}
}
