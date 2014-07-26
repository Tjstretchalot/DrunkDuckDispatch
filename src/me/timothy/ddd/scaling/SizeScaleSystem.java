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

package me.timothy.ddd.scaling;

import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class SizeScaleSystem {

	public static final float EXPECTED_WIDTH = 1280;
	public static final float EXPECTED_HEIGHT = 1024;
	
	// Used in various guis to signify the object should be centered
	public static final float CENTER_X = -1;
	public static final float TOP_THIRD = -2;
	public static final float CENTER_Y = -3;
	public static final float BOT_THIRD = -4;
	
	private static float realWidth;
	private static float realHeight;
	
	public static void expectedToActual(Rectangle2D.Float imgRect) {
		float x, y, w, h;
		if(imgRect.x != CENTER_X)
			x = (float) (imgRect.getX() * (realWidth / EXPECTED_WIDTH));
		else
			x = (float) (EXPECTED_WIDTH / 2 - imgRect.getWidth() / 2) * (realWidth / EXPECTED_WIDTH);

		if(imgRect.y == TOP_THIRD)
			y = (float) (EXPECTED_HEIGHT / 3 - imgRect.getHeight() / 2) * (realHeight / EXPECTED_HEIGHT);
		else if(imgRect.y == CENTER_Y)
			y = (float) (EXPECTED_HEIGHT / 2 - imgRect.getHeight() / 2) * (realHeight / EXPECTED_HEIGHT);
		else if(imgRect.y == BOT_THIRD)
			y = (float) ((EXPECTED_HEIGHT * 2) / 3 - imgRect.getHeight() / 2) * (realHeight / EXPECTED_HEIGHT);
		else 
			y = (float) (imgRect.getY() * (realHeight / EXPECTED_HEIGHT));
		
		w = (float) (imgRect.getWidth() * (realWidth / EXPECTED_WIDTH));
		h = (float) (imgRect.getHeight() * (realHeight / EXPECTED_HEIGHT));
		imgRect.setRect(x, y, w, h);
	}

	public static void setRealWidth(float f) {
		realWidth = f;
	}
	public static void setRealHeight(float f) {
		realHeight = f;
	}
	public static float getRealWidth() {
		return realWidth;
	}
	public static float getRealHeight() {
		return realHeight;
	}

	public static float adjRealToPixelX(float x) {
		return (x * (realWidth / EXPECTED_WIDTH));
	}
	public static float adjRealToPixelY(float y) {
		return (y * (realHeight / EXPECTED_HEIGHT));
	}
	
	public static float adjPixelToRealX(float x) {
		return x * (EXPECTED_WIDTH / realWidth);
	}
	
	public static float adjPixelToRealY(float y) {
		return y * (EXPECTED_HEIGHT / realHeight);
	}
}
