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

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.timothy.ddd.map.Carpet;
import me.timothy.ddd.resources.Resources;
import me.timothy.ddd.scaling.SizeScaleSystem;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

public class DDDEndGame_ArrowgantVictory extends BasicGameState {
	private class Archer {
		Rectangle rect;
		int kill;
		int state;
		int timer;
		int rotation;
		
		Archer(int x, int y, int kill) {
			rect = new Rectangle();
			rect.x = x;
			rect.y = y;
			rect.width = 128;
			rect.height = 128;
			
			this.kill = kill;
		}
	}
	private class Arrow {
		Rectangle2D.Float rect;
		int destX;
		int destY;
		int rotation;
		
		Arrow(int startX, int startY, int endX, int endY) {
			rect = new Rectangle2D.Float();
			rect.x = startX;
			rect.y = startY;
			rect.width = 64;
			rect.height = 32;
			destX = endX;
			destY = endY;
		}
	}
	
	private class Swordsman {
		Rectangle rect;
		int justHit;
		public int health;
		
		Swordsman(int x, int y) {
			health = 8;
			rect = new Rectangle();
			rect.x = x;
			rect.y = y;
			rect.width = 128;
			rect.height = 128;
		}
	}
	
	private Random random;
	private SpriteSheet gantArcherSheet;
	private SpriteSheet gantArcherSheet2;
	private SpriteSheet swordsmanImg;
	private Image arrowImg;
	private List<Archer> archers;
	private List<Arrow> arrows;
	
	private List<Swordsman> swordsmen;
	
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		random = new Random();
		gantArcherSheet = Resources.getSheet("arrowgant");
		gantArcherSheet2 = new SpriteSheet(gantArcherSheet.getFlippedCopy(true, false), 4, 1);
		swordsmanImg = Resources.getSheet("swordman");
		arrowImg = Resources.getImage("arrow.png");
		archers = new ArrayList<>();
		swordsmen = new ArrayList<>();
		
		for(int i = 0; i < 6; i++) {
			swordsmen.add(new Swordsman(800 + random.nextInt(200), 128 + random.nextInt(800)));
		}
		for(int i = 0; i < 40; i++) {
			archers.add(new Archer(128 + random.nextInt(3) * 128,
					64 * random.nextInt(15) + 64, random.nextInt(swordsmen.size())));
		}
		
