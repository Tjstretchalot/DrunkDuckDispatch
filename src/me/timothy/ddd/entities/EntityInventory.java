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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class EntityInventory extends JSONCompatible {
	private Logger logger;
	private List<String> items;
	
	public EntityInventory() {
		logger = LogManager.getLogger();
		items = new ArrayList<String>();
	}
	@Override
	public void loadFrom(JSONObject jsonObject) {
		Set<?> keys = jsonObject.keySet();

		for(Object o : keys) {
			if(o instanceof String) {
				String key = (String) o;
				switch(key.toLowerCase()) {
				case "items":
					JSONArray arr = getArray(jsonObject, key);
					for(Object o2 : arr) {
						if(o2 instanceof String)
							items.add((String) o2);
					}
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
		jsonObject.put("items", items);
	}
	public List<String> getItems() {
		return items;
	}
	public void setItems(List<String> items) {
		this.items = items;
	}

	public void addItem(String name) {
		items.add(name);
	}
	
	public boolean hasItem(String name) {
		return items.contains(name);
	}
	
	public void removeItem(String name) {
		items.remove(name);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
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
		EntityInventory other = (EntityInventory) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		return true;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EntityInventory [");
		if (items != null)
			builder.append("items=").append(items);
		builder.append("]");
		return builder.toString();
	}
}
