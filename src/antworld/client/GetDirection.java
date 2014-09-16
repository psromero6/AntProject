/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
  public Direction returnDirEnum(int x, int y)
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
    
    if(x==0)
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
    
    if(x==1)
    {
      if(y==-1)
      {
        return Direction.SOUTHEAST;
      }
      else if(y==0)
      {
        return Direction.EAST;
      }
      
      else if(y==1)//else if for security, could read else
      {
        return Direction.NORTHEAST;
      }      
    }
    
    return null;    
  }
  
}
