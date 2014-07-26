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

import java.util.List;

import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityConversationInfo;
import me.timothy.ddd.entities.EntityConversationInfo.ConversationMessage;
import me.timothy.ddd.entities.EntityMeta;

import org.json.simple.JSONObject;

public class Quest_LearnAboutGant extends BasicQuest {
	private int talkCounter;
	
	public Quest_LearnAboutGant(QuestManager questManager) {
		super(questManager);
	}

	@Override
	public void onApplied(Entity e, int[] choiceTree) {
		super.onApplied(e, choiceTree);
		
		onTalk(e, choiceTree);
	}
	
	@Override
	public void onContinued(Entity e, int[] choiceTree) {
		onTalk(e, choiceTree);
	}

	private void onTalk(Entity e, int[] choiceTree) {
		EntityMeta meta = e.getEntityInfo().getMeta();
		EntityConversationInfo eci = e.getEntityInfo().getConversation();
		List<ConversationMessage> text = eci.getText();
		switch(choiceTree[0]) {
		case 0:
			// Ignore me.
			meta.setHoverMessage("Rude");
			meta.setWalkMessage("Still waiting..");

			ConversationMessage intro = text.get(0);
			switch(talkCounter) {
			case 0:
				intro.setMsg("[Him] I wouldn't mind a Martini..");
				break;
			case 1:
				intro.setMsg("[Him] I'm beginning to think you are ignoring me");
				break;
			case 2:
				intro.setMsg("[Him] Lighten up, what did I do?");
				break;
			default:
				intro.setMsg("[Him] Fine, no tip for you");
				break;
			}
			talkCounter++;
			break;
		case 1:
			EntityConversationInfo insistent = e.getEntityInfo().getPotentialConversation("insistent");
			e.getEntityInfo().setConversation(insistent);
			meta.setHoverMessage("Captain of the Arrow Gants");
			meta.setWalkMessage("Humph.");
			
			meta.setName("Arrow Gant");
			finished();
			break;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject asObject() {
		JSONObject result = super.asObject();
		result.put("talkCounter", talkCounter);
		return result;
	}
	
	public static Quest_LearnAboutGant fromObject(QuestManager qManager, JSONObject object) {
		Quest_LearnAboutGant result = new Quest_LearnAboutGant(qManager);
		result.talkCounter = ((Number) object.get("talkCounter")).intValue();
		return result;
	}
}
