package com.github.alex1304.jdash.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import javax.imageio.ImageIO;

import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;

import com.github.alex1304.jdash.entity.IconType;
import com.github.alex1304.jdash.exception.SpriteLoadException;
import com.github.alex1304.jdash.util.Utils;

public class SpriteFactory {
	public static final Map<Integer, Color> COLORS = colors();
	
	private static BufferedImage spriteImg;
	private static final Map<String, List<Sprite>> SPRITES = new HashMap<>();
	private static final Object LOCK = new Object();
	private static volatile boolean loaded = false;
	
	private SpriteFactory() {
	}
	
	public BufferedImage makeSprite(IconType type, int id, int color1Id, int color2Id, boolean withGlowOutline) {
		if (!COLORS.containsKey(color1Id)) {
			throw new IllegalArgumentException("Color1 ID=" + color1Id + " does not exist");
		}
		if (!COLORS.containsKey(color2Id)) {
			throw new IllegalArgumentException("Color2 ID=" + color2Id + " does not exist");
		}
		List<Sprite> spList = SPRITES.get(type.name() + id);
		if (spList == null) {
			throw new IllegalArgumentException("Sprite ID=" + id + " for icon type " + type.name() + " does not exist");
		}
		spList = new ArrayList<>(spList);
		BufferedImage img = new BufferedImage(250, 250, spriteImg.getType());
		Graphics2D g = img.createGraphics();
		if (!withGlowOutline) {
			spList.removeIf(sp -> sp.getName().contains("_glow_"));
		}
		orderSprites(spList);
		for (Sprite sp : spList) {
			// Variables
			String name = sp.getName();
			int x = sp.getRectangle().x;
			int y = sp.getRectangle().y;
			int width = sp.getSizeX();
			int height = sp.getSizeY();
			int offX = sp.getOffsetX();
			int offY = sp.getOffsetY();
			int realWidth = sp.isRotated() ? sp.getRectangle().height : sp.getRectangle().width;
			int realHeight = sp.isRotated() ? sp.getRectangle().width : sp.getRectangle().height;
			Image subimg = spriteImg.getSubimage(x, y, realWidth, realHeight);
			int centerX = width / 2;
			int centerY = height / 2;
			// Fix orientation if rotated
			if (sp.isRotated()) {
				subimg = rotate(subimg, -90);
			}
			// Reduce brightness of robot/spider back legs
			if (name.contains("001D") && !name.contains("_glow_")) {
				subimg = reduceBrightness(subimg);
			}
			// Offset modifiers for robot legs
			if (name.contains("robot")) {
				if (name.contains(id + "_02_")) {
					subimg = rotate(subimg, 45);
					offX -= name.contains("001D") ? 50 : 40;
					offY -= 20;
					if (name.contains("_2_")) {
						switch (id) {
							case 2:
							case 5:
							case 6:
							case 8:
							case 9:
							case 11:
							case 12:
							case 15:
							case 17:
							case 24:
								offX += 15;
								offY -= 5;
								break;
							case 7:
							case 10:
							case 19:
							case 20:
								offX += 7;
								break;
							case 13:
								offX += 10;
								offY -= 4;
								break;
							case 18:
								offX -= 1;
								offY -= 1;
								break;
							case 21:
							case 25:
								offX += 12;
								break;
							case 22:
								offY -= 5;
								break;
							case 3:
							case 26:
								offX += 1;
								break;
							case 23:
								offX -= 3;
								offY -= 2;
								break;
						}
					}
				} else if (name.contains(id + "_03_")) {
					subimg = rotate(subimg, -45);
					offX -= name.contains("001D") ? 40 : 30;
					offY -= id == 21 && !name.contains("_2_") ? 52 : 60;
				} else if (name.contains(id + "_04_")) {
					offX -= name.contains("001D") ? 10 : 00;
					offY -= 70;
				}
			}
			// Offset modifiers for spider legs
			if (name.contains("spider")) {
				if (name.contains(id + "_02_") && name.endsWith("1D")) {
					offX += 18;
					offY -= 38;
				} else if (name.contains(id + "_02_") && name.endsWith("1DD")) {
					offX += 55;
					offY -= 38;
					if (name.contains("_2_")) {
						offX += 1;
					}
					subimg = horizontalFlip(subimg);
				} else if (name.contains(id + "_02_")) {
					offX -= 16;
					offY -= 38;
				} else if (name.contains(id + "_03_")) {
					offX -= 86;
					offY -= 38;
					if (id == 7) {
						offX += 15;
						offY += 13;
					}
					if (id == 15) {
						offX += 5;
						offY += 3;
					}
					if (name.contains("_2_")) {
						switch (id) {
							case 16:
								offY += 5;
								break;
							case 2:
							case 3:
								offX += 25;
								break;
							case 10:
								offX += 18;
								offY -= 5;
								break;
						}
					}
					if (name.contains("_glow_")) {
						offY += 3;
					}
					subimg = rotate(subimg, 45);
				} else if (name.contains(id + "_04_")) {
					offX -= 30;
					offY -= 20;
				}
			}
			
			// Apply player colors
			if (name.contains("_glow_")) {
				subimg = applyColor(subimg, getGlowColor(color1Id, color2Id));
			} else if (name.contains("_2_00")) {
				subimg = applyColor(subimg, COLORS.get(color2Id));
			} else if (!name.contains("extra") && !name.contains("_3_00")) {
				subimg = applyColor(subimg, COLORS.get(color1Id));
			}
			// Draw the result
			int drawX = 100 - centerX + offX;
			int drawY = 100 - centerY - offY;
			int drawOffX = 0, drawOffY;
			switch (type) {
				case ROBOT:
					drawOffY = -20;
					break;
				case SPIDER:
					drawOffX = 6;
					drawOffY = -5;
					break;
				case UFO:
					drawOffY = 30;
					break;
				default:
					drawOffY = 0;
					break;
			}
			g.drawImage(subimg, 25 + drawOffX + drawX, 25 + drawOffY + drawY, null);
		}
		g.dispose();
		if (withGlowOutline && type != IconType.ROBOT && type != IconType.SPIDER) {
			addGlow(img, color1Id, color2Id);
		}
		return img;
	}
	
