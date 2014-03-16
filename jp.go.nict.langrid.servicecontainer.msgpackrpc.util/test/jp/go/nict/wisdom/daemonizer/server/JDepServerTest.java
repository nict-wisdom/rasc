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

package jp.go.nict.wisdom.daemonizer.server;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.junit.Test;

public class JDepServerTest extends DaemonizerServerTest {
	private static Logger logger = Logger.getLogger(JDepServerTest.class.getName());

	static {
		target = "jdep";
	}

	@Test
	public void invokeOnce() throws Exception {

		String r = invoke(conf.getInputDirectory() + "/xaa");
		assertTrue(r.indexOf("EOS") >= 0);
	}

	@Test
	public void invokeManyTimes() throws Exception {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(conf.getInputDirectory()))) {
			for (Path path : directoryStream) {
				logger.info(path.toString());
				String r = invoke(path.toAbsolutePath().toString());
				assertTrue(r.indexOf("EOS") >= 0);
				logger.info(r);
			}
		}
	}

	public String invoke(String path) throws Exception {

		File file = new File(path);
		BufferedReader input = new BufferedReader(new FileReader(file));
		
		StringBuffer buf = new StringBuffer();
		String line;
		while ((line = input.readLine()) != null) {
			buf.append(client.analyze(line));
		}
		input.close();

		return buf.toString();
	}
}
