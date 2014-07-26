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

import org.json.simple.JSONObject;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

import me.timothy.ddd.DrunkDuckDispatch;
import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityInfo;
import me.timothy.ddd.entities.JSONCompatible;
import me.timothy.ddd.entities.Player;

public class Quest_BlameMatt extends BasicQuest {
	private boolean stoleJewels;
	private boolean agreedToHelp;
	
	public Quest_BlameMatt(QuestManager questManager) {
		super(questManager);
	}

	@Override
	public void onApplied(Entity e, int[] choiceTree) {
		super.onApplied(e, choiceTree);
		
		e.getEntityInfo().setConversation(e.getEntityInfo().getPotentialConversation("didnt_give_jewels"));
		Entity matt = getEntityByDebugName("listec_captain");
		matt.getEntityInfo().setConversation(matt.getEntityInfo().getPotentialConversation("being_blamed"));
	}

	@Override
	public void onContinued(Entity e, int[] choiceTree) {
		if(stoleJewels) {
			DrunkDuckDispatch.ddd.enterState(4, new FadeOutTransition(), new FadeInTransition());
			finished();
			return;
		}
		if(agreedToHelp)
			return;
		switch(choiceTree[0]) {
		case 0:
			switch(choiceTree[1]) {
			case 0:	
				// Death
				DrunkDuckDispatch.ddd.enterState(4, new FadeOutTransition(), new FadeInTransition());
				finished();
				break;
			case 1:
				handleRequestInfo(e, choiceTree, 2);
				break;
			}
			break;
		case 1:
			handleRequestInfo(e, choiceTree, 1);
			break;
		case 2:
			handleTellHimWhyYourHere(e, choiceTree, 1);
			break;
		}
	}

	private void handleRequestInfo(Entity e, int[] choiceTree, int i) {
		switch(choiceTree[i]) {
		case 0:
			handleWhereAreOtherPieces(e, choiceTree, i + 1);
			break;
		case 1:
			handleTellHimWhyYourHere(e, choiceTree, i);
			break;
		}
	}

	private void handleWhereAreOtherPieces(Entity e, int[] choiceTree, int i) {
		switch(choiceTree[i]) {
		case 0:
			i++;
			switch(choiceTree[i]) {
			case 0:
				handleTellHimWhyYourHere(e, choiceTree, i + 1);
				break;
			case 1:
				handleHelpMatt(e, choiceTree, i + 1);
				break;
			case 2:
				handleHowAreYouBetter(e, choiceTree, i + 1);
				break;
			}
			break;
		case 1:
			handleTellHimWhyYourHere(e, choiceTree, i + 1);
			break;
		}
	}

	private void handleTellHimWhyYourHere(Entity e, int[] choiceTree, int i) {
		switch(choiceTree[i]) {
		case 0:
			handleHelpMatt(e, choiceTree, i+1);
			break;
		case 1:
			handleHowAreYouBetter(e, choiceTree, i+1);
			break;
		}
	}

	private void handleHowAreYouBetter(Entity e, int[] choiceTree, int i) {
		switch(choiceTree[i]) {
		case 0:
			handleHelpMatt(e, choiceTree, i+1);
			break;
		case 1:
			stoleJewels = true;
			e.getEntityInfo().setConversation(e.getEntityInfo().getPotentialConversation("poststolenjewels"));
			Player pl = questManager.getPlayer();
			EntityInfo ei = pl.getEntityInfo();
			ei.getInventory().addItem("jewels");
			ei.getInventoryLost().removeItem("jewels");
			
			Entity gant = getEntityByDebugName("arrowgant_captain");
			gant.getEntityInfo().setConversation(gant.getEntityInfo().getPotentialConversation("stolejewelsforgant"));
			break;
		}
	}

	private void handleHelpMatt(Entity e, int[] choiceTree, int i) {
		agreedToHelp = true;
		e.getEntityInfo().setConversation(e.getEntityInfo().getPotentialConversation("agreedtohelp"));
		
		EntityInfo gantInf = getEntityByDebugName("arrowgant_captain").getEntityInfo();
		gantInf.setConversation(gantInf.getPotentialConversation("helpingmatt"));
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject asObject() {
		JSONObject result = super.asObject();
		result.put("stole_jewels", stoleJewels);
		result.put("agreed_to_help", agreedToHelp);
		return result;
	}

	public static Quest_BlameMatt fromObject(QuestManager qM, JSONObject jObj) {
		Quest_BlameMatt result = new Quest_BlameMatt(qM);
		result.stoleJewels = JSONCompatible.getBoolean(jObj, "stole_jewels");
		result.agreedToHelp = JSONCompatible.getBoolean(jObj, "agreed_to_help");
		return result;
	}
}
