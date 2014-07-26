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

package me.timothy.ddd.acheivements;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import me.timothy.ddd.DDDUtils;
import me.timothy.ddd.map.Chair;
import me.timothy.ddd.map.GridSpot;
import me.timothy.ddd.quests.Quest;
import me.timothy.ddd.quests.Quest_RichSon;
import me.timothy.ddd.scaling.SizeScaleSystem;

public class AchievementManager {
	private static final Rectangle DISPLAY_RECT = new Rectangle();
	private static final Rectangle DESC_RECT = new Rectangle();
	private static final Rectangle TITLE_RECT = new Rectangle();
	static {
		DISPLAY_RECT.x = (int) (SizeScaleSystem.EXPECTED_WIDTH * 3 / 7);
		DISPLAY_RECT.y = 0;
		DISPLAY_RECT.width = (int) (SizeScaleSystem.EXPECTED_WIDTH * 4 / 7);
		DISPLAY_RECT.height = (int) (SizeScaleSystem.EXPECTED_HEIGHT / 3);
		
		TITLE_RECT.x = DISPLAY_RECT.x;
		TITLE_RECT.y = DISPLAY_RECT.y;
		TITLE_RECT.height = DISPLAY_RECT.height / 3;
		TITLE_RECT.width = DISPLAY_RECT.width;
		
		DESC_RECT.x = DISPLAY_RECT.x;
		DESC_RECT.y = TITLE_RECT.y + TITLE_RECT.height;
		DESC_RECT.width = DISPLAY_RECT.width;
		DESC_RECT.height = DISPLAY_RECT.height - TITLE_RECT.height;
	}
	
	private Logger logger;
	private List<Achievement> achievements;
	private Queue<Achievement> toDisplay;
	
	private Color bkndColor;
	private Color titleColor;
	private Color textColor;
	private Achievement displaying;
	private String displayText; // More newlines bro
	private int timeRemaining;
	
	public AchievementManager() {
		logger = LogManager.getLogger();
		achievements = new ArrayList<>();
		toDisplay = new LinkedList<Achievement>();
		
		bkndColor = new Color(49, 45, 150);
		titleColor = new Color(215, 208, 39);
		textColor = new Color(215, 135, 39);
		
		final File achievs = new File("achievements_acquired.json");
		if(achievs.exists()) {
			try(FileReader fr = new FileReader(achievs)) {
				JSONArray jArr = (JSONArray) (new JSONParser().parse(fr));
				for(int i = 0; i < jArr.size(); i++) {
					Achievement ach = new Achievement();
					ach.loadFrom((JSONObject) jArr.get(i));
					achievements.add(ach);
				}
			}catch(IOException | ParseException e) {
				logger.throwing(e);
				throw new RuntimeException(e);
			}
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				JSONArray jArr = new JSONArray();
				for(Achievement ach : achievements) {
					JSONObject jObj = new JSONObject();
					ach.saveTo(jObj);
					jArr.add(jObj);
				}
				try(FileWriter fw = new FileWriter(achievs)) {
					DDDUtils.writeJSONPretty(fw, jArr);
				}catch(IOException e) {}
			}
			
		}));
	}
	
	public void onQuestComplete(Quest quest) {
		if(quest instanceof Quest_RichSon) {
			Achievement achievement = new Achievement();
			achievement.setName("Patient");
			achievement.setDebugName("rich_son");
			achievement.setText("Listened to the disheartening story of the unfortunate rich son");
			addAchievement(achievement);
		}
	}
	
	public void onQuestFailed(Quest quest) {
		
	}
	
	public void onInteract(int gridX, int gridY, GridSpot gridSpot) {
		if(gridSpot instanceof Chair && !hasAchievementByDebugName("chair")) {
			Achievement achievement = new Achievement();
			achievement.setName("Priorities");
			achievement.setDebugName("chair");
			achievement.setText("Sometimes you have to focus on the task at hand. And other times, you have to spin a chair.");
			addAchievement(achievement);
		}
	}

	public void onGoThrough(int x, int y) {
		if(!hasAchievementByDebugName("moved")) {
			Achievement achievement = new Achievement();
			achievement.setName("Tester");
			achievement.setDebugName("moved");
			achievement.setText("Attempted what must have been a plethora of buttons in order to find out how to move");
			addAchievement(achievement);
		}
		if(y >= 26 && !hasAchievementByDebugName("explorer")) {
			Achievement achievement = new Achievement();
			achievement.setName("Explorer");
			achievement.setDebugName("explorer");
			achievement.setText("You've boldly gone where nobody has gone before - and where no quest lies");
			addAchievement(achievement);
		}
	}
	
	private void addAchievement(Achievement achievement) {
		if(!toDisplay.offer(achievement)) {
			logger.warn("Failed to offer achievement; oh my");
		}
		achievements.add(achievement);
	}
	
	public boolean hasAchievementByDebugName(String debugName) {
		for(Achievement ach : achievements) {
			if(ach.getDebugName().equalsIgnoreCase(debugName)) {
				return true;
			}
		}
		return false;
	}
	
	public void render(Graphics g) {
		if(displaying == null)
			return;
		
		if(displayText == null) {
			displayText = DDDUtils.addNewlines(displaying.getText(), (int) (SizeScaleSystem.adjRealToPixelX(DISPLAY_RECT.width) / g.getFont().getWidth("W")));
		}
		
		g.setColor(bkndColor);
		g.fillRect(
				SizeScaleSystem.adjRealToPixelX(DISPLAY_RECT.x), 
				SizeScaleSystem.adjRealToPixelY(DISPLAY_RECT.y),
				SizeScaleSystem.adjRealToPixelX(DISPLAY_RECT.width),
				SizeScaleSystem.adjRealToPixelY(DISPLAY_RECT.height)
				);
		
		String txt = "Achievement Get! " + displaying.getName();
		Rectangle rect = TITLE_RECT;
		g.setColor(titleColor);
		g.drawString(
				txt,
				(int) (SizeScaleSystem.adjRealToPixelX(rect.x) + SizeScaleSystem.adjRealToPixelX(rect.width)/2 - g.getFont().getWidth(txt)/2),
				(int) (SizeScaleSystem.adjRealToPixelY(rect.y) + SizeScaleSystem.adjRealToPixelY(rect.height)/2 - g.getFont().getHeight(txt)/2)
				);
		
		txt = displayText;
		rect = DESC_RECT;
		g.setColor(textColor);
		g.drawString(
				txt,
				(int) (SizeScaleSystem.adjRealToPixelX(rect.x) + SizeScaleSystem.adjRealToPixelX(rect.width)/2 - g.getFont().getWidth(txt)/2),
				(int) (SizeScaleSystem.adjRealToPixelY(rect.y) + SizeScaleSystem.adjRealToPixelY(rect.height)/2 - g.getFont().getHeight(txt)/2)
				);
		
	}
	
	public void update(int delta) {
		if(displaying == null && !toDisplay.isEmpty()) {
			displaying = toDisplay.poll();
			timeRemaining = 5000;
		}else {
			timeRemaining -= delta;
			if(timeRemaining <= 0) {
				displaying = null;
				displayText = null;
				timeRemaining = 0;
			}
		}
	}
}
