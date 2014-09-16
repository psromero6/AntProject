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
  public static ArrayList<ArrayList<NodeData>> myMap = Control.myMap;
  
  public BLine()
  {
  }

  public LinkedList<AntAction> findPath(NodeData myStartLoc, NodeData myGoalLoc)
  {
    LinkedList<AntAction> moveList = new LinkedList<AntAction>();
    NodeData currentNode=myStartLoc;//currentNode and variables only necessary to draw the path
    GetDirection getDir=new GetDirection();
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
      int dirX=0;//values to pass to getDir to return direcrtion Enum
      int dirY=0;
      AntAction action = new AntAction(AntAction.AntActionType.MOVE, 0, 0);
      if (deltaCol > 0)
      {
//        action.x = deltaColSign;
        dirX=deltaColSign;
        currentCol+=deltaColSign;
        deltaCol--;
      }
      if (deltaRow > 0)
      {
//        action.y = deltaRowSign;
        dirY=deltaRowSign;
        currentRow+=deltaRowSign;
        deltaRow--;
      }
      action.direction=getDir.returnDirEnum(dirX, dirY);
      
      
      currentNode=myMap.get(currentRow).get(currentCol);
//      System.out.println("goalNode=("+currentRow+","+currentCol+")");
      drawStep(currentNode);
      moveList.add(action);
    }
    
    System.out.println("goalNode=("+myGoalLoc.getRowID()+","+myGoalLoc.getColID()+")");
    return moveList;
  }

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  public void drawStep(NodeData currentNode)
  {
    int currRow = currentNode.getRowID();
    int currCol = currentNode.getColID();
    int currH = 255;//Hvalue will not be populated for BLine
  //  ClientRandomWalk.myClient.antworld.drawMapPixel(currCol, currRow, currH);
  }
}
