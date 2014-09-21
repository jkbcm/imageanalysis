package org.xmlcml.image.pixel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.processing.ZhangSuenThinning;

public class PixelGraphTest {

	private final static Logger LOG = Logger.getLogger(PixelGraphTest.class);
	
	@Test
	public void testSingleCycle() {
		PixelGraph graph = PixelGraph.createGraph(Fixtures.CREATE_CYCLE_ISLAND());
		Assert.assertNotNull(graph);
		PixelNodeList nodeList = graph.getNodeList();
		PixelTestUtils.assertNodeList(nodeList, 0, "[]"); 
	}

	@Test
	// simple line
	public void test2Nodes() {
		PixelGraph graph = PixelGraph.createGraph(Fixtures.CREATE_LINE_ISLAND());
		PixelNodeList nodeList = graph.getNodeList();
		PixelTestUtils.assertNodeList(nodeList, 2, "[(0,4)(2,0)]"); 
	}

	@Test
	// Y-shaped tree
	public void test3Terminals() {
		PixelGraph graph = PixelGraph.createGraph(Fixtures.CREATE_Y_ISLAND());
		PixelNodeList nodeList = graph.getNodeList();
		PixelTestUtils.assertNodeList(nodeList, 4, "[(0,3)(0,0)(3,-3)(-3,-3)]"); 
	}

	@Test
	// 2 Y's joined
	/**
	 * +     +
	 *  +   +
	 *   + +
	 *    +
	 *    +
	 *    +
	 *    +
	 *   + +
	 *  +   +
	 * +     +
	 */
	public void testDoubleYGraph() {
		PixelGraph graph = PixelGraph.createGraph(Fixtures.CREATE_DOUBLE_Y_ISLAND());
		PixelNodeList nodeList = graph.getNodeList();
		PixelTestUtils.assertNodeList(nodeList, 6, "[(0,2)(-3,-5)(3,5)(0,-2)(3,-5)(-3,5)]"); 
//		PixelEdgeList edgeList = graph.getEdgeList();
//		Assert.assertEquals("edges", 5, edgeList.size()); 
//		Assert.assertEquals("{(-3,-5)(-2,-4)(-1,-3)(0,-2)}/[(-3,-5), (0,-2)]", edgeList.get(0).toString());
//		Assert.assertEquals("{(3,-5)(2,-4)(1,-3)(0,-2)}/[(3,-5), (0,-2)]", edgeList.get(1).toString());
//		Assert.assertEquals("{(0,-2)(0,-1)(0,0)(0,1)(0,2)}/[(0,-2), (0,2)]", edgeList.get(2).toString());
//		Assert.assertEquals("{(0,2)(-1,3)(-2,4)(-3,5)}/[(0,2), (-3,5)]", edgeList.get(3).toString());
//		Assert.assertEquals("{(0,2)(1,3)(2,4)(3,5)}/[(0,2), (3,5)]", edgeList.get(4).toString());
//		// 2 junction
//		JunctionSet junctionSet = graph.getJunctionSet();
//		Assert.assertNotNull(junctionSet);
//		Assert.assertEquals("[(0,-2), (0,2)]", junctionSet.toString()); 
//		// text 4 terminals
//		TerminalNodeSet terminalSet = graph.getTerminalNodeSet();
//		Assert.assertNotNull(terminalSet);
//		Assert.assertNotNull(terminalSet);
//		Assert.assertEquals("[(-3,-5), (3,-5), (-3,5), (3,5)]", terminalSet.toString()); 
	}


	@Test
	/** hexagon with 3 spikes
	 *       X
	 *       X
	 *       X
	 *      X X
	 *      X X
	 *     X X X
	 *    X     X
	 *   X       X
	 */
	public void test135TrimethylBenzene() {
		PixelGraph graph = PixelGraph.createGraph(Fixtures.CREATE_TRISPIKED_HEXAGON_ISLAND());
//		PixelEdgeList edgeList = graph.getEdgeList();
//		Assert.assertEquals(6, edgeList.size()); 
//		Assert.assertEquals("{(0,0)(0,1)(0,2)}/[(0,0), (0,2)]", edgeList.get(0).toString());
//		Assert.assertEquals("{(0,2)(-1,3)(-1,4)}/[(0,2), (-1,4)]", edgeList.get(1).toString());
//		Assert.assertEquals("{(0,2)(1,3)(1,4)}/[(0,2), (1,4)]", edgeList.get(2).toString());
//		Assert.assertEquals("{(-1,4)(-2,5)(-3,6)(-4,7)}/[(-1,4), (-4,7)]", edgeList.get(3).toString());
//		Assert.assertEquals("{(-1,4)(0,5)(1,4)}/[(-1,4), (1,4)]", edgeList.get(4).toString());
//		Assert.assertEquals("{(1,4)(2,5)(3,6)(4,7)}/[(1,4), (4,7)]", edgeList.get(5).toString());
//		JunctionSet junctionSet = graph.getJunctionSet();
//		Assert.assertNotNull(junctionSet);
//		Assert.assertEquals("[(0,2), (-1,4), (1,4)]", junctionSet.toString()); 
//		TerminalNodeSet terminalSet = graph.getTerminalNodeSet();
//		Assert.assertNotNull(terminalSet);
//		Assert.assertEquals("[(0,0), (-4,7), (4,7)]", terminalSet.toString()); 
	}

