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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.junit.Test;

public class JDepDaemonizedCommandTest extends DaemonizedCommandTest {

	protected static Logger logger = Logger.getLogger(JDepDaemonizedCommandTest.class.getName());

	static {
		target = "jdep";
	}

	@Test
	public void testRunJdepOnce() throws IOException, InterruptedException, ExecutionException {
		String inputStr = "テスト入力文章";
		cmd.put(inputStr);
		String result = cmd.getNextResult();

		assertTrue(result.indexOf("EOS") >= 0);
	}

	@Test
	public void testRunJdepManyTimes() throws IOException, InterruptedException, ExecutionException {

		final int COUNT = 100;
		String lineSep = System.getProperty("line.separator");

		for (int i = 0; i < COUNT; i++) {
			String inputStr = "テスト入力文章: " + i + "番目";
			cmd.put(inputStr);
		}

		int outputCount = 0;
		do {
			String r = cmd.getNextResult();
			System.out.print(r);
			outputCount++;
		} while (outputCount < COUNT);

		assertEquals(COUNT, outputCount);
	}

	@Test
	public void testRunJdepOnFile() throws IOException, InterruptedException {
		String path = conf.getInputDirectory() + "/xaa";
		String r = run(path);

		assertTrue(r.indexOf("EOS") >= 0);
	}

	@Override
	protected String run(String path) throws IOException, InterruptedException {
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line;
		while ((line = br.readLine()) != null) {
			cmd.put(line);
		}
		br.close();

		return cmd.getNextResult();
	}


}
