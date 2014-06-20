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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import jp.go.nict.wisdom.daemonizer.command.CommandPool;
import jp.go.nict.wisdom.wrapper.api.TextAnalysisService;

public abstract class AbstractTextAnalysisService
		extends AbstractAnalysisService
		implements TextAnalysisService {

	private static Logger logger = Logger.getLogger(AbstractTextAnalysisService.class.getName());

	protected static Map<String, CommandPool<String, String>> cmdPoolMap = new HashMap<>();
	
	public AbstractTextAnalysisService() {
		logger.info("new AbstractTextAnalysisService()");
	}

	@Override
	public String analyze(String text) throws Exception {
		TextAnalysisServiceImpl impl = new TextAnalysisServiceImpl(cmdPoolMap.get(getCmdLineAsKey()));
		return impl.analyze(text);
	}

	@Override
	public String[] analyzeArray(String[] text) throws Exception {
		TextAnalysisServiceImpl impl = new TextAnalysisServiceImpl(cmdPoolMap.get(getCmdLineAsKey()));
		 return impl.analyzeArray(text);
	}

	@Override
	public String getStatus() {
		TextAnalysisServiceImpl impl = new TextAnalysisServiceImpl(cmdPoolMap.get(getCmdLineAsKey()));
		return impl.getStatus();
	}
	
	protected String getCmdLineAsKey() {
		if (!cmdLine.isEmpty()) return cmdLine;
		return StringUtils.join(cmdArray, " ");
	}
}
