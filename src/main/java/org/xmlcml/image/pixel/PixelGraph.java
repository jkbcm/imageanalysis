package org.xmlcml.image.pixel;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.image.ImageParameters;
import org.xmlcml.image.pixel.PixelComparator.ComparatorType;


/**
 * holds PixelNodes and PixelEdges for pixelIsland
 * 
 * a graph may be a number of PixelNodes and PixelEdges. A single cycle will have a 
 * single PixelNode located in an arbitrary position
 * 
 * @author pm286
 * 
 */
public class PixelGraph {


	private static final String NODE_PREFIX = "zn";

	private final static Logger LOG = Logger.getLogger(PixelGraph.class);

	private static final Angle ANGLE_EPS = new Angle(0.03, Units.RADIANS);
	public static String[] COLOURS = new String[] {"red", "green", "pink", "cyan", "orange", "blue", "yellow"};

	private PixelEdgeList edgeList; 
	private PixelNodeList nodeList; 
	private PixelList pixelList;
	private PixelIsland island;
	private Stack<PixelNode> nodeStack;
	
	private PixelGraph() {
		
	}
	
	public PixelGraph(PixelIsland island) {
		this(island.getPixelList(), island);
	}
	
	public PixelGraph(PixelList list) {
		this.island = list.getIsland();
		createGraph(pixelList);
	}
	
	/** all pixels have to belong to island
	 * 
	 * @param pixelList
	 * @param island
	 */
	public PixelGraph(PixelList pixelList, PixelIsland island) {
		this.island = island;
		createGraph(pixelList);
	}

	private void createGraph(PixelList pixelList) {
		if (pixelList == null) {
			throw new RuntimeException("null pixelList");
		}
		this.pixelList = pixelList;
		this.createNodesAndEdges();
	}

	/** creates graph without pixels
	 * 
	 * @return
	 */
	public static PixelGraph createEmptyGraph() {
		return new PixelGraph();
	}

	/**
	 * creates graph and fills it.
	 * 
	 * @param island
	 * @return
	 */
	public static PixelGraph createGraph(PixelIsland island) {
		PixelGraph pixelGraph = null;
		if (island != null) {
			pixelGraph = new PixelGraph(island.getPixelList(), island);
		}
		return pixelGraph;
	}

	void createNodesAndEdges() {
		if (edgeList == null) {
			LOG.debug("CREATING NODES AND EDGES "+this.hashCode()+"; p:"+this.getPixelList().size());
			PixelNucleusFactory nucleusFactory = getNucleusFactory();
			edgeList = nucleusFactory.createPixelEdgeListFromNodeList();
			for (PixelEdge edge : edgeList) {
				LOG.debug("edge "+edge);
			}
		}
	}

	private void createNodeList() {
		nodeList = getNucleusFactory().getOrCreateNodeListFromNuclei();
	}

//	private void createNodeListFromEdgesOld() {
//		Set<PixelNode> nodeSet = new HashSet<PixelNode>();
//		nodeByNucleusMap = new HashMap<PixelNucleus, PixelNode>();
//		for (PixelEdge edge : pixelEdgeList) {
//			Pixel pixel0 = edge.getPixelList().get(0);
//			createAndAddNode(nodeSet, edge, pixel0, 0);
//			Pixel pixelLast = edge.getPixelList().last();
//			createAndAddNode(nodeSet, edge, pixelLast, 1);
//		}
//		pixelNodeList.addAll(nodeSet);
//	}

//	private void createAndAddNode(Set<PixelNode> nodeSet, PixelEdge edge,
//			Pixel pixel, int end) {
//		PixelNode node = createNode(pixel);
//		if (node == null) {
//			LOG.trace("null node");
//		} else {
//			// add serial
////			node.setId("nn"+pixel+nodeSet.size());
//			node.setId("nn"+pixel);
//			edge.addNode(node, end);
//			nodeSet.add(node);
////			usedNonNodePixelSet.add(node.getCentrePixel());
//		}
//	}

//	private PixelSet createConnectedDiagonalPixelSet(int neighbours) {
//		island.setDiagonal(true);
//		PixelSet connectedSet = new PixelSet();
//		for (Pixel pixel : pixelList) {
//			pixel.clearNeighbours();
//			if (pixel.isConnectedAny(island, neighbours)) {
//				connectedSet.add(pixel);
//			}
//		}
//		return connectedSet;
//	}
	
	
	/**
	 * gets next pixel in chain.
	 * 
	 * @param current
	 * @param last
	 * @param island
	 * @return next pixel or null if no more or branch
	 */
	static Pixel getNextUnusedInEdge(Pixel current, Pixel last,
			PixelIsland island) {
		Pixel next = null;
		PixelList neighbours = current.getOrCreateNeighbours(island);
		neighbours.remove(last);
		next = neighbours.size() == 1 ? neighbours.get(0) : null;
		Long time3 = System.currentTimeMillis();
		return next;
	}


