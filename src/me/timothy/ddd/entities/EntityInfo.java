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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

public class EntityInfo extends JSONCompatible {
	@SuppressWarnings("unused")
	private Logger logger;
	private int health;
	private String image;
	
	private EntityMeta meta;
	private EntityPosition position;
	private EntityConversationInfo conversation;
	private EntityInventory inventory;
	private EntityInventory inventoryLost;
	
	private Map<String, EntityConversationInfo> potentialConversations;
	
	public EntityInfo() {
		logger = LogManager.getLogger();
		potentialConversations = new HashMap<>();
		meta = new EntityMeta();
		position = new EntityPosition();
		conversation = new EntityConversationInfo();
		inventory = new EntityInventory();
		inventoryLost = new EntityInventory();
	}
	
	@Override
	public void loadFrom(JSONObject jsonObject) {
		Set<?> keys = jsonObject.keySet();
		
		for(Object o : keys) {
			if(!(o instanceof String)) 
				continue;
			String key = (String) o;
			switch(key.toLowerCase()) {
			case "health":
				health = getInt(jsonObject, key);
				break;
			case "image":
				image = getString(jsonObject, key);
				break;
			case "meta":
				meta.loadFrom(getObject(jsonObject, key));
				break;
			case "position":
				position.loadFrom(getObject(jsonObject, key));
				break;
			case "conversation":
				conversation.loadFrom(getObject(jsonObject, key));
				break;
			case "inventory":
				inventory.loadFrom(getObject(jsonObject, key));
				break;
			case "inventory_lost":
				inventoryLost.loadFrom(getObject(jsonObject, key));
				break;
			default:
				EntityConversationInfo eci = new EntityConversationInfo();
				eci.loadFrom(getObject(jsonObject, key));
				potentialConversations.put(key, eci);
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void saveTo(JSONObject jsonObject) {
		JSONObject jObj;
		jsonObject.put("health", health);
		jsonObject.put("image", image);
		if(meta != null) {
			jObj = new JSONObject();
			meta.saveTo(jObj);
			jsonObject.put("meta", jObj);
		}
		
		if(position != null) {
			jObj = new JSONObject();
			position.saveTo(jObj);
			jsonObject.put("position", jObj);
		}
		
		if(conversation != null) {
			jObj = new JSONObject();
			conversation.saveTo(jObj);
			jsonObject.put("conversation", jObj);
		}
		
		if(inventory != null) {
			jObj = new JSONObject();
			inventory.saveTo(jObj);
			jsonObject.put("inventory", jObj);
		}
		
		if(inventoryLost != null) {
			jObj = new JSONObject();
			inventoryLost.saveTo(jObj);
			jsonObject.put("inventory_lost", jObj);
		}
		
		Set<String> keys = potentialConversations.keySet();
		for(String key : keys) {
			jObj = new JSONObject();
			potentialConversations.get(key).saveTo(jObj);
			jsonObject.put(key, jObj);
		}
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public EntityMeta getMeta() {
		return meta;
	}

	public void setMeta(EntityMeta meta) {
		this.meta = meta;
	}

	public EntityPosition getPosition() {
		return position;
	}

	public void setPosition(EntityPosition position) {
		this.position = position;
	}

	public EntityConversationInfo getConversation() {
		return conversation;
	}

	public void setConversation(EntityConversationInfo conversation) {
		this.conversation = conversation;
	}

	public EntityInventory getInventory() {
		return inventory;
	}

	public void setInventory(EntityInventory inventory) {
		this.inventory = inventory;
	}
	
	public EntityInventory getInventoryLost() {
		return inventoryLost;
	}

	public void setInventoryLost(EntityInventory inventoryLost) {
		this.inventoryLost = inventoryLost;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((conversation == null) ? 0 : conversation.hashCode());
		result = prime * result + health;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result
				+ ((inventory == null) ? 0 : inventory.hashCode());
		result = prime * result + ((meta == null) ? 0 : meta.hashCode());
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
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
		EntityInfo other = (EntityInfo) obj;
		if (conversation == null) {
			if (other.conversation != null)
				return false;
		} else if (!conversation.equals(other.conversation))
			return false;
		if (health != other.health)
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (inventory == null) {
			if (other.inventory != null)
				return false;
		} else if (!inventory.equals(other.inventory))
			return false;
		if (meta == null) {
			if (other.meta != null)
				return false;
		} else if (!meta.equals(other.meta))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName()).append(" {\n\t");
		builder.append("health: ").append(health).append("\n\t");
		if (image != null)
			builder.append("image: ").append(image).append("\n\t");
		if (meta != null)
			builder.append("meta: ").append(meta).append("\n\t");
		if (position != null)
			builder.append("position: ").append(position).append("\n\t");
		if (conversation != null)
			builder.append("conversation: ").append(conversation)
					.append("\n\t");
		if (inventory != null)
			builder.append("inventory: ").append(inventory).append("\n\t");
		if (inventoryLost != null)
			builder.append("inventoryLost: ").append(inventoryLost);
		builder.append("\n}");
		return builder.toString();
	}

	public EntityConversationInfo getPotentialConversation(String str) {
		return potentialConversations.get(str);
	}
	
}
