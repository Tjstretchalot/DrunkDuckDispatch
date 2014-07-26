/* DrunkDuckDispatch Copyright (C) 2014  Timothy Moore
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.timothy.ddd;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONValue;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;


public class DDDUtils {
	private static final String[] INDENTS = {
		"",
		"  ",
		"    ",
		"      ",
		"        ",
		"          ",
		"            ",
		"              "
	};
	
	public static void writeJSONPretty(Writer out, Object o) throws IOException {
		if(o instanceof Map) {
			writeJSONPretty(out, (Map<?, ?>) o, 0);
		}else if(o instanceof Collection) {
			writeJSONPretty(out, (Collection<?>) o, 0);
		}else {
			throw new IllegalArgumentException("Really? " + o.getClass().getName());
		}
	}
	
	public static void writeJSONPretty(Writer out, Map<?, ?> map, int indentation) throws IOException {
		out.write('{');
		indentation++;
		boolean first = true;
		Set<?> keys = map.keySet();
		for(Object o : keys) {
			if(!(o instanceof String)) {
				throw new IllegalArgumentException("Invalid key: " + o.getClass().getName());
			}
			
			String str = (String) o;
			if(!first) {
				out.write(',');
			}else {
				first = false;
			}
			out.write('\n');
			writeIndentation(out, indentation);
			write(out, str, indentation);
			out.write(": ");
			Object val = map.get(o);
			write(out, val, indentation);
		}
		indentation--;
		out.write('\n');
		writeIndentation(out, indentation);
		out.write('}');
	}
	
	public static void writeJSONPretty(Writer out, Collection<?> c, int indentation) throws IOException {
		out.write('[');
		indentation++;
		
		boolean first = true;
		for(Object o : c) {
			if(!first) 
				out.write(',');
			else
				first = false;
			out.write('\n');
			writeIndentation(out, indentation);
			write(out, o, indentation);
		}
	
		indentation--;
		out.write('\n');
		writeIndentation(out, indentation);
		out.write(']');
	}
	
	private static void write(Writer out, Object o, int indentation) throws IOException {
		if(o == null) {
			out.write("null");
		}else if(o instanceof Map<?, ?>)
			writeJSONPretty(out, (Map<?, ?>) o, indentation);
		else if(o instanceof Collection<?>)
			writeJSONPretty(out, (Collection<?>) o, indentation);
		else if(o instanceof Number) {
			out.write(o.toString());
		}else if(o instanceof String) {
			out.write("\"");
			out.write(JSONValue.escape((String) o));
			out.write("\"");
		}else if(o instanceof Boolean) {
			out.write(o == Boolean.TRUE ? "true" : "false");
		}else {
			throw new IllegalArgumentException("How do I write " + o.getClass().getName() + "?");
		}
	}
	
	private static void writeIndentation(Writer out, int indent) throws IOException {
		if(indent >= INDENTS.length) {
			while(indent >= INDENTS.length) {
				indent -= INDENTS.length-1;
				out.write(INDENTS[INDENTS.length-1]);
			}
		}
		out.write(INDENTS[indent]);
	}
	
	public static double getAngleRads(double x1, double y1, double x2, double y2) {
	   return Math.atan2(y1-y2, x1-x2);
	}
	


	public static String addNewlines(String text, int maxLen) {
		if(text.length() > maxLen) {
			text = text.replaceAll("\n", " ");
			String[] spl = text.split(" ");

			StringBuilder result = new StringBuilder();
			int lenTotal = 0, len;
			for(int i = 0; i < spl.length; i++) {
				len = spl[i].length();
				if(lenTotal + len >= maxLen) {
					result.append("\n");
					lenTotal = 0;
				}else {
					if(result.length() > 0)
						result.append(" ");
					lenTotal += len;
				}
				result.append(spl[i]);
			}

			return result.toString();
		}
		return text;
	}

	public static String addNewlines(String text, Font f, float width) {
		if(f.getWidth(text) > width) {
			text = text.replaceAll("\n", " ");
			String[] spl = text.split(" ");
			
			StringBuilder result = new StringBuilder();
			StringBuilder thisLine = new StringBuilder();
			for(int i = 0; i < spl.length; i++) {
				if(f.getWidth(thisLine.toString() + " " + spl[i]) > width) {
					result.append('\n').append(spl[i]);
					thisLine.setLength(0);
					thisLine.append(spl[i]);
				}else {
					if(result.length() > 0)
						result.append(' ');
					if(thisLine.length() > 0)
						thisLine.append(' ');
					result.append(spl[i]);
					thisLine.append(spl[i]);
				}
			}
			return result.toString();
		}
		return text;
	}
}
