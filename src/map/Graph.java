package map;

import core.Logger;
import enums.Direction;
import map.Level;
import map.Square;

import java.util.*;

public class Graph {
	
	public List<Edge> edgeList = new ArrayList<>();
	public List<Node> nodeList = new ArrayList<>();
	private Level lvl;
	private Square[][] squares;
	Logger logger = new Logger("Graph");
	
	
	public Graph(Level lvl, Square[][] squares){
		this.lvl = lvl;
		this.squares = squares;
		createEdges();
		removeNoGoalEdge();
		createNode();
	}
	
	
	public void createEdges()
	{
		Square testSquare;
		for(int i = 0; i < lvl.MAX_ROW; i++)
		{
			for(int j = 0; j < lvl.MAX_COLUMN; j++)
			{
				testSquare = lvl.getSquare(i, j);
				if(!testSquare.isPassable()) continue;
				if(!testSquare.getIsSaveSpot(false) && testSquare instanceof GoalField)
				{
					addEdge(testSquare);
				}			
			}
		}
	}	
	
	private void addEdge(Square square)
	{
		
		if(square.row == 0 || square.col == 0 || square.row == lvl.MAX_ROW || square.col == lvl.MAX_COLUMN){
            return;
        }
		
		for(int i = 0; i < edgeList.size(); i++)
		{
			for(Square sq : edgeList.get(i).edges.values())
			{				
				for(Direction d : Direction.values())
				{
					Square dirSquare = lvl.getSquare(square, d);
					if(dirSquare != null && dirSquare.equals(sq))
					{
						edgeList.get(i).addEdge(square);
						return;							
					}					
				}				
			}
		}
		
		Edge edge = new Edge(lvl, squares, edgeList.size()+1);		
		edge.addEdge(square);
		edgeList.add(edge);	
	}
	
	private void removeNoGoalEdge()
	{
		boolean hasGoal = false;
		for(int i = 0; i < edgeList.size(); i++)
		{
			for(Square sq : edgeList.get(i).edges.values())
			{
				if(hasGoal) break;
				if(sq instanceof GoalField && ((GoalField) sq).getLetter() > 1)
				{
					hasGoal = true;
				}			
			}
			
			if(!hasGoal)
			{
				edgeList.remove(i);
				i--;
			}
			else
			{
				hasGoal = false;
				edgeList.get(i).cleanEdge();
			}
		}
		
	}
	
	private void createNode()
	{		
		ArrayDeque<Square> edgeQueue = new ArrayDeque<>();
		ArrayList<Square> endSquareEdge = new ArrayList<Square>();	
		
		for(int i = 0; i < edgeList.size(); i++)
		{
			endSquareEdge = edgeList.get(i).getEndPoint();
			for(int j=0; j < endSquareEdge.size(); j++)
			{
				edgeQueue.add(endSquareEdge.get(j));
			}					
		}
		
		
		while(!edgeQueue.isEmpty())
		{			
			createNodeByQueu(edgeQueue.pop());
		}
		
	}
	
	private void createNodeByQueu(Square square)
	{
		ArrayDeque<Square> nodeQueue = new ArrayDeque<>();
		ArrayList<Square> newNodes = new ArrayList<>();
		Square testSquare;
		Square nodeSquare;
		int testIndex;
		boolean isEdge = false;
		//Node node = new Node(lvl, squares, nodeList.size()+1);
				
		for(Direction d : Direction.values())
		{
			testSquare = lvl.getSquare(square, d);
			if(testSquare != null && testSquare.isPassable() && getListIndexNode(testSquare) < 0)
				newNodes.add(testSquare);
		}
		
		for(int i = 0; i < newNodes.size(); i++)
		{
			logger.info("newNodes " + newNodes.get(i).toString());
		}
		
		for(int i = 0; i < newNodes.size(); i++)
		{
			Node node = new Node(lvl, squares, nodeList.size()+1);
			nodeQueue.add(newNodes.get(i));
			if(getListIndexEdges(newNodes.get(i)) < 0)
			{
				node.addNode(newNodes.get(i));
			}
			
			while(!nodeQueue.isEmpty())
			{
				nodeSquare = nodeQueue.pop();
				//logger.info("Square pop = " + nodeSquare.toString());
				for(Direction d: Direction.values())
				{
					testSquare = lvl.getSquare(nodeSquare, d);
					testIndex = getListIndexEdges(testSquare);
					if(testIndex == -1)
					{
						isEdge = false;
					}
					else
					{
						isEdge = true;
					}
					
					//if(testSquare != null && testSquare.isPassable() && !isEdge)
					if(testSquare != null && testSquare.isPassable())
					{					
						if(isEdge)
						{
							node.addConnectedEdge(edgeList.get(testIndex));
							edgeList.get(testIndex).addConnectedNode(node);						
						}
						else
						{
							if(!nodeQueue.contains(testSquare) && !node.isPartOf(testSquare) && getListIndexNode(testSquare) < 0)
							{
								nodeQueue.add(testSquare);					
								node.addNode(testSquare);
							}
						}
					}
					
				}
			}
			
			if(node.nodes.isEmpty())
			{		
				for(int j = 0; j < edgeList.size(); j++)
				{
					edgeList.get(j).deleteNode(node);
				}
			}
			else
			{
				nodeList.add(node);
			}
		}
	}
	
