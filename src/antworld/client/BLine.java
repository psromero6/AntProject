/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antworld.client;

import antworld.data.AntAction;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Hans
 */
public class BLine
{
  public static ArrayList<ArrayList<NodeData>> myMap = MapControl.myMap;
  
  private static final boolean DRAWLINES = true;
  
  /****************************************************************************
   *findPath  
   *  input:start node and goal node for the BLine path
   *  output:list of AntAction of type move to travel from the start to goal location
   *  description: calculates a path in a "straight" line (on 8 dir move, traveling
   *    at a diagonal until only moves in x or y remain is as many steps as an
   *    apparent straight line) and returns this path as an ArrayList.
  ****************************************************************************/
  public LinkedList<AntAction> findPath(NodeData myStartLoc, NodeData myGoalLoc)
  {
    LinkedList<AntAction> moveList = new LinkedList<AntAction>();
    NodeData currentNode=myStartLoc;//currentNode and variables only necessary to draw the path
    //GetDirection getDir=new GetDirection();
    int startRow = myStartLoc.getRowID();
    int startCol = myStartLoc.getColID();
    int currentRow=startRow;//
    int currentCol=startCol;//
    int goalRow = myGoalLoc.getRowID();
    int goalCol = myGoalLoc.getColID();
    int deltaRow = goalRow - startRow;
    int deltaCol = goalCol - startCol;
    int deltaRowSign=0;
    int deltaColSign=0;
 
//    int deltaRow = startRow-goalRow;
//    int deltaCol = startCol-goalCol;
    if (deltaRow != 0)
    {
      deltaRowSign = deltaRow / Math.abs(deltaRow);
    }//make sure these 2 signs are in accordance with NSEW on Joel's directions
    if (deltaCol != 0)
    {
      deltaColSign = deltaCol / Math.abs(deltaCol);
    }
    deltaRow = Math.abs(deltaRow);//make these positive to simpify test cases below (if/while)
    deltaCol = Math.abs(deltaCol);//then increment by 1*deltaRow/ColSign
    

    while (deltaRow > 0 || deltaCol > 0)
    {
      int dirX;//values to pass to getDir to return direcrtion Enum
      int dirY;
    //  AntAction action = new AntAction(AntAction.AntActionType.MOVE);
      if (deltaCol > 0)
      {
        dirX=deltaColSign;
        currentCol+=deltaColSign;
        deltaCol--;
      }
      else{
      dirX=0;
      }
              
      if (deltaRow > 0)
      {
        dirY=deltaRowSign;
        currentRow+=deltaRowSign;
        deltaRow--;
      }
      else{
      dirY=0;
      }
    //  action.direction=getDir.returnDirEnum(dirX, dirY);
      
      
      currentNode=myMap.get(currentRow).get(currentCol);
//      System.out.println("goalNode=("+currentRow+","+currentCol+")");
   if(DRAWLINES) drawStep(currentNode);
        //System.out.println("start delta:"+(goalRow - startRow)+";"+(goalCol - startCol)+"running sign"+deltaColSign+";"+deltaRowSign+"calculating dir with"+dirX+";"+dirY+" what is left is:"+deltaCol+";"+deltaRow);
        
      moveList.add(new AntAction(AntAction.AntActionType.MOVE, GetDirection.returnDirEnum(dirX, dirY)));
     //System.out.println(moveList.peek().direction);
    }
    
   // System.out.println("goalNode=("+myGoalLoc.getRowID()+","+myGoalLoc.getColID()+")");
    return moveList;
  }

  /****************************************************************************
   *drawStep  
   * input:node to draw a pixel at
   *  output:none
   *  description: draws the path of the BLine on the map image (accesses 
   *    draw method in clientRandomWalk. called to show when an ant is traveling
   *    to food or back to base
  ****************************************************************************/
  public void drawStep(NodeData currentNode)
  {
    int currRow = currentNode.getRowID();
    int currCol = currentNode.getColID();
    int currH = 255;//Hvalue will not be populated for BLine
   // ClientRandomWalk.myClient.antworld.gameBoard.setCenter(currCol, currRow);
  if(ClientControl.doDraw())  ClientControl.myClient.antworld.drawMapPixel(currCol, currRow, currH);
    
  }
}
