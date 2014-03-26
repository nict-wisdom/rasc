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

public class SentenceExtractionCommandTest {

	@Test
	public void test() throws InterruptedException, IOException {

		SentenceExtractionCommand cmd = new SentenceExtractionCommand(new String[]{"/opt/PERL/perl-5.16.0-IT-TH-PL/bin/perl -I /Users/mtanaka/dev/daemonizer/text_mgr/WISDOM/datapool/WWW2sf/tool/perl -I /Users/mtanaka/perl5/lib/perl5 /Users/mtanaka/dev/wisdom/beta/repos/text_mgr_20130220/WISDOM/datapool/WWW2sf/tool/scripts/extract-sentences-stdin.perl"}, null, null, null, false, false, 60000, 1000, 1000, 2000000);
		cmd.start();

		String html = read("/Users/mtanaka/dev/daemonizer/test_resources/html/html.1352369160.15288");
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

