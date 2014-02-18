package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.Util;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.lines.DouglasPeucker;
import org.xmlcml.image.lines.PixelPath;
import org.xmlcml.image.processing.PixelIslandList.Operation;

import com.google.common.collect.Multimap;

public class PixelIslandTest {

	public final static Logger LOG = Logger.getLogger(PixelIslandTest.class);
	
	@Test
	@Ignore // non-deterministic?

	public void testAddPixel() {
		List<Pixel> longTList = Fixtures.LONG_T_LIST;
		PixelIsland island = new PixelIsland(longTList);
		List<Pixel> n0 = longTList.get(0).getNeighbours(island);
		Assert.assertEquals("0", 1, n0.size());
		List<Pixel> n1 = longTList.get(1).getNeighbours(island);
		Assert.assertEquals("1", 2, n1.size());
		List<Pixel> n2 = longTList.get(2).getNeighbours(island);
		Assert.assertEquals("2", 3, n2.size());
		List<Pixel> n3 = longTList.get(3).getNeighbours(island);
		Assert.assertEquals("3", 1, n3.size());
		List<Pixel> n4 = longTList.get(4).getNeighbours(island);
		Assert.assertEquals("3", 1, n4.size());
	}
	
	/**
	 * Tests the pixels below.
	 * 
	 * X is right Y is down 
	 * 
	 * +
	 * +
	 * ++
	 * +
	 */
	@Test
	@Ignore // non-deterministic?
	public void testAddPixelWithDiagonal() {
		boolean diagonal = true;
		List<Pixel> longTList = Fixtures.LONG_T_LIST;
		PixelIsland island = new PixelIsland(longTList, diagonal);
		List<Pixel> n0 = longTList.get(0).getNeighbours(island);
		Assert.assertEquals("0", 1, n0.size());
		List<Pixel> n1 = longTList.get(1).getNeighbours(island);
		Assert.assertEquals("1", 2, n1.size());
		List<Pixel> n2 = longTList.get(2).getNeighbours(island);
		Assert.assertEquals("2", 3, n2.size());
		List<Pixel> n3 = longTList.get(3).getNeighbours(island);
		Assert.assertEquals("3", 1, n3.size());
		List<Pixel> n4 = longTList.get(4).getNeighbours(island);
		Assert.assertEquals("4", 1, n4.size());
	}
	
	@Test
	public void testgetTerminalPixels() {
		List<Pixel> lineList = Fixtures.LINE_LIST;
		PixelIsland island = new PixelIsland(lineList);
		List<Pixel> terminalPixels = island.getTerminalPixels();
		Assert.assertEquals("terminal", 2, terminalPixels.size());
	}
	
	@Test
	public void testgetTerminalPixelsL() {
		List<Pixel> lList = Fixtures.L_LIST;
		PixelIsland island = new PixelIsland(lList);
		Assert.assertNotNull("island", island);
		List<Pixel> terminalPixels = island.getTerminalPixels();
		Assert.assertEquals("terminal", 2, terminalPixels.size());
	}
	
	@Test
	public void testgetTerminalPixelsT() {
		List<Pixel> tList = Fixtures.T_LIST;
		PixelIsland island = new PixelIsland(tList);
		List<Pixel> terminalPixels = island.getTerminalPixels();
		Assert.assertEquals("terminal", 3, terminalPixels.size());
		Assert.assertEquals("0", "(1,1)",  terminalPixels.get(0).getInt2().toString());
		Assert.assertEquals("0", "(1,5)",  terminalPixels.get(1).getInt2().toString());
		Assert.assertEquals("0", "(3,3)",  terminalPixels.get(2).getInt2().toString());
	}
	
	@Test
	public void testgetTerminalMaltoryzine1() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIsland island = floodFill.getPixelIslandList().get(1);
		Assert.assertEquals("size", 33, island.size());
//		island.cleanChains();
//		Assert.assertEquals("size", 28, island.size());
	}
	
	@Test
	public void testgetTerminalMaltoryzine0() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIsland island = floodFill.getPixelIslandList().get(0);
		Assert.assertEquals("size", 492, island.size());
