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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class DaemonizedCommandTest {

	protected static final String DELIMITER = "EOS" + System.getProperty("line.separator");

	protected static Logger logger = Logger.getLogger(DaemonizedCommandTest.class.getName());
	protected static final String propDir = "prop/";

	protected static TestConfUtil conf;
	protected static String target;

	protected JdeppCommand cmd;


	@BeforeClass
	public static void before() {
	}

	@Before
	public void setUp() throws Exception {

		ConfUtil conf = new ConfUtil(propDir + "/" + target + ".properties");
		cmd = new JdeppCommand(conf.getCommandPath(), 60000, 1000, 1000, 2000000);
		cmd.start();
	}

	@After
	public void tearDown() throws Exception {
		cmd.close();
	}

	protected abstract String run(String path) throws IOException, InterruptedException;
}