	public PixelNodeList getNodeList() {
		if (island != null) {
			nodeList = getNucleusFactory().getOrCreateNodeListFromNuclei();
		} else {
			ensureNodes();
		}
		return nodeList;
	}

	public PixelList getPixelList() {
		return pixelList;
	}

	public String toString() {
		getEdgeList();
		StringBuilder sb = new StringBuilder();
		sb.append("; edges: " + (edgeList == null ? "none" : edgeList.size()+"; "+edgeList.toString()));
		sb.append("\n     ");
		sb.append("nodes: " + (nodeList == null ? "none" : nodeList.size()+"; "+nodeList.toString()));
		return sb.toString();
	}

	/** get root pixel as middle of leftmost internode edge.
	 * 
	 *  where mid edge is vertical.
	 *  
	 * @return
	 */
	public PixelNode getRootNodeFromExtremeEdge(ComparatorType comparatorType) {
		PixelEdge extremeEdge = getExtremeEdge(comparatorType);
		if (extremeEdge == null) {
			throw new RuntimeException("Cannot find extreme edge for "+comparatorType);
		}
		LOG.trace("extreme "+extremeEdge+"; nodes "+extremeEdge.getNodes().size());
		
		Pixel midPixel = extremeEdge.getNearestPixelToMidPoint();
		PixelNode rootNode = splitEdgeAndInsertNewNode(extremeEdge, midPixel);
				
		return rootNode;
	}

	public PixelNode splitEdgeAndInsertNewNode(PixelEdge oldEdge, Pixel midPixel) {
		PixelNode rootNode = new PixelNode(midPixel, this);
		PixelList neighbours = midPixel.getOrCreateNeighbours(island);
		if (neighbours.size() != 2) {
			throw new RuntimeException("Should have exactly 2 neighbours "+neighbours.size());
		}

		PixelEdgeList edgeList = splitEdge(oldEdge, midPixel, rootNode);
		this.addEdge(edgeList.get(0));
		this.addEdge(edgeList.get(1));
		this.addNode(rootNode);
		this.removeEdge(oldEdge);
		return rootNode;
	}

	private void removeEdge(PixelEdge edge) {
		edgeList.remove(edge);
		PixelNodeList nodeList = edge.getNodes();
		for (PixelNode node : nodeList) {
			node.removeEdge(edge);
		}
	}

	private PixelEdgeList splitEdge(PixelEdge edge, Pixel midPixel,
			PixelNode rootNode) {
		
		PixelEdgeList edgeList = new PixelEdgeList();
		PixelNodeList nodes = edge.getNodes();
		if (nodes.size() != 2) {
			LOG.error("Should have exactly 2 extremeNodes found "+nodes.size());
			return edgeList;
		}
		
		PixelList edgePixelList = edge.getPixelList();
		PixelList beforePixelList = edgePixelList.getPixelsBefore(midPixel);
		PixelList afterPixelList = edgePixelList.getPixelsAfter(midPixel);
		
		Pixel beforePixelLast = beforePixelList.last();
		Pixel afterPixelLast = afterPixelList.last();
		if (!beforePixelLast.equals(beforePixelList.last())) {
			beforePixelList.add(beforePixelLast);
		}
		if (!afterPixelLast.equals(afterPixelList.last())) {
			afterPixelList.add(afterPixelLast);
		}
		
		PixelEdge edge0 = createEdge(rootNode, nodes.get(0), beforePixelList);
		edgeList.add(edge0);
		PixelEdge edge1 = createEdge(rootNode, nodes.get(1), afterPixelList);
		edgeList.add(edge1);
		
		return edgeList;
	}

