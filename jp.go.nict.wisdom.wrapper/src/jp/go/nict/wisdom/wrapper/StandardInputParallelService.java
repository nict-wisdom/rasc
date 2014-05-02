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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class StandardInputParallelService extends StandardInputService {
	private static Logger logger = Logger.getLogger(StandardInputParallelService.class.getName());

	public StandardInputParallelService() {
		logger.info("new StandardInputParallelService()");
	}

	@Override
	public String[] analyzeArray(String[] text) throws Exception {
		int execSize = text.length;

		ExecutorService exec = Executors.newFixedThreadPool(execSize);
		Future<?>[] ft = new Future<?>[execSize];

		for(int i = 0; i < execSize; i++){
			final String str = text[i];
			ft[i] = exec.submit(new Callable<String>() {
				@Override
				public String call() {
					while(true){
						try {
							String ret = analyze(str);
							return ret;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}

		String[] retText = new String[execSize];
		for(int i = 0; i < execSize; i++){
			retText[i] = (String)ft[i].get();
		}

		return retText;
	}
}
