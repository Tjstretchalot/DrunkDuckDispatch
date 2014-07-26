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

import static me.timothy.ddd.scaling.SizeScaleSystem.*;

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
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

public class DDDEndGame_ListecVictory extends BasicGameState {
	private class Archer {
		Rectangle rect;
		int hurt;
		int health;
		int rotation;
		int target;
		
		Archer(int x, int y, int target) {
			health = 5;
			rect = new Rectangle();
			rect.x = x;
			rect.y = y;
			rect.width = 128;
			rect.height = 128;
		}
	}
	
	private class Swordsman {
		Rectangle2D.Float rect;
		int toKill;
		int rotation;
		int state;
		public int timer;
		
		Swordsman(int x, int y, int toKill) {
			rect = new Rectangle2D.Float();
			rect.x = x;
			rect.y = y;
			rect.width = 128;
			rect.height = 128;
			this.toKill = toKill;
		}
	}
	private Random random;
	
	private List<Archer> archers;
	private List<Swordsman> swordsmen;
	
	private SpriteSheet swordsmanImg;
	private SpriteSheet arrowgantImg;
	
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		random = new Random();
		
		swordsmanImg = Resources.getSheet("swordman");
		arrowgantImg = Resources.getSheet("arrowgant");
		
		archers = new ArrayList<>();
		swordsmen = new ArrayList<>();
		
		for(int i = 0; i < 6; i++) {
			archers.add(new Archer(128 + random.nextInt(512), random.nextInt((int) (EXPECTED_HEIGHT - 128)), random.nextInt(20)));
		}
		
		for(int i = 0; i < 40; i++) {
			swordsmen.add(new Swordsman(512 + random.nextInt((int) (EXPECTED_WIDTH - 512 - 128)), 
					random.nextInt((int) (EXPECTED_HEIGHT - 128)), random.nextInt(archers.size())));
		}
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		g.setColor(Carpet.color);
		g.fillRect(0, 0, SizeScaleSystem.getRealWidth(), SizeScaleSystem.getRealHeight());
		for(Archer a : archers) {
			arrowgantImg.setCenterOfRotation(adjRealToPixelX(a.rect.width/2), adjRealToPixelY(a.rect.height/2));
			arrowgantImg.setRotation(a.rotation);
			if(a.hurt <= 0)
				arrowgantImg.draw(
						adjRealToPixelX(a.rect.x), adjRealToPixelY(a.rect.y), 
						adjRealToPixelX(a.rect.x + a.rect.width), adjRealToPixelY(a.rect.y + a.rect.height),
						0, 0, a.rect.width, a.rect.height
						);
			else
				arrowgantImg.draw(adjRealToPixelX(a.rect.x), adjRealToPixelY(a.rect.y), 
						adjRealToPixelX(a.rect.x + a.rect.width), adjRealToPixelY(a.rect.y + a.rect.height),
						0, 0, a.rect.width, a.rect.height, Color.red);
		}
		
		for(Swordsman sm : swordsmen) {
			swordsmanImg.setCenterOfRotation(adjRealToPixelX(sm.rect.width/2), adjRealToPixelY(sm.rect.height/2));
			swordsmanImg.setRotation(sm.rotation);
			swordsmanImg.draw(
					adjRealToPixelX(sm.rect.x), adjRealToPixelY(sm.rect.y), 
					adjRealToPixelX(sm.rect.x + sm.rect.width), adjRealToPixelY(sm.rect.y + sm.rect.height),
					sm.rect.width * sm.state, 0, sm.rect.width * (sm.state+1), sm.rect.height
					);
		}
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			System.exit(0);
		}
		
		for(Archer a : archers) {
			a.hurt -= delta;
			a.rotation = (int) Math.toDegrees(DDDUtils.getAngleRads(a.rect.x + a.rect.width/2, a.rect.y + a.rect.height/2,
					swordsmen.get(a.target).rect.x + swordsmen.get(a.target).rect.width/2, 
					swordsmen.get(a.target).rect.y + swordsmen.get(a.target).rect.height/2));
		}
		for(Swordsman sm : swordsmen) {
			if(archers.size() <= sm.toKill) {
				sm.toKill = archers.size() - 1;
			}
			Archer a = archers.get(sm.toKill);
			sm.rotation = (int) Math.toDegrees(DDDUtils.getAngleRads(sm.rect.x + sm.rect.width/2, sm.rect.y + sm.rect.height/2,
					a.rect.x + a.rect.width/2, 
					a.rect.y + a.rect.height/2));
			
			if(sm.rect.intersects(a.rect)) {
				sm.timer -= delta;
				if(sm.timer <= 0) {
					sm.timer += 100;
					sm.state++;
					if(sm.state == 3) {
						sm.timer += 1000;
						sm.state = 0;
						a.health--;
						if(a.health == 0) {
							archers.remove(a);
							if(archers.size() == 0) {
								game.enterState(3, new FadeOutTransition(), new FadeInTransition());
								return;
							}else {
								sm.toKill = archers.size() - 1;
							}
						}else {
							a.hurt = 250;
						}
					}
				}
			}else {
				sm.rect.x -= Math.cos(Math.toRadians(sm.rotation)) * 3;
				sm.rect.y -= Math.sin(Math.toRadians(sm.rotation)) * 3;
			}
		}
	}

	@Override
	public int getID() {
		return 2;
	}

}