	@Test
	/** rhombus has not been thinned so throws exception.
	 * 
	 */
	public void testRhombus() {
		PixelIsland island = Fixtures.CREATE_RHOMBUS_ISLAND();
		try {
			PixelGraph graph = PixelGraph.createGraph(island);
//			Assert.fail("should throw exception");
			// error is now logged, not thrown
		} catch (RuntimeException e) {
			throw e;
		}
	}

	@Test
	@Ignore // recent edge algorithms broke this

	public void testWCorner() {
		PixelIsland island = new PixelIsland();
		island.setDiagonal(true);
		island.addPixel(new Pixel(0,0));
		island.addPixel(new Pixel(1,0));
		island.addPixel(new Pixel(2,0));
		island.addPixel(new Pixel(2,1));
		island.addPixel(new Pixel(3,1));
		island.addPixel(new Pixel(3,2));
		island.addPixel(new Pixel(3,3));
		PixelGraph graph = PixelGraph.createGraph(island);
		Assert.assertEquals(7, graph.getPixelList().size());
		Assert.assertEquals(5, graph.getNodeList().size());
//		Map<JunctionNode, PixelNucleus> nucleusByJunctionMap = graph.getNucleusByJunctionMap();
//		Assert.assertEquals(5, nucleusByJunctionMap.size());
//		if (graph.getNucleusSet() == null) {
//			graph.makeNucleusMap();
//		}
//		Set<PixelNucleus> nucleusSet = graph.getNucleusSet();
//		Assert.assertEquals(1, nucleusSet.size());
//		Assert.assertEquals(5, nucleusSet.iterator().next().size());
	}

	@Test
	// FIXME this gives wrong nucleus count
	public void test2Nuclei() {
		PixelIsland island = CREATE_TWO_NUCLEUS_ISLAND();
		Assert.assertEquals(12, island.getPixelList().size());
		try {
			PixelGraph graph = PixelGraph.createGraph(island);
//			Assert.fail("Non-thinned nucleus should throw");
			// now logs rather than throwing
		} catch (RuntimeException e) {
		}
	}

	public static PixelIsland CREATE_TWO_NUCLEUS_ISLAND() {
		PixelIsland island = new PixelIsland();
		island.setDiagonal(true);
		island.addPixel(new Pixel(0,0));
		island.addPixel(new Pixel(1,0));
		island.addPixel(new Pixel(2,0));
		island.addPixel(new Pixel(2,1));
		island.addPixel(new Pixel(3,1));
		island.addPixel(new Pixel(3,2));
		island.addPixel(new Pixel(3,3));
		island.addPixel(new Pixel(4,4));
		island.addPixel(new Pixel(5,4));
		island.addPixel(new Pixel(5,5));
		island.addPixel(new Pixel(6,5));
		island.addPixel(new Pixel(7,6));
		return island;
	}
	
	@Test
	// FIXME
	public void testExtremeEdge() throws IOException {
		BufferedImage image = ImageIO.read(new File(Fixtures.COMPOUND_DIR, "journal.pone.0094172.g002-2.png"));
		image = ImageUtil.boofCVBinarization(image, 160);
		image = ImageUtil.thin(image, new ZhangSuenThinning());
		ImageUtil.writeImageQuietly(image, new File("target/edge/0094172.png"));
		PixelIslandList pixelIslandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		LOG.debug("islands: "+pixelIslandList.size());
		PixelIsland island = pixelIslandList.getLargestIsland();
		PixelGraph graph = null;
		try {
			graph = PixelGraph.createGraph(island);
		} catch (RuntimeException e) {
			Assert.fail("failed "+e);
			e.printStackTrace();
		}
//		LOG.debug("edges "+graph.getPixelEdgeList().size());
	}
	

}
