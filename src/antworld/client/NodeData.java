/******************************************************************************
 * Hans Weeks
 * 
 * Class for the NodeData object. This class contains all the information
 * and methods necessary to calculate an A* path, namely calculating
 * the G,H, and F values.
/*****************************************************************************/
package antworld.client;

import antworld.data.LandType;
import java.awt.Color;
import java.awt.image.Raster;
import java.util.Arrays;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;


public class NodeData
{
  public int colID;
  public int rowID;
  
  private int h_heuristic;
  private int g_movecost;
  private int f_value;
  private int[] color;
  private int elevation;//included for printing purposes
  //private boolean startLoc=false;
  //private boolean goalLoc=false;
  private NodeData parentNode;
  private NodeData tentativeParentNode;

  /******************************************************************************
   * Input:integers for the column and row of the node int the map, and its
   *  movement weight as an int.
   * Output:None, this is the constructor
   * Description:creates a node object from three ints provided
  /*****************************************************************************/
  public NodeData(int row, int col, int green)//int[] colorin)
  {    
   rowID=row;
   colID=col;
   
   elevation=green;
  }
  
  
  public void setParent(NodeData child)
  {
    child.parentNode=this;
  }
  
  public void setTentativeParent(NodeData child)
  {
    child.tentativeParentNode=this;
  }
  
  /******************************************************************************
   * Input:startNode
   * Output:none
   * Description:calculates the cost to move from an adjacent node to this one,
   *  based on elevation changes
  /*****************************************************************************/  
  public int calcBase_Movecost(NodeData startLoc)
  {
    //TODO base startLoc is saved to the node rather than the path, for 100 ants this results in 100 start nodes
    if(this.isStart(startLoc)){return 0;}
    int thisEle=this.elevation;
    int parEle=this.getParentNode().elevation;
    int elevationChange=thisEle-parEle;
    
    int base_movecost;
    //check for null parent
    if(this.isStart(startLoc)){base_movecost=0;}//if startLoc, move=0
    else if(elevationChange<1)//flat elevation or decline
    {
      base_movecost=1;
    }
    else//if(elevationChange>1)//incline
    {
      base_movecost=2;
    }
    return base_movecost;
  }
  
  
  public static int[] RGB(int rgb){
      
  int r = (rgb)&0xFF;
int g = (rgb>>8)&0xFF;
int b = (rgb>>16)&0xFF;
  int[] rgbMat={r,g,b};
  return rgbMat;
  }
  
  
  /******************************************************************************
   * Input:none
   * Output:none
   * Description:Calculates G movecost from this.base_movecost and adding it 
   * to its parents G movecost
  /*****************************************************************************/
  public void calcG(NodeData startLoc)//will require access to parent node
  {
    int base_movecost=calcBase_Movecost(startLoc);
    //check for null parent
    if(this.isStart(startLoc)){g_movecost=0;}
    else{g_movecost=base_movecost+parentNode.getG();}
  }
  
  /******************************************************************************
   * Input:none
   * Output:none
   * Description: tests whether the current parent or tentative parent G movecost
   * is lower, and sets the parent accordingly
  /*****************************************************************************/
  public boolean testG(NodeData startLoc)
  {
    boolean testResult=false;
    int test_movecost;
    int base_movecost=calcBase_Movecost(startLoc);
    if(this.isStart(startLoc)){test_movecost=0;}
    else{test_movecost=base_movecost+tentativeParentNode.getG();}
    if(test_movecost<g_movecost)
    {
      //if we get here, it means that the tentParent is a better path to the current node and we reparent
      testResult=true;
      parentNode=tentativeParentNode;
      tentativeParentNode=null;
    }
    return testResult;    
  }
  
  /******************************************************************************
   * Input:NodeData goal location
   * Output:none
   * Description:calculates the manhattan distance from node to goal node
  /*****************************************************************************/
  public void calcH(NodeData goalLoc)
  {
    //TODO consider making this euclidean instead of manhattanian
    int deltaX=0;
    int deltaY=0;
    int deltaZ=0;
    deltaX=Math.abs(goalLoc.colID-this.colID);
    deltaY=Math.abs(goalLoc.rowID-this.rowID);
    deltaZ=goalLoc.getElevation()-this.getElevation();
    if(deltaZ<0){deltaZ=0;}//add distance only if traveling uphill, as downill speed=flat speed
    h_heuristic=deltaZ+(int)Math.sqrt((deltaX*deltaX)+(deltaY*deltaY));
    //h_heuristic=deltaX+deltaY;
  }
  
  /******************************************************************************
   * Input:none
   * Output:none
   * Description:calculates the F values (the key value to A*) by adding the 
   * G and H scores
  /*****************************************************************************/
  public void calcF()
  {
    f_value=g_movecost+h_heuristic;
  }
  
  public int getH()
  {
    return this.h_heuristic;
  }
  
  public int getF()
  {
    return this.f_value;
  }
  
  public int getG()
  {
    return this.g_movecost;
  }
  
  public int getElevation()
  {
    return this.elevation;
  }
  
  public int getColID()
  {
    return this.colID;
  }
  
  public int getRowID()
  {
    return this.rowID;
  }
  
  public NodeData getParentNode()
  {
    return this.parentNode;
  }
  
  public boolean isStart(NodeData startLoc)
  {
    if((this.getRowID()==startLoc.getRowID())&&(this.getColID()==startLoc.getColID()))
    {
      return true;
    }
    return false;
  }
  
  public boolean isGoal(NodeData goalLoc)
  {
    if((this.getRowID()==goalLoc.getRowID())&&(this.getColID()==goalLoc.getColID()))
    {
      return true;
    }
    return false;
  }
  
  public void print()
  {
    System.out.print(elevation);
  }  
}