	public int getListIndexEdges(Square square)
	{
		for(int i = 0; i < edgeList.size(); i++)
		{
			if(edgeList.get(i).isPartOf(square))
			{
				return i;
			}
		}
		
		return -1;
	}
	
	public int getListIndexNode(Square square)
	{
		for(int i = 0; i < nodeList.size(); i++)
		{
			if(nodeList.get(i).isPartOf(square))
			{
				return i;
			}
		}
		
		return -1;
	}	
	
	public Element getElement(Square square){
		int number = -2;
		if((number = getListIndexEdges(square))>= 0){
			return (Element)edgeList.get(number);
		}
		
		return (Element)nodeList.get(getListIndexNode(square));
	}
	
	public List<Edge> getEdges()
	{
		return this.edgeList;
	}
	
	public List<Node> getNodes()
	{
		return this.nodeList;
	}
	
	public List<Element> getPath(Element from, Element to)
    {
    	logger.error("From: " + from.getClass() + from.getNumber() + "To: " + to.getClass() + to.getNumber());
    	ArrayDeque<Element> queue = new ArrayDeque<>(); 
    	
    	queue.add(from);    
    	
    	return getPathRec(queue, to, new ArrayList<>());
    	//while(!queue.isEmpty())
    	//{
    		
    	//}    	
    }
	
	private List<Element> getPathRec(ArrayDeque<Element> queue, Element to, List<Element> visited)
	{
		List<Element> elements = new ArrayList<>();
		List<Element> tempElements = new ArrayList<>();
		Element tempElement;
		tempElement = queue.pop();
		
		visited.add(tempElement);
		
		if(tempElement.equals(to))
		{
			elements.add(to);
			return elements;
		}
		
		if(tempElement instanceof Node)
		{
			for(Edge edge : ((Node) tempElement).getConnectedEdges())
			{
				if(!visited.contains(edge)){
					queue.add(edge);
					tempElements = getPathRec(queue, to, visited);
					
					if(!tempElements.isEmpty() && tempElements.contains(to))
					{
						elements = new ArrayList<>(tempElements);
						elements.add(tempElement);
						return elements;
					}
				}
			}
		}
		else
		{
			for(Node node : ((Edge)tempElement).getConnectedNode())
			{
				if(!visited.contains(node)){
					queue.add(node);
					tempElements = getPathRec(queue, to, visited);
					
					if(!tempElements.isEmpty() && tempElements.contains(to))
					{
						elements = tempElements;
						elements.add(tempElement);
						return elements;
					}
				}
			}
		}
		
		String elemString = "Elements";
		for(Element elem : elements){
			elemString += "" + elem.getClass() + ", " + elem.getNumber();
		}
		
		logger.error(elemString);
		
		return elements;
		
	}
	
	public List<Node> getCriticalNode()
	{
		List<Node> criticalNodes = new ArrayList<>();
		
		for(int i = 0; i < nodeList.size(); i++)
		{
			if(nodeList.get(i).getConnectedEdges().size() == 1)
			{
				criticalNodes.add(nodeList.get(i));
			}
		}
		
		for(int i = 0; i < criticalNodes.size(); i++)
		{
			logger.info("Sorted List "+i+" Node " + criticalNodes.get(i).getNumber()+ " No edges = " + criticalNodes.get(i).getConnectedEdges().size());
		}
		
		return criticalNodes;		
	}
	
