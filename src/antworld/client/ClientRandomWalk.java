package antworld.client;

import static antworld.client.AntWorld.readImage;
import static antworld.client.AntWorld.yourMap;
import antworld.data.AntAction;
import antworld.data.AntAction.AntActionType;
import antworld.data.AntData;
import antworld.data.AntType;
import antworld.data.CommData;
import antworld.data.Constants;
import antworld.data.Direction;
import antworld.data.FoodData;
import antworld.data.LandType;
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
import java.util.Random;

public class ClientRandomWalk
{
  private static final boolean DEBUG = true;
  private static final TeamNameEnum myTeam = TeamNameEnum.Buffalograss;
  private static final long password = 122538603443L;//Each team has been assigned a random password.
  private ObjectInputStream inputStream = null;
  private ObjectOutputStream outputStream = null;
  private boolean isConnected = false;
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
    }
    catch (UnknownHostException e)
    {
      System.err.println("ClientRandomWalk Error: Unknown Host " + host);
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      System.err.println("ClientRandomWalk Error: Could not open connection to " + host + " on port " + portNumber);
      e.printStackTrace();
      return false;
    }

    try
    {
      outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
      inputStream = new ObjectInputStream(clientSocket.getInputStream());

    }
    catch (IOException e)
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
        if (outputStream != null) outputStream.close();
        if (inputStream != null) inputStream.close();
        clientSocket.close();
      }
      catch (IOException e)
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
      try { Thread.sleep(100); } catch (InterruptedException e1) {}
        
      NestNameEnum requestedNest = NestNameEnum.values()[random.nextInt(NestNameEnum.SIZE)];
      CommData data = new CommData(requestedNest, myTeam);
      data.password = password;
      
      if( sendCommData(data) )
      {
        try
        {
          if (DEBUG) System.out.println("ClientRandomWalk: listening to socket....");
          CommData recvData = (CommData) inputStream.readObject();
          if (DEBUG) System.out.println("ClientRandomWalk: recived <<<<<<<<<"+inputStream.available()+"<...\n" + recvData);
          
          if (recvData.errorMsg != null)
          {
            System.err.println("ClientRandomWalk***ERROR***: " + recvData.errorMsg);
            continue;
          }
  
          if ((myNestName == null) && (recvData.myTeam == myTeam))
          { myNestName = recvData.myNest;
            centerX = recvData.nestData[myNestName.ordinal()].centerX;
            centerY = recvData.nestData[myNestName.ordinal()].centerY;
            System.out.println("ClientRandomWalk: !!!!!Nest Request Accepted!!!! " + myNestName);
            return recvData;
          }
        }
        catch (IOException e)
        {
          System.err.println("ClientRandomWalk***ERROR***: client read failed");
          e.printStackTrace();
        }
        catch (ClassNotFoundException e)
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
      AntWorld antworld=new AntWorld(data);
     // drawAnts(data);
    while (true)
    {
        antworld.draw(data);
      try
      {
        if (DEBUG) System.out.println("ClientRandomWalk: chooseActions: " + myNestName);
System.out.println("the loop is working");
        chooseActionsOfAllAnts(data,antworld);
        
 
        
        
        
        
        
        
        CommData sendData = data.packageForSendToServer();
        
        //System.out.println("ClientRandomWalk: Sending>>>>>>>: " + sendData);
        outputStream.writeObject(sendData);
        outputStream.flush();
        outputStream.reset();
       

        if (DEBUG) System.out.println("ClientRandomWalk: listening to socket....");
        CommData recivedData = (CommData) inputStream.readObject();
        if (DEBUG) System.out.println("ClientRandomWalk: received <<<<<<<<<"+inputStream.available()+"<...\n" + recivedData);
        data = recivedData;
  
        
        if ((myNestName == null) || (data.myTeam != myTeam))
        {
          System.err.println("ClientRandomWalk: !!!!ERROR!!!! " + myNestName);
        }
      }
      catch (IOException e)
      {
        System.err.println("ClientRandomWalk***ERROR***: client read failed");
        e.printStackTrace();
        try { Thread.sleep(1000); } catch (InterruptedException e1) {}

      }
      catch (ClassNotFoundException e)
      {
        System.err.println("ServerToClientConnection***ERROR***: client sent incorect data format");
        e.printStackTrace();
        try { Thread.sleep(1000); } catch (InterruptedException e1) {}
      }

    }
  }
  
  
  private boolean sendCommData(CommData data)
  {
    
    CommData sendData = data.packageForSendToServer();
    try
    {
      if (DEBUG) System.out.println("ClientRandomWalk.sendCommData(" + sendData +")");
      
      outputStream.writeObject(sendData);
      outputStream.flush();
      outputStream.reset();
    }
    catch (IOException e)
    {
      System.err.println("ClientRandomWalk***ERROR***: client read failed");
      e.printStackTrace();
      try { Thread.sleep(1000); } catch (InterruptedException e1) {}
      return false;
    }

    return true;
    
  }

  private void chooseActionsOfAllAnts(CommData commData,AntWorld antworld)
  {
    for (AntData ant : commData.myAntList)
    {
      AntAction action = chooseAction(commData, ant,antworld);
      ant.myAction = action;
    }
  }

  private AntAction chooseAction(CommData data, AntData ant,AntWorld world)
  {
    AntAction action = new AntAction(AntActionType.STASIS);
    
    if (ant.ticksUntilNextAction > 0) return action;

    if (ant.underground)
    {
      action.type = AntActionType.EXIT_NEST;
      action.x = centerX - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
      action.y = centerY - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
      return action;
    }

    action.type = AntActionType.MOVE;
   // System.out.println(data.nestData.length);
  //  NestData nestData=data.nestData[myNestName.ordinal()];
    
    Point home=new Point(centerX , centerY);
    
    int dx=ant.gridX-home.x;
    int dy=ant.gridY-home.y;
    
    if(dx<0){
        if(dy<0){
            action.direction = Direction.SOUTHWEST;
        }
             action.direction = Direction.NORTHWEST;
    }
    else{
    if(dy<0){
            action.direction = Direction.SOUTHEAST;
        }
             action.direction = Direction.NORTHEAST;
             
    
    }
    
    
    action.direction = Direction.getRandomDir();
    
  FoodData food= (FoodData) data.foodSet.toArray()[0];
  int deltax=ant.gridX-food.gridX;
        int deltay=ant.gridY-food.gridY;
       System.out.println("FoodCheck:"+Math.abs(food.gridX-ant.gridX)+";"+Math.abs(food.gridY-ant.gridY));
    if(Math.abs(deltax)<=1&&Math.abs(deltay)<=1){
        
        System.out.println("FOOOOOOOOD"+ant.id);
        for(Direction dir : Direction.values()){
            if ( dir.deltaX()==deltax&&dir.deltaY()==deltay){
                System.out.println("Pickup called");
            action.type = AntActionType.PICKUP;
                action.direction=dir;
            System.out.println("Pickup called"+"-->"+dir.name()+":"+deltax+";"+deltay);
            }
        
        }
        
        
    }
    else{
        if(deltax>0)action.direction=Direction.EAST;
        else if(deltax<0)action.direction=Direction.WEST;
        else{
            if(deltay<0)action.direction=Direction.NORTH;
             if(deltay>0)action.direction=Direction.SOUTH;
        }
    
       
    
}
    
    if(ant.carryType!=null){
        int deltahomex=ant.gridX-centerX;
        int deltahomey=ant.gridY-centerY;
         if(deltahomex>0)action.direction=Direction.EAST;
        else if(deltahomex<0)action.direction=Direction.WEST;
        else{
            if(deltahomey<0)action.direction=Direction.NORTH;
             if(deltahomey>0)action.direction=Direction.SOUTH;
        }
         if(world.yourMap.get(ant.gridX).get(ant.gridY).terrain==LandType.NEST){
         action.type=AntActionType.DROP;
         }
    
    
    }
    
    
    
    return action;
  }
  
  
  public void drawAnts(CommData data) throws IOException{
  AntWorld antworld=new AntWorld(data);
  
  
  }

  public static void main(String[] args) throws IOException
  {
    String serverHost = "b146-76";
    System.out.println(args.length);
   if (args.length > 0) serverHost = args[0];
    
  new ClientRandomWalk(serverHost, Constants.PORT);
 }

}
