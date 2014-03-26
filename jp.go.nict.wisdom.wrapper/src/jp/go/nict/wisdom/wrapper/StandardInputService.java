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

public class StandardInputService extends AbstractTextAnalysisService {
	private static Logger logger = Logger.getLogger(StandardInputService.class.getName());

	public StandardInputService() {
		logger.info("new StandardInputService()");
	}

	@Override
	protected StringIOCommand getInstance() throws IOException, InterruptedException {
		return super.getInstance(StandardInputCommand.class);
	}
}
