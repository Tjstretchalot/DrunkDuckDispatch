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

package me.timothy.ddd.entities;

import me.timothy.ddd.conversation.Conversation;
import me.timothy.ddd.map.GameMap;
import me.timothy.ddd.quests.QuestManager;
import me.timothy.ddd.resources.Resources;
import me.timothy.ddd.scaling.SizeScaleSystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class Entity {
	private static final int TEXT_Y_PADDING = 30;
	private EntityInfo entityInfo;
	private Image realImage;
	private Image imageLeft;
	protected boolean left;
	private int lastRenderX;
	private int lastRenderY;
	private boolean beingHovered;
	private boolean nearPlayer;
	private boolean hadConvo;
	
	private Logger logger;
	private Conversation currentConversation;
	protected int drawRed;
	private int hitDelay;
	
	protected Color textColor;
	
	public Entity(EntityInfo info) {
		logger = LogManager.getLogger();
		entityInfo = info;
		
		realImage = Resources.getImage(getEntityInfo().getImage());
		imageLeft = realImage.getFlippedCopy(true, false);
		textColor = new Color(0, 0, 205);
		
		EntityPosition ep = info.getPosition();
		float x = ep.getX();
		float width = ep.getWidth();
		float y = ep.getY();
		float height = ep.getHeight();
		ep.setX(((int) x / GameMap.DEF_GRID_WIDTH) * GameMap.DEF_GRID_WIDTH + GameMap.DEF_GRID_WIDTH/2 - width/2);
		ep.setY(((int) y / GameMap.DEF_GRID_HEIGHT) * GameMap.DEF_GRID_HEIGHT + GameMap.DEF_GRID_HEIGHT/2 - height/2);
	}
	
	public EntityInfo getEntityInfo() {
		return entityInfo;
	}
	
	public void render(Graphics g, int centerx, int centery) {
		EntityPosition pos = getEntityInfo().getPosition();
		float pixelX = (float) SizeScaleSystem.adjRealToPixelX(pos.getX() - centerx + SizeScaleSystem.EXPECTED_WIDTH/2);
		float pixelY = (float) SizeScaleSystem.adjRealToPixelY(pos.getY() - centery + SizeScaleSystem.EXPECTED_HEIGHT/2);
		float pixelWidth = SizeScaleSystem.adjRealToPixelX(pos.getWidth());
		float pixelHeight = SizeScaleSystem.adjRealToPixelY(pos.getHeight());
		String hoverMessage = getEntityInfo().getMeta().getHoverMessage();
		String walkMessage = getEntityInfo().getMeta().getWalkMessage();
		String name = getEntityInfo().getMeta().getName(); 
		Image img = left ? imageLeft : realImage;
		lastRenderX = (int) pixelX;
		lastRenderY = (int) pixelY;
		if(drawRed <= 0) {
			g.drawImage(img, (int) pixelX, (int) pixelY,
					(int) (pixelX + pixelWidth), 
					(int) (pixelY + pixelHeight), 0, 0, img.getWidth(), img.getHeight());
		}else {
			drawRed--;
			g.drawImage(img, (int) pixelX, (int) pixelY,
					(int) (pixelX + pixelWidth), 
					(int) (pixelY + pixelHeight), 0, 0, img.getWidth(), img.getHeight(), Color.red);
		}
		int nMessages = 0;
		nMessages += beingHovered ? hoverMessage != null && !hoverMessage.isEmpty() ? 1 : 0 : 0;
		nMessages += nearPlayer ? walkMessage != null && !walkMessage.isEmpty() ? 1 : 0 : 0;
		nMessages += name != null && !name.isEmpty() ? 1 : 0;
		int textY = lastRenderY - nMessages * TEXT_Y_PADDING;
		
		g.setColor(textColor);
		if(beingHovered) {
			String text = hoverMessage;
			if(text != null && !text.isEmpty()) {
				int textWidth = g.getFont().getWidth(text);

				int textX = (int) (lastRenderX + pixelWidth / 2 - textWidth / 2);
				g.drawString(text, textX, textY);
				textY += TEXT_Y_PADDING;
			}
		}
		
		if(nearPlayer) {
			String text = walkMessage;
			if(text != null && !text.isEmpty()) {
				int textWidth = g.getFont().getWidth(text);

				int textX = (int) (lastRenderX + pixelWidth / 2 - textWidth / 2);
				g.drawString(text, textX, textY);
				textY += TEXT_Y_PADDING;
			}
		}
		
		String text = name;
		if(text != null && !text.isEmpty()) {
			int textWidth = g.getFont().getWidth(text);

			int textX = (int) (lastRenderX + pixelWidth / 2 - textWidth / 2);
			g.drawString(text, textX, textY);
		}
		
		if(currentConversation != null) {
			currentConversation.render(g);
		}
	}

	protected void checkForHover() {
		EntityPosition pos = getEntityInfo().getPosition();
		int mouseX = Mouse.getX();
		int mouseY = (int) (SizeScaleSystem.getRealHeight() - Mouse.getY());
		float pixelWidth = SizeScaleSystem.adjRealToPixelX(pos.getWidth());
		float pixelHeight = SizeScaleSystem.adjRealToPixelY(pos.getHeight());


		if(mouseX > lastRenderX && mouseY > lastRenderY && mouseX < lastRenderX + pixelWidth && mouseY < lastRenderY + pixelHeight) {
			beingHovered = true;
		}else {
			beingHovered = false;
		}
	}

	protected void checkForNearby(Player player) {
		EntityPosition playerPos = player.getEntityInfo().getPosition();
		EntityPosition mPos = getEntityInfo().getPosition();
		float nearestPlayerX = playerPos.getX() < mPos.getX() ? playerPos.getX() + playerPos.getWidth() : playerPos.getX();
		float nearestPlayerY = playerPos.getY() < mPos.getY() ? playerPos.getY() + playerPos.getHeight() : playerPos.getY();
		float myNearestX = playerPos.getX() < mPos.getX() ? mPos.getX() : mPos.getX() + mPos.getWidth();
		float myNearestY = playerPos.getY() < mPos.getY() ? mPos.getY() : mPos.getY() + mPos.getHeight();
		
		float dX = myNearestX - nearestPlayerX;
		float dY = myNearestY - nearestPlayerY;
		
		if(Math.sqrt(dX * dX + dY * dY) < GameMap.DEF_GRID_WIDTH) {
			nearPlayer = true;
		}else {
			nearPlayer = false;
		}
	}
	
	public void update(int delta, Player player, EntityManager entityManager, QuestManager questManager, GameMap gameMap) {
		checkForHover();
		checkForNearby(player);
		
		boolean aggressive = getEntityInfo().getMeta().isAggressive();
		if(isNearPlayer() && aggressive && currentConversation == null) {
			if(!hadConvo) {
				currentConversation = new Conversation(this, player, entityManager, questManager, gameMap);
				logger.debug("Conversation forcible started");
			}else {
				if(player.drawRed <= 0) {
					hitDelay -= delta;
					if(hitDelay <= 0) {
						hitDelay = 480;
						player.drawRed = 10;
					}
				}
			}
		}
	}

	public boolean onClick(Player player, EntityManager entityManager, QuestManager questManager, GameMap gameMap, int x, int y) {
		if(currentConversation != null) {
			if(!currentConversation.onClick(x, y)) {
				logger.debug("Conversation ended");
				hadConvo = true;
				currentConversation = null;
				
				if(getEntityInfo().getMeta().isCollectable()) {
					String debugName = getEntityInfo().getMeta().getDebugName();
					EntityInventory inv = player.getEntityInfo().getInventory();
					inv.addItem(debugName);
					questManager.onItemCollected(debugName);
					entityManager.removeEntity(this);
				}
				return true;
			}
			return true;
		}
		checkForHover();
		checkForNearby(player);
		
		if(beingHovered && isNearPlayer()) {
			boolean aggressive = getEntityInfo().getMeta().isAggressive();
			if(hadConvo && aggressive)
				return false;
			if(entityInfo.getConversation().getText() != null)
				currentConversation = new Conversation(this, player, entityManager, questManager, gameMap);
			logger.debug("Conversation started");
			return true;
		}
		return false;
	}

	public boolean isNearPlayer() {
		return nearPlayer;
	}

	public boolean hadConversation() {
		return hadConvo;
	}

	public void setShowRed(int i) {
		drawRed = i;
	}

	public boolean isBeingHoveredOn() {
		return beingHovered;
	}

	public boolean isHavingConversation() {
		return currentConversation != null;
	}
}