	private static void orderSprites(List<Sprite> spList) {
		Collections.reverse(spList);
		pushSpriteToBackIf(spList, sp -> sp.getName().contains("_2_"));
		pullSpriteToFrontIf(spList, sp -> sp.getName().matches("(robot|spider)_[0-9]{2,3}_(02|03|04)_.*"));
		dupeSpriteIf(spList, sp -> sp.getName().matches("robot_[0-9]{2,3}_(02|03|04)_.*"), 1, false);
		dupeSpriteIf(spList, sp -> sp.getName().matches("spider_[0-9]{2,3}_02_.*") && !sp.getName().contains("extra"), 2, false);
		pullSpriteToFrontIf(spList, sp -> sp.getName().matches("robot_[0-9]{2,3}_02_.*") && !sp.getName().endsWith("D"));
		pullSpriteToFrontIf(spList, sp -> sp.getName().matches("robot_[0-9]{2,3}_04_.*") && !sp.getName().endsWith("D"));
		pushSpriteToBackIf(spList, sp -> !sp.getName().contains("_2_") && sp.getName().endsWith("D"));
		pushSpriteToBackIf(spList, sp -> sp.getName().contains("_2_") && sp.getName().endsWith("D"));
		pushSpriteToBackIf(spList, sp -> sp.getName().matches("spider_[0-9]{2,3}_04_.*"));
		pullSpriteToFrontIf(spList, sp -> sp.getName().contains("extra"));
		pushSpriteToBackIf(spList, sp -> sp.getName().contains("_glow_"));
	}
	
	private static void pullSpriteToFrontIf(List<Sprite> spList, Predicate<Sprite> cond) {
		int offset = 0;
		for (int i = 0 ; i < spList.size() ; i++) {
			Sprite sp = spList.get(i - offset);
			if (cond.test(sp)) {
				spList.remove(i - offset);
				spList.add(sp);
				offset++;
			}
		}
	}
	
	private static void pushSpriteToBackIf(List<Sprite> spList, Predicate<Sprite> cond) {
		for (int i = 0 ; i < spList.size() ; i++) {
			Sprite sp = spList.get(i);
			if (cond.test(sp)) {
				spList.remove(i);
				spList.add(0, sp);
			}
		}
	}
	
	private static void dupeSpriteIf(List<Sprite> spList, Predicate<Sprite> cond, int nbDup, boolean front) {
		final int initialSize = spList.size();
		int offset = 0;
		for (int i = 0 ; i < initialSize ; i++) {
			Sprite sp = spList.get(i + offset);
			if (cond.test(sp)) {
				Sprite dupe = sp.duplicate();
				for (int d = 0 ; d < nbDup ; d++) {
					if (front) {
						spList.add(dupe);
					} else {
						spList.add(0, dupe);
						offset++;
					}
					dupe = dupe.duplicate();
				}
			}
		}
	}