	private PixelEdge createEdge(PixelNode splitNode, PixelNode newEndNode, PixelList pixelList) {
		PixelEdge edge = new PixelEdge(island);
		edge.addNode(splitNode, 0);
		edge.addNode(newEndNode, 1);
		edge.addPixelList(pixelList);
		return edge;
	}

	@Deprecated
	private PixelEdge getExtremeEdge(ComparatorType comparatorType) {
		PixelEdge extremeEdge = null;
		double extreme = Double.MAX_VALUE;
		for (PixelEdge edge : edgeList) {
			LOG.trace(edge);
			PixelSegmentList segmentList = edge.getOrCreateSegmentList(getParameters().getSegmentTolerance());
			LOG.trace("PL "+segmentList.size()+"  /  "+segmentList.getReal2Array());
			// look for goal post edge
			if (segmentList.size() != 3) {
				continue;
			}
			Line2 crossbar = segmentList.get(1).getEuclidLine();
			Real2 midPoint = crossbar.getMidPoint();
			// LHS
			if (ComparatorType.LEFT.equals(comparatorType) && crossbar.isVertical(ANGLE_EPS)) {
				if (midPoint.getX() < extreme) {
					extreme = midPoint.getX();
					extremeEdge = edge;
					LOG.trace("edge "+midPoint);
				}
			// RHS
			} else if (ComparatorType.RIGHT.equals(comparatorType) && crossbar.isVertical(ANGLE_EPS)) {
				if (midPoint.getX() > extreme) {
					extreme = midPoint.getX();
					extremeEdge = edge;
				}
			// TOP
			} else if (ComparatorType.TOP.equals(comparatorType) && crossbar.isHorizontal(ANGLE_EPS)) {
				if (midPoint.getY() < extreme) {
					extreme = midPoint.getY();
					extremeEdge = edge;
				}
			// BOTTOM
			} else if (ComparatorType.BOTTOM.equals(comparatorType) && crossbar.isHorizontal(ANGLE_EPS)) {
				if (midPoint.getY() > extreme) {
					extreme = midPoint.getY();
					extremeEdge = edge;
				}
			}
		}
		return extremeEdge;
	}

	/** assume node in middle of 3-segment path.
	 * 
	 * @return
	 */
	public PixelNodeList getPossibleRootNodes2() {
		PixelNodeList nodeList = new PixelNodeList();
		PixelEdge rootEdge = null;
		PixelNode midNode = null;
		for (PixelEdge edge : edgeList) {
			LOG.trace(edge.getNodes());
			PixelSegmentList segmentList = edge.getOrCreateSegmentList(getParameters().getSegmentTolerance());
			Angle deviation = segmentList.getSignedAngleOfDeviation();
			if (Math.abs(deviation.getRadian()) < 2.0) continue;
			LOG.trace("POLY "+segmentList.get(0)+"/"+segmentList.getLast()+"/"+deviation);
			if (segmentList.size() == 3) {
				SVGLine midline = segmentList.get(1).getSVGLine();
				Pixel midPixel = edge.getNearestPixelToMidPoint(midline.getMidPoint());
				midNode = new PixelNode(midPixel, this);
				nodeList.add(midNode);
				rootEdge = edge;
			}
		}
		if (nodeList.size() == 1) {
			PixelNode rootNode = nodeList.get(0);
			removeOldEdgeAndAddNewEdge(rootNode, rootEdge, 0);
			removeOldEdgeAndAddNewEdge(rootNode, rootEdge, 1);
		}
		return nodeList;
	}

