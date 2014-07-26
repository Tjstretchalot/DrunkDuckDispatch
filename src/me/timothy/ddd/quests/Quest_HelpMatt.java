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

import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

import me.timothy.ddd.DrunkDuckDispatch;
import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityInfo;

public class Quest_HelpMatt extends BasicQuest {

	public Quest_HelpMatt(QuestManager questManager) {
		super(questManager);
	}

	@Override
	public void onApplied(Entity e, int[] choiceTree) {
		super.onApplied(e, choiceTree);
		
		e.getEntityInfo().setConversation(e.getEntityInfo().getPotentialConversation("post_stole_set"));
		
		EntityInfo mattInf = getEntityByDebugName("listec_captain").getEntityInfo();
		mattInf.setConversation(mattInf.getPotentialConversation("given_persuasion_set"));
	}

	@Override
	public void onContinued(Entity e, int[] choiceTree) {
		if(e.getEntityInfo().getMeta().getDebugName().equals("listec_captain")) {
			DrunkDuckDispatch.ddd.enterState(2, new FadeOutTransition(), new FadeInTransition());
			finished();
		}else {
			DrunkDuckDispatch.ddd.enterState(4, new FadeOutTransition(), new FadeInTransition());
			finished();
		}
	}

	
}