		arrows = new ArrayList<>();
		initRotations();
	}

	@Override
	public void render(GameContainer c, StateBasedGame sbg, Graphics g)
			throws SlickException {
		g.setColor(Carpet.color);
		g.fillRect(0, 0, SizeScaleSystem.getRealWidth(), SizeScaleSystem.getRealHeight());
		for(Archer a : archers) {
			if(a.rotation < 90 || a.rotation > 270) {
				gantArcherSheet.setCenterOfRotation(SizeScaleSystem.adjRealToPixelX(64), SizeScaleSystem.adjRealToPixelY(64));
				gantArcherSheet.setRotation(a.rotation);
				gantArcherSheet.draw(SizeScaleSystem.adjRealToPixelX(a.rect.x), 
						SizeScaleSystem.adjRealToPixelY(a.rect.y),
						SizeScaleSystem.adjRealToPixelX(a.rect.x+a.rect.width),
						SizeScaleSystem.adjRealToPixelY(a.rect.y+a.rect.height),
						(a.state) * a.rect.width, 0, (a.state+1)*a.rect.width, a.rect.height);
			}else {
				gantArcherSheet2.setCenterOfRotation(SizeScaleSystem.adjRealToPixelX(a.rect.width/2), SizeScaleSystem.adjRealToPixelY(a.rect.height/2));
				gantArcherSheet2.setRotation(a.rotation-180);
				gantArcherSheet2.draw(SizeScaleSystem.adjRealToPixelX(a.rect.x), 
						SizeScaleSystem.adjRealToPixelY(a.rect.y),
						SizeScaleSystem.adjRealToPixelX(a.rect.x+a.rect.width),
						SizeScaleSystem.adjRealToPixelY(a.rect.y+a.rect.height),
						(a.state) * a.rect.width, 0, (a.state+1)*a.rect.width, a.rect.height);
			}
		}
		
		for(Arrow a : arrows) {
			arrowImg.setCenterOfRotation(SizeScaleSystem.adjRealToPixelX(32), SizeScaleSystem.adjRealToPixelY(16));
			arrowImg.setRotation(a.rotation);
			arrowImg.draw(
					SizeScaleSystem.adjRealToPixelX(a.rect.x),
					SizeScaleSystem.adjRealToPixelY(a.rect.y),
					SizeScaleSystem.adjRealToPixelX(a.rect.width),
					SizeScaleSystem.adjRealToPixelY(a.rect.height));
		}
		
		swordsmanImg.startUse();
		for(Swordsman sm : swordsmen) {
			if(sm.justHit <= 0) {
				swordsmanImg.drawEmbedded(
						SizeScaleSystem.adjRealToPixelX(sm.rect.x),
						SizeScaleSystem.adjRealToPixelY(sm.rect.y),
						SizeScaleSystem.adjRealToPixelX(sm.rect.x + sm.rect.width),
						SizeScaleSystem.adjRealToPixelY(sm.rect.y + sm.rect.height),
						0, 0, sm.rect.width, sm.rect.height);
			}else {
				swordsmanImg.drawEmbedded(
						SizeScaleSystem.adjRealToPixelX(sm.rect.x),
						SizeScaleSystem.adjRealToPixelY(sm.rect.y),
						SizeScaleSystem.adjRealToPixelX(sm.rect.x + sm.rect.width),
						SizeScaleSystem.adjRealToPixelY(sm.rect.y + sm.rect.height),
						0, 0, sm.rect.width, sm.rect.height, Color.red);
			}
		}
		swordsmanImg.endUse();
	}

	@Override
	public void update(GameContainer arg0, StateBasedGame arg1, int arg2)
			throws SlickException {
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			System.exit(0);
		}
		for(Archer a : archers) {
			a.timer -= arg2;
			if(a.timer <= 0) {
				a.state--;
				if(a.state == 0) {
					if(a.kill >= swordsmen.size()) {
						if(swordsmen.size() > 0) {
							a.kill = swordsmen.size() - 1;
						}else {
							continue;
						}
					}
					Swordsman sm = swordsmen.get(a.kill);
					Arrow arrow = new Arrow(a.rect.x + a.rect.width/2, a.rect.y + a.rect.height/2, sm.rect.x + sm.rect.width/2, sm.rect.y + sm.rect.height/2);
					arrows.add(arrow);
					a.timer = 100;
				}else {
					switch(a.state) {
					case -1:
						a.state = 4;
						a.timer = 1000 + random.nextInt(1000);
						break;
					case 1:
						a.timer = 100;
						break;
					case 2:
						a.timer = 300;
						break;
					case 3:
						a.timer = 500;
						break;
					}
				}
				
			}
		}
		
		for(Swordsman sm : swordsmen) {
			if(sm.justHit > 0)
				sm.justHit -= arg2;
		}
		
		for(int i = 0; i < arrows.size(); i++) {
			Arrow a = arrows.get(i);
			if(a.rect.width/2 + a.rect.x - a.destX < 15 && a.rect.width/2 +  a.rect.x - a.destX > -15 && a.rect.height/2 + a.rect.y - a.destY < 15 && a.rect.height/2 + a.rect.y - a.destY > -15) {
				arrows.remove(i);
				i--;
				continue;
			}
			for(int j = 0; j < swordsmen.size(); j++) {
				Swordsman sm = swordsmen.get(j);
				if(sm.rect.intersects(a.rect)) {
					sm.health--;
					if(sm.health > 0) {
						sm.justHit = 250;
					}else {
						swordsmen.remove(sm);
						if(swordsmen.size() == 0) {
							
							arg1.enterState(3, new FadeOutTransition(), new FadeInTransition());
						}
					}
					arrows.remove(i);
					i--;
					break;
				}
			}
			a.rotation = (int) Math.toDegrees(DDDUtils.getAngleRads(a.rect.x + a.rect.width/2, a.rect.y + a.rect.height/2, a.destX, a.destY));
			a.rect.x -= Math.cos(Math.toRadians(a.rotation)) * 5;
			a.rect.y -= Math.sin(Math.toRadians(a.rotation)) * 5;
		}
	}
	
	private void initRotations() {
		for(Archer a : archers) {
			Swordsman target = swordsmen.get(a.kill);
			double sX = a.rect.x + a.rect.width/2;
			double sY = a.rect.y + a.rect.height/2;
//			double tX = SizeScaleSystem.adjPixelToRealX(Mouse.getX());
//			double tY = SizeScaleSystem.EXPECTED_HEIGHT-SizeScaleSystem.adjPixelToRealX(Mouse.getY());
			double tX = target.rect.x + target.rect.width/2;
			double tY = target.rect.y + target.rect.height/2;
			int newRot = (int) Math.toDegrees(DDDUtils.getAngleRads(sX, sY, tX, tY));
			newRot = newRot < 0 ? newRot + 360 : newRot;
			if(newRot != a.rotation) {
				a.rotation = newRot;
			}
		}
	}

	@Override
	public int getID() {
		return 1;
	}
	
}
