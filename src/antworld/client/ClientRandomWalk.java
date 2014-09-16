package antworld.client;

import static antworld.client.AntWorld.readImage;
import antworld.client.DistanceCompare;
import antworld.data.AntAction;
import antworld.data.AntAction.AntActionType;
import antworld.data.AntData;
import antworld.data.AntType;
import antworld.data.CommData;
import antworld.data.Constants;
import antworld.data.Direction;
import antworld.data.FoodData;
import antworld.data.NestData;
import antworld.data.NestNameEnum;
import antworld.data.TeamNameEnum;
import java.awt.Point;
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
  private static final boolean DRAW = false;
  private static final TeamNameEnum myTeam = TeamNameEnum.Buffalograss;
  private static final long password = 122538603443L;//Each team has been assigned a random password.
  static ClientRandomWalk myClient;//package private?
  static AntWorld antworld;
  static ActionQueue commandAnts;
  private ObjectInputStream inputStream = null;
  private ObjectOutputStream outputStream = null;
  private boolean isConnected = false;
  private boolean collectWater= true;
  private NestNameEnum myNestName = null;
  private int centerX, centerY;

  private Socket clientSocket;

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
            System.out.println("ClientRandomWalk: listening to socket....");
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
    Control myControl = new Control();//this populates the map in control, which is reffered to in goto and actionqueue

    commandAnts = new ActionQueue(data);

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
    {
      if (DRAW)
      {
        antworld.draw(data);
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
        System.out.println(data.gameTick);
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
    for (AntData ant : data.myAntList)
    {
      commandAnts.updateActionQueue(ant);
      AntAction action = chooseAction(data, ant);

      //  System.out.println(action.type+";"+action.direction);
      ant.myAction = action;
    }
    if(collectWater)//change this to if(base has less than 200 water)
    {
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
      for(int i=0;i<numberOfAntsToCollectWater;i++)
      {
        antListToCollectWater.add(mySortedAntList.get(i));
      }
      if(antListToCollectWater.size()!=numberOfAntsToCollectWater)
      {
        System.out.println("wrong number of ants to collect water");
      }
      //now we have 10 ants closest to water
      for (AntData ant : antListToCollectWater)
      {
        commandAnts.collectFood(ant,closestWaterNode);//tells the ants to collect the water
      }
      collectWater=false;
    }
  }

  private AntAction chooseAction(CommData data, AntData ant)
  {
    AntAction action = new AntAction(AntActionType.STASIS);

    //testing
    // if(ant.alive){action.type=AntActionType.MOVE;
    //action.direction=Direction.getRandomDir();
    //return action;}
    LinkedList<AntAction> myActionQueue;
    myActionQueue = commandAnts.commandMap.get(ant.id);
    
    if (ant.ticksUntilNextAction > 0)
    {
      //System.out.println("waiting on " + ant.ticksUntilNextAction + " ticks   " + ant.id);
      return action;
    }

    if((ant.health<10))//test to see if ant needs to return home to heal
    {
      System.out.println("ant"+ant.id+" has low health, returning to base");
      AntAction currentQuest=commandAnts.questMapping.get(ant.id);
      if(currentQuest.type!=AntActionType.HEAL)
      {
        commandAnts.nestToHeal(ant);//builds a list of actions to return and heal
      }
    }
    
    if(commandAnts.questMapping.get(ant.id)!=null)//then it has been sent on a quest
    {
      if ((commandAnts.questMapping.get(ant.id).type == AntActionType.HEAL))//if that quest was to heal
      {
        System.out.println("ant" + ant.id + " has low health, returning to base");
        if (ant.health == 20)//test to see if healing complete
        {
          commandAnts.questMapping.put(ant.id, null);
        }
        LinkedList<AntAction> actionList = commandAnts.commandMap.get(ant.id);
        AntAction nextActionFromList = actionList.getFirst();//after list is built, get the first action on that list
        actionList.removeFirst();
        return nextActionFromList;
      }
    }
      
    
    if (ant.underground)
    {
      if(ant.health<10)//do not emerge at less than half health
      {
        System.out.println("ant"+ant.id+" is underground with low health of "+ant.health);
        AntAction healAction = new AntAction(AntActionType.HEAL);
        return healAction;
      }
      System.out.println("Climbing out   " + ant.id);
      //initalizing the queque

      action = scatter(ant.id);

      for (int i = 0; i < 100; i++)
      {
        myActionQueue.add(action);
      }
      AntAction exitAction = new AntAction(AntActionType.EXIT_NEST);
      exitAction.x = centerX - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
      exitAction.y = centerY - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
      return exitAction;
    }
        //with an empty queue the ant runs one direction until he hits a wall. then he will run another

    //  NestData nestData=data.nestData[myNestName.ordinal()];
    HashSet<FoodData> food = data.foodSet;
    FoodData[] myFoodArray = new FoodData[food.size()];
    food.toArray(myFoodArray);
    //   System.out.println(a.length+";"+data.foodSet.isEmpty());

    for (FoodData f : myFoodArray)
    {
      //all ants within 200 pixels of the food will go pick it up.
      //   System.out.println(Math.abs(f.gridX-ant.gridX)+Math.abs(f.gridY-ant.gridY));
      if (Math.abs(f.gridX - ant.gridX) + Math.abs(f.gridY - ant.gridY) < 200)
      {
        AntAction currentQuest = commandAnts.questMapping.get(ant.id);//if the ant is within 200 units of foos AND its current action is not to go get that, then build and action list to do so
        
        if (currentQuest==null)
        {
          System.out.println("starting food quest");
          NodeData foodNode=Control.myMap.get(f.gridY).get(f.gridX);
          commandAnts.collectFood(ant, foodNode);//builds a list of actions to go get food and return with it
          break;//break in case an ant is within 200 units of multiple food sources, it'll just go to the first on on the list
        }
        //else keep doing the quest you were doing
      }
    }
    
    if(commandAnts.questMapping.get(ant.id)!=null)//then it has been sent on a quest
    {
      if ((commandAnts.questMapping.get(ant.id).type == AntActionType.PICKUP))//if that quest was to pick up food
      {
        //then follow the actions is the list to go get the food
        //System.out.println("ant"+ant.id+" following path to food");
        LinkedList<AntAction> actionList = commandAnts.commandMap.get(ant.id);
        //System.out.println("size of ants action list:"+actionList.size());
        AntAction nextActionFromList = actionList.pop();//after list is built, get the first action on that list
        return nextActionFromList;
      }
    }   
    
    
    //myActionQueue = commandAnts.commandMap.get(ant.id);//returns actionQueue
    //commandAnts.updateActionQueue(ant);
    //System.out.println("actions remaining for ant" +ant.id+" ="+myActionQueue.size()+"   counter:"+data.wallClockMilliSec);//debug
    if (myActionQueue.isEmpty() || ant.myAction.type == AntActionType.STASIS)
    {
      return randomTrack(ant);
    } else
    {
      action = myActionQueue.getFirst();
      myActionQueue.removeFirst();
      //System.out.println("actionlistItem:" + action.type + "  " + action.direction);
    }

    return action;
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
      case 1:
        action.direction = Direction.NORTH;
        break;
      case 0:
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

  public AntAction randomTrack(AntData ant)
  {
    AntAction action = new AntAction(AntActionType.MOVE);
    if (ant.myAction.type == AntActionType.STASIS)
    {
      action.direction = Direction.getRandomDir();
      return action;
    }

    return ant.myAction;

  }

  public void drawAnts(CommData data) throws IOException
  {
    //AntWorld 
    antworld = new AntWorld(data);

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
