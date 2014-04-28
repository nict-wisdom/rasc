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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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

	final LinkedBlockingQueue<NumberedString> queue = new LinkedBlockingQueue<>();
	for (int i=0; i<text.length; i++) {
	    queue.add(new NumberedString(i, text[i]));
	}

	int poolSize = getPoolSize();
	int execNum = text.length < poolSize ? text.length : poolSize;
	ExecutorService exec = Executors.newFixedThreadPool(execNum);
	Future<?>[] ft = new Future<?>[execNum];
	final String[] retText = new String[text.length];
	for(int i = 0; i < execNum; i++){
	    ft[i] = exec.submit(new Runnable() {
		    @Override
			public void run() {
			while (true) {
			    try {
				NumberedString nstr = queue.poll();
				if (nstr == null) return;
				String ret = analyze(nstr.str);
				retText[nstr.num] = ret;
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			}
		    }
		});
	}

	try {
	    exec.shutdown();
	    if(!exec.awaitTermination(timeOut, TimeUnit.MILLISECONDS)){
		exec.shutdownNow();
	    }
	} catch (InterruptedException e) {
	    logger.warning("Parallel execution didn't finish: " + e);
	    exec.shutdownNow();
	}

	return retText;
    }

    private class NumberedString {
	public int num;
	public String str;

	public NumberedString(int num, String str) {
	    super();
	    this.num = num;
	    this.str = str;
	}
    }
}
