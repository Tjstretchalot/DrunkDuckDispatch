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

package me.timothy.ddd.conversation;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import me.timothy.ddd.DDDUtils;
import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityConversationInfo;
import me.timothy.ddd.entities.EntityConversationInfo.ConversationChoice;
import me.timothy.ddd.entities.EntityManager;
import me.timothy.ddd.entities.Player;
import me.timothy.ddd.map.GameMap;
import me.timothy.ddd.quests.Quest;
import me.timothy.ddd.quests.QuestManager;
import me.timothy.ddd.scaling.SizeScaleSystem;

public class Conversation {
	private static final int SPACING_WIDTH = 25;
	private static final Rectangle2D.Float TEXT_AREA;
	private static final Rectangle2D.Float BUTTON_AREA;
	private static final int MAX_BUTTON_WIDTH = 410;
	private static final int MAX_BUTTON_HEIGHT = 100;

	static {
		TEXT_AREA = new Rectangle2D.Float();
		TEXT_AREA.x = 0;
		TEXT_AREA.y = (SizeScaleSystem.EXPECTED_HEIGHT * 2) / 3;
		TEXT_AREA.width = SizeScaleSystem.EXPECTED_WIDTH;
		TEXT_AREA.height = (SizeScaleSystem.EXPECTED_HEIGHT * 2) / 9;

		BUTTON_AREA = new Rectangle2D.Float();
		BUTTON_AREA.x = 0;
		BUTTON_AREA.y = (SizeScaleSystem.EXPECTED_HEIGHT * 8) / 9;
		BUTTON_AREA.width = SizeScaleSystem.EXPECTED_WIDTH;
		BUTTON_AREA.height = (SizeScaleSystem.EXPECTED_HEIGHT * 1) / 9;
	}

	private Logger logger;
	private GameMap gameMap;
	private Entity entity;
	private Player player;
	private EntityManager entityManager;
	private QuestManager questManager;

	private int[] choiceTree;
	private List<EntityConversationInfo.ConversationChoice> currentChoices;
	private List<Rectangle2D.Float> buttonLocations;
	private int index;
	private String text;
	private Color defaultBknd;
	private Color choiceBtnBknd;
	private Color textOnBtnColor;
	private int counter;


	public Conversation(Entity entity, Player player,
			EntityManager entityManager, QuestManager questManager, GameMap gameMap) {
		logger = LogManager.getLogger();
		this.entity = entity;
		this.player = player;
		this.entityManager = entityManager;
		this.questManager = questManager;
		this.gameMap = gameMap;
		buttonLocations = new ArrayList<>();

		defaultBknd = new Color(49, 79, 79);
		choiceBtnBknd = Color.black;
		textOnBtnColor = new Color(255, 250, 250);
		onClick(-1, -1); // its kind of hackey but don't question it
	}

	// Returns if the conversation is still going
	public boolean onClick(int x, int y) {
		EntityConversationInfo convInfo = entity.getEntityInfo().getConversation();
		List<EntityConversationInfo.ConversationMessage> messages = convInfo.getText();
		if(messages.size() <= index) {
			if(convInfo.getQuest() != null) {
				questManager.convoEnded(entity, convInfo.getQuest(), choiceTree);
			}
			text = null;
			return false;
		}

		EntityConversationInfo.ConversationMessage currentMessage = messages.get(index);
		if(currentChoices == null) {
			logger.info("Conversation continued..");
			text = currentMessage.getMsg();
			text = DDDUtils.addNewlines(text, 45);

			currentChoices = currentMessage.getChoices();
			createButtons();

			if(choiceTree == null) {
				choiceTree = new int[messages.size()];
			}
			if(currentChoices == null) {
				if(currentMessage.getJumpTo() != EntityConversationInfo.NO_JUMP_TO) {
					index = currentMessage.getJumpTo();
				}else if(!currentMessage.isEnd())
					index++;
				else
					index = Integer.MAX_VALUE;
			}
		}else {
			int choice = -1;
			for(int i = 0; i < currentChoices.size(); i++) {
				Rectangle2D.Float rect = buttonLocations.get(i);
				if(rect.contains(x, y)) {
					choice = i;
					break;
				}
			}
			if(choice == -1) {
				return true;
			}

			choiceTree[counter++] = choice;
			ConversationChoice choiceObj = currentChoices.get(choice);
			boolean finished = choiceObj.isEnd() || choiceObj.getJumpTo() == EntityConversationInfo.NO_JUMP_TO;
			if(finished) {
				if(convInfo.getQuest() != null) {
					questManager.convoEnded(entity, convInfo.getQuest(), choiceTree);
				}
				text = null;
				return false;
			}

			int newIndex = choiceObj.getJumpTo();
			index = newIndex;
			currentMessage = messages.get(index);

			text = currentMessage.getMsg();
			text = DDDUtils.addNewlines(text, 45);

			currentChoices = currentMessage.getChoices();
			createButtons();
			if(currentChoices == null) {
				if(currentMessage.isEnd()) {
					index = Integer.MAX_VALUE;
				}else {
					index++;
				}
			}
			logger.printf(Level.INFO, "Chose choice %d which jumped up to index %d", choice, newIndex);
		}
		return true;
	}

