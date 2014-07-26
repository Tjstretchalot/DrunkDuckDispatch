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

public class EntityMeta extends JSONCompatible {
	private Logger logger;
	private String name;
	private String debugName;
	private String hoverMessage;
	private String walkMessage;
	private boolean aggressive;
	private boolean collectable;
	private boolean goodWill;
	
	public EntityMeta() {
		logger = LogManager.getLogger();
	}
	
	@Override
	public void loadFrom(JSONObject jsonObject) {
		Set<?> keys = jsonObject.keySet();
		
		for(Object o : keys) {
			if(o instanceof String) {
				String key = (String) o;
				switch(key.toLowerCase()) {
				case "name":
					name = getString(jsonObject, key);
					break;
				case "debug_name":
					debugName = getString(jsonObject, key);
					break;
				case "hover_message":
					hoverMessage = getString(jsonObject, key);
					break;
				case "walk_message":
					walkMessage = getString(jsonObject, key);
					break;
				case "aggressive":
					aggressive = getBoolean(jsonObject, key);
					break;
				case "collectable":
					collectable = getBoolean(jsonObject, key);
					break;
				case "good_will":
					goodWill = getBoolean(jsonObject, key);
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
		jsonObject.put("name", name);
		jsonObject.put("debug_name", debugName);
		jsonObject.put("hover_message", hoverMessage);
		jsonObject.put("walk_message", walkMessage);
		jsonObject.put("aggressive", aggressive);
		jsonObject.put("collectable", collectable);
		jsonObject.put("good_will", goodWill);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDebugName() {
		return debugName;
	}

	public void setDebugName(String debugName) {
		this.debugName = debugName;
	}

	public String getHoverMessage() {
		return hoverMessage;
	}

	public void setHoverMessage(String hoverMessage) {
		this.hoverMessage = hoverMessage;
	}

	public String getWalkMessage() {
		return walkMessage;
	}

	public void setWalkMessage(String walkMessage) {
		this.walkMessage = walkMessage;
	}

	public boolean isAggressive() {
		return aggressive;
	}

	public void setAggressive(boolean aggressive) {
		this.aggressive = aggressive;
	}
	
	public boolean isCollectable() {
		return collectable;
	}
	
	public void setCollectable(boolean collectable) {
		this.collectable = collectable;
	}
	
	public void setGoodWill(boolean goodWill) {
		this.goodWill = goodWill;
	}
	
	public boolean hasGoodWill() {
		return goodWill;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (aggressive ? 1231 : 1237);
		result = prime * result + (collectable ? 1231 : 1237);
		result = prime * result + (goodWill ? 1231 : 1237);
		result = prime * result
				+ ((debugName == null) ? 0 : debugName.hashCode());
		result = prime * result
				+ ((hoverMessage == null) ? 0 : hoverMessage.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((walkMessage == null) ? 0 : walkMessage.hashCode());
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
		EntityMeta other = (EntityMeta) obj;
		if (aggressive != other.aggressive)
			return false;
		if (collectable != other.collectable)
			return false;
		if(goodWill != other.goodWill)
			return false;
		if (debugName == null) {
			if (other.debugName != null)
				return false;
		} else if (!debugName.equals(other.debugName))
			return false;
		if (hoverMessage == null) {
			if (other.hoverMessage != null)
				return false;
		} else if (!hoverMessage.equals(other.hoverMessage))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (walkMessage == null) {
			if (other.walkMessage != null)
				return false;
		} else if (!walkMessage.equals(other.walkMessage))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EntityMeta [");
		if (name != null)
			builder.append("name=").append(name).append(", ");
		if (debugName != null)
			builder.append("debugName=").append(debugName).append(", ");
		if (hoverMessage != null)
			builder.append("hoverMessage=").append(hoverMessage).append(", ");
		if (walkMessage != null)
			builder.append("walkMessage=").append(walkMessage).append(", ");
		builder.append("aggressive=").append(aggressive).append(", ");
		builder.append("goodWill=").append(goodWill).append(", ");
		builder.append("collectable=").append(collectable).append("]");
		return builder.toString();
	}
	
}
