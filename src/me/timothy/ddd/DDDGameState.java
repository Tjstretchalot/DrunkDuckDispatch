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

package me.timothy.ddd;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import me.timothy.ddd.acheivements.AchievementManager;
import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityInfo;
import me.timothy.ddd.entities.EntityManager;
import me.timothy.ddd.entities.EntityMeta;
import me.timothy.ddd.entities.EntityPosition;
import me.timothy.ddd.entities.Player;
import me.timothy.ddd.map.GameMap;
import me.timothy.ddd.quests.QuestManager;
import me.timothy.ddd.resources.Resources;
import me.timothy.ddd.scaling.SizeScaleSystem;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

public class DDDGameState implements GameState {

	private Logger logger;
	private GameMap gameMap;
	private EntityManager entityManager;
	private QuestManager questManager;
	private AchievementManager achievementManager;
	private Player player;
	private int mapCenterX, mapCenterY;

	public DDDGameState() {
		logger = LogManager.getLogger();
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		for(int i = 0; i < entityManager.size(); i++) {
			if(entityManager.get(i).onClick(player, entityManager, questManager, gameMap, x, y))
				return;
		}

		if(button == 0 && clickCount < 2) 
			return;

		int nGoalX = (int) ((SizeScaleSystem.adjPixelToRealX(x) - SizeScaleSystem.EXPECTED_WIDTH/2) + 
				(mapCenterX-(GameMap.DEF_GRID_WIDTH * GameMap.SPOTS_WIDE)/2)
				+ SizeScaleSystem.EXPECTED_WIDTH / 2);
		int nGoalY = (int) ((SizeScaleSystem.adjPixelToRealY(y) - SizeScaleSystem.EXPECTED_HEIGHT/2) + 
				(mapCenterY-(GameMap.DEF_GRID_HEIGHT * GameMap.SPOTS_TALL)/2)
				+ SizeScaleSystem.EXPECTED_HEIGHT / 2);

		boolean clickedEntity = false;
		for(int i = 0; i < entityManager.size(); i++) {
			if(entityManager.get(i).isBeingHoveredOn()) {
				clickedEntity = true;
			}
		}
		int gridX = nGoalX / GameMap.DEF_GRID_WIDTH;
		int gridY = nGoalY / GameMap.DEF_GRID_HEIGHT;
		if(!clickedEntity) {
			EntityPosition plPos = player.getEntityInfo().getPosition();
			int curGridX = (int) (plPos.getX() / GameMap.DEF_GRID_WIDTH);
			int curGridY = (int) (plPos.getY() / GameMap.DEF_GRID_HEIGHT);

			int dX = curGridX > gridX ? curGridX - gridX : gridX - curGridX;
			int dY = curGridY > gridY ? curGridY - gridY : gridY - curGridY;
			if(dX + dY == 1 && gameMap.canInteractWith(gridX, gridY)) {
				gameMap.interactWith(player, gridX, gridY);
				achievementManager.onInteract(gridX, gridY, gameMap.getGrid()[gridX][gridY]);
				return;
			}
		}else {
			// prefer going to the left, then right, then up, then down
			int bestX = gridX - 1;
			int bestY = gridY;
			int bestVal = bestX < 0 ? Integer.MAX_VALUE : gameMap.getGrid()[bestX][bestY].difficultyPassing();

			int testX = gridX + 1;
			int testY = gridY;
			int testVal = testX >= gameMap.getWidth() ? Integer.MAX_VALUE : gameMap.getGrid()[testX][testY].difficultyPassing();
			if(testVal < bestVal) {
				bestX = testX;
				bestY = testY;
				bestVal = testVal;
			}

			testX = gridX;
			testY = gridY - 1;
			testVal = testY < 0 ? Integer.MAX_VALUE : gameMap.getGrid()[testX][testY].difficultyPassing();
			if(testVal < bestVal) {
				bestX = testX;
				bestY = testY;
				bestVal = testVal;
			}
			testX = gridX;
			testY = gridY + 1;
			testVal = testVal >= gameMap.getHeight() ? Integer.MAX_VALUE : gameMap.getGrid()[testX][testY].difficultyPassing();
			if(testVal < bestVal) {
				bestX = testX;
				bestY = testY;
				bestVal = testVal;
			}

			// + 1 corrects for rounding, since it will be attached to the grid later it will look fine
			nGoalX = bestX * GameMap.DEF_GRID_WIDTH + 1;
			nGoalY = bestY * GameMap.DEF_GRID_HEIGHT + 1;
		}
		// This will be stuck to the grid
		Resources.getRandomAudio().playAsSoundEffect(1, 1, false);
		player.setGoal(nGoalX, nGoalY);
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		mapCenterX -= (int) SizeScaleSystem.adjRealToPixelX((newx-oldx) * 2);
		mapCenterY -= (int) SizeScaleSystem.adjRealToPixelY((newy-oldy) * 2);

		focusMap();
	}