	private void createButtons() {
		buttonLocations.clear();
		if(currentChoices != null) {
			int bAreaX = (int) SizeScaleSystem.adjRealToPixelX(BUTTON_AREA.x);
			int bAreaY = (int) SizeScaleSystem.adjRealToPixelY(BUTTON_AREA.y);
			int bAreaWidth = (int) SizeScaleSystem.adjRealToPixelX(BUTTON_AREA.width);
			int bAreaHeight = (int) SizeScaleSystem.adjRealToPixelX(BUTTON_AREA.height);

			int spacingWidth = (currentChoices.size() - 1) * SPACING_WIDTH;
			int buttonWidth = (int) ((bAreaWidth - spacingWidth) / currentChoices.size());
			if(buttonWidth > MAX_BUTTON_WIDTH) {
				buttonWidth = MAX_BUTTON_WIDTH;
			}

			int sidePadding = (int) ((bAreaWidth - (buttonWidth * currentChoices.size()) - spacingWidth) / 2);

			int buttonHeight = bAreaHeight;
			if(buttonHeight > MAX_BUTTON_HEIGHT) {
				buttonHeight = MAX_BUTTON_HEIGHT;
			}

			int topPadding = (bAreaHeight - buttonHeight) / 2;

			int x = bAreaX + sidePadding;
			int y = bAreaY + topPadding;

			for(int i = 0; i < currentChoices.size(); i++) {
				buttonLocations.add(new Rectangle2D.Float(x, y, buttonWidth, buttonHeight));
				x += buttonWidth + SPACING_WIDTH;
			}
		}
	}

	public void render(Graphics g) {
		if(text != null) {
			g.setColor(defaultBknd);
			g.fillRect(
					(int) SizeScaleSystem.adjRealToPixelX(TEXT_AREA.x),
					(int) SizeScaleSystem.adjRealToPixelY(TEXT_AREA.y),
					(int) SizeScaleSystem.adjRealToPixelX(TEXT_AREA.width),
					(int) SizeScaleSystem.adjRealToPixelY(TEXT_AREA.height)
					);
			g.fillRect(
					(int) SizeScaleSystem.adjRealToPixelX(BUTTON_AREA.x),
					(int) SizeScaleSystem.adjRealToPixelY(BUTTON_AREA.y) - 1, // correct for rounding
					(int) SizeScaleSystem.adjRealToPixelX(BUTTON_AREA.width),
					(int) SizeScaleSystem.adjRealToPixelY(BUTTON_AREA.height)
					);

			int y = (int) (SizeScaleSystem.adjRealToPixelX(TEXT_AREA.y + TEXT_AREA.height / 2) - g.getFont().getHeight(text) / 2);
			int x = (int) (SizeScaleSystem.adjRealToPixelY(TEXT_AREA.x + TEXT_AREA.width / 2) - g.getFont().getWidth(text) / 2);

			g.setColor(Color.black);
			g.drawString(text, x, y);

			if(currentChoices != null) {
				for(int i = 0; i < currentChoices.size(); i++) {
					Rectangle2D.Float rect = buttonLocations.get(i);
					g.setColor(choiceBtnBknd);
					g.fillRect(rect.x, rect.y, rect.width, rect.height);

					String choiceText = currentChoices.get(i).getMsg();
					if(choiceText == null) {
						logger.warn(entity.getEntityInfo().toString());
					}
					y = (int) (rect.y + rect.height/2 - g.getFont().getHeight(choiceText) / 2);
					x = (int) (rect.x + rect.width/2 - g.getFont().getWidth(choiceText) / 2);

					g.setColor(textOnBtnColor);
					g.drawString(choiceText, x, y);
				}
			}
		}
	}
}
