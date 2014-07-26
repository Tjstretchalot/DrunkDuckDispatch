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

import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityConversationInfo;
import me.timothy.ddd.entities.EntityInfo;
import me.timothy.ddd.entities.EntityMeta;

public class Quest_LearnAboutMatt extends BasicQuest {

	public Quest_LearnAboutMatt(QuestManager questManager) {
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
		EntityInfo ei = e.getEntityInfo();
		EntityMeta meta = ei.getMeta();
		EntityConversationInfo conversation = ei.getConversation();
		
		switch(choiceTree[0]) {
		case 0: case 2:
			meta.setHoverMessage("Wants something fancy");
			break;
		case 1:
			Quest_MattLikesShinyThings likesShinyThings = new Quest_MattLikesShinyThings(questManager);
			likesShinyThings.onApplied(e, choiceTree);
			questManager.addQuest(likesShinyThings);
			
			meta.setName("Matt Eri Listec");
			meta.setWalkMessage("Have something?");
			
			switch(choiceTree[1]) {
			case 0:
				meta.setHoverMessage("Buys shiny things");
				break;
			case 1:
				meta.setHoverMessage("Entirely too confident");
				break;
			}
			
			conversation.getText().get(0).getChoices().remove(1);
			conversation.setQuest(null);
			finished();
			break;
		}
	}
}
