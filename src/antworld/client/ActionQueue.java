/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antworld.client;

import antworld.data.AntAction;
import antworld.data.AntAction.AntActionType;
import antworld.data.AntData;
import antworld.data.CommData;
import antworld.data.Direction;
import antworld.data.NestNameEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author 363620
 */
public class ActionQueue
{

    public ArrayList<Integer> antList = new ArrayList<Integer>();//arrayList of AntData.id
    public HashMap<Integer, LinkedList<AntAction>> commandMap = new HashMap<Integer, LinkedList<AntAction>>();
    public HashMap<Integer, AntAction> questMapping=new HashMap<Integer, AntAction>();//this mapping will tell us what quest an ant is currently on //AntAction.HEAL if returning to heak, AntAction.PICKUP if going to collect food etc.
    public final int centerX, centerY;

    //need a way to add remove queues and ants if they are born or if they die

    public ActionQueue(CommData myComm)
    {
            NestNameEnum myNestName = myComm.myNest;
            centerX = myComm.nestData[myNestName.ordinal()].centerX;
            centerY = myComm.nestData[myNestName.ordinal()].centerY;
        int i = 0;
        for (AntData myAntData : myComm.myAntList)
        {
            LinkedList antActionQueue = new LinkedList<>();//each ant will have a queue of actions to take. we want it to work like a queue, except that we wont "pop"
            //the queue until we get confirmation that the previous move happened.
            antList.add(myAntData.id);
            commandMap.put(myAntData.id, antActionQueue);
        }
    }

//    public void updateActionQueue(AntData ant)
//    {
//        LinkedList<AntAction> myActionQueue = commandMap.get(ant.id);//get the value for this key
//
//        AntAction myAction = new AntAction(AntActionType.STASIS);
//        myActionQueue.add(myAction);
//    }
    
    public LinkedList<AntAction> collectFood(AntData ant, NodeData foodNode)//currently, this method returns the action list it built AND also populates the list of the and handed to it
  {
        System.out.println("getfood");
    LinkedList<AntAction> fetchList = commandMap.get(ant.id);//get the actionList associated with the ant
    BLine myLine = new BLine();
    NodeData adjFoodNode;//Node adjacent to food
    AntAction action;
    AntAction currentQuest=new AntAction(AntActionType.PICKUP);
    questMapping.put(ant.id,currentQuest);
    while(fetchList.size()>0)
    {
      fetchList.removeFirst();//clear the action list
    }
    
    //\\First, calculate tile adjacent to foodNode, closest to startNode
    int currentRow = ant.gridY;
    int currentCol = ant.gridX;
    NodeData currentNode=Control.myMap.get(currentRow).get(currentCol);
    int foodRow = foodNode.getRowID();
    int foodCol = foodNode.getColID();
    int adjFoodRow;
    int adjFoodCol;
    int xDirection = currentCol - foodCol;
    int yDirection = currentRow - foodRow;
    
    if (xDirection != 0)
    {
      xDirection /= Math.abs(xDirection);//make unit length
    }
    if (yDirection != 0)
    {
      yDirection /= Math.abs(yDirection);//make unit length
    }
    adjFoodRow=foodRow+yDirection;
    adjFoodCol=foodCol+xDirection;
    adjFoodNode=Control.myMap.get(adjFoodRow).get(adjFoodCol);
    
    //\\second, set fetchList equal to the path to food
    fetchList = myLine.findPath(currentNode, adjFoodNode);

    //\\third, add ant action gather the food and take a few steps away from it (orthoganally?)
    action=new AntAction(AntActionType.PICKUP);
    xDirection=(-xDirection);//set these as direction TO foodNode FROM adjFoodNode
    yDirection=(-yDirection);//set these as direction TO foodNode FROM adjFoodNode
    GetDirection getDir=new GetDirection();
    action.direction=getDir.returnDirEnum(xDirection, yDirection);
    action.quantity=50;//this is max?
   // System.out.println("should be pickup:"+action.type+";"+action.direction);
    fetchList.add(action);
   // System.out.println("should be the first of food quest:"+fetchList.peek().direction);
 
    //if x or y Dir == 0, swap them to move orthoganally
    if(xDirection==0||yDirection==0)
    {
      int holdValue=xDirection;
      xDirection=yDirection;
      yDirection=holdValue;
    }
    //otherwise, change the sign of xDir (or yDir) to move orth
    else
    {
     xDirection=-xDirection; 
    }    
    
    int stepOrthFromFood=3;
    for(int i=0;i<stepOrthFromFood;i++)
    {
      //tell ant to move three steps orthoganally from food
      action=new AntAction(AntActionType.MOVE);
      action.direction=getDir.returnDirEnum(xDirection, yDirection);
      fetchList.add(action);
    }
    adjFoodRow=adjFoodRow+(stepOrthFromFood*yDirection);
    adjFoodCol=adjFoodCol+(stepOrthFromFood*xDirection);
    if(adjFoodRow<0)adjFoodRow=0;
    if(adjFoodCol<0)adjFoodCol=0;
    adjFoodNode=Control.myMap.get(adjFoodRow).get(adjFoodCol);
    
    //\\fourth and final, ADD moveList to return to nest
    //System.out.println("what's the meaning?"+ClientRandomWalk.myClient.meaningOfLife);
    currentNode=Control.myMap.get(centerY).get(centerX);
    LinkedList<AntAction> returnList = new LinkedList<>();
    returnList=myLine.findPath(adjFoodNode,currentNode);//TODO change this to antHill, not current

    
    for (AntAction addAction : returnList) {
            fetchList.add(addAction);
        }
    //add drop to end of list
        AntAction dropaction=new AntAction(AntActionType.DROP);
        dropaction.direction=Direction.NORTH;
        dropaction.quantity=50;
        
        fetchList.add(dropaction);
        
  //  System.out.println("Ant:"+ant.id+" has actionQueue size "+commandMap.get(ant.id).size()+", but fetch list of size "+fetchList.size());
    commandMap.put(ant.id,fetchList);
   // System.out.println("Ant:"+ant.id+" has actionQueue size "+commandMap.get(ant.id).size()+", but fetch list of size "+fetchList.size());
    return fetchList;
  }
    
   public LinkedList<AntAction> nestToHeal(AntData ant)
   {
    int currentRow = ant.gridY;
    int currentCol = ant.gridY;
    int homeRow = centerY;
    int homeCol = centerX;
    NodeData currentNode=Control.myMap.get(currentRow).get(currentCol);
    NodeData homeNode=Control.myMap.get(homeRow).get(homeCol);
    AntAction currentQuest=new AntAction(AntActionType.HEAL);
    questMapping.put(ant.id,currentQuest);
    BLine pathHome=new BLine();
    
    //\\first tell ant to return home
    LinkedList<AntAction> actionList=commandMap.get(ant.id);
    while(actionList.size()!=0)
    {
      actionList.removeFirst();
    }
    actionList=pathHome.findPath(currentNode, homeNode);
    
    //\\second tell ant to enter nest
     AntAction enterAction = new AntAction(AntActionType.ENTER_NEST);
     actionList.add(enterAction);
    
    //\\third tell ant to heal
     int maxHealth=20;//not sure this is max
     int unitsToHeal=maxHealth-ant.health;
     for(int i=0;i<unitsToHeal;i++)
     {
       AntAction healAction=new AntAction(AntActionType.HEAL);
       actionList.add(healAction);
     }
     
     return actionList;
   }
   
   public LinkedList<AntAction> collectWater(AntData ant, NodeData foodNode)//currently, this method returns the action list it built AND also populates the list of the and handed to it
  {
     return null;
  }
}
