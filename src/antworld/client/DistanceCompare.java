/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package antworld.client;

import antworld.data.AntData;
import java.util.Comparator;

/**
 *
 * @author Hans
 */
public class DistanceCompare implements Comparator<AntData>//returns lesser distance
{
  public NodeData goalNode=MapControl.myMap.get(0).get(0);//distance will be measured to this node, it MUST be set, or else it will measure distance to the upper lefthand
  
  /****************************************************************************
   *compare
   *  input:2 AntDatas to compare in distance to a third location
   *  output: integer, positive, zero, or negative for first closer, equidisant
   *    or second closer, repectively
   *  description: compares distances for 2 ant. must set the node where comparison
   *    occurs externally.
  ****************************************************************************/
  @Override
  public int compare(AntData ant1, AntData ant2)
  {
     int ant1Row=ant1.gridY;
     int ant1Col=ant1.gridX;
     int ant1Distance;
     
     int ant2Row=ant2.gridY;
     int ant2Col=ant2.gridX;
     int ant2Distance;
     
     int goalRow=goalNode.rowID;
     int goalCol=goalNode.colID;
     
     ant1Distance=Math.abs(goalRow-ant1Row)+Math.abs(goalCol-ant1Col);
     ant2Distance=Math.abs(goalRow-ant2Row)+Math.abs(goalCol-ant2Col);
     if(ant1Distance>ant2Distance)
     {
       return 1;
     }
     else if(ant1Distance>ant2Distance)
     {
       return -1;
     }       
     else
     {
       return 0;
     }
  }  
}