	private static Image rotate(Image img, double deg) {
		deg = deg % 360 + (Math.abs(deg) > 180 ? -Math.signum(deg) * 360 : 0);
		double rad = Math.toRadians(deg);
		int width = img.getWidth(null);
		int height = img.getHeight(null);
		// surrounding rectangle
		Rectangle2D newImgRect = AffineTransform.getRotateInstance(rad, 0, 0)
				.createTransformedShape(new Rectangle(width, height)).getBounds2D();
		int newWidth = (int) newImgRect.getWidth();
		int newHeight = (int) newImgRect.getHeight();
		BufferedImage newImage = new BufferedImage(newWidth, newHeight, spriteImg.getType());
		Graphics2D g2 = newImage.createGraphics();
		int tx, ty;
		if (deg < 0 && deg >= -90) {
			tx = 0;
			ty = (int) (width * Math.abs(Math.sin(rad)));
		} else if (deg < 90 && deg >= 0) {
			tx = (int) (height * Math.abs(Math.sin(rad)));
			ty = 0;
		} else if (deg < -90 && deg >= -180) {
			tx = (int) (width * Math.abs(Math.cos(rad)));
			ty = newHeight;
		} else {
			tx = newWidth;
			ty = (int) (height * Math.abs(Math.cos(rad)));
		}
		g2.translate(tx, ty);
		g2.rotate(rad, 0, 0);
		g2.drawImage(img, 0, 0, null);
		return newImage;
	}
	
	private static Image horizontalFlip(Image img) {
		int width = img.getWidth(null), height = img.getHeight(null);
		BufferedImage newImg = new BufferedImage(width, height, spriteImg.getType());
		Graphics2D g = newImg.createGraphics();
		AffineTransform flipTr = AffineTransform.getScaleInstance(-1, 1);
		flipTr.concatenate(AffineTransform.getTranslateInstance(-width, 0));
		g.transform(flipTr);
		g.drawImage(img, 0, 0, null);
		return newImg;
	}
	
