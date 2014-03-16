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

package jp.go.nict.ial.servicecontainer.fluentd.decorator;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import jp.go.nict.langrid.commons.util.CalendarUtil;
import jp.go.nict.langrid.servicecontainer.decorator.Decorator;
import jp.go.nict.langrid.servicecontainer.decorator.DecoratorChain;
import jp.go.nict.langrid.servicecontainer.decorator.Request;

import org.fluentd.logger.FluentLogger;

/**
 * FluentLogger用サービスデコレータの実装クラス.
 * <br>
 * サービスXMLでこのデコレータを指定すると、FluentLoggerサーバへログを記録する.<br>
 * jp.go.nict.langrid.servicecontainer.decorator.Decoratorの実装クラス.<br>
 *
 */
public class FluentLoggerDecorator implements Decorator {
	/**
	 * Tagを設定する.
	 * @param tag 設定する文字列
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * hostを設定する.
	 * @param host 設定するホスト名(IPアドレス)
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * portを設定する.
	 * @param port 設定するポート番号
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * labelを設定する.
	 * @param label 設定するラベル
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * exclusionMethodリストを取得する
	 * @return exclusionMethodリスト
	 */
	public List<String> getExclusionMethod() {
		return exclusionMethod;
	}

	/**
	 * exclusionMethodリストを設定する.
	 * @param exclusionMethod 設定するexclusionMethodリスト
	 */
	public void setExclusionMethod(List<String> exclusionMethod) {
		this.exclusionMethod = exclusionMethod;
	}

	/* (非 Javadoc)
	 * @see jp.go.nict.langrid.servicecontainer.decorator.Decorator#doDecorate(jp.go.nict.langrid.servicecontainer.decorator.Request, jp.go.nict.langrid.servicecontainer.decorator.DecoratorChain)
	 */
	@Override
	@SuppressWarnings("serial")
	public Object doDecorate(final Request request, DecoratorChain chain)
			throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException {
		long s = System.currentTimeMillis();
		final Calendar c = Calendar.getInstance();
		Object ret = null;
		try {
			ret = chain.next(request);
			return ret;
		} finally {
			if (!exclusionMethod.contains(request.getMethod().getName())) {
				long in = 0;
				long out = 0;
				Object[] args = request.getArgs();
				for (Object o1 : args) {
					if (o1.getClass().isArray()) {
						Object[] obj = (Object[]) o1;
						for (Object o2 : obj) {
							in += o2.toString().length();
						}
					} else {
						in = o1.toString().length();
					}
				}
				if (ret != null) {
					if (ret.getClass().isArray()) {
						Object[] obj = (Object[]) ret;
						for (Object o : obj) {
							out += o.toString().length();
						}
					} else {
						out = ret.toString().length();
					}
				}
				final long d = System.currentTimeMillis() - s;
				final long input = in;
				final long output = out;
				FluentLogger logger = getLogger();
				try {
					logger.log(label, new HashMap<String, Object>() {
						{
							put("serviceId", request.getServiceId());
							put("method", request.getMethod().getName());
							put("start", CalendarUtil.formatToW3CDTF(c));
							put("hostname", InetAddress.getLocalHost().getHostName().split("\\.")[0]);
							put("elapse", d);
							put("input", input);
							put("output", output);
						}
					});
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * FluentLoggerのLoggerオブジェクトを取得する.
	 * @return Loggerオブジェクト
	 */
	private synchronized FluentLogger getLogger() {
		if (logger == null) {
			logger = FluentLogger.getLogger(tag, host, port);
		}
		return logger;
	}

	/**
	 * tag情報
	 */
	private String tag = "logger";
	/**
	 * host情報
	 */
	private String host = "127.0.0.1";
	/**
	 * port番号
	 */
	private int port = 24224;
	/**
	 * label情報
	 */
	private String label = "elapse";
	/**
	 * exclusionMethodリスト
	 */
	private List<String> exclusionMethod = new ArrayList<String>();
	/**
	 * FluentLoggerロガーオブジェクト
	 */
	private static FluentLogger logger;
}
