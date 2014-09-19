/****************************************************************************
   *  Authors: Hans Weeks and Stephen Romero
   * 
   *This is a helper class which tells you what direction enum represents an x
   * and y direction. To use it, pass unit directions of -1,0,1
  ****************************************************************************/

package antworld.client;

import antworld.data.Direction;

/**
 *
 * @author Hans
 * 
 * This object returns directions of NORTH, SOUTH, EAST, WEST, and combinations
 * when given deltaX and deltaY values because the AntAction's x and y are absolute
 * coordinates and there are no methods to resolve deltaX and deltaY to Directions
 * in AntAction or in Direction.
 */
public class GetDirection
{
  /****************************************************************************
   *returnDirEnum  
   *  input: x and y, only -1-->1 
   *  output: direction represented as Direction enum from Direction class
   *  description: returns the dir enum for the given x and y direction
  ****************************************************************************/
  public static Direction returnDirEnum(int x, int y)
  {
    if(x==-1)
    {
      if(y==-1)
      {
        return Direction.NORTHWEST;
      }
      else if(y==0)
      {
        return Direction.WEST;
      }
      
      else if(y==1)//else if for security, could read else
      {
        return Direction.SOUTHWEST;
      }      
    }
    
   else if(x==0)
    {
      if(y==-1)
      {
        return Direction.NORTH;
      }
      else if(y==0)
      {
        return null;//because 0,0 is not direction
      }
      
      else if(y==1)//else if for security, could read else
      {
        return Direction.SOUTH;
      }      
    }
    
  else  if(x==1)
    {
      if(y==1)
      {
        return Direction.SOUTHEAST;
      }
      else if(y==0)
      {
        return Direction.EAST;
      }
      
      else if(y==-1)//else if for security, could read else
      {
        return Direction.NORTHEAST;
      }      
    }
    
    return null;    
  }
  
}