	public List<Node> getNodeListSmallestFirst()
	{
		List<Node> sorted = new ArrayList<>();		
		
		for(int i = 0; i < nodeList.size(); i++)
		{
			if(sorted.isEmpty())
			{
				sorted.add(nodeList.get(i));
				continue;
			}
			
			sortListSmallestFirst(sorted, i);			
		}	
		
		for(int i = 0; i < sorted.size(); i++)
		{
			logger.info("Sorted List "+i+" Node " + sorted.get(i).getNumber()+ " No edges = " + sorted.get(i).getConnectedEdges().size());
		}
		return sorted;
	}
	
	public Node getLargestNode()
	{
		if(getNodeListLargestFirst().isEmpty()) return null;

		return getNodeListLargestFirst().get(0);
	}
	
	public List<Node> getNodeListLargestFirst()
	{
		List<Node> sorted = new ArrayList<>();		
		
		for(int i = 0; i < nodeList.size(); i++)
		{
			if(sorted.isEmpty())
			{
				sorted.add(nodeList.get(i));
				continue;
			}
			
			sortListLargestFirst(sorted, i);			
		}	
		
		/*for(int i = 0; i < sorted.size(); i++)
		{
			logger.info("Sorted List "+i+" Node " + sorted.get(i).getNumber()+ " No edges = " + sorted.get(i).getConnectedEdges().size());
		}*/
		return sorted;
	}
	
	private void sortListLargestFirst(List<Node> sorted, int index)
	{
		for(int j = 0; j < sorted.size(); j++)
		{
			if(sorted.get(j).getConnectedEdges().size() < nodeList.get(index).getConnectedEdges().size())
			{
				sorted.add(j, nodeList.get(index));
				return;
			}
		}
		sorted.add(nodeList.get(index));
	}
	
	private void sortListSmallestFirst(List<Node> sorted, int index)
	{
		for(int j = 0; j < sorted.size(); j++)
		{
			if(sorted.get(j).getConnectedEdges().size() > nodeList.get(index).getConnectedEdges().size())
			{
				sorted.add(j, nodeList.get(index));
				return;
			}
		}
		sorted.add(nodeList.get(index));
	}
	
	public void printgraph()
	{
		String[][] print = new String[lvl.MAX_ROW][lvl.MAX_COLUMN];
		
		for(int i = 0; i < lvl.MAX_ROW; i++)
		{
			for(int j = 0; j < lvl.MAX_COLUMN; j++)
			{
				print[i][j] = "[ ]";
			}
		}
		
		for(int i = 0; i < nodeList.size(); i++)
		{		
			for(Square sq : nodeList.get(i).nodes.values())
			{
				print[sq.row][sq.col] = "N" + nodeList.get(i).getNumber();
			}
		}
		
		for(int i = 0; i < edgeList.size(); i++)
		{
			for(Square sq : edgeList.get(i).edges.values())
			{
				print[sq.row][sq.col] = "E" + edgeList.get(i).getNumber();
			}
		}
				
		String line = "";
		for(int i = 0; i < lvl.MAX_COLUMN; i++)
		{
			if(i==0)
			{
				line += "   ";
			}
			if(i<10)
			{
				line += "  " + i;
			}
			else
			{
				line += " " + i;
			}
			
		}
		logger.info(line);
		line = "";
		for(int i = 0; i < lvl.MAX_ROW;i++)
		{
			line = "";
			for(int j = 0; j < lvl.MAX_COLUMN; j++)
			{
				if(print[i][j].length() < 3)
					line+= " ";
				line += print[i][j];				
			}
			if(i<10)
			{
				line = " " + i + ":" + line;
			}
			else
			{
				line = i + ":" + line;
			}
				
			logger.info(line);
		}
		printConnected();
	}
	
	private void printConnected()
	{
		ArrayDeque<Square> queue = new ArrayDeque<>();
		ArrayList<Node> alNode;
		ArrayList<Edge> alEdge;
		String print = "";
		for(int i = 0; i < edgeList.size(); i++)
		{
			print += "E" + edgeList.get(i).getNumber();
			alNode = edgeList.get(i).getConnectedNode();
			for(int j= 0; j < alNode.size(); j++)
			{
				print += " -> " + alNode.get(j).getNumber();
			}
			logger.info(print);
			print = "";
		}
		
		for(int i = 0; i < nodeList.size(); i++)
		{
			print += "N" + nodeList.get(i).getNumber();
			alEdge = nodeList.get(i).getConnectedEdges();
			for(int j= 0; j < alEdge.size(); j++)
			{
				print += " -> " + alEdge.get(j).getNumber();
			}
			logger.info(print);
			print = "";
		}
		
	}
	
}
