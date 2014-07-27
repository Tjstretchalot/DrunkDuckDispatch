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

package me.timothy.ddd.resources;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;

public class Resources {
	private static HashMap<String, Image> images;
	private static HashMap<String, SpriteSheet> animations;
	private static HashMap<String, Audio> sounds;
	private static Random random;
	
	private static Logger logger;

	public static void init() {
		images = new HashMap<>();
		animations = new HashMap<>();
		sounds = new HashMap<>();
		logger = LogManager.getLogger();
		random = new Random();
	}

	public static void initRes() {
		logger.info("Initializing resources..");
		try {
			List<File> toCheck = new ArrayList<>();
			toCheck.addAll(Arrays.asList(new File(".").listFiles()));
			toCheck.addAll(Arrays.asList(new File("resources").listFiles()));
			toCheck.addAll(Arrays.asList(new File("sounds").listFiles()));
			
			for(File f : toCheck) {
				if(f.isDirectory())
					continue;
				if(f.getName().endsWith("png")) {
					Image img = read(f);
					if(f.getName().startsWith("couch-")) {
						images.put(f.getName(), img);
						images.put(f.getName() + "-inverted", img.getFlippedCopy(false, true));
					}else if(f.getName().equals("arrowgant.png")) {
						SpriteSheet sheet = new SpriteSheet(img, 4, 1);
						animations.put("arrowgant", sheet);
					}else if(f.getName().equals("swordman.png")) {
						SpriteSheet sheet = new SpriteSheet(img, 3, 1);
						animations.put("swordman", sheet);
					}else {
						images.put(f.getName(), img);
					}
				}else if(f.getName().endsWith("ogg")) {
					sounds.put(f.getName(), AudioLoader.getStreamingAudio("OGG", f.toURI().toURL()));
				}
			}
		} catch (SlickException | IOException e) {
			e.printStackTrace();
		}
		logger.printf(Level.INFO, "Done initializing resources (Loaded %d images, %d animations, and %d sounds)", images.size(), animations.size(), sounds.size());
	}
	
	public static Image read(File file) throws SlickException {
		return new Image(file.getAbsolutePath());
	}

	public static Image getImage(String string) {
		return images.get(string);
	}

	public static SpriteSheet getSheet(String string) {
		return animations.get(string);
	}

	public static Audio getRandomAudio() {
		Set<String> keys = sounds.keySet();
		return sounds.get(keys.toArray()[random.nextInt(keys.size())]);
	}
	
	public static void downloadIfNotExists(String fileName, String url, long expectedSize) {
		downloadIfNotExists(new File("."), fileName, url, null, null, null, expectedSize);
	}
	public static void downloadIfNotExists(File dir, String fileName,
			String downloadURL, String messageIfINeedToDownload, 
			String messageOnError, Runnable runOnError, long expectedSize) {
		if(logger == null)
			init();
		File checking = new File(dir, fileName);
		if(checking.exists())
			return;
		if(!dir.exists())
			dir.mkdirs();
		if(!dir.isDirectory()) {
			logger.error("Expected a directory at " + dir.getAbsolutePath());
			System.exit(1);
		}

		logger.log(Level.DEBUG, "Have to download " + fileName + ", potentially asking user");
		if(messageIfINeedToDownload != null) {
			int resp = JOptionPane.showConfirmDialog(null, messageIfINeedToDownload);
			if(resp != JOptionPane.YES_OPTION) {
				logger.log(Level.DEBUG, "User said not to, cancelling");
				System.exit(0);
			}
		}
		logger.log(Level.DEBUG, "Given permission, starting download");

		try {
			URL url = new URL(downloadURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream inStream = conn.getInputStream();
			FileOutputStream outStream = new FileOutputStream(checking);
			copyInputStream(inStream, outStream, expectedSize, true);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Couldn't complete download at " + downloadURL + ", io exception", e);
			
			logger.throwing(Level.ERROR, e);
			if(runOnError == null) {
				throw new RuntimeException(e);
			}
			if(JOptionPane.showConfirmDialog(null, messageOnError) == JOptionPane.YES_OPTION)
				runOnError.run();
			System.exit(1);
		}
		
	}

	public static void extractIfNotFound(File dir, String zipFileStrInDir, String fileToSearchFor) {
		
		
		File toSearchFor = new File(dir, fileToSearchFor);
		if(toSearchFor.exists())
			return;
		File zipFileReal = new File(dir, zipFileStrInDir);
		String zipFileStr = zipFileReal.getAbsolutePath();
		// unzip http://www.devx.com/getHelpOn/10MinuteSolution/20447
		try {
			ZipFile zipFile = new ZipFile(zipFileStr);
			JFrame frame = new JFrame();
			frame.setLocationRelativeTo(null);
			JProgressBar progressBar = new JProgressBar();
			progressBar.setMaximum((int) zipFileReal.length());
			progressBar.setToolTipText("Extracting");
			frame.add(progressBar);
			frame.pack();
			frame.setVisible(true);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();

				if(entry.isDirectory()) {
					// Assume directories are stored parents first then children.
					logger.log(Level.DEBUG, "Extracting directory: " + entry.getName());
					// This is not robust, just for demonstration purposes.
					(new File(dir, entry.getName())).mkdir();
					continue;
				}

				logger.log(Level.DEBUG, "Extracting file: " + entry.getName());
				File resultingFile = new File(dir, entry.getName());
				File resultFileDir = resultingFile.getParentFile();
				if(!resultFileDir.exists())
					resultFileDir.mkdirs();
				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(new File(dir, entry.getName()))), entry.getSize(), false);
				progressBar.setValue((int) (progressBar.getValue() + entry.getCompressedSize()));
			}
			progressBar.setValue(progressBar.getMaximum());
			zipFile.close();
			frame.dispose();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			logger.catching(Level.ERROR, ioe);
			System.exit(1);
		}
	}

	private static void copyInputStream(InputStream in, OutputStream out, long expectedSize, boolean makeProgressBar)
			throws IOException {
		JProgressBar progressBar = null;
		JFrame frame = null;
		if(makeProgressBar) {
			frame = new JFrame();
			frame.setLocationRelativeTo(null);
			progressBar = new JProgressBar();
			progressBar.setMaximum((int) expectedSize);
			frame.add(progressBar);
			frame.pack();
			frame.setVisible(true);
		}
		byte[] buffer = new byte[1024];
		int len;
		int totalBytes = 0;
		while((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
			totalBytes += len;
			if(makeProgressBar)
				progressBar.setValue(totalBytes);
		}

		in.close();
		out.close();
		if(makeProgressBar)
			frame.dispose();
	}
}
