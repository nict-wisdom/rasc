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

package jp.go.nict.ial.servicecontainer.handler.websocketjson.test;

import java.util.ArrayList;
import java.util.List;

import jp.go.nict.langrid.servicecontainer.executor.StreamingNotifier;
import jp.go.nict.langrid.servicecontainer.executor.StreamingReceiver;

public class HelloArrayMeasure implements HelloArrayIntf, StreamingNotifier<String>{
	@Override
	public String[] hello(String message) {
		List<String> r = new ArrayList<>();
		for(int i = 0; i < 100; i++){
			String ret = message + "-" + i;
			System.out.println(i + System.nanoTime());
			if(!receiver.receive(ret)){
				r.add(ret);
			}
		}
		return r.toArray(new String[]{});
	}

	@Override
	public void setReceiver(StreamingReceiver<String> receiver) {
		this.receiver = receiver;
	}

	private StreamingReceiver<String> receiver;
}
