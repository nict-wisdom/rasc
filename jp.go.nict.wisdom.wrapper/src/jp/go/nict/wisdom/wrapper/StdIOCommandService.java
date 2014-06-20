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

import jp.go.nict.wisdom.daemonizer.command.Command;
import jp.go.nict.wisdom.daemonizer.command.CommandPool;
import jp.go.nict.wisdom.daemonizer.command.StandardInputCommand;

public class StdIOCommandService extends AbstractTextAnalysisService {
	private static Logger logger = Logger.getLogger(StdIOCommandService.class.getName());

	public StdIOCommandService() {
		logger.info("new StdIOCommandService()");
	}

	@Override
	public void init() {
		if (cmdPoolMap.containsKey(getCmdLineAsKey())) return;
		
		CommandPool<String, String> cmdPool = new CommandPool<String, String>(
				cmdLine, cmdArray, directory, delimiterIn, delimiterOut,
				delLastNewline, includeDelim, timeOut, startWait, restartWait,
				bufSize, pollTimeOut, poolSize, initPoolSize) {
					public Command<String, String> getInstance() throws IOException, InterruptedException {
					return getInstance(StandardInputCommand.class);
				}
		};
		// 本当はキーはサービス名の方がいい
		cmdPoolMap.put(getCmdLineAsKey(), cmdPool);
		cmdPool.init();
	}
}
