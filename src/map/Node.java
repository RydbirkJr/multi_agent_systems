package map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import enums.Direction;
import planning.Goal;
import core.Box;
import core.Logger;
import enums.Direction;

public class Node extends Element{

	public HashMap<Square,Square> nodes = new HashMap<>();;
	private Level lvl;
	private Square[][] squares;
	private ArrayList<Edge> connectedEdges = new ArrayList<>();
	Logger logger = new Logger("Node");
	private int number;
	
	private ArrayList<Goal> goals = new ArrayList<>();
	private ArrayList<Box> boxes = new ArrayList<>();
	
	public Node(Level lvl, Square[][] squares, int number)
	{
		this.lvl = lvl;
		this.squares = squares;		
		this.number = number;
	}
	
	public void addNode(Square square)
	{		
		if(!nodes.containsKey(square))
		{
			nodes.put(square, square);
			
		}
	}
	
	public boolean isPartOf(Square square)
	{
		return nodes.containsKey(square);
	}
	
	public void addConnectedEdge(Edge edge)
	{
		if(!connectedEdges.contains(edge))
			connectedEdges.add(edge);
	}
	
	public ArrayList<Edge> getConnectedEdges()
	{
		return connectedEdges;
	}
	
	public void setNumber(int number)
	{
		this.number = number;
	}
	public int getNumber()
	{
		return number;
	}
		
	public void addGoal(Goal goal)
	{
		goals.add(goal);
	}
	public ArrayList<Goal> getGoals()
	{
		return goals;
	}
	
	public void addBox(Box box)
	{
		boxes.add(box);
	}
	public ArrayList<Box> getBoxes()
	{
		return boxes;
	}
	
	public void removeBox(Box box)
	{		
		boxes.remove(box);
	}
	
	public Set<Square> getResources()
	{
		Set<Square> resources = new HashSet<>();
			
		resources.addAll(nodes.values());
		
		return resources;
	}
}
