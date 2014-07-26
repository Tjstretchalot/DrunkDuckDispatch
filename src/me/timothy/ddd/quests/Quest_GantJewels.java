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

import me.timothy.ddd.DrunkDuckDispatch;
import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityConversationInfo;
import me.timothy.ddd.entities.EntityConversationInfo.ConversationMessage;
import me.timothy.ddd.entities.EntityInfo;

import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

public class Quest_GantJewels extends BasicQuest {
	private boolean reallyAccepted;
	private boolean failure;

	public Quest_GantJewels(QuestManager questManager) {
		super(questManager);
	}

	@Override
	public void onApplied(Entity e, int[] choiceTree) {
		super.onApplied(e, choiceTree);

		handleAccept(e, choiceTree);
	}

	private void handleAccept(Entity e, int[] choiceTree) {
		EntityInfo ei = e.getEntityInfo();
		EntityConversationInfo conversation = ei.getConversation();
		List<ConversationMessage> text = conversation.getText();

		switch(choiceTree[0]) {
		case 0:
			if(!reallyAccepted) {
				ConversationMessage initMsg = text.get(0);
				initMsg.setMsg("[Him] Want to help out with the jewels now?");
			}
			break;
		case 1:
			reallyAccepted = true;
			EntityConversationInfo noJewelsYet = ei.getPotentialConversation("nojewelsyet");
			ei.setConversation(noJewelsYet);

			Entity bystander1 = getEntityByDebugName("bystander1");
			EntityConversationInfo lookingForJewels = bystander1.getEntityInfo().getPotentialConversation("where_are_jewels");
			bystander1.getEntityInfo().setConversation(lookingForJewels);

			bystander1.getEntityInfo().getMeta().setHoverMessage("Maybe he knows something?");
			if(questManager.getPlayer().getEntityInfo().getInventory().hasItem("jewels")) {
				onCollectJewels();
			}else if(questManager.getPlayer().getEntityInfo().getInventoryLost().hasItem("jewels")) {
				onLoseJewels();
			}
			break;
		}
	}

	@Override
	public void onContinued(Entity e, int[] choiceTree) {
		if(!reallyAccepted) {
			handleAccept(e, choiceTree);
		}else {
			if(!questManager.getPlayer().getEntityInfo().getInventory().hasItem("jewels")) {
				// lie and blame
				if(getEntityByDebugName("listec_captain").getEntityInfo().getMeta().hasGoodWill()) { 
					questManager.getPlayer().getEntityInfo().getMeta().setHoverMessage("Hope Gant doesn't find out about the jewels..");
					Quest_BlameMatt blameMatt = new Quest_BlameMatt(questManager);
					blameMatt.onApplied(e, choiceTree);
					questManager.addQuest(blameMatt);
					failure = true;
					finished();
				}
			}else if(!questManager.getPlayer().getEntityInfo().getInventoryLost().hasItem("jewels")) {
				questManager.getPlayer().getEntityInfo().getInventory().removeItem("jewels");
				questManager.getPlayer().getEntityInfo().getInventoryLost().addItem("jewels");

				e.getEntityInfo().getMeta().setGoodWill(true);
				questManager.onItemLost("jewels");

				DrunkDuckDispatch.ddd.enterState(1, new FadeOutTransition(), new FadeInTransition());
				finished();
			}
		}
	}

	@Override
	public void onFinished(Quest q) {
		super.onFinished(q);

		if(q instanceof Quest_LearnAboutMatt && questManager.getPlayer().getEntityInfo().getInventory().hasItem("jewels")) {
			Entity gant = getEntityByDebugName("arrowgant_captain");
			gant.getEntityInfo().setConversation(gant.getEntityInfo().getPotentialConversation("acquired_jewels_withmatt"));
		}
	}

	@Override
	public void onItemCollected(String item) {
		if("jewels".equals(item)) {
			onCollectJewels();
		}
	}

	private void onCollectJewels() {
		Entity gant = getEntityByDebugName("arrowgant_captain");
		if(questManager.isFinished(Quest_LearnAboutMatt.class))
			gant.getEntityInfo().setConversation(gant.getEntityInfo().getPotentialConversation("acquired_jewels_withmatt"));
		else
			gant.getEntityInfo().setConversation(gant.getEntityInfo().getPotentialConversation("acquired_jewels_nomatt"));
	}

	@Override
	public void onItemLost(String item) {
		if("jewels".equals(item) && !getEntityByDebugName("arrowgant_captain").getEntityInfo().getMeta().hasGoodWill()) {
			onLoseJewels();
		}
	}

	private void onLoseJewels() {
		Entity gant = getEntityByDebugName("arrowgant_captain");
		gant.getEntityInfo().setConversation(gant.getEntityInfo().getPotentialConversation("gaveaway_jewels_but_havent_talked"));
		failure = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject asObject() {
		JSONObject result = super.asObject();
		result.put("reallyAccepted", reallyAccepted);
		return result;
	}

	@Override
	void finished() {
		Entity bystander1 = getEntityByDebugName("bystander1");
		if(bystander1.getEntityInfo().getMeta().hasGoodWill()) {
			bystander1.getEntityInfo().setConversation(bystander1.getEntityInfo().getPotentialConversation("goodwill1"));
		}else {
			bystander1.getEntityInfo().setConversation(bystander1.getEntityInfo().getPotentialConversation("standard"));
			Quest q = questManager.getQuest(Quest_RichSon.class);
			if(q != null)
				questManager.finishedQuest(q);
		}
		bystander1.getEntityInfo().getMeta().setHoverMessage("");
		super.finished();
	}
	

	@Override
	public boolean questFailed() {
		return failure;
	}

	public static Quest_GantJewels fromObject(QuestManager qM, JSONObject jObj) {
		Quest_GantJewels result = new Quest_GantJewels(qM);
		result.reallyAccepted = jObj.get("reallyAccepted") == Boolean.TRUE;
		return result;
	}
}
