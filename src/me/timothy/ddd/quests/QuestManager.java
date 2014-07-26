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

package me.timothy.ddd.quests;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import me.timothy.ddd.DrunkDuckDispatch;
import me.timothy.ddd.acheivements.AchievementManager;
import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityManager;
import me.timothy.ddd.entities.Player;
import me.timothy.ddd.map.GameMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class QuestManager {
	private Logger logger;
	private EntityManager entityManager;
	private AchievementManager achievementManager;
	private GameMap gameMap;
	private Player player;
	private List<Quest> acceptedQuests;
	private List<Class<? extends Quest>> completedQuests;
	
	@SuppressWarnings("unchecked")
	public QuestManager(Player player, AchievementManager aManager, EntityManager entManager) {
		logger = LogManager.getLogger();
		entityManager = entManager;
		achievementManager = aManager;
		acceptedQuests = new ArrayList<>();
		completedQuests = new ArrayList<>();
		this.player = player;
		
		File questsFile = new File("quests.json");
		if(questsFile.exists()) {
			try(FileReader fr = new FileReader(new File("quests.json"))) {
				JSONObject jObj = (JSONObject) (new JSONParser().parse(fr));
				
				JSONArray questsArr = (JSONArray) jObj.get("current");
				for(int i = 0; i < questsArr.size(); i++) {
					JSONObject questObj = (JSONObject) questsArr.get(i);
					String classStr = (String) questObj.get("class");
					Class<?> cl = Class.forName(classStr);
					Quest quest = null;
					try {
						quest = (Quest) cl.getMethod("fromObject", getClass(), JSONObject.class).invoke(null, this, questObj);
					}catch(NoSuchMethodException nsme) {
						quest = (Quest) cl.getConstructor(QuestManager.class).newInstance(this);
					}
					acceptedQuests.add(quest); 
				}
				
				JSONArray complete = (JSONArray) jObj.get("complete");
				for(int i = 0; i < complete.size(); i++) {
//					completedQuests.add((Class<? extends Quest>) Class.forName((String) complete.get(i)));
				}
			}catch(Exception e) {
				logger.catching(e);
			}
		}
		
		Runnable saveQuests = new Runnable() {

			@Override
			public void run() {
				if(!DrunkDuckDispatch.ddd.save)
					return;
				JSONObject jObj = new JSONObject();
				JSONArray questsArr = new JSONArray();
				for(Quest qu : acceptedQuests) {
					questsArr.add(qu.asObject());
				}
				jObj.put("current", questsArr);
				JSONArray completed = new JSONArray();
				for(Class<? extends Quest> cl : completedQuests) {
					completed.add(cl.getCanonicalName());
				}
				jObj.put("complete", completed);
				try(FileWriter fw = new FileWriter(new File("quests.json"))) {
					jObj.writeJSONString(fw);
				}catch(IOException ie) {
					
				}
			}
			
		};
		
		Runtime.getRuntime().addShutdownHook(new Thread(saveQuests));
	}
	
	public void convoEnded(Entity entity, String questName, int[] choiceTree) {
		try {
			for(Quest quest : acceptedQuests) {
				if(quest.getClass().getName().equals(questName)) {
					quest.onContinued(entity, choiceTree);
					return;
				}
			}
			Class<?> hopefullyTheQuestsClass = Class.forName(questName);
			Constructor<?> constr = hopefullyTheQuestsClass.getConstructor(QuestManager.class);
			Quest quest = (Quest) constr.newInstance(this);
			acceptedQuests.add(quest);
			quest.onApplied(entity, choiceTree);
		} catch (Exception e) {
			logger.printf(Level.WARN, "convo ended with an invalid quest %s", questName);
			logger.catching(e);
		}
	}
	
	void addQuest(Quest quest) {
		acceptedQuests.add(quest);
	}
	
	EntityManager getEntityManager() {
		return entityManager;
	}
	
	GameMap getGameMap() {
		return gameMap;
	}

	Player getPlayer() {
		return player;
	}
	
	public void onDeath(Entity e) {
		for(Quest quest : acceptedQuests) {
			quest.onDeath(e);
		}
	}

	public void finishedQuest(Quest quest) {
		if(quest == null) {
			logger.error("Failed to remove null quest");
			return;
		}
		if(!acceptedQuests.remove(quest)) {
			logger.error("Failed to remove quest " + quest.getClass().getCanonicalName());
		}
		for(Quest qu : acceptedQuests) {
			qu.onFinished(quest);
		}
		if(quest.questFailed()) {
			achievementManager.onQuestFailed(quest);
		}else {
			achievementManager.onQuestComplete(quest);
		}
		completedQuests.add(quest.getClass());
	}
	
	public boolean isFinished(Class<? extends Quest> cl) {
		return completedQuests.contains(cl);
	}

	public void onItemCollected(String itemName) {
		logger.info("Item collected: " + itemName);
		for(Quest qu : acceptedQuests) {
			qu.onItemCollected(itemName);
		}
	}
	
	public void onItemLost(String item) {
		logger.info("Item lost: " + item);
		for(int i = 0; i < acceptedQuests.size(); i++) {
			acceptedQuests.get(i).onItemLost(item);
		}
	}

	public Quest getQuest(Class<? extends Quest> cl) {
		for(Quest qu : acceptedQuests) {
			if(qu.getClass().equals(cl)) 
				return qu;
		}
		return null;
	}
}
