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

import org.junit.Test;

public class JdeppCommandTest {

	@Test
	public void test() throws InterruptedException, IOException {

		JdeppCommand cmd = new JdeppCommand(new String[]{"/Users/mtanaka/dev/wisdom/beta/tools/jdep/bin/jdep"}, null, null, null, false, false, 60000, 1000, 1000, 2000000);
		cmd.start();
		cmd.put("TEST");
		System.out.print(cmd.getNextResult());
		cmd.put("続けて入力");
		System.out.print(cmd.getNextResult());
		cmd.close();
	}

}
