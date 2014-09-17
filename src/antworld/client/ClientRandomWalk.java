package antworld.client;

import static antworld.client.AntWorld.gameBoard;
import antworld.data.AntAction;
import antworld.data.AntAction.AntActionType;
import antworld.data.AntData;
import antworld.data.AntType;
import antworld.data.CommData;
import antworld.data.Constants;
import antworld.data.Direction;
import antworld.data.FoodData;
import antworld.data.FoodType;
import antworld.data.NestNameEnum;
import antworld.data.TeamNameEnum;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_X;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class ClientRandomWalk
{
  private static final boolean DEBUG = false;
  private static final boolean TRACKACTION = false;
  private static final boolean SCOREING = true;
  private static final boolean DRAW = true;
  private static final boolean BUILD = false;
  private static final TeamNameEnum myTeam = TeamNameEnum.Buffalograss;
  private static final long password = 122538603443L;//Each team has been assigned a random password.
  static ClientRandomWalk myClient;//package private?
  static AntWorld antworld;
  static ActionQueue commandAnts;
  private ObjectInputStream inputStream = null;
  private ObjectOutputStream outputStream = null;
  private boolean isConnected = false;
  private boolean goHome=false;
  private NestNameEnum myNestName = null;
  private int centerX, centerY;
  private int[] solidFood;
  private Socket clientSocket;
  private ArrayList<FoodData> oldFood;
  private static Random random = Constants.random;

  public ClientRandomWalk(String host, int portNumber) throws IOException
  {
    System.out.println("Starting ClientRandomWalk: " + System.currentTimeMillis());
    isConnected = false;
    while (!isConnected)
    {
      isConnected = openConnection(host, portNumber);
    }
    CommData data = chooseNest();
    mainGameLoop(data);
    closeAll();
  }

  private boolean openConnection(String host, int portNumber)
  {

    try
    {
      clientSocket = new Socket(host, portNumber);
    } catch (UnknownHostException e)
    {
      System.err.println("ClientRandomWalk Error: Unknown Host " + host);
      e.printStackTrace();
      return false;
    } catch (IOException e)
    {
      System.err.println("ClientRandomWalk Error: Could not open connection to " + host + " on port " + portNumber);
      e.printStackTrace();
      return false;
    }

    try
    {
      outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
      inputStream = new ObjectInputStream(clientSocket.getInputStream());

    } catch (IOException e)
    {
      System.err.println("ClientRandomWalk Error: Could not open i/o streams");
      e.printStackTrace();
      return false;
    }

    return true;

  }

  public void closeAll()
  {
    System.out.println("ClientRandomWalk.closeAll()");
    {
      try
      {
        if (outputStream != null)
        {
          outputStream.close();
        }
        if (inputStream != null)
        {
          inputStream.close();
        }
        clientSocket.close();
      } catch (IOException e)
      {
        System.err.println("ClientRandomWalk Error: Could not close");
        e.printStackTrace();
      }
    }
  }

  public CommData chooseNest()
  {
    while (myNestName == null)
    {
      try
      {
        Thread.sleep(100);
      } catch (InterruptedException e1)
      {
      }

      NestNameEnum requestedNest = NestNameEnum.BULLET;//.values()[random.nextInt(NestNameEnum.SIZE)];
      CommData data = new CommData(requestedNest, myTeam);
      data.password = password;

      if (sendCommData(data))
      {
        try
        {

          if (DEBUG)
          {
            System.out.println("ClientRandomWalk: listening to socket...."+inputStream.toString());
          }
          CommData recvData = (CommData) inputStream.readObject();

          if (DEBUG)
          {
            System.out.println("ClientRandomWalk: recived <<<<<<<<<" + inputStream.available() + "<...\n" + recvData);
          }

          if (recvData.errorMsg != null)
          {
            System.err.println("ClientRandomWalk***ERROR***: " + recvData.errorMsg);
            continue;
          }

          if ((myNestName == null) && (recvData.myTeam == myTeam))
          {
            myNestName = recvData.myNest;
            centerX = recvData.nestData[myNestName.ordinal()].centerX;
            centerY = recvData.nestData[myNestName.ordinal()].centerY;
            System.out.println("ClientRandomWalk: !!!!!Nest Request Accepted!!!! " + myNestName);
            return recvData;
          }
        } catch (IOException e)
        {
          System.err.println("ClientRandomWalk***ERROR***: client read failed");
          e.printStackTrace();
        } catch (ClassNotFoundException e)
        {
          System.err.println("ClientRandomWalk***ERROR***: client sent incorect data format");
        }
      }
    }
    return null;
  }

  public void mainGameLoop(CommData data) throws IOException
  {
    System.out.println("mainGameLoop");
    if (DRAW)
    {
      antworld = new AntWorld(data);
    }
     System.out.println("controlStart");
    Control myControl = new Control();//this populates the map in control, which is reffered to in goto and actionqueue
    oldFood=new ArrayList<>();
 System.out.println("controlStop");
    commandAnts = new ActionQueue(data);
 System.out.println("actionlQueue made");
    //antworld.setLocation(centerX, centerX);
    //testing with initalizing actions psr
    AntAction action = new AntAction(AntActionType.STASIS);
    LinkedList<AntAction> myActionQueue;

//        for(AntData ant :data.myAntList){
//        myActionQueue = commandAnts.commandMap.get(ant.id);
//        action.type=AntActionType.MOVE;
//        action.direction=Direction.NORTH;
//            for(int i=0;i<100;i++){ 
//               myActionQueue.add(action);
//           }
//        }
    // drawAnts(data);
    while (true)
    {antworld.draw(data);
      if (DRAW&&(data.gameTick%50==0))
      {
          antworld=new AntWorld(data);
          System.out.println("draw");
          antworld.setLocation(centerX, centerY);
        
      }
      try
      {
           
        if (DEBUG)
        {
          System.out.println("ClientRandomWalk: chooseActions: " + myNestName);
        }

        chooseActionsOfAllAnts(data);

        CommData sendData = data.packageForSendToServer();

        //System.out.println("ClientRandomWalk: Sending>>>>>>>: " + sendData);
        outputStream.writeObject(sendData);
        outputStream.flush();
        outputStream.reset();

        if (DEBUG)
        {
          System.out.println("ClientRandomWalk: listening to socket....");
        }
        
        
        HashSet<FoodData> food = data.foodSet;
    FoodData[] myFoodArray = new FoodData[food.size()];
    food.toArray(myFoodArray);
       if(SCOREING){ System.out.println(data.gameTick);
        for(int fd :data.foodStockPile){
        System.out.print(fd+";");        
        }
        
        
        
        System.out.println("total number of ants"+data.myAntList.size()+" xxxxxxxx");
       }
        for(FoodData fd : myFoodArray){
           // System.out.println(fd.foodType+";"+fd.gridX+";"+fd.gridY);
            if((oldFood.isEmpty()||oldFood.contains(fd)||(data.gameTick%500==0))&&fd.foodType!=FoodType.WATER)collectFood(myFoodArray,data);
            
            
        }
        oldFood.clear();
        oldFood.addAll(food);
        
        
        
        CommData recivedData = (CommData) inputStream.readObject();
        if (DEBUG)
        {
          System.out.println("ClientRandomWalk: received <<<<<<<<<" + inputStream.available() + "<...\n" + recivedData);
        }
        data = recivedData;

        if ((myNestName == null) || (data.myTeam != myTeam))
        {
          System.err.println("ClientRandomWalk: !!!!ERROR!!!! " + myNestName);
        }
      } catch (IOException e)
      {
        System.err.println("ClientRandomWalk***ERROR***: client read failed");
        e.printStackTrace();
        try
        {
          Thread.sleep(1000);
        } catch (InterruptedException e1)
        {
        }

      } catch (ClassNotFoundException e)
      {
        System.err.println("ServerToClientConnection***ERROR***: client sent incorect data format");
        e.printStackTrace();
        try
        {
          Thread.sleep(1000);
        } catch (InterruptedException e1)
        {
        }
      }

    }
  }

  private boolean sendCommData(CommData data)
  {

    CommData sendData = data.packageForSendToServer();
    try
    {
      if (DEBUG)
      {
        System.out.println("ClientRandomWalk.sendCommData(" + sendData + ")");
      }

      outputStream.writeObject(sendData);
      outputStream.flush();
      outputStream.reset();
    } catch (IOException e)
    {
      System.err.println("ClientRandomWalk***ERROR***: client read failed");
      e.printStackTrace();
      try
      {
        Thread.sleep(1000);
      } catch (InterruptedException e1)
      {
      }
      return false;
    }

    return true;

  }

  private void chooseActionsOfAllAnts(CommData data)
  {
     // if(data.foodStockPile[0]<350) collectWater=true;
    BLine myPath=new BLine();
    NodeData homeNode=Control.myMap.get(centerY).get(centerX);
    
    
    
    HashSet<FoodData> food = data.foodSet;
    FoodData[] myFoodArray = new FoodData[food.size()];
    food.toArray(myFoodArray);
    
   
    
    
    
    
    
    
    
    for (AntData ant : data.myAntList)
    {
        int nestDistance=Math.abs(ant.gridX-centerX)+Math.abs(ant.gridY-centerY);
        
        
        
        if(ant.carryUnits>0&&nestDistance<10){
      if(SCOREING) System.out.println("Drop it sucka");
        AntAction dropaction=new AntAction(AntActionType.DROP);
        dropaction.direction=Direction.getRandomDir();
        dropaction.quantity=ant.carryUnits;
        ant.myAction=dropaction;
        }
        
        
        else if(ant.health<10&&nestDistance<10&&!ant.underground){
         if(SCOREING) System.out.println("GetDown");
         AntAction duckaction=new AntAction(AntActionType.ENTER_NEST);
         ant.myAction=duckaction;
        }
        
        
        
        else if(goHome){
        NodeData currentNode=Control.myMap.get(ant.gridY).get(ant.gridX);
            commandAnts.commandMap.put(ant.id,myPath.findPath(currentNode, homeNode));
            
            commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.ENTER_NEST));
        }
        else{
        if(ant.carryUnits>0&&(commandAnts.questMapping.get(ant.id)==null||(commandAnts.questMapping.get(ant.id).type!=AntActionType.DROP)))
        {
            if(TRACKACTION) System.out.println("I have grub.........................................");
            NodeData currentNode=Control.myMap.get(ant.gridY).get(ant.gridX);
            commandAnts.commandMap.put(ant.id,myPath.findPath(currentNode, homeNode));
            
            commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.DROP));
        }
        else
        {
           // commandAnts.updateActionQueue(ant);
            AntAction action = chooseAction(data, ant);
            if(action.type==AntActionType.DROP)
            {
             if(TRACKACTION)    System.out.println("Ant"+ant.id+" dropping off");
            }
            if(action.type==AntActionType.PICKUP)
            {
             if(TRACKACTION)    System.out.println("Ant"+ant.id+" picking up");
            }

            //  System.out.println(action.type+";"+action.direction);
            ant.myAction = action;
        }
    }
    goHome=false;
    
    if(data.foodStockPile[1]<200) collectWater(data);

    
    
    
   
    
    
    
  }
    if(BUILD){
   if(data.foodStockPile[3]>100){
  AntData attackAnt=new AntData(Constants.UNKNOWN_ANT_ID, AntType.ATTACK, data.myNest, data.myTeam);
  attackAnt.myAction=new AntAction(AntActionType.BIRTH); 
  data.myAntList.add(attackAnt);
   
  }
  if(data.foodStockPile[4]>100){
  AntData speedAnt=new AntData(Constants.UNKNOWN_ANT_ID, AntType.SPEED, data.myNest, data.myTeam);
  speedAnt.myAction=new AntAction(AntActionType.BIRTH); 
  data.myAntList.add(speedAnt);
   
  }
    }
  for(AntData dickAnt: data.enemyAntSet){
  if(dickAnt.myAction.type==AntActionType.ATTACK)System.out.println("enemy ant attack!!!!!!!!!!!!!!!!");
  
  }
  
  
  }

  private AntAction chooseAction(CommData data, AntData ant)
  {
      
    AntAction action = new AntAction(AntActionType.STASIS);
    BLine myPath=new BLine();
    //testing
    // if(ant.alive){action.type=AntActionType.MOVE;
    //action.direction=Direction.getRandomDir();
    //return action;}
    LinkedList<AntAction> myActionQueue;
    myActionQueue = commandAnts.commandMap.get(ant.id);
   
    
    
    
    
    
    
    if (ant.ticksUntilNextAction > 0)
    {
      //System.out.println("waiting on " + ant.ticksUntilNextAction + " ticks   " + ant.id);
      return ant.myAction;
    }

    
    
    if(ant.carryUnits>0){
   if(TRACKACTION)  System.out.println("has food.."+ant.gridX+";"+ant.gridY+"...................");
    
    }
    
    
    
    
    
    if(ant.carryUnits>0&&commandAnts.questMapping.get(ant.id)==null){
        
       commandAnts.commandMap.get(ant.id).clear();
        
        
      if(TRACKACTION)   System.out.println("dropoff food");
    NodeData currentNode=Control.myMap.get(ant.gridY).get(ant.gridX);
        commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.DROP));
            commandAnts.commandMap.put(ant.id,myPath.findPath(currentNode,Control.myMap.get(centerY).get(centerX)));
            
           
    
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    if (ant.underground)
    {
      if(ant.health<10)//do not emerge at less than half health
      {
       if(TRACKACTION)  System.out.println("ant"+ant.id+" is underground with low health of "+ant.health);
        AntAction healAction = new AntAction(AntActionType.HEAL);
        return healAction;
      }
     if(TRACKACTION)  System.out.println("Climbing out   " + ant.id);
      //initalizing the queque

      action = scatter(ant.id);

//      for (int i = 0; i < 100; i++)
//      {
//      myActionQueue.add(action);
//      }
      AntAction exitAction = new AntAction(AntActionType.EXIT_NEST);
      exitAction.x = centerX - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
      exitAction.y = centerY - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
      return exitAction;
    }
       
    