	private void removeOldEdgeAndAddNewEdge(PixelNode rootNode, PixelEdge rootEdge, int nodeNum) {
		PixelNode childNode = rootEdge.getPixelNode(nodeNum);
		this.removeEdgeFromNode(childNode, rootEdge);
		addNewEdge(rootNode, childNode);
	}

	private void addNewEdge(PixelNode node0, PixelNode node1) {
		PixelEdge edge = new PixelEdge(this);
		if (node0 != null) {
	 		edge.addNode(node0, 0);
			node0.addEdge(edge);
		}
		if (node1 != null) {
			edge.addNode(node1, 1);
			node1.addEdge(edge);
		}
		this.edgeList.add(edge);
	}

	private void removeEdgeFromNode(PixelNode node, PixelEdge edge) {
		if (node != null) {
			node.removeEdge(edge);
		}
		edgeList.remove(edge);
	}

	public void addNode(PixelNode node) {
		ensureNodes();
		if (!nodeList.contains(node)) {
			nodeList.add(node);
		}
	}

	public void addEdge(PixelEdge edge) {
		ensureEdges();
		if (!edgeList.contains(edge)) {
			edgeList.add(edge);
			addNode(edge.getPixelNode(0));
			addNode(edge.getPixelNode(1));
		}
	}

	private void ensureNodes() {
		if (nodeList == null) {
			nodeList = new PixelNodeList();
		}
	}

	private void ensureEdges() {
		if (edgeList == null) {
			edgeList = new PixelEdgeList();
		}
	}

	public void numberTerminalNodes() {
		int i = 0;
		for (PixelNode node : getNodeList()) {
//			if (node != instanceof TerminalNode) {
				node.setLabel(NODE_PREFIX + i);
//			}
			Pixel pixel = node.getCentrePixel();
			Int2 int2 = pixel == null ? null : pixel.getInt2();
			Integer x = (int2 == null) ? null : int2.getX();
			Integer y = (int2 == null) ? null : int2.getY();
			if (x == null || y == null) {
				node.setLabel("N"+i);
			} else {
				node.setLabel(x+"_"+y);
			}
			i++;
		}
	}

	public SVGG drawEdgesAndNodes(String[] colours) {
		SVGG g = new SVGG();
		SVGG rawPixelG = pixelList.plotPixels("magenta");
		g.appendChild(rawPixelG);
		drawEdges(colours, g);
		drawNodes(colours, g);
		return g;
	}

	public void drawNodes(String[] colours, SVGG g) {
		for (int i = 0; i < nodeList.size(); i++) {
			String col = colours[i % colours.length];
			PixelNode node = nodeList.get(i);
			if (node != null) {
				SVGG nodeG = node.createSVG(1.0);
				nodeG.setStroke(col);
				nodeG.setStrokeWidth(0.1);
				nodeG.setOpacity(0.5);
				nodeG.setFill("none");
				g.appendChild(nodeG);
			}
		}
	}

	public void drawEdges(String[] colours, SVGG g) {
		for (int i = 0; i < edgeList.size(); i++) {
			String col = colours[i % colours.length];
			PixelEdge edge = edgeList.get(i);
			SVGG edgeG = edge.createPixelSVG(col);
			edgeG.setFill(col);
			g.appendChild(edgeG);
			SVGG lineG = edge.createLineSVG();
			lineG.setFill(col);
			g.appendChild(lineG);
		}
	}

	@Deprecated
	public PixelNode createRootNodeEmpirically(ComparatorType rootPosition) {
		throw new RuntimeException("MEND or KILL");
//		PixelNode rootNode = null;
//		PixelNodeList rootNodes = getPossibleRootNodes(null);
//		Collections.sort(rootNodes.getList());
//		if (rootNodes.size() > 0) {
//			rootNode = rootNodes.get(0);
//		} else {
//			try {
//				rootNode = getRootNodeFromExtremeEdge(rootPosition);
//			} catch (RuntimeException e) {
//					throw(e);
//			}
//		}
//		return rootNode;
	}

	public ImageParameters getParameters() {
		return getIsland().getParameters();
	}