//		island.cleanChains();
//		Assert.assertEquals("size", 478, island.size());
	}

	@Test
	public void testCreateSeveralPixelIslands() throws IOException {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIslandList islandList = floodFill.getPixelIslandList();
		Assert.assertEquals("islands", 5, islandList.size());
		int[] islands =   {492, 33,  25,  29,  25};
		int[] terminals = {6,   2,   2,   2,   2};
		int[] count2 =    {409, 12,  23,  27,  23};
		int[] count3 =    {74,  18,  0,   0,   0};
		int[] count4 =    {3,   1,   0,   0,   0};
		int[] count5 =    {0,   0,   0,   0,   0};
		for (int i = 0; i < islandList.size(); i++) {
			
			PixelIsland island = islandList.get(i);
			Assert.assertEquals("island "+i, islands[i], island.size());
			Assert.assertEquals("terminal "+i, terminals[i], island.getTerminalPixels().size());
			Assert.assertEquals("2-nodes "+i, count2[i], island.getNodesWithNeighbours(2).size());
			Assert.assertEquals("3-nodes "+i, count3[i], island.getNodesWithNeighbours(3).size());
			Assert.assertEquals("4-nodes "+i, count4[i], island.getNodesWithNeighbours(4).size());
			Assert.assertEquals("5-nodes "+i, count5[i], island.getNodesWithNeighbours(5).size());
		}
	
		islandList.get(0).flattenNuclei();
		islandList.get(1).flattenNuclei();
		islandList.get(2).flattenNuclei();
		islandList.get(3).flattenNuclei();
		islandList.get(4).flattenNuclei();
		
		
		PixelIsland island1 = islandList.get(1);
		List<Pixel> pixelList = island1.getPixelList();
		for (Pixel pixel : pixelList) {
			LOG.trace("pixel "+pixel.getInt2());
		}
		Pixel pixel32 = pixelList.get(32);
		Assert.assertEquals("pixel32", new Int2(206, 66), pixel32.getInt2());
		island1.createSpanningTree(pixel32);
	
	}
	
	@Test
	public void testCreateLinePixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.LINE_PNG);
		List<PixelPath> pixelPaths = island.createPixelPathList();
		Assert.assertEquals("paths", 1, pixelPaths.size());
		DouglasPeucker douglasPeucker = new DouglasPeucker(2.0);
		List<Real2> reduced = douglasPeucker.reduce(pixelPaths.get(0).getPoints());
		LOG.debug(reduced);
	}

	@Test
	public void testCreateZigzagPixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.ZIGZAG_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 1, segmentArrayList.size());
		island.debugSVG("target/zigzag.svg");
	}

	@Test
	public void testCreateHexagonPixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.HEXAGON_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 1, segmentArrayList.size());
		island.debugSVG("target/hexagon.svg");
	}

	@Test
	public void testCreateBranch0PixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.BRANCH0_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 3, segmentArrayList.size());
		island.debugSVG("target/branch0.svg");
	}

	@Test
	public void testCreateMaltoryzine0PixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.MALTORYZINE0_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 6, segmentArrayList.size());
		island.debugSVG("target/maltoryzine0.svg");
	}

	@Test
	public void testCreateMaltoryzinePixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.MALTORYZINE_THINNED_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 6, segmentArrayList.size());
		island.debugSVG("target/maltoryzine.svg");
	}

	/** this one has a terminal nucleus
	 * 
	 * [point: (50,59); neighbours:  (51,59) (50,58) (49,58); marked:, 
	 *  point: (50,58); neighbours:  (49,58) (51,59) (50,59); marked:,
	 *  point: (51,59); neighbours:  (50,59) (52,60) (50,58); marked:]
	 *  
	 *  (49,58) is marked as a spike but not part of the nucleus. 
	 *  
	 *  also (52,60) and (53,60)
	 *  Finally (54,61) is a terminal and is identified as such.
	 *  
	 *  The search starts at (54,61) and terminates correctly at (49,58)
	 *  
	 * @throws IOException
	 */
	@Test
	public void testCreateTerminalPixelPaths() throws IOException {
		PixelIsland island = createFirstPixelIsland(Fixtures.TERMINAL_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 1, segmentArrayList.size());
		island.debugSVG("target/terminalnode.svg");
	}

	/** this one has 2 terminal nuclei
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateTerminalsPixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.TERMINALS_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		island.debugSVG("target/terminalnodes.svg");
		Assert.assertEquals("segmentArray", 1, segmentArrayList.size());
	}


	/** this one has a terminal nucleus
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateBranchPixelPaths() throws IOException {
		PixelIsland island = createFirstPixelIsland(Fixtures.BRANCH_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 3, segmentArrayList.size());
		debug(segmentArrayList);
		island.debugSVG("target/branch.svg");
	}

	/** this one has a terminal nucleus
	 * 
	 * @throws IOException
	 */
	@Test
	public void testDehypotenuse() throws IOException {
		PixelIsland island = createFirstPixelIsland(Fixtures.TERMINAL_PNG);
		island.removeHypotenuses();
		List<Nucleus> nucleusList = island.getNucleusList();
		LOG.debug("NUC "+nucleusList);
	}

	@Test
	public void testBoundingBox2() throws IOException {
		PixelIsland island = createFirstPixelIsland(Fixtures.MALTORYZINE0_PNG);
		Real2Range bbox = island.getBoundingBox();
		LOG.debug(bbox);;
	}
	
	@Test
	public void testBoundingBoxes() throws IOException {
		PixelIslandList islands = PixelIsland.createPixelIslandList(ImageIO.read(Fixtures.MALTORYZINE_PNG));
		for (PixelIsland island : islands) {
			Real2Range bbox = island.getBoundingBox();
			LOG.debug(island+" "+bbox);
		}
	}
	
	@Test
	public void testLargePhyloJpg() throws IOException {
		PixelIslandList islands = PixelIslandList.createPixelIslandList(Fixtures.LARGE_PHYLO_JPG, Operation.BINARIZE, Operation.THIN);
		Collections.sort(islands.getList(), new PixelIslandComparator(PixelIslandComparator.ComparatorType.SIZE));
		LOG.debug(islands.get(0).size());
		Assert.assertTrue(islands.size() > 2000);
		Assert.assertEquals(2224, islands.size());
		debug(islands, "target/largePhyloBoxes.svg");
	}

