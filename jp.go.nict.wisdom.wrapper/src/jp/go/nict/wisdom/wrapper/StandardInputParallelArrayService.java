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
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import jp.go.nict.wisdom.daemonizer.command.StandardInputCommand;
import jp.go.nict.wisdom.daemonizer.command.StringIOCommand;

public class StandardInputParallelArrayService extends AbstractTextAnalysisService {
	private static Logger logger = Logger.getLogger(StandardInputParallelArrayService.class.getName());

	public StandardInputParallelArrayService() {
		logger.info("new StandardInputArrayParallelService()");
	}

	@Override
	protected StringIOCommand getInstance() throws IOException, InterruptedException {
		return super.getInstance(StandardInputCommand.class);
	}

	@Override
	public String[] analyzeArray(String[] text) throws Exception {
		int poolSize = getPoolSize();
		int execSize = text.length / poolSize;

		if(text.length % poolSize != 0){
			execSize++;
		}

		int execNum = text.length < poolSize ? text.length : poolSize;
		ExecutorService exec = Executors.newFixedThreadPool(execNum);
		Future<?>[] ft = new Future<?>[execNum];

		for(int i = 0; i < execNum; i++){
			int from = i * execSize;
			int to = from + execSize;
			to = to < text.length ? to : text.length;
			final String[] str = Arrays.copyOfRange(text, from, to);
			ft[i] = exec.submit(new Callable<String[]>() {
				@Override
				public String[] call() {
					while(true){
						try {
							String[] ret = doAnalyzeArray(str);
							return ret;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}

		String[] retText = new String[text.length];
		for(int i = 0; i < execNum; i++){
			String[] ret = (String[])ft[i].get();
			System.arraycopy(ret, 0, retText, i * execSize, ret.length);
		}

		return retText;
	}

	private String[] doAnalyzeArray(String[] text) throws Exception {
		return super.analyzeArray(text);
	}
}
