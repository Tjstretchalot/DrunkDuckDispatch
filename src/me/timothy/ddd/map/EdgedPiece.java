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
import java.util.List;

import me.timothy.ddd.scaling.SizeScaleSystem;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class EdgedPiece implements GridSpot {
	private static final int THICKNESS = 12;
	public enum Edge {
		TOP,
		LEFT,
		RIGHT,
		BOTTOM
	}
	
	private List<Edge> edges;
	private Color edgeColor;
	private Color mainColor;
	
	public EdgedPiece() {
		edges = new ArrayList<>();
		for(Edge e : Edge.values()) {
			edges.add(e);
		}
		mainColor = new Color(205, 192, 176);
		edgeColor = new Color(139, 131, 120);
	}
	
	@Override
	public void render(Graphics g, int leftX, int topY, int width, int height) {
		int realThicknessX = (int) SizeScaleSystem.adjRealToPixelX(THICKNESS);
		int realThicknessY = (int) SizeScaleSystem.adjRealToPixelY(THICKNESS);
		
		g.setColor(mainColor);
		g.fillRect(leftX, topY, width, height);
		g.setColor(edgeColor);
		if(hasEdge(Edge.TOP)) {
			g.setLineWidth(realThicknessY);
			g.drawLine(leftX + realThicknessY / 2, topY + realThicknessY / 2, leftX + width - realThicknessY / 2, topY + realThicknessY / 2);
		}
		
		if(hasEdge(Edge.LEFT)) {
			g.setLineWidth(realThicknessX);
			g.drawLine(leftX + realThicknessX / 2, topY + realThicknessX / 2 - (!hasEdge(Edge.TOP) ? 2 : 0), leftX + realThicknessX / 2, topY + height - realThicknessX/2);
		}
		
		if(hasEdge(Edge.RIGHT)) {
			g.setLineWidth(realThicknessX);
			g.drawLine(leftX + width - realThicknessX / 2, topY + realThicknessX / 2 - (!hasEdge(Edge.TOP) ? 2 : 0), leftX + width - realThicknessX / 2, topY + height);
		}
		
		if(hasEdge(Edge.BOTTOM)) {
			g.setLineWidth(realThicknessY);
			g.drawLine(leftX + realThicknessY / 2, topY + height - realThicknessY / 2, leftX + width - realThicknessY / 2, topY + height - realThicknessY / 2);
		}
	}

	public boolean hasEdge(Edge edge) {
		return edges.contains(edge);
	}
	
	public void setEdge(Edge edge, boolean b) {
		if(b) {
			if(!edges.contains(edge)) 
				edges.add(edge);
		}else {
			edges.remove(edge);
		}
	}

	@Override
	public int difficultyPassing() {
		return 50;
	}

	@Override
	public int getId() {
		return 2;
	}

}