	public void focusMap() {
		EntityPosition plPos = player.getEntityInfo().getPosition();
		int plWidthReal = plPos.getWidth();
		int plHeightReal = plPos.getHeight();

		// if the left side of the map is greater than the left side of the player
		if(mapCenterX - SizeScaleSystem.EXPECTED_WIDTH/2 > plPos.getX()) {
			mapCenterX = (int) (plPos.getX() + SizeScaleSystem.EXPECTED_WIDTH/2);
		}

		// if the right side of the map is less than the right side of the player
		if(mapCenterX + SizeScaleSystem.EXPECTED_WIDTH/2 < plPos.getX() + plWidthReal) {
			mapCenterX = (int) (plPos.getX() + plWidthReal - SizeScaleSystem.EXPECTED_WIDTH/2 - 1);
		}

		if(mapCenterY - SizeScaleSystem.EXPECTED_HEIGHT/2 > plPos.getY()) {
			mapCenterY = (int) (plPos.getY() + SizeScaleSystem.EXPECTED_HEIGHT/2);
		}

		if(mapCenterY + SizeScaleSystem.EXPECTED_HEIGHT/2 < plPos.getY() + plHeightReal) {
			mapCenterY = (int) (plPos.getY() + plHeightReal - SizeScaleSystem.EXPECTED_HEIGHT/2 - 1);
		}

		if(mapCenterX < 640)
			mapCenterX = 640;
		if(mapCenterY < 512)
			mapCenterY = 512;
		
		if(mapCenterX > 3456) 
			mapCenterX = 3456;
		if(mapCenterY > 5632)
			mapCenterY = 5632;
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelMoved(int change) {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputEnded() {
	}

	@Override
	public void inputStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAcceptingInput() {
		return true;
	}

	@Override
	public void setInput(Input input) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(int key, char c) {
		if(key == Keyboard.KEY_SPACE) {
			if(player.canAttack()) {
				for(int i = 0; i < entityManager.size(); i++) {
					Entity e = entityManager.get(i);
					if(e.getEntityInfo().getMeta().isAggressive() && e.isNearPlayer() && e.hadConversation()) {
						e.getEntityInfo().setHealth(e.getEntityInfo().getHealth() - 1);
						e.setShowRed(3);
						if(e.getEntityInfo().getHealth() <= 0) {
							questManager.onDeath(e);
							entityManager.removeEntity(e);
						}
					}
				}
			}
		}
	}

	@Override
	public void keyReleased(int key, char c) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerButtonPressed(int controller, int button) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerButtonReleased(int controller, int button) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerDownPressed(int controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerDownReleased(int controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerLeftPressed(int controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerLeftReleased(int controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerRightPressed(int controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerRightReleased(int controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerUpPressed(int controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void controllerUpReleased(int controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getID() {
		return 0;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		logger.debug("Initializing ddd game state..");
		try {
			File map = new File("map.binary");
			File savedMap = new File("map_saved.binary");
			gameMap = new GameMap(savedMap.exists() ? savedMap : map);
			achievementManager = new AchievementManager();
			entityManager = new EntityManager();
			File defEnts = new File("entities.json");
			File newEnts = new File("entities_saved.json");
			entityManager.loadEntities(!newEnts.exists() ? defEnts : newEnts);

			File playerFile = new File("player.json");
			if(playerFile.exists()) {
				try(FileReader fr = new FileReader(new File("player.json"))) {
					JSONObject jObj = (JSONObject) new JSONParser().parse(fr);
					EntityInfo ei = new EntityInfo();
					ei.loadFrom(jObj);
					player = new Player(ei, achievementManager, gameMap, entityManager);
				}
			}else {
				EntityInfo ei = new EntityInfo();
				ei.setImage("player-still.png");
				ei.getPosition().setX(288);
				ei.getPosition().setY(288);
				ei.getPosition().setWidth(64);
				ei.getPosition().setHeight(64);

				EntityMeta meta = ei.getMeta();
				meta.setHoverMessage("Wonder what I should do now..");
				meta.setDebugName("player");
				meta.setAggressive(false);
				meta.setCollectable(false);

				player = new Player(ei, achievementManager, gameMap, entityManager);
			}

			questManager = new QuestManager(player, achievementManager, entityManager);
			File miscFile = new File("misc.json");
			if(miscFile.exists()) {
				try(FileReader fr = new FileReader(miscFile)) {
					JSONObject jObj = (JSONObject) new JSONParser().parse(fr);
					mapCenterX = ((Number) jObj.get("mapCenterX")).intValue();
					mapCenterY = ((Number) jObj.get("mapCenterY")).intValue();
				}catch(NullPointerException npe) {
					logger.warn("mapCenterX / mapCenterY not loaded correctly!");
					mapCenterX = 640;
					mapCenterY = 512;
				}
			}else {
				mapCenterX = 640;
				mapCenterY = 512;
			}
		} catch (IOException | ParseException e) {
			logger.catching(Level.ERROR, e);
			if(e.getCause() != null)
				logger.catching(Level.ERROR, e.getCause());
			System.exit(1);
		}
		Runnable onExit = new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				if(!DrunkDuckDispatch.ddd.save)
					return;
				try {
					gameMap.saveMap(new File("map_saved.binary"));
				}catch(IOException ie) {

				}

				try(FileWriter fw = new FileWriter(new File("player.json"))) {
					JSONObject jObj = new JSONObject();
					player.getEntityInfo().saveTo(jObj);
					DDDUtils.writeJSONPretty(fw, jObj);
				}catch(IOException e) {
				}

				try(FileWriter fw = new FileWriter(new File("entities_saved.json"))) {
					entityManager.saveEntities(fw);
				}catch(IOException e) {
				}

				try(FileWriter fw = new FileWriter(new File("misc.json"))) {
					JSONObject jObj = new JSONObject();
					jObj.put("mapCenterX", mapCenterX);
					jObj.put("mapCenterY", mapCenterY);
					DDDUtils.writeJSONPretty(fw, jObj);
				}catch(IOException e) {
				}
			}
		};

		Runtime.getRuntime().addShutdownHook(new Thread(onExit));
		logger.debug("Done initializing ddd game state");
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game)
			throws SlickException {
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		gameMap.render(g, mapCenterX, mapCenterY);

		int renderLater = -1;
		for(int i = 0; i < entityManager.size(); i++) {
			if(entityManager.get(i).isHavingConversation())
				renderLater = i;
			else
				entityManager.get(i).render(g, mapCenterX, mapCenterY);
		}
		player.render(g, mapCenterX, mapCenterY);
		if(renderLater != -1) {
			entityManager.get(renderLater).render(g, mapCenterX, mapCenterY);
		}

		achievementManager.render(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			System.exit(0);
		}
		player.update(gameMap, delta);
		for(int i = 0; i < entityManager.size(); i++) {
			entityManager.get(i).update(delta, player, entityManager, questManager, gameMap);
		}
		achievementManager.update(delta);
		focusMap();
	}

}
