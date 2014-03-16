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


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class TestConfUtil {
	private static Logger logger = Logger.getLogger(TestConfUtil.class.getName());

	private Properties conf;

	public TestConfUtil(String properties) throws IOException {
			FileInputStream fin = new FileInputStream(properties);
			conf = new Properties();
			conf.load(fin);
	}

	public String getInputDirectory() {
		return conf.getProperty("input.directory");
	}
}