//	@Test
//	public void testLargePhyloJpg1() throws IOException {
//		PixelIslandList islands = PixelIslandList.createPixelIslandList(Fixtures.LARGE_PHYLO_JPG);
//		PixelIslandList characters = islands.smallerThan(new Real2(20., 20.));
//		Assert.assertEquals(2202, characters.size());
//		debug(characters, "target/characters.svg");
//	}

	@Test
	public void testLargePhyloJpgChars() throws IOException {
		int heightCount[] = new int[] {
				201,86,7,40,21,74,629,391,148,374, // 0-9
				148,33,3,15,13,9,5,0,2,3,           //10-19                      
				0,3,2,1,0,0};
		PixelIslandList islands = PixelIslandList.createPixelIslandList(Fixtures.LARGE_PHYLO_JPG, Operation.BINARIZE, Operation.THIN);
		PixelIslandList characters = islands.isContainedIn(new RealRange(0., 25.), new RealRange(0., 25.));
		Assert.assertEquals("all chars", 2206, characters.size());
		debug(characters, "target/charsHeight.svg");
		Multimap<Integer, PixelIsland> charactersByHeight = characters.createCharactersByHeight();
		for (Integer height : charactersByHeight.keySet()) {
			PixelIslandList charsi = new PixelIslandList(charactersByHeight.get(height));
			Assert.assertEquals("counts"+height, heightCount[height], charsi.size());
			debug(charsi, "target/chars"+height+".svg");
		}
	}


	@Test
	public void testLargePhyloJpgSmallChars() throws IOException {
		PixelIslandList islands = PixelIslandList.createPixelIslandList(Fixtures.LARGE_PHYLO_JPG, Operation.BINARIZE, Operation.THIN);
		PixelIslandList characters = islands.isContainedIn(new RealRange(0., 20.), new RealRange(0., 1.));
		Assert.assertEquals("all chars", 287, characters.size());
		debug(characters, "target/chars0-1.svg");
	}

	@Test
	public void testLargePhyloJpgLargeChars() throws IOException {
		PixelIslandList islands = PixelIslandList.createPixelIslandList(Fixtures.LARGE_PHYLO_JPG, Operation.BINARIZE, Operation.THIN);
		PixelIslandList characters = islands.isContainedIn(new RealRange(0., 20.), new RealRange(12., 25.));
		Assert.assertEquals("all chars", 54, characters.size());
		debug(characters, "target/charsHeightLarge.svg");
	}

	@Test
	public void testLargePhyloJpgCharsBinarize() throws IOException {
		int heightCount[] = new int[] {
				155,152,18,43,5,9,428,619,6,391, // 0-9
				304,10,17,31,1,4,22,15,1,0,           //10-19                      
				0,8,0,0,0,1};
		PixelIslandList islands = PixelIslandList.createPixelIslandList(Fixtures.LARGE_PHYLO_JPG, Operation.BINARIZE);
		PixelIslandList characters = islands.isContainedIn(new RealRange(0., 25.), new RealRange(0., 25.));
		Assert.assertEquals("all chars", 2240, characters.size());
		debug(characters, "target/charsHeight.svg");
		Multimap<Integer, PixelIsland> charactersByHeight = characters.createCharactersByHeight();
		for (Integer height : charactersByHeight.keySet()) {
			PixelIslandList charsi = new PixelIslandList(charactersByHeight.get(height));
			Assert.assertEquals("counts"+height, heightCount[height], charsi.size());
			LOG.debug(height+" "+charsi.size());
			debug(charsi, "target/charsNoThin"+height+".svg");
		}
	}

	@Test
	public void testLargePhyloJpgCharsReconstruct() throws IOException {
		PixelIslandList islands = PixelIslandList.createPixelIslandList(Fixtures.LARGE_PHYLO_JPG, Operation.BINARIZE);
		PixelIslandList characters = islands.isContainedIn(new RealRange(0., 25.), new RealRange(0., 25.));
		Multimap<Integer, PixelIsland> charactersByHeight = characters.createCharactersByHeight();
		PixelIslandList brackets = new PixelIslandList(charactersByHeight.get(21));
		int nchar = brackets.size();
		Assert.assertEquals("bracket", 8, nchar);
		for (int i = 0; i < nchar; i++) {
			for (int j = i; j < nchar; j++) {
				double cor = Util.format(brackets.get(i).correlation(brackets.get(j), i+" "+j), 2);
				System.out.print(cor+" ");
			}
			System.out.println();
		}
	}

	@Test
	/** correlates every character with every other.
	 * very crude. Finds sets of highly correlated characters and labels first one on
	 * diagram
	 * @throws IOException
	 */
	public void testLargePhyloJpgCharsReconstruct1() throws IOException {
		double correlation = 0.75;
		String colors[] = {"red", "blue", "green", "yellow", "purple", "cyan", "brown", "pink", "lime", "orange"};
		PixelIslandList islands = PixelIslandList.createPixelIslandList(Fixtures.LARGE_PHYLO_JPG, Operation.BINARIZE);
		PixelIslandList characters = islands.isContainedIn(new RealRange(0., 15.), new RealRange(0., 12.));
		Multimap<Integer, PixelIsland> charactersByHeight = characters.createCharactersByHeight();
		PixelIslandList chars = new PixelIslandList(charactersByHeight.get(10));
		Collections.sort(chars.getList(), new PixelIslandComparator(
				PixelIslandComparator.ComparatorType.TOP, PixelIslandComparator.ComparatorType.LEFT));
		int nchar = chars.size();
		Assert.assertEquals("10-high characters", 304, nchar);
		List<List<Integer>> groupList = new ArrayList<List<Integer>>();
		Set<Integer> usedSet = new HashSet<Integer>();
		SVGG allg = new SVGG();
		for (int i = 0; i < Math.min(2000, nchar); i++) {
			if (usedSet.contains(i)) continue;
			usedSet.add(i);
			SVGG g = new SVGG();
			List<Integer> newList = new ArrayList<Integer>();
			newList.add(i);
			groupList.add(newList);
			for (int j = i+1; j < nchar; j++) {
				if (usedSet.contains(j)) continue;
				double cor = Util.format(chars.get(i).correlation(chars.get(j), "dummy"), 2);
				if (cor > correlation) {
					newList.add(j);
					usedSet.add(j);
					System.out.print(i+" "+j+": "+cor+" ");
					g.appendChild(chars.get(j).createSVG(true));
				}
			}
			Collections.sort(newList);
			SVGG gk = null;
			for (Integer k : newList) {
				gk = chars.get(k).createSVG(true);
				g.appendChild(gk);
			}
			gk = new SVGG(gk);
			SVGText text = new SVGText(gk.getBoundingBox().getCorners()[0], String.valueOf(newList.get(0)));
			gk.appendChild(text);
			text.setOpacity(0.6);
			text.setFontSize(20.0);
			text.setFill("green");
			allg.appendChild(gk);
			SVGSVG.wrapAndWriteAsSVG(g, new File("target/charsCorr"+newList.get(0)+".svg"));
			System.out.println();
		}
		SVGSVG.wrapAndWriteAsSVG(allg, new File("target/charsCorrAll.svg"));
		Collections.sort(groupList, new ListComparator());
		for (List<Integer> listi : groupList) {
			System.out.println(listi);
		}
		
	}

	@Test
	/** correlates every character with every other.
	 * very crude. Finds sets of highly correlated characters and labels first one on
	 * diagram
	 * @throws IOException
	 */
	public void testLargePhyloJpgCharsCorrelateA() throws IOException {
		// "A"s selected manually
		int[] charsA = {90, 274, 
				97, 
				98, 133, 202, 283,
				136, 143,
				1,2 // dummies
		};
		BufferedImage rawImage = ImageIO.read(Fixtures.LARGE_PHYLO_JPG);
		ImageIO.write(rawImage, "jpg", new File("target/test.jpg"));
		ImageIO.write(rawImage, "png", new File("target/test.png"));
		BufferedImage subImage = org.xmlcml.image.Util.clipSubImage(rawImage, new Int2Range(new IntRange(50, 100), new IntRange(150, 300)));
		Assert.assertEquals(50, subImage.getWidth());
		Assert.assertEquals(150, subImage.getHeight());
		ImageIO.write(subImage, "png", new File("target/testclip.png"));
		PixelIslandList islands = PixelIslandList.createPixelIslandList(Fixtures.LARGE_PHYLO_JPG, Operation.BINARIZE);
		PixelIslandList characters = islands.isContainedIn(new RealRange(0., 15.), new RealRange(0., 12.));
		Multimap<Integer, PixelIsland> charactersByHeight = characters.createCharactersByHeight();
		PixelIslandList chars = new PixelIslandList(charactersByHeight.get(10));
		Collections.sort(chars.getList(), new PixelIslandComparator(
				PixelIslandComparator.ComparatorType.TOP, PixelIslandComparator.ComparatorType.LEFT));
		PixelIslandList islandsA = new PixelIslandList();
		for (int charA : charsA) {
			PixelIsland island = chars.get(charA);
			islandsA.add(island);
			Int2Range ibbox = island.getIntBoundingBox();
			BufferedImage subImage1 = org.xmlcml.image.Util.clipSubImage(rawImage, ibbox);
			ImageIO.write(subImage1, "png", new File("target/clip"+charA+".png"));
		}
		int nchar = islandsA.size();
		System.out.println("size: "+nchar);
		for (int i = 0; i < nchar; i++) {
			for (int j = i; j < nchar; j++) {
				double cor = Util.format(islandsA.get(i).correlation(islandsA.get(j), i+"-"+j), 2);
				System.out.print(i+"-"+j+": "+cor+" ");
			}
			System.out.println();
		}
	}
	
