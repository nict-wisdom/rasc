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

package jp.go.nict.isp.wisdom2013.api.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 半角、全角変換用ユーティリティクラス
 * @author kishimoto
 *
 */
public final class ConvertCharCodeUtil {

	/**
	 * 半角、全角変換テーブル
	 */
	private static final Map<String, String> convTable = new HashMap<String, String>() {
		private static final long serialVersionUID = 2786891988415860191L;

		{
			put("'", "’");
			put("-", "－");
			put("ﾞ", "゛");
			put("ﾟ", "゜");
			put("!", "！");
			put("\"", "”");
			put("#", "＃");
			put("$", "＄");
			put("%", "％");
			put("&", "＆");
			put("(", "（");
			put(")", "）");
			put("*", "＊");
			put(",", "，");
			put("､", "、");
			put(".", "．");
			put("｡", "。");
			put("/", "／");
			put(":", "：");
			put(";", "；");
			put("?", "？");
			put("@", "＠");
			put("[", "［");
			put("]", "］");
			put("^", "＾");
			put("_", "＿");
			put("`", "｀");
			put("{", "｛");
			put("|", "｜");
			put("}", "｝");
			put("~", "￣");
			put("｢", "「");
			put("｣", "」");
			put("+", "＋");
			put("<", "＜");
			put("=", "＝");
			put(">", "＞");
			put("･", "・");
			put("ｧ", "ァ");
			put("ｱ", "ア");
			put("ｨ", "ィ");
			put("ｲ", "イ");
			put("ｩ", "ゥ");
			put("ｳ", "ウ");
			put("ｪ", "ェ");
			put("ｴ", "エ");
			put("ｫ", "ォ");
			put("ｵ", "オ");
			put("ｶ", "カ");
			put("ｷ", "キ");
			put("ｸ", "ク");
			put("ｹ", "ケ");
			put("ｺ", "コ");
			put("ｻ", "サ");
			put("ｼ", "シ");
			put("ｽ", "ス");
			put("ｾ", "セ");
			put("ｿ", "ソ");
			put("ﾀ", "タ");
			put("ﾁ", "チ");
			put("ｯ", "ッ");
			put("ﾂ", "ツ");
			put("ﾃ", "テ");
			put("ﾄ", "ト");
			put("ﾅ", "ナ");
			put("ﾆ", "ニ");
			put("ﾇ", "ヌ");
			put("ﾈ", "ネ");
			put("ﾉ", "ノ");
			put("ﾊ", "ハ");
			put("ﾋ", "ヒ");
			put("ﾌ", "フ");
			put("ﾍ", "ヘ");
			put("ﾎ", "ホ");
			put("ﾏ", "マ");
			put("ﾐ", "ミ");
			put("ﾑ", "ム");
			put("ﾒ", "メ");
			put("ﾓ", "モ");
			put("ｬ", "ャ");
			put("ﾔ", "ヤ");
			put("ｭ", "ュ");
			put("ﾕ", "ユ");
			put("ｮ", "ョ");
			put("ﾖ", "ヨ");
			put("ﾗ", "ラ");
			put("ﾘ", "リ");
			put("ﾙ", "ル");
			put("ﾚ", "レ");
			put("ﾛ", "ロ");
			put("ﾜ", "ワ");
			put("ｦ", "ヲ");
			put("ﾝ", "ン");
			put("ｰ", "ー");
		}
	};

	/**
	 * 半濁音変換テーブル
	 */
	private static final Map<String, String> hsndakuTable = new HashMap<String, String>() {
		private static final long serialVersionUID = -4136868463566976144L;

		{
			put("ﾊ", "パ");
			put("ﾋ", "ピ");
			put("ﾌ", "プ");
			put("ﾍ", "ペ");
			put("ﾎ", "ポ");
		}
	};

	/**
	 * 濁音変換テーブル
	 */
	private static final Map<String, String> dakuTable = new HashMap<String, String>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 4332226866453192093L;

		{
			put("ｶ", "ガ");
			put("ｷ", "ギ");
			put("ｸ", "グ");
			put("ｹ", "ゲ");
			put("ｺ", "ゴ");
			put("ｻ", "ザ");
			put("ｼ", "ジ");
			put("ｽ", "ズ");
			put("ｾ", "ゼ");
			put("ｿ", "ゾ");
			put("ﾀ", "ダ");
			put("ﾁ", "ヂ");
			put("ﾂ", "ヅ");
			put("ﾃ", "デ");
			put("ﾄ", "ド");
			put("ﾊ", "バ");
			put("ﾋ", "ビ");
			put("ﾌ", "ブ");
			put("ﾍ", "ベ");
			put("ﾎ", "ボ");
			put("ｳ", "ヴ");
		}
	};

	/**
	 * 半角を全角に変換する
	 * @param src 変換前の文字列
	 * @return 全角に変換された文字列
	 */
	public static final String convHalfToFullString(final String src) {
		final int src_len = src.length();
		final StringBuffer sb = new StringBuffer(src_len);

		for (int i = 0; i < src_len; i++) {
			char c = src.charAt(i);
			final String s = String.valueOf(c);

			if (c >= 'a' && c <= 'z') {
				c = (char) (c - 'a' + 'ａ');
			} else if (c >= 'A' && c <= 'Z') {
				c = (char) (c - 'A' + 'Ａ');
			} else if (c >= '0' && c <= '9') {
				c = (char) (c - '0' + '０');
			} else if (c == '\\') {
				c = '￥';
			} else {
				switch (c) {
				case 'ﾊ':
				case 'ﾋ':
				case 'ﾌ':
				case 'ﾍ':
				case 'ﾎ':
					if ((i + 1) < src_len) {
						if (src.charAt(i + 1) == 'ﾞ') {
							c = dakuTable.get(s).charAt(0);
							i++;
						} else if (src.charAt(i + 1) == 'ﾟ') {
							c = hsndakuTable.get(s).charAt(0);
							i++;
						} else {
							c = convTable.get(s).charAt(0);
						}
					} else {
						c = convTable.get(s).charAt(0);
					}
					break;
				case 'ｶ':
				case 'ｷ':
				case 'ｸ':
				case 'ｹ':
				case 'ｺ':
				case 'ｻ':
				case 'ｼ':
				case 'ｽ':
				case 'ｾ':
				case 'ｿ':
				case 'ﾀ':
				case 'ﾁ':
				case 'ﾂ':
				case 'ﾃ':
				case 'ﾄ':
				case 'ｳ':
					if ((i + 1) < src_len) {
						if (src.charAt(i + 1) == 'ﾞ') {
							c = dakuTable.get(s).charAt(0);
							i++;
						} else {
							c = convTable.get(s).charAt(0);
						}
					} else {
						c = convTable.get(s).charAt(0);
					}
					break;
				default:
					if (convTable.containsKey(s)) {
						c = convTable.get(s).charAt(0);
					}
					break;
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}
}
