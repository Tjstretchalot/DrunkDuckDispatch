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

package me.timothy.ddd.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import me.timothy.ddd.entities.Entity;
import me.timothy.ddd.entities.EntityManager;
import me.timothy.ddd.entities.Player;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AstarPathfinding {
	public static final class Coordinate {
		public static final Coordinate NON_COORDINATE = new Coordinate();
		public Coordinate(int x2, int y2) {
			this.x = x2;
			this.y = y2;
		}
		public Coordinate() {}
		public int x;
		public int y;
		public float hScore;
		public float gScore;
		public float fScore;
		Coordinate parent;
		@Override
		public String toString() {
			return "Coordinate [x=" + x + ", y=" + y + ", hScore=" + hScore
					+ ", gScore=" + gScore + ", fScore=" + fScore + "]";
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof Coordinate))
				return false;
			
			Coordinate other = (Coordinate) o;
			if(other.x != x)
				return false;
			if(other.y != y)
				return false;
			return true;
		}
		
	}

	private Logger logger;
	private GameMap gameMap;
	private boolean pathSaved;
	private Coordinate start;
	private Coordinate finish;
	private LinkedList<Coordinate> path;
	private Coordinate currCoord;
	private ListIterator<Coordinate> iterator;
	private EntityManager entityManager;
	
	
	public AstarPathfinding(GameMap gMap, EntityManager eManager) {
		logger = LogManager.getLogger();
		gameMap = gMap;
		entityManager = eManager;
		pathSaved = false;
		start = new Coordinate();
		finish = new Coordinate();
		path = new LinkedList<>();
		currCoord = null;
		iterator = null;
	}
	
	public void setPath(int startX, int startY, int endX, int endY) {
		if(startX < 0 || startX >= gameMap.getGrid().length) 
			throw new IllegalArgumentException("startX " + startX + " doesn't make sense");
		if(endX < 0 || endX >= gameMap.getGrid().length)
			throw new IllegalArgumentException("endX " + endX + " doesn't make sense");
		
		if(startY < 0 || startY >= gameMap.getGrid()[0].length) 
			throw new IllegalArgumentException("startY " + startY + " doesn't make sense");
		if(endY < 0 || endY >= gameMap.getGrid()[0].length)
			throw new IllegalArgumentException("endY " + endY + " doesn't make sense");
		start.x = startX;
		start.y = startY;
		finish.x = endX;
		finish.y = endY;
		path.clear();
		currCoord = null;
		iterator = null;
		
		createPath();
		pathSaved = true;
		
		iterator = path.listIterator();
		currCoord = !iterator.hasNext() ? null : iterator.next();
	}
	
	public Coordinate getCurrent() {
		return currCoord;
	}
	
	public Coordinate getNext() {
		return currCoord = !iterator.hasNext() ? null : iterator.next();
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}
	
	public boolean hasPath() {
		return pathSaved;
	}
	
	public void onFinishPath() {
		pathSaved = false;
	}

	public void createPath() {
		GridSpot[][] map = gameMap.getGrid();
		
		if(map[start.x][start.y].difficultyPassing() == Integer.MAX_VALUE) {
			logger.warn("Starting on a wall!");
			return;
		}
		if(map[finish.x][finish.y].difficultyPassing() == Integer.MAX_VALUE) {
			logger.warn("Ending on a wall!");
			return;
		}
			
		List<Coordinate> open = new ArrayList<>();
		List<Coordinate> closed = new ArrayList<>();
		
		open.add(new Coordinate(start.x, start.y));
		open.get(0).hScore = heuristicDistance(start, finish);
		open.get(0).gScore = map[start.x][start.y].difficultyPassing();
		open.get(0).fScore = open.get(0).hScore;
		
		List<Coordinate> neighbors = new ArrayList<>();
		int counter = 0;
		while(open.size() > 0) {
			counter++;
			Coordinate node = open.get(0);
//			logger.info(node.toString());
		
			if(node.x == finish.x && node.y == finish.y) {
				getPath(node);
				return;
			}
			open.remove(node);
			closed.add(node);
			
			neighbors.clear();
			getNeighbors(node, neighbors);
//			logger.printf(Level.INFO, "  Found %d neighbors..", neighbors.size());
			for(Coordinate n : neighbors) {
				Entity e = entityManager.entityAt(n.x, n.y);
				
				float gScore = node.gScore + map[n.x][n.y].difficultyPassing() + (e != null && !(e instanceof Player) ? 100 : 0);
				float hScore = heuristicDistance(n, finish);
				float fScore = gScore + hScore;
//				logger.printf(Level.INFO, "    Checking x=%d, y=%d, gScore=%f, hScore=%f, fScore=%f", n.x, n.y, gScore, hScore, fScore);
				
				if(closed.contains(n) && fScore >= closed.get(closed.indexOf(n)).fScore) {
//					logger.info("      In closed set as " + closed.get(closed.indexOf(n)).fScore);
					continue;
				}
				
				if(!open.contains(n) || fScore < n.fScore) {
//					logger.info("      Added to open set");
					n.parent = node;
					n.gScore = gScore;
					n.hScore = hScore;
					n.fScore = fScore;
					if(!open.contains(n)) {
						sortedInsert(n, open);
					}else {
						open.remove(n);
						sortedInsert(n, open);
					}
				}
			}
		}
		logger.printf(Level.WARN, "Path could not be found from (%d, %d) to (%d, %d) after %d iterations", start.x, start.y, finish.x, finish.y, counter); 
		logger.printf(Level.DEBUG, "%d open: %s", open.size(), open.toString());
		logger.printf(Level.DEBUG, "%d closed: %s", closed.size(), closed.toString());
		path.clear();
	}
	
	private void getNeighbors(Coordinate node, List<Coordinate> neighbors) {
		neighbors.add(new Coordinate(node.x - 1, node.y));
		neighbors.add(new Coordinate(node.x + 1, node.y));
		neighbors.add(new Coordinate(node.x, node.y - 1));
		neighbors.add(new Coordinate(node.x, node.y + 1));
		
		GridSpot[][] map = gameMap.getGrid();
		for(int i = 0; i < neighbors.size(); i++) {
			Coordinate c = neighbors.get(i);
			if(c.x < 0 || c.x >= gameMap.getWidth()|| c.y < 0 || c.y >= gameMap.getHeight()
					|| map[c.x][c.y].difficultyPassing() == Integer.MAX_VALUE) {
				neighbors.remove(c);
				i--;
				continue;
			}
		}
	}

	public void sortedInsert(Coordinate c, List<Coordinate> list) {
		list.add(Coordinate.NON_COORDINATE);
		for(int i = 0; i < list.size() - 1; i++) {
			if(c.fScore < list.get(i).fScore) {
				// bubble the rest forward
				Coordinate prev = list.get(i), tmp;
				for(int j = i+1; j < list.size(); j++) {
					tmp = list.get(j);
					list.set(j, prev);
					prev = tmp;
				}
				list.set(i, c);
				return;
			}
		}
		list.set(list.size()-1, c);
	}

	private void getPath(Coordinate c) {
		if(c == null)
			return;
		do {
			path.add(c);
			c = c.parent;
		}while(c != null && c.parent != null);
		Collections.reverse(path);
	}
	
	private int heuristicDistance(Coordinate st, Coordinate fin) {
		return (fin.x > st.x ? fin.x - st.x : st.x - fin.x) +
				(fin.y > st.y ? fin.y - st.y : st.y - fin.y);
	}
}