//	private void debug(PixelIsland pixelIsland, String filename) {
//		SVGG g = pixelIsland.createSVG(true);
//		SVGSVG.wrapAndWriteAsSVG(g, new File(filename));
//	}

	@Test
	@Ignore // it looks like a bad idea to omit binarization on antialised jpegs.
	public void testLargePhyloJpgCharsRaw() throws IOException {
		int heightCount[] = new int[] {
//				155,152,18,43,5,9,428,619,6,391, // 0-9
//				304,33,3,15,13,9,5,0,2,3,           //10-19                      
//				0,3,2,1,0,0};
		};
		PixelIslandList islands = PixelIslandList.createPixelIslandList(Fixtures.LARGE_PHYLO_JPG);
		PixelIslandList characters = islands.isContainedIn(new RealRange(0., 25.), new RealRange(0., 25.));
		Assert.assertEquals("all chars", 4146, characters.size());
		debug(characters, "target/charsRaw.svg");
		Multimap<Integer, PixelIsland> charactersByHeight = characters.createCharactersByHeight();
		for (Integer height : charactersByHeight.keySet()) {
			PixelIslandList charsi = new PixelIslandList(charactersByHeight.get(height));
//			Assert.assertEquals("counts"+height, heightCount[height], charsi.size());
			LOG.debug(height+" "+charsi.size());
			debug(charsi, "target/charsRaw"+height+".svg");
		}
	}

	// =============================================================

	private void debug(PixelIslandList islands, String filename) {
		SVGG g = new SVGG();
		for (PixelIsland island : islands) {
			Real2Range bbox = island.getBoundingBox();
			LOG.trace(island+" "+island.size()+" "+bbox);
			SVGRect rect = new SVGRect(bbox);
			g.appendChild(rect);
			SVGG gg = island.createSVG(true);
			int n = gg.getChildCount();
			for (int i = n - 1; i >= 0; i--) {
				SVGElement ggg = (SVGElement) gg.getChild(i);
				ggg.detach();
				g.appendChild(ggg);
			}
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File(filename));
	}
	
	// =====================================================
	
	private void debug(List<Real2Array> segmentArrayList) {
		for (Real2Array coords : segmentArrayList) {
			System.out.println(coords);
		}
	}

	private PixelIsland createFirstPixelIsland(File file) throws IOException {
		return PixelIsland.createPixelIslandList(ImageIO.read(file)).get(0);
	}


}
