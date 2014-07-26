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

package me.timothy.ddd.acheivements;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import me.timothy.ddd.entities.JSONCompatible;

public class Achievement extends JSONCompatible {
	private Logger logger;
	private String debugName;
	private String name;
	private String text;
	
	public Achievement() {
		logger = LogManager.getLogger();
	}
	@Override
	public void loadFrom(JSONObject jsonObject) {
		Set<?> keys = jsonObject.keySet();
		
		for(Object o : keys) {
			if(o instanceof String) {
				String key = (String) o;
				switch(key.toLowerCase()) {
				case "debug_name":
					debugName = getString(jsonObject, key);
					break;
				case "name":
					name = getString(jsonObject, key);
					break;
				case "text":
					text = getString(jsonObject, key);
					break;
				default:
					logger.warn("Unknown key: " + key);
					break;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void saveTo(JSONObject jsonObject) {
		jsonObject.put("debug_name", debugName);
		jsonObject.put("name", name);
		jsonObject.put("text", text);
	}
	public String getDebugName() {
		return debugName;
	}
	public void setDebugName(String debugName) {
		this.debugName = debugName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((debugName == null) ? 0 : debugName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Achievement other = (Achievement) obj;
		if (debugName == null) {
			if (other.debugName != null)
				return false;
		} else if (!debugName.equals(other.debugName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Achievement [");
		if (debugName != null)
			builder.append("debugName=").append(debugName);
		if (name != null) {
			if(builder.length() > "Achievement [".length())
				builder.append(", ");
			builder.append("name=").append(name);
		}
		if (text != null) {
			if(builder.length() > "Achievement [".length())
				builder.append(", ");
			builder.append("text=").append(text);
		}
		builder.append("]");
		return builder.toString();
	}

	
}
