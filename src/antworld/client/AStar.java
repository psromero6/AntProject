package antworld.client;

//import antworld.client.Control;
//import antworld.client.NodeData;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class AStar
{  
  public static ArrayList<ArrayList<NodeData>> myMap=Control.myMap;
  LinkedList<NodeData> openList= new LinkedList<NodeData>();
  LinkedList<NodeData> closedList= new LinkedList<NodeData>();
  ArrayList<NodeData> path=new ArrayList<NodeData>();
  LinkedList<Character> solvedMovedList;
  NodeData startLoc;
  NodeData goalLoc;
   
  /****************************************************************************
   *  input:
   *  output:
   *  description:
  ****************************************************************************/

  /****************************************************************************
   *findPath
   *  input: node to calculation at, node to end calculation at
   *  output: outputs a LinkedList representing the movements from the start to
   *    goal node
   *  description: method controls the calculation of the AStar path. holds the
   *    open and closed lists of nodes in the search, calls the helping functions
   *    to find shortest path
  ****************************************************************************/
  public LinkedList<Character> findPath(NodeData myStartLoc, NodeData myGoalLoc)
  {
    startLoc=myStartLoc;
    goalLoc=myGoalLoc;
    NodeData currentNode=null;
    ArrayList<NodeData> neighborList=null;
    
    insertSorted(startLoc,openList);
    startLoc.calcG(startLoc);
    startLoc.calcH(goalLoc);
    startLoc.calcF();
    
    while(openList!=null)
    {
      //currentNode=findLowF();
      currentNode=openList.getFirst();
      neighborList=findNeighbors(currentNode);
      
      //TODO dangerous to flag goal found with a null list. Is it possible for there to be a nodes with no neighbors?
      //probably not, as the nodes are removed from this list after the findNeighbor method
      if(neighborList==null)
      {
        return solvedMovedList;
      }//neighbor list returns null once path is found
      
      int[][] ID=new int[1][2];//debug variable, currentNodeID variable
      ID[0][0]=currentNode.getRowID();//debug
      ID[0][1]=currentNode.getColID();//debug
      
      openList.remove(currentNode);
      insertSorted(currentNode,closedList);
      //closedList.add(currentNode);
      
      neighborList=updateNeighborVal(neighborList, currentNode, goalLoc);
      neighborList=compareNeighborList(neighborList,openList);//remove duplicate
      neighborList=compareNeighborList(neighborList,closedList);
            
      while(neighborList.size()!=0)
      {
      insertSorted(neighborList.get(0),openList);
        neighborList.remove(0);
      }
    }    
    return null;
  }
  
  /****************************************************************************
   * findLowF
   *  input:non
   *  output: outputs the node with the lowest F value
   *  description: Finds the node with the lowest F value in the open list and
   *    returns it.
  ****************************************************************************/
  public NodeData findLowF()
  {
    //TODO make this favor nodes in the correct direction first
    //TODO keep the nodes ordered by f value then by h value
    NodeData lowF=openList.getFirst();
    int fVal=lowF.getF();
    for(int i=0;i<openList.size();i++)
    {
      if(lowF.getF()>openList.get(i).getF())
      {
        lowF=openList.get(i);
        fVal=lowF.getF();
        i=-1;
      }
    }    
    return lowF;    
  }
  
  /****************************************************************************
   *insertSorted
   *  input:node to insert, list to insert node to
   *  output: none
   *  description: searches F values of the list until larger value than that
   *    of the given node is found. For same F values, compares H values next
  ****************************************************************************/
  public void insertSorted(NodeData currentNode, LinkedList<NodeData> myList)//add to open or closed lists
  {    
    if(myList.size()==0){myList.add(currentNode);return;}
    
    int currentF=currentNode.getF();
    int listF=myList.getFirst().getF();
    int listIndex=0;//index within myList
    
    while(currentF>listF)
    {
      if(listIndex+1==myList.size())
      {
        myList.add(currentNode);
        return;
        //append node to end
      }
      listIndex++;
      listF=myList.get(listIndex).getF();
    }
    //here, currentF<=listF
    if(currentF==listF)
    {
    //tie breaker on H value (favor nodes closest to goal)
      int currentH=currentNode.getH();
      int listH=myList.get(listIndex).getH();
      if(currentH<listH){myList.add(listIndex, currentNode);return;}
      while(currentH>=listH)//if H value also equal, add after listH's value (arbitrary)
      {
        if(listIndex+1==myList.size())
        {
          myList.add(currentNode);
          return;
          //append node to end
        }
        listIndex++;
        listH=myList.get(listIndex).getH();
      }
      myList.add(listIndex, currentNode);//else
      return;
    }
    //else currentNode<listF
    {//consider making this an else/if (but the code should only get this facr if current is smaller than listF
      myList.add(listIndex,currentNode);
      return;
    }   
    //else add current node before (currentF<listF)
    
    //add a break to add at the end if currentF is larger than any value in the
    //list
  }
  
  
  /****************************************************************************
   *findNeighbors
   *  input:node to find neighbors of
   *  output:ArrayList of nodes neighbors to input node
   *  description: finds 8 neighbors of given node, removes from list if impassable
  ****************************************************************************/
  public ArrayList<NodeData> findNeighbors(NodeData currentNode)
  {
    //neighbors will be total of 8 nodes
    int colID;
    int rowID;
    ArrayList<NodeData> neighborList=new ArrayList<NodeData>();
    
    colID=currentNode.getColID();
    rowID=currentNode.getRowID();
    
    for(int rowMod=-1;rowMod<2;rowMod++)
    {
      for(int colMod=-1;colMod<2;colMod++)
      {
        if(rowMod==colMod&&colMod==0){colMod++;}//don't add currentNode to neighborList
        boolean inBounds=true;
        if((rowID+rowMod<0)||(rowID+rowMod>myMap.size())){inBounds=false;}//check row+mod for in bounds
        if(inBounds)//if it is
        {
          if((colID+colMod<0)||(colID+colMod>myMap.get(rowID+rowMod).size())){inBounds=false;}//then check col+mod for in bounds
        }
        if(inBounds)//if it passes both inBounds tests, we add it as a neighbor
        {
          neighborList.add(myMap.get(rowID+rowMod).get(colID+colMod));
        }
      }
    }
    
    //add parent to nodes with no parent, test for goal as neighbor
    for(int i=0;i<neighborList.size();i++)
    {
      //TODO would this be the best place to check for a reparent? not if g/h not calced yet
      if(neighborList.get(i).getParentNode()==null)
      {
        currentNode.setParent(neighborList.get(i));
      }
      if(neighborList.get(i).isGoal(goalLoc))
      {
        //returnPath returns a linkedList, unnecessary in this implementation
//        solvedMovedList=returnPath(neighborList.get(i));
        goalLoc.calcG(startLoc);
        returnPath(neighborList.get(i));
        
        return null;
      }
    }    
    return neighborList;    
  }
  
  /****************************************************************************
   *compareNeighborList
   *  input:list of neighbor nodes and list to compare against (works with both
   *    the open and closed lists)
   *  output:the adjusted neighborList
   *  description:makes sure the neighbor nodes are not already in the list to compare
   *    to ensure no duplicates
  ****************************************************************************/
  public ArrayList<NodeData> compareNeighborList(
      ArrayList<NodeData> neighborList, LinkedList<NodeData> myList)//myList=open or closedList
  {
    // n variables correspond to neighborList, m variables to myList
  //n variables correspond to neighborList, m variables to myList
    int neighborF, nRow, nCol;
    int myF, mRow, mCol;
    
    if(myList.size()==0||neighborList.size()==0){return neighborList;}//base case
    
    for(int nIndex=0;nIndex<neighborList.size();nIndex++)//these don't need to be for loops, as the nested while loops increment/return
    {
      for(int mIndex=0;mIndex<myList.size();mIndex++)//
      {
        neighborF=neighborList.get(nIndex).getF();
        myF=myList.get(mIndex).getF();
        while(neighborF>myF)//myList ordered smallest to largest
        {
          mIndex++;
          if(mIndex==myList.size())//mIndex already incremented, no mIndex+1==
          {
            break;//
          }
          //update values for new indices for next while() test
          neighborF=neighborList.get(nIndex).getF();
          myF=myList.get(mIndex).getF();          
        }
        if(neighborF==myF)
        {
          nRow=neighborList.get(nIndex).getRowID();
          nCol=neighborList.get(nIndex).getColID();
          mRow=myList.get(mIndex).getRowID();
          mCol=myList.get(mIndex).getColID();
          if(nRow==mRow&&nCol==mCol)
          {
            neighborList.remove(nIndex);
            nIndex--;//neighborList.size() just shrank by 1
            break;
          }          
        }
        else if(neighborF<myF)
        {
          break;//myF only get bigger as we increment, so break and compare next neighbor
        }
      }
    }
    
    for(int i=0;i<neighborList.size();i++)
    {
      //TODO get terrain, implement it here!
      if(neighborList.get(i).getElevation()==Integer.MAX_VALUE)
      {
        neighborList.remove(i);
        i--;//because the list just shrank
      }
    }
    
    return neighborList; 
  }
  
  /****************************************************************************
   *updateNeighborVal
   *  input:list of neighborNodes, and the current and end locations
   *  output:list of neighbor nodes
   *  description: checks to see if the current node is a better parent to the
   *    neighbor nodes then calculates f,g,h values
  ****************************************************************************/
  public ArrayList<NodeData> updateNeighborVal(ArrayList<NodeData> neighborList, NodeData currentNode, NodeData goalLoc)
  {
  //set and test tentative parents
    for(int i=0;i<neighborList.size();i++)
    {
      boolean betterParent=false;//flag to see if parent was reset
      currentNode.setTentativeParent(neighborList.get(i));
      neighborList.get(i).calcG(startLoc);
      betterParent=neighborList.get(i).testG(startLoc);//this function will reset the parent if it finds a better parent
      neighborList.get(i).calcG(startLoc);
      neighborList.get(i).calcH(goalLoc);
      neighborList.get(i).calcF();
    }    
    
    return neighborList;    
  }


  /****************************************************************************
   *returnPath
   *  input:goal Node
   *  output:String representing the path from start to goal node
   *  description: Once an open list node is adjacent to the goal, this is called,
   *    and tracks the path by parent back to the start node
  ****************************************************************************/
  public String returnPath(NodeData goalLoc)
  {
    int columnDiff, rowDiff;
    char up='U';
    char down='D';
    char left='L';
    char right='R';
    //LinkedList<Character> moveList=new LinkedList<Character>();
    String moveList="";
    NodeData currentNode=goalLoc;
    NodeData previousNode=goalLoc;//place holder
    Object[] pathArr=new Object[2];
    
    while(currentNode.isStart(startLoc)==false)
    {
      path.add(currentNode);
      currentNode=currentNode.getParentNode();
    }
    path.add(currentNode);    
    
    for(int i=path.size()-1;i>-1;i--)
    {
      currentNode=path.get(i);
      if(i>0)
      {previousNode=path.get(i-1);}
      else{break;}
      if((columnDiff=currentNode.getColID()-previousNode.getColID())!=0)
      {
        if(columnDiff==1){moveList+=left;}
        else{moveList+=right;}
      }
      if((rowDiff=currentNode.getRowID()-previousNode.getRowID())!=0)
      {
        if(rowDiff==1){moveList+=up;}
        else{moveList+=down;}
      }
      moveList+=',';
      //print (row,col) coord:
      //System.out.print("("+path.get(i).getRowID()+","+path.get(i).getColID()+")");
    }
//    System.out.println(moveList.size());//print number of moves
    System.out.println("Number of moves="+(path.size()-1));
//    System.out.println("move cost="+goalLoc.getParentNode().getG());
    System.out.println("move cost="+goalLoc.getG());    
    System.out.println("start elevation="+startLoc.getElevation());
    System.out.println("goal elevation="+goalLoc.getElevation());
    
    System.out.println(moveList);
   
    
    return moveList;    
  }
  
  
  /****************************************************************************
   *displayImage
   * input: buffered image to draw on
   * output: none
   * description: draws the calculated path on the map image
  ****************************************************************************/
  public void displayImage(BufferedImage img)
  {
    Image scaledImg=img.getScaledInstance(1728, 972, 0);
    JFrame myFrame=new JFrame();
    JLabel label=new JLabel(new ImageIcon(scaledImg));
    myFrame.getContentPane().setLayout(new FlowLayout());
    myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    myFrame.getContentPane().add(label);
    myFrame.pack();
    myFrame.setLocation(100,100);
    myFrame.setVisible(true);
  }
}