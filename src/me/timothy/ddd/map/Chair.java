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

import java.util.logging.Logger;

import me.timothy.ddd.entities.Player;
import me.timothy.ddd.resources.Resources;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class Chair implements GridSpot, Interactable {
	public static final int DOWN = 0;
	public static final int LEFT = 1;
	public static final int UP = 2;
	public static final int RIGHT = 3;
	
	private int direction;
	
	private Image image;

	public Chair(int dir) {
		this.direction = dir;
		image = Resources.getImage("chair.png");
	}
	
	@Override
	public void render(Graphics g, int leftX, int topY, int width, int height) {
		g.setColor(Carpet.color);
		g.fillRect(leftX, topY, width, height);
		image.setCenterOfRotation(width/2f, height/2f);
		image.setRotation(direction * 90);
		image.draw(leftX, topY, width, height);
	}

	@Override
	public int difficultyPassing() {
		return 4;
	}

	@Override
	public void interact(Player player) {
		direction++;
		if(direction > 3)
			direction = 0;
	}

	@Override
	public int getId() {
		return 0xf9 - direction;
	}

}