	private static Image applyColor(Image img, Color color) {
		return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(img.getSource(), new RGBImageFilter() {
			@Override
			public int filterRGB(int x, int y, int rgb) {
				return rgb & color.getRGB();
			}
		}));
	}
	
	private static Image reduceBrightness(Image img) {
		return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(img.getSource(), new RGBImageFilter() {
			@Override
			public int filterRGB(int x, int y, int rgb) {
				return rgb & 0xFF808080;
			}
		}));
	}
	
	private static Color getGlowColor(int color1Id, int color2Id) {
		if (color2Id == 15) {
			color2Id = color1Id == 15 ? 12 : color1Id;
		}
		return COLORS.get(color2Id);
	}
	
	private static void addGlow(BufferedImage img, int color1Id, int color2Id) {
		// White glow if both colors are black. If color2 is black, use color1 instead.
		final Color color = getGlowColor(color1Id, color2Id);
		final int w = img.getWidth(), h = img.getHeight();
		final int treshold = 120;
		final int glowWidth = 4;
		// create an array of distances (1 per pixel) and fill it with -1's
		int[][] distances = new int[h][w];
		for (int i = 0 ; i < h ; i++) {
			Arrays.fill(distances[i], -1);
		}
		// for each pixel, mark the black ones as distance 0, and add them to a deque
		ArrayDeque<Point> deque = new ArrayDeque<>();
		for (int y = 0; y < h; y++) {
		    for (int x = 0; x < w; x++) {
				int data = img.getRGB(x, y);
				int alpha = (data & 0xff000000) >> 24;
				int red = (data & 0x00ff0000) >> 16;
				int green = (data & 0x0000ff00) >> 8;
				int blue = data & 0x000000ff;
				if (red < treshold && green < treshold && blue < treshold && (alpha > 255 - treshold || alpha == -1)) {
					distances[y][x] = 0;
					deque.add(new Point(x, y));
				} 
		    }
		}
		// for each pixel in deque, look at pixels immediately around that are -1,
		// change them to the value of distance + 1 and add them to deque
		while (!deque.isEmpty()) {
			Point pix = deque.remove();
			int pixDistance = distances[pix.y][pix.x];
			for (int y = Math.max(0, pix.y - 1) ; y <= Math.min(pix.y + 1, h - 1) ; y++) {
				for (int x = Math.max(0, pix.x - 1) ; x <= Math.min(pix.x + 1, w - 1) ; x++) {
					int lookAtDistance = distances[y][x];
					if (lookAtDistance == -1) {
						distances[y][x] = pixDistance + 1;
						deque.add(new Point(x, y));
					}
				}
			}
		}
		// Colorize all pixels that have a distance value less than or equal to glowWidth
		for (int y = 0; y < h; y++) {
		    for (int x = 0; x < w; x++) {
				int alpha = (img.getRGB(x, y) & 0xff000000) >> 24;
		    	// Apply color only if transparent pixel
		    	if (alpha != -1 && alpha < treshold && distances[y][x] <= glowWidth) {
		    		img.setRGB(x, y, color.getRGB());
		    	}
		    }
		}
	}
	
	@Override
	public String toString() {
		return SPRITES.toString();
	}
	
	private static void loadSprites(XMLPropertyListConfiguration spritePlist) {
		final Map<String, IconType> prefixes = new LinkedHashMap<>();
		prefixes.put("frames.player_ball_", IconType.BALL);
		prefixes.put("frames.player_", IconType.CUBE);
		prefixes.put("frames.ship_", IconType.SHIP);
		prefixes.put("frames.bird_", IconType.UFO);
		prefixes.put("frames.dart_", IconType.WAVE);
		prefixes.put("frames.robot_", IconType.ROBOT);
		prefixes.put("frames.spider_", IconType.SPIDER);
		final Map<String, Map<String, double[]>> offsets = new HashMap<>();
		final Map<String, Map<String, double[]>> sizes = new HashMap<>();
		final Map<String, Map<String, double[]>> sourceSizes = new HashMap<>();
		final Map<String, Map<String, double[]>> rectangles = new HashMap<>();
		final Map<String, Map<String, Boolean>> rotatedStates = new HashMap<>();
		Iterator<String> it = spritePlist.getKeys();
		while (it.hasNext()) {
			String key = it.next();
			for (Entry<String, IconType> prefix : prefixes.entrySet()) {
				if (key.startsWith(prefix.getKey())) {
					String partKey = prefix.getValue().name() + Utils.partialParseInt(key.substring(prefix.getKey().length()));
					final String f_key = key;
					if (key.endsWith("spriteOffset")) {
						offsets.compute(partKey, (k, v) -> {
							if (v == null) {
								v = new LinkedHashMap<>();
							}
							v.put(f_key, parseSimpleTuple(spritePlist.getString(f_key)));
							return v;
						});
					} else if (key.endsWith("spriteSize")) {
						sizes.compute(partKey, (k, v) -> {
							if (v == null) {
								v = new LinkedHashMap<>();
							}
							v.put(f_key, parseSimpleTuple(spritePlist.getString(f_key)));
							return v;
						});
					} else if (key.endsWith("spriteSourceSize")) {
						sourceSizes.compute(partKey, (k, v) -> {
							if (v == null) {
								v = new LinkedHashMap<>();
							}
							v.put(f_key, parseSimpleTuple(spritePlist.getString(f_key)));
							return v;
						});
					} else if (key.endsWith("textureRect")) {
						rectangles.compute(partKey, (k, v) -> {
							if (v == null) {
								v = new LinkedHashMap<>();
							}
							v.put(f_key, parseDoubleTuple(spritePlist.getString(f_key)));
							return v;
						});
					} else if (key.endsWith("textureRotated")) {
						rotatedStates.compute(partKey, (k, v) -> {
							if (v == null) {
								v = new LinkedHashMap<>();
							}
							v.put(f_key, spritePlist.getBoolean(f_key));
							return v;
						});
					}
					break;
				}
			}
		}
		for (String key : offsets.keySet()) {
			List<Sprite> spList = new ArrayList<>();
			Iterator<Entry<String, double[]>> offsetIt = offsets.get(key).entrySet().iterator();
			Iterator<double[]> sizeIt = sizes.get(key).values().iterator();
			Iterator<double[]> sourceSizeIt = sourceSizes.get(key).values().iterator();
			Iterator<double[]> rectangleIt = rectangles.get(key).values().iterator();
			Iterator<Boolean> rotatedIt = rotatedStates.get(key).values().iterator();
			while (offsetIt.hasNext()) {
				Entry<String, double[]> entry = offsetIt.next();
				String spriteName = entry.getKey().split("\\.")[1];
				double[] offset = entry.getValue();
				double[] size = sizeIt.next();
				double[] sourceSize = sourceSizeIt.next();
				double[] rectangle = rectangleIt.next();
				boolean isRotated = rotatedIt.next();
				spList.add(new Sprite(spriteName, (int) offset[0], (int) offset[1], (int) size[0], (int) size[1], (int) sourceSize[0], (int) sourceSize[1],
						new Rectangle((int) rectangle[0], (int) rectangle[1], (int) rectangle[2], (int) rectangle[3]), isRotated));
			}
			SPRITES.put(key, spList);
		}
	}
	
	private static double[] parseSimpleTuple(String tupleStr) {
		String[] split = tupleStr.substring(1, tupleStr.length() - 1).split(",");
		return new double[] { Double.parseDouble(split[0]), Double.parseDouble(split[1]) };
	}
	
	private static double[] parseDoubleTuple(String tupleStr) {
		String[] split = tupleStr.substring(2, tupleStr.length() - 2).split("\\}?,\\{?");
		return new double[] { Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]) };
	}
	
	private static Map<Integer, Color> colors() {
		Map<Integer, Color> colors = new HashMap<>();
		colors.put(0, new Color(125, 255, 0));
		colors.put(1, new Color(0, 255, 0));
		colors.put(2, new Color(0, 255, 125));
		colors.put(3, new Color(0, 255, 255));
		colors.put(4, new Color(0, 125, 255));
		colors.put(5, new Color(0, 0, 255));
		colors.put(6, new Color(125, 0, 255));
		colors.put(7, new Color(255, 0, 255));
		colors.put(8, new Color(255, 0, 125));
		colors.put(9, new Color(255, 0, 0));
		colors.put(10, new Color(255, 125, 0));
		colors.put(11, new Color(255, 255, 0));
		colors.put(12, new Color(255, 255, 255));
		colors.put(13, new Color(185, 0, 255));
		colors.put(14, new Color(255, 185, 0));
		colors.put(15, new Color(0, 0, 0));
		colors.put(16, new Color(0, 200, 255));
		colors.put(17, new Color(175, 175, 175));
		colors.put(18, new Color(90, 90, 90));
		colors.put(19, new Color(255, 125, 125));
		colors.put(20, new Color(0, 175, 75));
		colors.put(21, new Color(0, 125, 125));
		colors.put(22, new Color(0, 75, 175));
		colors.put(23, new Color(75, 0, 175));
		colors.put(24, new Color(125, 0, 125));
		colors.put(25, new Color(175, 0, 75));
		colors.put(26, new Color(175, 75, 0));
		colors.put(27, new Color(125, 125, 0));
		colors.put(28, new Color(75, 175, 0));
		colors.put(29, new Color(255, 75, 0));
		colors.put(30, new Color(150, 50, 0));
		colors.put(31, new Color(150, 100, 0));
		colors.put(32, new Color(100, 150, 0));
		colors.put(33, new Color(0, 150, 100));
		colors.put(34, new Color(0, 100, 150));
		colors.put(35, new Color(100, 0, 150));
		colors.put(36, new Color(150, 0, 100));
		colors.put(37, new Color(150, 0, 0));
		colors.put(38, new Color(0, 150, 0));
		colors.put(39, new Color(0, 0, 150));
		colors.put(40, new Color(125, 255, 175));
		colors.put(41, new Color(125, 125, 255));
		return Collections.unmodifiableMap(colors);
	}

	/**
	 * Creates a new sprite factory. This method will load the sprite images in
	 * memory.
	 * 
	 * @return the sprite factory
	 * @throws SpriteLoadException if something goes wrong when loading the sprites.
	 */
	public static SpriteFactory create() throws SpriteLoadException {
		try {
			if (!loaded) {
				synchronized (LOCK) {
					if (!loaded) {
						spriteImg = ImageIO.read(SpriteFactory.class.getResource("/GJ_GameSheet02-uhd.png"));
						loadSprites(new Configurations()
								.fileBased(XMLPropertyListConfiguration.class, SpriteFactory.class
										.getResource("/GJ_GameSheet02-uhd.plist")));
						loaded = true;
					}
				}
			}
			return new SpriteFactory();
		} catch (IOException | ConfigurationException e) {
			throw new SpriteLoadException(e);
		}
	}
}
