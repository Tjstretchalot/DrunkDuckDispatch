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

import me.timothy.ddd.resources.Resources;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class CouchEdge implements GridSpot {

	public static final int LEFT_EDGE = 0;
	public static final int CENTER = 1;
	public static final int RIGHT_EDGE = 2;
	public static final int DOWN = 3;
	public static final int UP = 4;
	
	private int type;
	private int dir;
	private Image image;
	
	public CouchEdge(int type, int dir) {
		if(type != LEFT_EDGE && type != CENTER && type != RIGHT_EDGE) {
			throw new IllegalArgumentException("invalid type " + type);
		}
		if(dir != UP && dir != DOWN) {
			throw new IllegalArgumentException("invalid dir " + dir);
		}
		this.type = type;
		this.dir = dir;
		
		String name = null;
		switch(type) {
		case LEFT_EDGE:
			name = "couch-left.png";
			break;
		case CENTER:
			name = "couch-center.png";
			break;
		case RIGHT_EDGE:
			name = "couch-right.png";
			break;
		}
		
		if(dir == DOWN) {
			name = name + "-inverted";
		}
		image = Resources.getImage(name);
	}

	@Override
	public void render(Graphics g, int leftX, int topY, int width, int height) {
		image.draw(leftX, topY, width, height);
	}

	@Override
	public int difficultyPassing() {
		return 10;
	}

	@Override
	public int getId() {
		int result = 0xff;
		if(dir == UP)
			result -= 3;
		result -= type;
		return result;
	}

}
