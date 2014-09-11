package antworld.client;

import static antworld.client.AntWorld.readImage;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class ClientRandomWalk
{

    private static final boolean DEBUG = true;
    private static final TeamNameEnum myTeam = TeamNameEnum.Buffalograss;
    private static final long password = 122538603443L;//Each team has been assigned a random password.
    static ClientRandomWalk myClient;//package private?
    static AntWorld antworld;
    static ActionQueue commandAnts;
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

            NestNameEnum requestedNest = NestNameEnum.values()[random.nextInt(NestNameEnum.SIZE)];
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
        antworld = new AntWorld(data);
        commandAnts = new ActionQueue(data);
        
        antworld.setLocation(centerX, centerX);
        
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
            antworld.draw(data);
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
            ant.myAction = action;
        }
    }

    private AntAction chooseAction(CommData data, AntData ant)
    {
        AntAction action = new AntAction(AntActionType.STASIS);

        LinkedList<AntAction> myActionQueue;
        myActionQueue = commandAnts.commandMap.get(ant.id);
        if (ant.ticksUntilNextAction > 0)
        {
           // System.out.println("waiting on "+ant.ticksUntilNextAction+" ticks   "+ant.id);
            return action;
        }

        if (ant.underground)
        {
            System.out.println("Climbing out   "+ant.id);
            //initalizing the queque with explore sequence
            action = scatter(ant.id);
 
           for(int i=0;i<50;i++){ 
               myActionQueue.add(action);
           }
           
           
           
           AntAction exitAction= new AntAction(AntActionType.EXIT_NEST);
            exitAction.x = centerX - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
            exitAction.y = centerY - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
            return exitAction;
        }
        //with an empty queue the ant runs one direction until he hits a wall. then he will run another
       if(myActionQueue.isEmpty()||ant.myAction.type==AntActionType.STASIS){return randomTrack(ant);}
        //  NestData nestData=data.nestData[myNestName.ordinal()];
       HashSet<FoodData> food = data.foodSet;
       FoodData[] a=new FoodData[food.size()];
       food.toArray(a);
       for(FoodData f:a){
        //all ants within 200 pixels of the food will go pick it up.
             if(Math.abs(f.gridX-ant.gridX)+Math.abs(f.gridY-ant.gridY)<200)  return goToShitty(ant,a[0].gridX, a[0].gridY,AntActionType.PICKUP,Math.min(ant.carryUnits, a[0].getCount()));
        
       }
       ///this method needs to be changed. it is really shitty
       if(ant.carryType!=null)return goToShitty(ant,centerX, centerY,AntActionType.DROP,ant.carryUnits);
       

       //myActionQueue = commandAnts.commandMap.get(ant.id);//returns actionQueue
        //commandAnts.updateActionQueue(ant);
       
       //System.out.println("actions remaining for ant" +ant.id+" ="+myActionQueue.size()+"   counter:"+data.wallClockMilliSec);//debug
        if(!myActionQueue.isEmpty()){  
        action = myActionQueue.getFirst();
            myActionQueue.removeFirst();
             System.out.println(action.type+"  "+action.direction);
        }
    

        return action;
    }

    //quick and dirty go to. when we get there we exicute dowhat Action. this will die soon
    public AntAction goToShitty(AntData ant, int x, int y,AntActionType dowhat,int quant){
        AntAction action= new AntAction(AntActionType.MOVE);
    if((Math.abs(ant.gridX-x)+Math.abs(ant.gridY-y))==1){
        action.type=dowhat;
        if (dowhat==AntActionType.PICKUP||dowhat==AntActionType.DROP){action.quantity=quant;}
        System.out.println("quantity:"+quant+"action:"+dowhat+"ant ID:"+ant.id+"antx:"+ant.gridX+"anty:"+ant.gridY+"foodx:"+x+"foody:"+y);
    }
    
    
    if(x>ant.gridX)action.direction=Direction.EAST;
    else if(x<ant.gridX)action.direction=Direction.WEST;
    else{
        if(y>ant.gridY)action.direction=Direction.SOUTH;
        else if(y<ant.gridY)action.direction=Direction.NORTH;
        else{System.out.println("error in ant motion selection");}
    }
    
    System.out.println(action.direction);
    return action;
    }
    
    
    
    
    
    //takes the id of an ant and returns a move action that assigns the direction
    //of motion based on the modulis of the id. This is intended to give a uniform
    //scatter of the ants at any given point.
    public AntAction scatter(int id){
    AntAction action= new AntAction(AntActionType.MOVE);
        int modid=id%8;
  
        switch(modid){
                case 1: action.direction=Direction.NORTH;
                    break;
                case 0: action.direction=Direction.SOUTH;
                    break;
                case 3: action.direction=Direction.EAST;
                    break;
                case 2: action.direction=Direction.WEST;
                    break;
                case 7: action.direction=Direction.NORTHEAST;
                    break;
                case 6: action.direction=Direction.NORTHWEST;
                    break;
                case 5: action.direction=Direction.SOUTHEAST;
                    break;
                case 4: action.direction=Direction.SOUTHWEST;
                    break;
    
            
            }
    return action;
    
    
    }
    //sets new move action to the last action done by this ant
    //this is usefull for random scattering at a long distance.
    public AntAction randomTrack(AntData ant){
    AntAction action= new AntAction(AntActionType.MOVE);
    if(ant.myAction.type==AntActionType.STASIS){
            action.direction = Direction.getRandomDir();
            return action;}

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

}
