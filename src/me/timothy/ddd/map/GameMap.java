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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.timothy.ddd.entities.Player;
import me.timothy.ddd.map.EdgedPiece.Edge;
import me.timothy.ddd.scaling.SizeScaleSystem;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newdawn.slick.Graphics;

public class GameMap {
	public static final int DEF_GRID_WIDTH = 128;
	public static final int DEF_GRID_HEIGHT = 128;
	
	public static final int SPOTS_WIDE = (int) (SizeScaleSystem.EXPECTED_WIDTH / DEF_GRID_WIDTH);
	public static final int SPOTS_TALL = (int) (SizeScaleSystem.EXPECTED_HEIGHT / DEF_GRID_HEIGHT);
	
	@SuppressWarnings("unchecked")
	public static final Class<? extends GridSpot> GSPOTCLASSES[] = new Class[] {
			Carpet.class, Wall.class, EdgedPiece.class
	};
	
	private Logger logger;
	private GridSpot[][] map;
	
	public GameMap(File file) throws IOException {
		logger = LogManager.getLogger();
		loadMap(file);
	}
	
	public void loadMap(File file) throws IOException {
		logger.printf(Level.INFO, "Loading map from %s...", file.getCanonicalPath());
		try(FileInputStream inStream = new FileInputStream(file)) {
			int width = inStream.read();
			int height = inStream.read();
			while(inStream.read() != 0xAA){}
			map = new GridSpot[width][height];
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					try {
						int id = inStream.read();
						
						if(id < GSPOTCLASSES.length) {
							map[x][y] = GSPOTCLASSES[id].newInstance();
						}else {
							/*
							 * Starts counting from the top down
							 */
							switch(id) {
							case 0xff:
								// couch left edge facing down
								map[x][y] = new CouchEdge(CouchEdge.LEFT_EDGE, CouchEdge.DOWN);
								break;
							case 0xfe:
								// couch center facing down
								map[x][y] = new CouchEdge(CouchEdge.CENTER, CouchEdge.DOWN);
								break;
							case 0xfd:
								// couch right edge facing down
								map[x][y] = new CouchEdge(CouchEdge.RIGHT_EDGE, CouchEdge.DOWN);
								break;
							case 0xfc:
								// couch left edge facing up
								map[x][y] = new CouchEdge(CouchEdge.LEFT_EDGE, CouchEdge.UP);
								break;
							case 0xfb:
								// couch center facing up
								map[x][y] = new CouchEdge(CouchEdge.CENTER, CouchEdge.UP);
								break;
							case 0xfa:
								// couch right edge facing up;
								map[x][y] = new CouchEdge(CouchEdge.RIGHT_EDGE, CouchEdge.UP);
								break;
							case 0xf9:
								map[x][y] = new Chair(Chair.DOWN);
								break;
							case 0xf8:
								map[x][y] = new Chair(Chair.LEFT);
								break;
							case 0xf7:
								map[x][y] = new Chair(Chair.UP);
								break;
							case 0xf6:
								map[x][y] = new Chair(Chair.RIGHT);
								break;
							default:
								logger.printf(Level.WARN, "Invalid id %d at %d, %d", id, x, y);	
							}
						}
					} catch (InstantiationException | IllegalAccessException e) {
						logger.catching(Level.ERROR, e);
						System.exit(1);
					}
				}
			}
		}
		logger.printf(Level.INFO, "Successfully loaded map that is %d x %d", getWidth(), getHeight());
		logger.debug("Connected edged pieces..");
		
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				GridSpot spot = map[x][y];
				if(spot instanceof EdgedPiece) {
					EdgedPiece ep = (EdgedPiece) spot;
					if(x > 0 && map[x - 1][y] instanceof EdgedPiece) {
						ep.setEdge(Edge.LEFT, false);
					}
					if(y > 0 && map[x][y - 1] instanceof EdgedPiece) {
						ep.setEdge(Edge.TOP, false);
					}
					if(x < getWidth() - 1 && map[x + 1][y] instanceof EdgedPiece) {
						ep.setEdge(Edge.RIGHT, false);
					}
					if(y < getHeight() - 1 && map[x][y + 1] instanceof EdgedPiece) {
						ep.setEdge(Edge.BOTTOM, false);
					}
				}
			}
		}
	}
	
	public void saveMap(File file) throws IOException {
		try(FileOutputStream fos = new FileOutputStream(file)) {
			fos.write((byte) getWidth());
			fos.write((byte) getHeight());
			for(int i = 0; i < 29; i++) {
				fos.write((byte) 0x00);
			}
			fos.write((byte) 0xaa);
			for(int y = 0; y < getHeight(); y++) {
				for(int x = 0; x < getWidth(); x++) {
					fos.write((byte) map[x][y].getId());
				}
			}
		}
	}

	public int getHeight() {
		return map[0].length;
	}

	public int getWidth() {
		return map.length;
	}

	public void render(Graphics g, int centerMapX, int centerMapY) {
		if(centerMapX < (SPOTS_WIDE / 2) * DEF_GRID_WIDTH)
			centerMapX = (SPOTS_WIDE / 2) * DEF_GRID_WIDTH;
		
		if(centerMapY < (SPOTS_TALL / 2) * DEF_GRID_HEIGHT)
			centerMapY = (SPOTS_TALL / 2) * DEF_GRID_HEIGHT;
		
		int realGridWidth = (int) SizeScaleSystem.adjRealToPixelX(DEF_GRID_WIDTH);
		int realGridHeight = (int) SizeScaleSystem.adjRealToPixelY(DEF_GRID_HEIGHT);
		
		int pixelsOnLeftBeforeYSeperator = centerMapX % DEF_GRID_WIDTH;
		
		int pixelsAboveBeforeXSeperator = centerMapY % DEF_GRID_HEIGHT;
		
		boolean extraX = pixelsOnLeftBeforeYSeperator != 0;
		
		boolean extraY = pixelsAboveBeforeXSeperator != 0;
		
		int stXGrid = (centerMapX / DEF_GRID_WIDTH - SPOTS_WIDE / 2);
		int stYGrid = (centerMapY / DEF_GRID_HEIGHT - SPOTS_TALL / 2);
		
		int enXGrid = stXGrid + SPOTS_WIDE;
		int enYGrid = stYGrid + SPOTS_TALL;
		
		if(extraX) {
			enXGrid++;
		}
		if(extraY) {
			enYGrid++;
		}
		
		if(stXGrid < 0) {
			stXGrid = 0;
			enXGrid = SPOTS_WIDE + 1;
		}
		if(stYGrid < 0) {
			stYGrid = 0;
			enYGrid = SPOTS_TALL + 1;
		}
		if(enXGrid >= getWidth()) {
			enXGrid = getWidth() - 1;
			stXGrid = enXGrid - SPOTS_WIDE;
			if(centerMapX / DEF_GRID_WIDTH + SPOTS_WIDE / 2 >= getWidth())
				pixelsOnLeftBeforeYSeperator = DEF_GRID_WIDTH;
			else
				pixelsOnLeftBeforeYSeperator = centerMapX % DEF_GRID_WIDTH;
		}
		if(enYGrid >= getHeight()) {
			enYGrid = getHeight() - 1;
			stYGrid = enYGrid - SPOTS_TALL;
			if(centerMapY / DEF_GRID_HEIGHT + SPOTS_TALL / 2 >= getHeight())
				pixelsAboveBeforeXSeperator = DEF_GRID_HEIGHT;
			else
				pixelsAboveBeforeXSeperator = centerMapY % DEF_GRID_HEIGHT;
		}
		
		int xPixelSt = (int) -SizeScaleSystem.adjRealToPixelX(pixelsOnLeftBeforeYSeperator);
		int yPixelSt = (int) -SizeScaleSystem.adjRealToPixelY(pixelsAboveBeforeXSeperator);
		int xPixel = xPixelSt;
		int yPixel = yPixelSt;
		for(int y = stYGrid; y < enYGrid; y++) {
			for(int x = stXGrid; x < enXGrid; x++) {
				map[x][y].render(g, xPixel, yPixel, realGridWidth, realGridHeight);
				xPixel += realGridWidth;
			}
			yPixel += realGridHeight;
			xPixel = xPixelSt;
		}
	}

	public boolean canInteractWith(int x, int y) {
		return map[x][y] instanceof Interactable;
	}
	
	public void interactWith(Player player, int x, int y) {
		((Interactable) map[x][y]).interact(player);
	}
	public GridSpot[][] getGrid() {
		return map;
	}
	
}