	private PixelIsland getIsland() {
		if (island == null) {
			throw new RuntimeException("Island is required");
		}
		return island;
	}

	/** creates segmented lines from pixels adds them to edges and draws them.
	 *  
	 *  uses parameters to 
	 * @return
	 */
	public SVGG createSegmentedEdges() {
		SVGG g = new SVGG();
		for (PixelEdge edge: edgeList) {
			PixelSegmentList pixelSegmentList = edge.getOrCreateSegmentList(getParameters().getSegmentTolerance());
			pixelSegmentList.setStroke(getParameters().getStroke());
			pixelSegmentList.setWidth(getParameters().getLineWidth());
			pixelSegmentList.setFill(getParameters().getFill());
			g.appendChild(pixelSegmentList.getOrCreateSVG());
		}
		return g;
	}

	public void createAndDrawGraph(SVGG g) {
		LOG.error("createAndDrawGraph NYI");
	}

	private PixelNode getNode(Pixel pixel) {
		for (PixelNode node : nodeList) {
			if (pixel.equals(node.getCentrePixel())) {
				return node;
			}
		}
		return null;
	}

	public PixelEdge createEdge(PixelNode node) {
		PixelEdge edge = null;
		if (node.hasMoreUnusedNeighbours()) {
			Pixel neighbour = node.getNextUnusedNeighbour();
			edge = createEdge(node, neighbour);
		}
		return edge;
	}

	public PixelEdge createEdge(PixelNode node, Pixel next) {
		PixelEdge edge = new PixelEdge(this);
		Pixel current = node.getCentrePixel();
		edge.addNode(node, 0);
		LOG.trace("start "+node);
		node.removeUnusedNeighbour(next);
		edge.addPixel(current);
		edge.addPixel(next);
		while (true) {
			PixelList neighbours = next.getOrCreateNeighbours(island);
			if (neighbours.size() != 2) {
				break;
			}
			Pixel next0 = neighbours.getOther(current);
			edge.addPixel(next0);
			current = next;
			next = next0;
			LOG.trace(current);
		}
		LOG.trace("end "+next);
		PixelNode node2 = getNode(next);
		if (node2 == null) {
			throw new RuntimeException("cannot find node for pixel: "+next);
		}
		node2.removeUnusedNeighbour(current);
		edge.addNode(node2, 1);
		return edge;
	}

	private Stack<PixelNode> createNodeStack() {
		createNodeList();
		nodeStack = new Stack<PixelNode>();
		for (PixelNode node : nodeList) {
			nodeStack.push(node);
		}
		return nodeStack;
	}

	private PixelNucleusFactory getNucleusFactory() {
		if (island == null) {
			throw new RuntimeException("Island must not be null");
		}
		return island.getOrCreateNucleusFactory();
	}

	public PixelNucleusList getPixelNucleusList() {
		return getNucleusFactory().getOrCreateNucleusList();
	}

	public PixelEdgeList getEdgeList() {
		if (island != null) {
			edgeList = getNucleusFactory().getEdgeList();
		} else {
			ensureEdges();
		}
		return edgeList;
	}

	public void numberAllNodes() {
		ensureNodeList();
		int i = 0;
		for (PixelNode node : nodeList) {
			node.setLabel("n"+(i++));
		}
	}

	public void addCoordsToNodes() {
		ensureNodeList();
		for (PixelNode node : nodeList) {
			Int2 coord = node.getInt2();
			if (coord != null) {
				String label = String.valueOf(coord).replaceAll("[\\(\\)]", "").replaceAll(",","_");
				node.setLabel(label);
			}
		}
	}

	private void ensureNodeList() {
		if (nodeList == null) {
			nodeList = new PixelNodeList();
		}
	}

	public void debug() {
		LOG.debug("graph...");
		for (PixelNode node : this.getNodeList()) {
			LOG.debug("n> "+ node.toString());
			for (PixelEdge edge : node.getEdges()) {
				LOG.debug("  e: "+edge.getNodes());
			}
		}
	}



}
