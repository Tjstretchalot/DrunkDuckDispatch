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

import java.util.Arrays;
import java.util.List;

import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityConversationInfo;
import me.timothy.ddd.entities.EntityConversationInfo.ConversationChoice;
import me.timothy.ddd.entities.EntityConversationInfo.ConversationMessage;
import me.timothy.ddd.entities.EntityMeta;

public class Quest_MattLikesShinyThings extends BasicQuest {
	private boolean failed;
	
	public Quest_MattLikesShinyThings(QuestManager questManager) {
		super(questManager);
	}

	
	@Override
	public void onApplied(Entity e, int[] choiceTree) {
		super.onApplied(e, choiceTree);
		
		if(questManager.getPlayer().getEntityInfo().getInventory().hasItem("jewels"))
			onCollectJewels();
		
	}

	@Override
	public void onContinued(Entity e, int[] choiceTree) {
		if(choiceTree[0] != 2) {
			System.out.println(Arrays.toString(choiceTree));
			return;
		}
		e.getEntityInfo().setConversation(e.getEntityInfo().getPotentialConversation("given_jewels"));
		EntityMeta mattMeta = e.getEntityInfo().getMeta();
		mattMeta.setGoodWill(true);
		
		questManager.getPlayer().getEntityInfo().getInventory().removeItem("jewels");
		questManager.getPlayer().getEntityInfo().getInventoryLost().addItem("jewels");
		questManager.onItemLost("jewels");
		
		finished();
	}

	@Override
	public void onItemCollected(String item) {
		if("jewels".equals(item))
			onCollectJewels();
	}


	private void onCollectJewels() {
		Entity e = getEntityByDebugName("listec_captain");
		EntityConversationInfo conversation = e.getEntityInfo().getConversation();
		
		List<ConversationMessage> text = conversation.getText();
		ConversationMessage initMsg = text.get(0);
		List<ConversationChoice> choices = initMsg.getChoices();
		
		ConversationChoice choice = conversation.new ConversationChoice();
		choice.setEnd(false);
		choice.setJumpTo(text.size());
		System.out.println("Choice @ " + choice.getJumpTo());
		choice.setMsg("I have these\njewels...");
		
		choices.add(choice);
		
		text.addAll(createConversationChain(conversation, 
				"[Him] These are mighty precious... So shiny...",
				"[You] My reward?",
				"[Him] Oh yeah; if you need anything just ask. And be careful about Mr. Gant as he's known to have a short temper"
				));
		text.get(text.size() - 1).setEnd(true);
		conversation.setQuest(getClass().getName());
	}
	
	@Override
	public void onItemLost(String item) {
		if("jewels".equals(item)) {
			Entity matt = getEntityByDebugName("listec_captain");
			EntityMeta meta = matt.getEntityInfo().getMeta();
			if(!meta.hasGoodWill()) 
				onLoseJewels();
		}
	}
	
	private void onLoseJewels() {
		Entity matt = getEntityByDebugName("listec_captain");
		EntityConversationInfo conversation = matt.getEntityInfo().getConversation();
		List<ConversationMessage> messages = conversation.getText();
		
		ConversationMessage initMsg = messages.get(0);
		ConversationChoice thirdChoice = initMsg.getChoices().get(2);
		int jumpTo = thirdChoice.getJumpTo();
		
		while(!messages.get(jumpTo).isEnd()) {
			messages.remove(jumpTo);
		}
		
		conversation.setQuest(null);
		failed = true;
		finished();
	}


	@Override
	public boolean questFailed() {
		return failed;
	}
	
	
}
