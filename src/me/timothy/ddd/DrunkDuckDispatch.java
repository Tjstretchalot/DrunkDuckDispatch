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

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.swing.JOptionPane;

import me.timothy.ddd.entities.JSONCompatible;
import me.timothy.ddd.resources.Resources;
import me.timothy.ddd.scaling.SizeScaleSystem;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class DrunkDuckDispatch extends StateBasedGame {
	public static DrunkDuckDispatch ddd;
	private Logger logger;
	public boolean save;
	
	public DrunkDuckDispatch() {
		super("Drunk Duck Dispatch");
		logger = LogManager.getLogger();
		save = true;
	}
	
	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		Resources.initRes();
		logger.debug("Initializing states list..");
		addState(new DDDGameState());
		addState(new DDDEndGame_ArrowgantVictory());
		addState(new DDDEndGame_ListecVictory());
		addState(new DDDVictory());
		addState(new DDDFailure());
		logger.debug("States list initialized");
	}

	public static void main(String[] args) throws LWJGLException {
		try
		{
			float defaultDisplayWidth = SizeScaleSystem.EXPECTED_WIDTH / 2;
			float defaultDisplayHeight = SizeScaleSystem.EXPECTED_HEIGHT / 2;
			boolean fullscreen = false, defaultDisplay = !fullscreen;
			File resolutionInfo = new File("graphics.json");

			if(resolutionInfo.exists()) {
				try(FileReader fr = new FileReader(resolutionInfo)) {
					JSONObject obj = (JSONObject) new JSONParser().parse(fr);
					Set<?> keys = obj.keySet();
					for(Object o : keys) {
						if(o instanceof String) {
							String key = (String) o;
							switch(key.toLowerCase()) {
							case "width":
								defaultDisplayWidth = JSONCompatible.getFloat(obj, key);
								break;
							case "height":
								defaultDisplayHeight = JSONCompatible.getFloat(obj, key);
								break;
							case "fullscreen":
								fullscreen = JSONCompatible.getBoolean(obj, key);
								defaultDisplay = !fullscreen;
								break;
							}
						}
					}
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
				float expHeight = defaultDisplayWidth * (SizeScaleSystem.EXPECTED_HEIGHT/SizeScaleSystem.EXPECTED_WIDTH);
				float expWidth = defaultDisplayHeight * (SizeScaleSystem.EXPECTED_WIDTH/SizeScaleSystem.EXPECTED_HEIGHT);
				if(Math.round(defaultDisplayWidth) != Math.round(expWidth)) {
					if(defaultDisplayHeight < expHeight) {
						System.err.printf("%f x %f is an invalid resolution; adjusting to %f x %f\n", defaultDisplayWidth, defaultDisplayHeight, defaultDisplayWidth, expHeight);
						defaultDisplayHeight = expHeight;
					}else {
						System.err.printf("%f x %f is an invalid resolution; adjusting to %f x %f\n", defaultDisplayWidth, defaultDisplayHeight, expWidth, defaultDisplayHeight);
						defaultDisplayWidth = expWidth;
					}
				}
			}
			File dir = null;
			String os = getOS();
			if(os.equals("windows")) {
				dir = new File(System.getenv("APPDATA"), "timgames/");
			}else {
				dir = new File(System.getProperty("user.home"), ".timgames/");
			}
			File lwjglDir = new File(dir, "lwjgl-2.9.1/");
			Resources.init();
			Resources.downloadIfNotExists(lwjglDir, "lwjgl-2.9.1.zip", 
					"http://umad-barnyard.com/lwjgl-2.9.1.zip",
					"Necessary LWJGL natives couldn't be found, I can attempt " +
					"to download it, but I make no promises",
					"Unfortunately I was unable to download it, so I'll open up the " +
					"link to the official download. Make sure you get LWJGL version 2.9.1, " +
					"and you put it at " + dir.getAbsolutePath() + "/lwjgl-2.9.1.zip", 
					new Runnable() {

						@Override
						public void run() {
							if(!Desktop.isDesktopSupported()) {
								JOptionPane.showMessageDialog(null, "I couldn't " +
										"even do that! Download it manually and try again");
								return;
							}
							
							try {
								Desktop.getDesktop().browse(new URI("http://www.lwjgl.org/download.php"));
							} catch (IOException | URISyntaxException e) {
								JOptionPane.showMessageDialog(null, "Oh cmon.. Address is http://www.lwjgl.org/download.php, good luck");
								System.exit(1);
							}
						}
				
			}, 5843626);
			
			Resources.extractIfNotFound(lwjglDir, "lwjgl-2.9.1.zip", "lwjgl-2.9.1");
			System.setProperty("org.lwjgl.librarypath", new File(dir, "lwjgl-2.9.1/lwjgl-2.9.1/native/" + os).getAbsolutePath()); // deal w/ it
			System.setProperty("net.java.games.input.librarypath", System.getProperty("org.lwjgl.librarypath"));
			
			Resources.downloadIfNotExists("entities.json", "http://umad-barnyard.com/ddd/entities.json", 16142);
			Resources.downloadIfNotExists("map.binary", "http://umad-barnyard.com/ddd/map.binary", 16142);
			File resFolder = new File("resources/");
			if(!resFolder.exists()) {
				Resources.downloadIfNotExists("resources.zip", "http://umad-barnyard.com/ddd/resources.zip", 54484);
				Resources.extractIfNotFound(new File("."), "resources.zip", "player-still.png");
				new File("resources.zip").delete();
			}
			AppGameContainer appgc;
			ddd = new DrunkDuckDispatch();
			appgc = new AppGameContainer(ddd);
			appgc.setTargetFrameRate(60);
			appgc.setShowFPS(false);
			appgc.setAlwaysRender(true);
			if(fullscreen) {
				DisplayMode[] modes = Display.getAvailableDisplayModes();
				DisplayMode current, best = null;
				float ratio = 0f;
				for (int i=0;i<modes.length;i++) {
					current = modes[i];
					float rX = (float) current.getWidth() / SizeScaleSystem.EXPECTED_WIDTH;
					float rY = (float) current.getHeight() / SizeScaleSystem.EXPECTED_HEIGHT;
					System.out.println(current.getWidth() + "x" + current.getHeight() + " -> " + rX + "x" + rY);
					if(rX == rY && rX > ratio) {
						best = current;
						ratio = rX;
					}
				}
				if(best == null) {
					System.out.println("Failed to find an appropriately scaled resolution, using default display");
					defaultDisplay = true;
				} else {
					appgc.setDisplayMode(best.getWidth(), best.getHeight(), true);
					SizeScaleSystem.setRealHeight(best.getHeight());
					SizeScaleSystem.setRealWidth(best.getWidth());
					System.out.println("I choose " + best.getWidth() + "x" + best.getHeight());
				}
			}
			
			if(defaultDisplay) {
				SizeScaleSystem.setRealWidth(Math.round(defaultDisplayWidth));
				SizeScaleSystem.setRealHeight(Math.round(defaultDisplayHeight));
				appgc.setDisplayMode(Math.round(defaultDisplayWidth), Math.round(defaultDisplayHeight), false);
			}
			ddd.logger.info("SizeScaleSystem: " + SizeScaleSystem.getRealWidth() + "x" + SizeScaleSystem.getRealHeight());
			appgc.start();
		}
		catch (SlickException ex)
		{
			LogManager.getLogger(DrunkDuckDispatch.class.getSimpleName()).catching(Level.ERROR, ex);
		}
	}
	/**
	 * Gets the OS type, prompts the user for a choice if it cannot be determined.
	 * 
	 * @return the os
	 */
	private static String getOS() {
		String envOS = System.getProperty("os.name").toLowerCase();
		System.out.println(envOS);
		if(envOS.contains("win"))
			return "windows";
		else if(envOS.contains("mac"))
			return "macosx";
		else if(envOS.contains("nix"))
			return "linux";
		else if(envOS.contains("sunos"))
			return "solaris";

		String[] os = new String[] {"Windows", "Linux", "MacOSX", "Solaris"};
		String choice = (String) JOptionPane.showInputDialog(null,
				"Your OS could not be detected, please choose from the available options below", "OS Picker",
				JOptionPane.QUESTION_MESSAGE, null, os, os[1]);
		if(choice == null)
			System.exit(0);
		return choice.toLowerCase();
	}
}
