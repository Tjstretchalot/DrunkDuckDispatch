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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.timothy.ddd.DDDUtils;
import me.timothy.ddd.map.GameMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("unchecked")
public class EntityManager {
	private Logger logger;
	private List<Entity> entities;
	
	public EntityManager() {
		logger = LogManager.getLogger();
		entities = new ArrayList<>();
	}
	
	public void loadEntities(File file) throws IOException, ParseException {
		logger.printf(Level.INFO, "Loading entities from %s (exists: %b)", file.getCanonicalPath(), file.exists());
		JSONArray jsonArray;
		try(FileReader fr = new FileReader(file)) {
			JSONParser parser = new JSONParser();
			jsonArray = (JSONArray) parser.parse(fr);
		}
		
		for(int i = 0; i < jsonArray.size(); i++) {
			EntityInfo ei = new EntityInfo();
			ei.loadFrom((JSONObject) jsonArray.get(i));
			logger.trace(ei.toString());
			entities.add(new Entity(ei));
		}
		logger.printf(Level.INFO, "Successfully loaded %d entities", entities.size());
	}
	
	public void saveEntities(FileWriter fw) throws IOException {
		JSONArray jArr = new JSONArray();
		JSONObject jObj;
		
		for(Entity e : entities) {
			EntityInfo ei = e.getEntityInfo();
			jObj = new JSONObject();
			ei.saveTo(jObj);
			jArr.add(jObj);
		}
		DDDUtils.writeJSONPretty(fw, jArr);
	}
	
	public Entity get(int i) {
		return entities.get(i);
	}
	
	public void add(Entity e) {
		entities.add(e);
	}

	public int size() {
		return entities.size();
	}

	public void removeEntity(Entity e) {
		entities.remove(e);
	}

	public Entity entityAt(int x, int y) {
		for(Entity e : entities) {
			if(e.getEntityInfo().getPosition().getX() / GameMap.DEF_GRID_WIDTH == x
					&& e.getEntityInfo().getPosition().getY() / GameMap.DEF_GRID_HEIGHT == y)
				return e;
		}
		return null;
	}
}
