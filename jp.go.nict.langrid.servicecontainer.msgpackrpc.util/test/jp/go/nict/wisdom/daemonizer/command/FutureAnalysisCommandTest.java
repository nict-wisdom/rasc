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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

public class FutureAnalysisCommandTest {

	@Test
	public void test() throws InterruptedException, IOException {

		FutureAnalysisCommand cmd = new FutureAnalysisCommand("/opt/PERL/perl-5.16.0-IT-TH-PL/bin/perl -I /Users/mtanaka/dev/ch_scripts/Caus/bin /Users/mtanaka/dev/ch_scripts/Caus/bin/struct-to-feats_daemon.pl -e /Users/mtanaka/dev/ch_scripts/Caus/WISDOM-feat-extr/ei-ns -c /Users/mtanaka/dev/ch_scripts/Caus/WISDOM-feat-extr/jpe-wc", 60000, 1000, 1000, 2000000);
		cmd.start();
		String html = read("/Users/mtanaka/dev/ch_scripts/Caus/WISDOM-feat-extr/dir-structs/0000018274f29edc7d90345791385af669e25d53_1356884728.struct.xml");
		cmd.put(html);
		System.out.println(cmd.getNextResult());

		cmd.close();
	}

	private String read(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));

		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null){
			stringBuffer.append(line).append("\n");
		}
		br.close();
		return stringBuffer.toString();
	}
}

