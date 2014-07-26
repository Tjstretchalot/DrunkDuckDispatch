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

import me.timothy.ddd.acheivements.AchievementManager;
import me.timothy.ddd.map.AstarPathfinding;
import me.timothy.ddd.map.GameMap;
import me.timothy.ddd.map.GridSpot;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Player extends Entity {
	private static final int SPEED_MULT = 10;
	private Logger logger;
	private AchievementManager achievementManager;
	private AstarPathfinding pathfinding;
	
	private float xVel;
	private float yVel;
	private int nextAttack;
	private boolean lastDirReal = false;
	
	
	public Player(EntityInfo ei, AchievementManager achManager, GameMap gMap, EntityManager eManager) {
		super(ei);
		logger = LogManager.getLogger();
		achievementManager = achManager;
		pathfinding = new AstarPathfinding(gMap, eManager);
		logger.printf(Level.TRACE, "Player loaded: %s", ei.toString());
		nextAttack = 0;
	}
	
	
	public void update(GameMap map, int delta) {
		
		EntityPosition pos = getEntityInfo().getPosition();
		float x = pos.getX();
		float y = pos.getY();
		if(nextAttack > 0)
			nextAttack -= delta;
		super.checkForHover();
		if(pathfinding.hasPath()) {
			int goalGridX = pathfinding.getCurrent().x;
			int goalGridY = pathfinding.getCurrent().y;
			
			float realX = goalGridX * GameMap.DEF_GRID_WIDTH + GameMap.DEF_GRID_WIDTH/2 - pos.getWidth()/2;
			float realY = goalGridY * GameMap.DEF_GRID_HEIGHT + GameMap.DEF_GRID_HEIGHT/2 - pos.getHeight()/2;
			float dSquared = (realX - x) * (realX - x) + (realY - y) * (realY - y);
			if(pathfinding.hasNext() && dSquared < SPEED_MULT*SPEED_MULT || dSquared < SPEED_MULT*SPEED_MULT/2.) {
				AstarPathfinding.Coordinate next = pathfinding.getNext();
				if(next == null) {
					pathfinding.onFinishPath();
					xVel = (realX - x) / SPEED_MULT;
					yVel = (realY - y) / SPEED_MULT;
				}else {
					goalGridX = next.x;
					goalGridY = next.y;
					realX = goalGridX * GameMap.DEF_GRID_WIDTH + GameMap.DEF_GRID_WIDTH/2 - pos.getWidth()/2;
					realY = goalGridY * GameMap.DEF_GRID_HEIGHT + GameMap.DEF_GRID_HEIGHT/2 - pos.getHeight()/2;
				}
			}else {
				
			}
			
			normVelocityTo(realX, realY);
			xVel *= SPEED_MULT;
			yVel *= SPEED_MULT;
			if(yVel < 0.5f) {
				boolean tmp = false;
				if(xVel < -SPEED_MULT + 1)
					tmp = true;
				else if(xVel > SPEED_MULT - 1)
					tmp = false;
				
				if(tmp != lastDirReal) {
					lastDirReal = tmp;
				}else {
					left = tmp;
				}
			}
			float difficultyMoving = movementDifficulty(map);
			if(difficultyMoving > 1) {
				xVel /= difficultyMoving;
				yVel /= difficultyMoving;
			}
			pos.setX(x + xVel);
			pos.setY(y + yVel);
			if((int) pos.getX() / GameMap.DEF_GRID_WIDTH != (int) x / GameMap.DEF_GRID_WIDTH || (int) pos.getY() / GameMap.DEF_GRID_HEIGHT != (int) y / GameMap.DEF_GRID_HEIGHT) {
				achievementManager.onGoThrough((int) pos.getX() / GameMap.DEF_GRID_WIDTH, (int) pos.getY() / GameMap.DEF_GRID_HEIGHT);
			}
		}
	}
	
	private float movementDifficulty(GameMap map) {
		EntityPosition pos = getEntityInfo().getPosition();
		float x = pos.getX();
		float y = pos.getY();
		int xGridLeft = (int) (x / GameMap.DEF_GRID_WIDTH);
		int yGridTop = (int) (y / GameMap.DEF_GRID_HEIGHT);
		GridSpot[][] mapGrid = map.getGrid();
		int result = mapGrid[xGridLeft][yGridTop].difficultyPassing();
		int totalSpots = 1;
		int cntr = 1;
		while((xGridLeft + cntr) * GameMap.DEF_GRID_WIDTH < x + pos.getWidth()) {
			result += mapGrid[xGridLeft + cntr][yGridTop].difficultyPassing();
			cntr++;
			totalSpots++;
		}
		cntr = 1;
		while((yGridTop + cntr) * GameMap.DEF_GRID_HEIGHT < y + pos.getHeight()) {
			result += mapGrid[xGridLeft][yGridTop + cntr].difficultyPassing();
			cntr++;
			totalSpots++;
		}
		return result / (float) totalSpots;
	}


	private void normVelocityTo(float goalx, float goaly) {
		EntityPosition pos = getEntityInfo().getPosition();
		float x = pos.getX();
		float y = pos.getY();
		if(x - goalx < 1 && goalx - x < 1) {
			xVel = 0;
		}else {
			xVel = goalx - x;
		}
		
		if(y - goaly < 1 && goaly - y < 1) {
			yVel = 0;
		}else {
			yVel = goaly - y;
		}
		
		// normalize
		if(xVel != 0 || yVel != 0) {
			float sumSq = xVel * xVel + yVel * yVel;
			float sumSqRt = (float) Math.sqrt(sumSq);

			xVel /= sumSqRt;
			yVel /= sumSqRt;
		}
	}
	
	public void setGoal(int realx, int realy) {
		EntityPosition pos = getEntityInfo().getPosition();
		float x = pos.getX();
		float y = pos.getY();
		int startGridX = (int) (x / GameMap.DEF_GRID_WIDTH);
		int startGridY = (int) (y / GameMap.DEF_GRID_HEIGHT);
		
		int endGridX = realx / GameMap.DEF_GRID_WIDTH;
		int endGridY = realy / GameMap.DEF_GRID_HEIGHT;
		
		pathfinding.setPath(startGridX, startGridY, endGridX, endGridY);
	}


	public boolean canAttack() {
		if(nextAttack <= 0) {
			nextAttack = 500;
			return true;
		}
		return false;
	}
}
