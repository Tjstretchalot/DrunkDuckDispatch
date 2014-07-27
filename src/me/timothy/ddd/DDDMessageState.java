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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import me.timothy.ddd.scaling.SizeScaleSystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class DDDMessageState extends BasicGameState {
	private static final Rectangle TEXT_RECT;
	private static final Rectangle RESET_GAME;
	
	
	static {
		TEXT_RECT = new Rectangle();
		TEXT_RECT.x = 0;
		TEXT_RECT.y = 0;
		TEXT_RECT.width = (int) SizeScaleSystem.EXPECTED_WIDTH;
		TEXT_RECT.height = (int) (SizeScaleSystem.EXPECTED_HEIGHT * 2 / 3);
		
		RESET_GAME = new Rectangle();
		RESET_GAME.x = (int) (TEXT_RECT.x + SizeScaleSystem.EXPECTED_WIDTH / 7);
		RESET_GAME.y = TEXT_RECT.y + TEXT_RECT.height;
		RESET_GAME.width = TEXT_RECT.width - RESET_GAME.x * 2 + TEXT_RECT.x * 2;
		RESET_GAME.height = (int) (SizeScaleSystem.EXPECTED_HEIGHT - TEXT_RECT.height);
	}
	
	private Logger logger;
	private String fileName;
	private int id;
	private String displayText;
	private Color notHoverColor;
	private Color hoverColor;
	private Color resetGameTextColor;
	private boolean willBeReset;
	
	private boolean hoveringOnResetGame;
	
	public DDDMessageState(String fileName, int id) {
		logger = LogManager.getLogger();
		this.fileName = fileName;
		this.id = id;
		notHoverColor = new Color(15, 15, 41);
		hoverColor = new Color((15 * 3) / 2, (15 * 3) / 2, (41 * 3) / 2);
		resetGameTextColor = new Color(241, 241, 255);
		hoveringOnResetGame = false;
		willBeReset = false;
	}
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		int width = g.getFont().getWidth(displayText);
		if(width > SizeScaleSystem.getRealWidth() - 40) {
			displayText = DDDUtils.addNewlines(displayText, g.getFont(), SizeScaleSystem.getRealWidth() - 40);
			width = g.getFont().getWidth(displayText);
		}
		int height = g.getFont().getHeight(displayText);
		g.setColor(Color.white);
		g.drawString(displayText, 
				SizeScaleSystem.adjRealToPixelX(TEXT_RECT.x) + SizeScaleSystem.adjRealToPixelX(TEXT_RECT.width)/2 - width/2,
				SizeScaleSystem.adjRealToPixelY(TEXT_RECT.y) + SizeScaleSystem.adjRealToPixelY(TEXT_RECT.height)/2 - height/2);

		int x = (int) Math.round(SizeScaleSystem.adjRealToPixelX(RESET_GAME.x));
		int y = (int) Math.round(SizeScaleSystem.adjRealToPixelY(RESET_GAME.y));
		width = (int) Math.round(SizeScaleSystem.adjRealToPixelX(RESET_GAME.width));
		height = (int) Math.round(SizeScaleSystem.adjRealToPixelY(RESET_GAME.height));
		String text;
		
		if(!willBeReset) {
			text = "Reset Game";
			if(hoveringOnResetGame)
				g.setColor(hoverColor);
			else
				g.setColor(notHoverColor);
			g.fillRect(x, y, width, height);
		}else {
			text = "Game will be reset!";
		}
		int textWidth = g.getFont().getWidth(text);
		int textHeight = g.getFont().getHeight(text);
		g.setColor(resetGameTextColor);
		g.drawString(text, x + width/2 - textWidth/2, y + height/2 - textHeight/2);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			System.exit(0);
		}
		
		if(!willBeReset) {
			int mouseX = (int) SizeScaleSystem.adjPixelToRealX(Mouse.getX());
			int mouseY = (int) (SizeScaleSystem.EXPECTED_HEIGHT - SizeScaleSystem.adjPixelToRealY(Mouse.getY()));
			if(RESET_GAME.contains(mouseX, mouseY)) {
				hoveringOnResetGame = true;

				if(Mouse.isButtonDown(0)) {
					willBeReset = true;	

					new File("entities_saved.json").deleteOnExit();
					new File("map_saved.binary").deleteOnExit();
					new File("misc.json").deleteOnExit();
					new File("player.json").deleteOnExit();
					new File("quests.json").deleteOnExit();
				}
			}else {
				hoveringOnResetGame = false;
			}
		}
	}
	
	
	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		try(BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {
			displayText = br.readLine();
			String ln;
			while((ln = br.readLine()) != null) {
				displayText += " " + ln;
			}
		}catch (IOException e) {
			logger.throwing(e);
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int getID() {
		return id;
	}
}
