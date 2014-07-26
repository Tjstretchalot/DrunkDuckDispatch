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

import me.timothy.ddd.entities.Entity;

public interface Quest {
	public JSONObject asObject();
	public void onApplied(Entity e, int[] choiceTree);
	public void onContinued(Entity e, int[] choiceTree);
	public void onDeath(Entity e);
	public void onFinished(Quest q);
	public void onItemCollected(String itemName);
	public void onItemLost(String item);
	
	/** Solely for achievements **/
	public boolean questFailed();
}
