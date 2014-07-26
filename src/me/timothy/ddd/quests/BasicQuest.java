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

import java.util.ArrayList;
import java.util.List;

import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityConversationInfo;
import me.timothy.ddd.entities.EntityConversationInfo.ConversationMessage;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

public class BasicQuest implements Quest {
	private Logger logger;
	protected QuestManager questManager;
	
	public BasicQuest(QuestManager questManager) {
		logger = LogManager.getLogger();
		this.questManager = questManager;
	}
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject asObject() {
		JSONObject result = new JSONObject();
		result.put("class", getClass().getName());
		return result;
	}

	@Override
	public void onApplied(Entity e, int[] choiceTree) {
		logger.printf(Level.INFO, "Quest \"%s\" accepted!", getClass().getSimpleName());
	}

	@Override
	public void onContinued(Entity e, int[] choiceTree) {
		finished();
	}

	@Override
	public void onDeath(Entity e) {}
	
	void finished() {
		questManager.finishedQuest(this);
		logger.printf(Level.INFO, "Quest \"%s\" %s!", getClass().getSimpleName(), questFailed() ? "failed" : "completed");
	}
	
	protected Entity getEntityByDebugName(String debugName) {
		for(int i = 0; i < questManager.getEntityManager().size(); i++) {
			if(questManager.getEntityManager().get(i).getEntityInfo().getMeta().getDebugName().equals(debugName)) {
				return questManager.getEntityManager().get(i);
			}
		}
		return null;
	}
	@Override
	public void onFinished(Quest q) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onItemCollected(String item) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onItemLost(String item) {
		// TODO Auto-generated method stub
		
	}
	
	protected static List<ConversationMessage> createConversationChain(EntityConversationInfo eci, String... msgs) {
		List<ConversationMessage> result = new ArrayList<>(msgs.length);
		for(String str : msgs) {
			ConversationMessage cm = eci.new ConversationMessage();
			cm.setMsg(str);
			result.add(cm);
		}
		return result;
	}
	@Override
	public boolean questFailed() {
		// TODO Auto-generated method stub
		return false;
	}
}
