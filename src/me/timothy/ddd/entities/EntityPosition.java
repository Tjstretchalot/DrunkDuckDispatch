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

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

public class EntityPosition extends JSONCompatible {
	private Logger logger;
	private float x;
	private float y;
	private int width;
	private int height;
	
	public EntityPosition() {
		logger = LogManager.getLogger();
	}
	
	@Override
	public void loadFrom(JSONObject jsonObject) {
		Set<?> keys = jsonObject.keySet();
		
		for(Object o : keys) {
			if(o instanceof String) {
				String key = (String) o;
				switch(key.toLowerCase()) {
				case "x":
					x = getFloat(jsonObject, key);
					break;
				case "y":
					y = getFloat(jsonObject, key);
					break;
				case "width":
					width = getInt(jsonObject, key);
					break;
				case "height":
					height = getInt(jsonObject, key);
					break;
				default:
					logger.warn("Unknown key: " + key);
				}
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void saveTo(JSONObject jsonObject) {
		jsonObject.put("x", x);
		jsonObject.put("y", y);
		jsonObject.put("width", width);
		jsonObject.put("height", height);
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + width;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		EntityPosition other = (EntityPosition) obj;
		if (height != other.height)
			return false;
		if (width != other.width)
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EntityPosition [x=").append(x).append(", y=").append(y)
				.append(", width=").append(width).append(", height=")
				.append(height).append("]");
		return builder.toString();
	}
}
