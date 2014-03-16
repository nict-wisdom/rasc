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

/**
 * 
 */
package jp.go.nict.ial.mock;

import org.msgpack.annotation.Message;

/**
 * @author kishimoto
 * 
 */
@Message
public class MsgResult {

	public MsgResult() {

	}

	public final DataDummy[] getDataDummy() {
		return dataDummy;
	}
	public final String getMsg() {
		return msg;
	}
	public final long getTime() {
		return time;
	}

	public final void setDataDummy(DataDummy[] dataDummy) {
		this.dataDummy = dataDummy;
	}

	public final void setMsg(String msg) {
		this.msg = msg;
	}

	public final void setTime(long time) {
		this.time = time;
	}

	private DataDummy[] dataDummy;

	private String msg;

	private long time;

}
