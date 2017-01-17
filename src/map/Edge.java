package map;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.NodeList;

import core.Logger;
import enums.Direction;

public class Edge extends Element{

	public HashMap<Square,Square> edges = new HashMap<Square,Square>();
	private Level lvl;
	private Square[][] squares;
	Logger logger = new Logger("Edge");
	private ArrayList<Node> connectedNodes = new ArrayList<>();
	private int number;
	
	public Node prev, next;
	
	public Edge(Level lvl, Square[][] squares, int nodeNumber)
	{
		this.lvl = lvl;
		this.squares = squares;
		this.number = nodeNumber;
	}	
	
	public void addEdge(Square edge)
	{
		edges.put(edge, edge);
	}
	
	public void mergeEdges(Edge mergeEdge)
	{
		for(Square mergeSquare : mergeEdge.edges.values())
		{
			this.addEdge(mergeSquare);
		}
	}
	
	public ArrayList<Square> getEndPoint()
	{
		ArrayList<Square> squares = new ArrayList<Square>();
		int count = 0;
		Square testSquare;
		for(Square sq : edges.values())
		{		
			for(Direction d: Direction.values())
			{
				testSquare = lvl.getSquare(sq, d);
				if(testSquare != null && (testSquare.isPassable() && !isPartOf(testSquare)))
				{
					count++;
				}
			}
			
			if(count > 0)
			{
				squares.add(sq);
				count = 0;
			}
		}
		
		return squares;
	}
	
	
	public void cleanEdge()
	{
		ArrayList<Square> edge = new ArrayList<Square>();		
		
		Square testSquare;
		
		edge = getEndPoint();
		
		for(int i = 0; i < edge.size(); i++)	
		{
			if(edge.get(i) instanceof GoalField) continue;
			
			for(Direction d: Direction.values())
			{
				testSquare = lvl.getSquare(edge.get(i), d);
				if(testSquare != null && (testSquare.isPassable() && !isPartOf(testSquare)))
				{
					edges.remove(edge.get(i));	
				}
			}
			
		}
	}
	
	public void deleteNode(Node node)
	{
		connectedNodes.remove(node);		
	}
	
	public boolean isPartOf(Square square)
	{
		return edges.containsKey(square);
	}
		
	public void addConnectedNode(Node node)
	{
		if(!connectedNodes.contains(node))
			connectedNodes.add(node);
	}
	
	public ArrayList<Node> getConnectedNode()
	{
		return connectedNodes;
	}
	public void setNumber(int number)
	{
		this.number = number;
	}
	public int getNumber()
	{
		return number;
	}
	
	public Set<Square> getResources()
	{
		Set<Square> resources = new HashSet<>();

		if(next == null) return resources;


		resources = new HashSet<>(next.getResources());
		//resources.addAll(edges.values());
		
		return resources;
	}
}
