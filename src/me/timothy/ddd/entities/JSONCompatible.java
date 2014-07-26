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

package me.timothy.ddd.entities;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class JSONCompatible {
	public abstract void loadFrom(JSONObject jsonObject);
	public abstract void saveTo(JSONObject jsonObject);
	
	@Override
	public abstract boolean equals(Object o);
	@Override
	public abstract int hashCode();
	
	public static int getInt(JSONObject obj, String key) {
		return ((Number) obj.get(key)).intValue();
	}
	
	public static float getFloat(JSONObject obj, String key) {
		return ((Number) obj.get(key)).floatValue();
	}
	
	public static double getDouble(JSONObject obj, String key) {
		return ((Number) obj.get(key)).doubleValue();
	}
	
	public static String getString(JSONObject obj, String key) {
		return ((String) obj.get(key)) != null ? ((String) obj.get(key)).replace("\\n", "\n") : null;
	}
	
	public static JSONObject getObject(JSONObject obj, String key) {
		return (JSONObject) obj.get(key);
	}
	public static JSONArray getArray(JSONObject obj, String key) {
		return (JSONArray) obj.get(key);
	}
	
	public static boolean getBoolean(JSONObject obj, String key) {
		return obj.get(key) == Boolean.TRUE;
	}
}
