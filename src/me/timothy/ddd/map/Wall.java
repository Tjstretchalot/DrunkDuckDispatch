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

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class Wall implements GridSpot {

	@Override
	public void render(Graphics g, int leftX, int topY, int width, int height) {
		g.setColor(Color.black);
		g.fillRect(leftX, topY, width, height);
	}

	@Override
	public int difficultyPassing() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getId() {
		return 1;
	}

}
