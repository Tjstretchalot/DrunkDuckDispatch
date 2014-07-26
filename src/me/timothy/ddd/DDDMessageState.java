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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import me.timothy.ddd.scaling.SizeScaleSystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class DDDMessageState extends BasicGameState {
	private Logger logger;
	private String fileName;
	private int id;
	private String displayText;
	
	public DDDMessageState(String fileName, int id) {
		logger = LogManager.getLogger();
		this.fileName = fileName;
		this.id = id;
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
		g.drawString(displayText, SizeScaleSystem.getRealWidth()/2 - width/2, SizeScaleSystem.getRealHeight()/2 - height/2);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			System.exit(0);
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