if((ant.health<10)&&(commandAnts.questMapping.get(ant.id)==null||commandAnts.questMapping.get(ant.id).type!=AntActionType.HEAL))//test to see if ant needs to return home to heal
    {
     if(TRACKACTION)  System.out.println("ant"+ant.id+" has low health, returning to base");
      AntAction currentQuest=commandAnts.questMapping.get(ant.id);
     
        if(TRACKACTION)   System.out.println("quest set to heal");
          NodeData currentNode=Control.myMap.get(ant.gridY).get(ant.gridX);
          NodeData homeNode=Control.myMap.get(centerY).get(centerX);
           commandAnts.commandMap.put(ant.id,myPath.findPath(currentNode, homeNode));
       commandAnts.questMapping.put(ant.id,new AntAction(AntActionType.HEAL));//builds a list of actions to return and heal
      
    }









//with an empty queue the ant runs one direction until he hits a wall. then he will run another

    //  NestData nestData=data.nestData[myNestName.ordinal()];
    HashSet<FoodData> food = data.foodSet;
    FoodData[] myFoodArray = new FoodData[food.size()];
    food.toArray(myFoodArray);
    
    //   System.out.println(a.length+";"+data.foodSet.isEmpty());
    
    
   
    if(true){
if (commandAnts.commandMap.get(ant.id)==null||commandAnts.commandMap.get(ant.id).isEmpty()||commandAnts.questMapping.get(ant.id)==null||commandAnts.questMapping.get(ant.id).type==AntActionType.MOVE){
    for (FoodData f : myFoodArray)
    {
      //all ants within 5 pixels of the food will go pick it up.
      //   System.out.println(Math.abs(f.gridX-ant.gridX)+Math.abs(f.gridY-ant.gridY));
      if(Math.abs(f.gridX - ant.gridX) + Math.abs(f.gridY - ant.gridY) < 5&&f.foodType!=FoodType.WATER&&ant.carryUnits<1)
      {
      if(TRACKACTION)     System.out.println("food seen:"+ant.id);
        AntAction currentQuest = commandAnts.questMapping.get(ant.id);//if the ant is within 200 units of foos AND its current action is not to go get that, then build and action list to do so
      if(TRACKACTION)   System.out.println("food seen"+currentQuest);
        if(currentQuest==null||currentQuest.type==AntActionType.MOVE){
       if(TRACKACTION)    System.out.println("starting food quest");
          NodeData foodNode=Control.myMap.get(f.gridY).get(f.gridX);
          commandAnts.commandMap.put(ant.id,commandAnts.collectFood(ant, foodNode));//builds a list of actions to go get food and return with it
          commandAnts.questMapping.put(ant.id,new AntAction(AntActionType.PICKUP));
          break;//break in case an ant is within 5 units of multiple food sources, it'll just go to the first on on the list
      }
        //else keep doing the quest you were doing
      }
    }
}}
    
    
    
    
    
    
    
    
    
    
    
    
    
    if(commandAnts.questMapping.get(ant.id)!=null)//then it has been sent on a quest
    {
        //disabling for debugging
        if ((commandAnts.questMapping.get(ant.id).type == AntActionType.HEAL))//if that quest was to heal
         {
//       
        if (ant.health == 20)//test to see if healing complete
        {
//          System.out.println("ant" + ant.id + " has low health, starting return to base");
         commandAnts.questMapping.put(ant.id, null);
        }
       
       
//       if(commandAnts.commandMap.get(ant.id).isEmpty())commandAnts.commandMap.put(ant.id , commandAnts.nestToHeal(ant));
      // myActionQueue = commandAnts.commandMap.get(ant.id);
      //  AntAction nextActionFromList = myActionQueue.pop();//after list is built, get the first action on that list
       // return nextActionFromList;
      }
        //System.out.println("this ant is on a quest"+ant.gridX+";"+ant.gridY);
        else if ((commandAnts.questMapping.get(ant.id).type == AntActionType.PICKUP)&&!myActionQueue.isEmpty())//if that quest was to pick up food
      {
       if(TRACKACTION)  System.out.println(ant.id+":pickup");
        AntAction nextActionFromList = myActionQueue.pop();//after list is built, get the first action on that list
        
       //System.out.println("geting list action"+nextActionFromList.type+";"+nextActionFromList.direction);
        return nextActionFromList;
      }
      else if((commandAnts.questMapping.get(ant.id).type == AntActionType.MOVE)){
        //  System.out.println(ant.id+":exploring"+ant.myAction+";"+ant.ticksUntilNextAction);
         if(isObstructed(ant,data)){
             randomTrack(ant);
         }
       
         else{ return commandAnts.questMapping.get(ant.id);
         }
      
      }else if((commandAnts.questMapping.get(ant.id).type == AntActionType.DROP)){
        //  System.out.println(ant.id+":exploring"+ant.myAction+";"+ant.ticksUntilNextAction);
         
            if(commandAnts.commandMap==null||commandAnts.commandMap.get(ant.id)==null||commandAnts.commandMap.get(ant.id).isEmpty()||commandAnts.commandMap.isEmpty()){
            NodeData currentNode=Control.myMap.get(ant.gridY).get(ant.gridX);
            commandAnts.commandMap.put(ant.id,myPath.findPath(currentNode, Control.myMap.get(centerY).get(centerX)));
       if(TRACKACTION)      System.out.println(ant.gridY+";"+ant.gridX+commandAnts.commandMap.get(ant.id));
            
            }
            
            if(commandAnts.commandMap.get(ant.id).isEmpty()){
                
                commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.STASIS));
                return new AntAction(AntActionType.STASIS);}
        //System.out.println(commandAnts.commandMap.get(ant.id));
            action= commandAnts.commandMap.get(ant.id).pop();
         // System.out.println(action);
      return action;
      }
        
        
    }   
    if(Math.abs(centerX - ant.gridX) + Math.abs(centerY - ant.gridY) > 700&& (commandAnts.questMapping.get(ant.id).type==AntActionType.MOVE)){
        NodeData currentNode=Control.myMap.get(ant.gridY).get(ant.gridX);
            commandAnts.commandMap.put(ant.id,myPath.findPath(currentNode, Control.myMap.get(centerY).get(centerX)));
            commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.MOVE));
    }
    
    myActionQueue = commandAnts.commandMap.get(ant.id);//returns actionQueue
    //commandAnts.updateActionQueue(ant);
    //System.out.println("actions remaining for ant" +ant.id+" ="+myActionQueue.size()+"   counter:"+data.wallClockMilliSec);//debug
    if (myActionQueue==null||myActionQueue.isEmpty())
    {
        
      if(TRACKACTION)   System.out.println("running random track:"+ant.gridX+";"+ant.gridY);
      randomTrack(ant);
      return commandAnts.questMapping.get(ant.id);
    } else
    {
        action = myActionQueue.pop();
      if(TRACKACTION)   System.out.println(ant.id+":getting action"+action.type+" from queue   ");
      if(action.type==AntActionType.DROP) commandAnts.questMapping.put(ant.id,null);
     // myActionQueue.removeFirst();
      
      //System.out.println("actionlistItem:" + action.type + "  " + action.direction);
    }
 if(TRACKACTION)System.out.println(ant.id+":at the bottom with job:"+action.type+";"+action.direction);
 if(TRACKACTION)System.out.println("on quest:"+commandAnts.questMapping.get(ant.id));
    return action;
  }

  
  
   private void collectFood(FoodData[] food, CommData data)
    {
        for(FoodData fd : food){
       if(TRACKACTION)  System.out.println("getting Food");
      ArrayList<AntData> antListToCollectFood=new ArrayList<AntData>();
      NodeData closestfoodNode=Control.myMap.get(fd.gridY).get(fd.gridX);//consider making this an algorithm, this is closest to Bullet base
      ArrayList<AntData> mySortedAntList=data.myAntList;//
      DistanceCompare myDistComp=new DistanceCompare();//SET the compare node in this class!!!      
      int numberOfAntsToCollectfood=1;
      myDistComp.goalNode=closestfoodNode;//now it is set
      
      Collections.sort(mySortedAntList,myDistComp);//sortedAntList now sorted
      
 
      boolean getAnt;
      int j;
      for(int i=0;i<numberOfAntsToCollectfood;i++)
      { getAnt=true;
        j=0;
          while(getAnt&&(i+j)<mySortedAntList.size()){
              AntData ant=mySortedAntList.get(i+j);
        // System.out.println((commandAnts.questMapping==null)+";"+(commandAnts.questMapping.isEmpty())+";"+(commandAnts.questMapping.get(ant.id)==null)+";"+(commandAnts.questMapping.get(ant.id).type));
          if(ant.myAction.type!=AntActionType.BIRTH&&(commandAnts.questMapping==null||commandAnts.questMapping.isEmpty()||commandAnts.questMapping.get(ant.id)==null||(commandAnts.questMapping.get(ant.id).type)==AntActionType.MOVE))
          {
              antListToCollectFood.add(ant);
             if(TRACKACTION)  System.out.println("assign this ant");
              getAnt=false;
          }
          j++;
          }
          
      }
      if(antListToCollectFood.size()!=numberOfAntsToCollectfood)
      {
       // System.out.println("wrong number of ants to collect food:"+antListToCollectFood.size());
      }
      else{
    if(TRACKACTION)   System.out.println("getting food as assigned:"+fd.foodType);
      }
      //now we have 10 ants closest to water
      for (AntData ant : antListToCollectFood)
      {
          commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.PICKUP));
        commandAnts.commandMap.put(ant.id, commandAnts.collectFood(ant,closestfoodNode));//tells the ants to collect the water
      }
      
    }
    }
  
  
  
  
  
  
  
  
  
  private void collectWater(CommData data)
    {
        System.out.println("getting water");
      ArrayList<AntData> antListToCollectWater=new ArrayList<AntData>();
      NodeData closestWaterNode=Control.myMap.get(2382).get(2064);//consider making this an algorithm, this is closest to Bullet base
      ArrayList<AntData> mySortedAntList=data.myAntList;//
      DistanceCompare myDistComp=new DistanceCompare();//SET the compare node in this class!!!      
      int numberOfAntsToCollectWater=10;
      myDistComp.goalNode=closestWaterNode;//now it is set
      
      Collections.sort(mySortedAntList,myDistComp);//sortedAntList now sorted
      
      while(antListToCollectWater.size()<0)
      {
        antListToCollectWater.remove(0);//make sure ant list is empty before beginning
      }
      int j=0;
      for(int i=0;i<numberOfAntsToCollectWater;i++)
      {
          AntAction currentquest=commandAnts.questMapping.get(antListToCollectWater.get(i+j));
          if(currentquest==null||currentquest.type==AntActionType.MOVE)
        antListToCollectWater.add(mySortedAntList.get(i+j));
          else j++;
      }
      if(antListToCollectWater.size()!=numberOfAntsToCollectWater)
      {
     if(TRACKACTION)    System.out.println("wrong number of ants to collect water");
      }
      //now we have 10 ants closest to water
      for (AntData ant : antListToCollectWater)
      {
          commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.PICKUP));
        commandAnts.commandMap.put(ant.id, commandAnts.collectFood(ant,closestWaterNode));//tells the ants to collect the water
      }
    }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  //takes the id of an ant and returns a move action that assigns the direction
  //of motion based on the modulis of the id. This is intended to give a uniform
  //scatter of the ants at any given point.
  public AntAction scatter(int id)
  {
    AntAction action = new AntAction(AntActionType.MOVE);
    int modid = id % 8;

    switch (modid)
    {
      case 0:
        action.direction = Direction.NORTH;
        break;
      case 1:
        action.direction = Direction.SOUTH;
        break;
      case 3:
        action.direction = Direction.EAST;
        break;
      case 2:
        action.direction = Direction.WEST;
        break;
      case 7:
        action.direction = Direction.NORTHEAST;
        break;
      case 6:
        action.direction = Direction.NORTHWEST;
        break;
      case 5:
        action.direction = Direction.SOUTHEAST;
        break;
      case 4:
        action.direction = Direction.SOUTHWEST;
        break;

    }
    return action;

  }

  public void randomTrack(AntData ant)
  {
      
    AntAction action = new AntAction(AntActionType.MOVE);
   
      action.direction = Direction.getRandomDir();
     LinkedList<AntAction> explore=new LinkedList<>();
    for(int i=0;i<50;i++){explore.add(action);}
      commandAnts.commandMap.put(ant.id,explore);
    
      
    commandAnts.questMapping.put(ant.id,action);
  

  }

  public void drawAnts(CommData data) throws IOException
  {
    //AntWorld 
    //antworld = new AntWorld(data);

  }

  public static void main(String[] args) throws IOException
  {
    String serverHost = "b146-76";
    System.out.println(args.length);
    if (args.length > 0)
    {
      serverHost = args[0];
    }

    myClient = new ClientRandomWalk(serverHost, Constants.PORT);
          gameBoard.addKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent e) {
              
if(e.isControlDown()&&e.getKeyCode()==VK_X)      myClient.closeAll();
// throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void keyPressed(KeyEvent e) {
               // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void keyReleased(KeyEvent e) {
              //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        
        
        
        });
        
 
  }

  
  
  private boolean isObstructed(AntData ant,CommData data){
  
  for(int i=-1;i<2;i++){
      for(int j=-1;j<2;j++){
          if(Control.myMap.get(i+ant.gridY).get(j+ant.gridX).getElevation()==Integer.MAX_VALUE){
      return true;
      }
          ArrayList<AntData> antList=new ArrayList<>();
          antList.addAll(data.myAntList);
          antList.addAll(data.enemyAntSet);
          antList.remove(ant);
         for(AntData otherAnt: antList){
         if(ant.gridX+i==otherAnt.gridX&&ant.gridY+j==otherAnt.gridY){
         return true;
         }
         
         
         }
  
  }}
  return false;
  
  
  
  }
  
  
  
  
  
  public int getCenterX()
  {
    //centerX is not updating correctly, hardcode
//    return centerX;
    int x = 2064;
    return x;
  }

  public int getCenterY()
  {
    //centerX is not updating correctly, hardcode
//    return centerY;
    int y = 2125;
    return y;
  }

}
