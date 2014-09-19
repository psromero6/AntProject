package antworld.client;

import static antworld.client.GraphicContainer.gameBoard;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class ClientControl {

    private static final boolean DEBUG = false;
    private static final boolean TRACKACTION = false;//print individual ant actions
    private static final boolean SCOREING = true;//print score information
    private static final boolean DRAW = true;//draw a gameboard
    private static final boolean DRAWTASK = true;//outline individual ant tasks
    private static final boolean BUILD = false;//build more ants
    private static final TeamNameEnum myTeam = TeamNameEnum.Buffalograss;
    private static final long password = 122538603443L;//Each team has been assigned a random password.
    static ClientControl myClient;
    static GraphicContainer antworld;
    static ActionQueue commandAnts;//ant action tracker
    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream = null;
    private boolean isConnected = false;
    private boolean goHome = true;//sends all ants home
    private NestNameEnum myNestName = null;
    private int centerX, centerY;
    private int[] solidFood;//food to pickup-worthpoints
    private Socket clientSocket;
    private ArrayList<FoodData> oldFood;//Food that we knew about
    private ArrayList<AntData> antsToKill;//Ants that are a threat
    private static Random random = Constants.random;
    //colors for various actions
    private int[] boomRGB = {255, 0, 0};
    private int[] stalkRGB = {0, 0, 255};
    private int[] carryRGB = {0, 125, 125};
    private ArrayList<NodeData> sweetSpotInt;//points of interest, places that we found food.

    public ClientControl(String host, int portNumber) throws IOException {
        System.out.println("Starting ClientRandomWalk: " + System.currentTimeMillis());
        isConnected = false;
        while (!isConnected) {
            isConnected = openConnection(host, portNumber);
        }
        CommData data = chooseNest();
        mainGameLoop(data);
        closeAll();
    }

    private boolean openConnection(String host, int portNumber) {

        try {
            clientSocket = new Socket(host, portNumber);
        } catch (UnknownHostException e) {
            System.err.println("ClientRandomWalk Error: Unknown Host " + host);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.err.println("ClientRandomWalk Error: Could not open connection to " + host + " on port " + portNumber);
            e.printStackTrace();
            return false;
        }

        try {
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());

        } catch (IOException e) {
            System.err.println("ClientRandomWalk Error: Could not open i/o streams");
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public void closeAll() {
        System.out.println("ClientRandomWalk.closeAll()");
        {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("ClientRandomWalk Error: Could not close");
                e.printStackTrace();
            }
        }
    }

    public CommData chooseNest() {
        while (myNestName == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
            }

            NestNameEnum requestedNest = NestNameEnum.BULLET;//.values()[random.nextInt(NestNameEnum.SIZE)];
            CommData data = new CommData(requestedNest, myTeam);
            data.password = password;

            if (sendCommData(data)) {
                try {

                    if (DEBUG) {
                        System.out.println("ClientRandomWalk: listening to socket...." + inputStream.toString());
                    }
                    CommData recvData = (CommData) inputStream.readObject();

                    if (DEBUG) {
                        System.out.println("ClientRandomWalk: recived <<<<<<<<<" + inputStream.available() + "<...\n" + recvData);
                    }

                    if (recvData.errorMsg != null) {
                        System.err.println("ClientRandomWalk***ERROR***: " + recvData.errorMsg);
                        continue;
                    }

                    if ((myNestName == null) && (recvData.myTeam == myTeam)) {
                        myNestName = recvData.myNest;
                        centerX = recvData.nestData[myNestName.ordinal()].centerX;
                        centerY = recvData.nestData[myNestName.ordinal()].centerY;
                        System.out.println("ClientRandomWalk: !!!!!Nest Request Accepted!!!! " + myNestName);
                        return recvData;
                    }
                } catch (IOException e) {
                    System.err.println("ClientRandomWalk***ERROR***: client read failed");
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    System.err.println("ClientRandomWalk***ERROR***: client sent incorect data format");
                }
            }
        }
        return null;
    }

    public void mainGameLoop(CommData data) throws IOException {

        if (DRAW) {
            //makes drawing object
            antworld = new GraphicContainer(data);
        }

        MapControl myControl = new MapControl();
        oldFood = new ArrayList<>();

        commandAnts = new ActionQueue(data);

        sweetSpotInt = new ArrayList<>();
//hardCoded rich deposit of food for Bullet Nest
        NodeData knownsweet = myControl.myMap.get(2077).get(2457);
        sweetSpotInt.add(knownsweet);

        antsToKill = new ArrayList<>();

        while (true) {
            if (DRAW) {
                antworld.draw(data);
            }
            //reset antworld every 150 game ticks
            if (DRAW && (data.gameTick % 150 == 0)) {
                antworld.resetPic();
                System.out.println("draw");
          //antworld.setLocation(-1500, -1755);

            }
            try {

                if (DEBUG) {
                    System.out.println("ClientRandomWalk: chooseActions: " + myNestName);
                }

                chooseActionsOfAllAnts(data);

                CommData sendData = data.packageForSendToServer();

                outputStream.writeObject(sendData);
                outputStream.flush();
                outputStream.reset();

                if (DEBUG) {
                    System.out.println("ClientRandomWalk: listening to socket....");
                }

                //puts all known food data into ah array
                HashSet<FoodData> food = data.foodSet;
                FoodData[] myFoodArray = new FoodData[food.size()];
                food.toArray(myFoodArray);

                //prints out a continuous count of food and game tick and total number of ants
                if (SCOREING) {
                    System.out.println(data.gameTick);
                    for (int fd : data.foodStockPile) {
                        System.out.print(fd + ";");
                    }
                    System.out.println("total number of ants" + data.myAntList.size() + " xxxxxxxx");
                }

                //looks for new food added
                for (FoodData fd : myFoodArray) {

                    if ((oldFood.isEmpty() || oldFood.contains(fd) || (data.gameTick % 150 == 0)) && fd.foodType != FoodType.WATER) {
                        NodeData foodSpot = MapControl.myMap.get(fd.gridY).get(fd.gridX);
                        //  if(!sweetSpotInt.contains(foodSpot)){sweetSpotInt.add(foodSpot);}
                        collectFood(myFoodArray, data);
                    }

                }
                oldFood.clear();
                oldFood.addAll(food);

                //adds new enemy ants to antsToKill 
                for (AntData ant : data.enemyAntSet) {
                    // System.out.println(fd.foodType+";"+fd.gridX+";"+fd.gridY);
                    if ((antsToKill.isEmpty() || antsToKill.contains(ant) || (data.gameTick % 150 == 0))) {
                        antsToKill.add(ant);
                    }
                }

                CommData recivedData = (CommData) inputStream.readObject();
                if (DEBUG) {
                    System.out.println("ClientRandomWalk: received <<<<<<<<<" + inputStream.available() + "<...\n" + recivedData);
                }
                data = recivedData;

                if ((myNestName == null) || (data.myTeam != myTeam)) {
                    System.err.println("ClientRandomWalk: !!!!ERROR!!!! " + myNestName);
                }
            } catch (IOException e) {
                System.err.println("ClientRandomWalk***ERROR***: client read failed");
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }

            } catch (ClassNotFoundException e) {
                System.err.println("ServerToClientConnection***ERROR***: client sent incorect data format");
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
            }

        }
    }

    private boolean sendCommData(CommData data) {

        CommData sendData = data.packageForSendToServer();
        try {
            if (DEBUG) {
                System.out.println("ClientRandomWalk.sendCommData(" + sendData + ")");
            }

            outputStream.writeObject(sendData);
            outputStream.flush();
            outputStream.reset();
        } catch (IOException e) {
            System.err.println("ClientRandomWalk***ERROR***: client read failed");
            e.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
            }
            return false;
        }

        return true;

    }

    //applies myAction to all ants in myAntlist
    private void chooseActionsOfAllAnts(CommData data) {

        BLine myPath = new BLine();
        NodeData homeNode = MapControl.myMap.get(centerY).get(centerX);

        HashSet<FoodData> food = data.foodSet;
        FoodData[] myFoodArray = new FoodData[food.size()];
        food.toArray(myFoodArray);

        for (AntData ant : data.myAntList) {
            int nestDistance = Math.abs(ant.gridX - centerX) + Math.abs(ant.gridY - centerY);

            //primary action is to drop food on the nest.
            if (ant.carryUnits > 0 && nestDistance < 19) {
                if (SCOREING) {
                    System.out.println("Droped Food in Nest");
                }
                AntAction dropaction = new AntAction(AntActionType.DROP);
                dropaction.direction = Direction.getRandomDir();
                dropaction.quantity = ant.carryUnits;
                ant.myAction = dropaction;
            } //heals low health ants if they appoach the nest
            else if (ant.health < 20 && nestDistance < 10 && !ant.underground) {
                if (SCOREING) {
                    System.out.println("GetDown");
                }
                LinkedList<AntAction> getdown=new LinkedList<>();
                 AntAction duckaction = new AntAction(AntActionType.ENTER_NEST);
                ant.myAction = duckaction;
                
                //continue;
                getdown.add(duckaction);
                commandAnts.commandMap.put(ant.id,getdown);

                commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.ENTER_NEST));
               
                
            } //buil in command to have all ants hide
            else if (goHome) {
                NodeData currentNode = MapControl.myMap.get(ant.gridY).get(ant.gridX);
                commandAnts.commandMap.put(ant.id, myPath.findPath(currentNode, homeNode));

                commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.ENTER_NEST));
            } ///set this action for really aggressive suicidal ants
            else if (getObstructedDir(ant, antsToKill) != null && ant.carryUnits == 0) {

                AntAction driveBy = new AntAction(AntActionType.ATTACK);
                driveBy.direction = getObstructedDir(ant, antsToKill);

        //ant.myAction=driveBy;
            } else {
                //sends ants on a quest home
                if (ant.carryUnits > 0 && (commandAnts.questMapping.get(ant.id) == null || (commandAnts.questMapping.get(ant.id).type != AntActionType.DROP))) {
                    if (TRACKACTION) {
                        System.out.println("I have grub.........................................");
                    }
                    //home location varries to prevent ant pile-ups
                    float homeishy = (getCenterY() + 10 * (random.nextFloat() - 0.5f));
                    float homeishx = (getCenterX() + 10 * (random.nextFloat() - 0.5f));
                    NodeData currentNode = MapControl.myMap.get(ant.gridY).get(ant.gridX);

                    commandAnts.commandMap.put(ant.id, myPath.findPath(currentNode, MapControl.myMap.get((int) homeishy).get((int) homeishx)));

                    commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.DROP));
                } else {
                    // returns quest completion info;
                    AntAction action = chooseAction(data, ant);
                    if (action.type == AntActionType.DROP) {
                        if (TRACKACTION) {
                            System.out.println("Ant" + ant.id + " dropping off");
                        }
                    }
                    if (action.type == AntActionType.PICKUP) {
                        if (TRACKACTION) {
                            System.out.println("Ant" + ant.id + " picking up");
                        }
                    }

                    //  System.out.println(action.type+";"+action.direction);
                    ant.myAction = action;
                }
            }
            //only one goHome call is needed
            goHome = false;
            //checks water stockpile every 1000 game ticks
            if (data.foodStockPile[1] < 200 && data.gameTick % 1000 == 0) {
                collectWater(data);
            }

        }

        //builds new ants of the kind that we want, saves the rest for points
        if (BUILD && data.myAntList.size() < 250) {
            if (data.foodStockPile[3] > 100) {
                AntData attackAnt = new AntData(Constants.UNKNOWN_ANT_ID, AntType.ATTACK, data.myNest, data.myTeam);
                attackAnt.myAction = new AntAction(AntActionType.BIRTH);
                data.myAntList.add(attackAnt);

            }
            if (data.foodStockPile[4] > 100) {
                AntData speedAnt = new AntData(Constants.UNKNOWN_ANT_ID, AntType.SPEED, data.myNest, data.myTeam);
                speedAnt.myAction = new AntAction(AntActionType.BIRTH);
                data.myAntList.add(speedAnt);

            }

        }

        //if an enemy ant attacks, we begin a blind flurry assault
        for (AntData dickAnt : data.enemyAntSet) {
            //System.out.println(dickAnt.myAction.type);
            if (dickAnt.myAction.type == AntActionType.ATTACK) {
                System.out.println("enemy ant attack!!!!!!!!!!!!!!!!");
                attackIntruders(data, antsToKill);
            }
        }

    }
//chose action for individual ant, this is for ants that are already questing

    private AntAction chooseAction(CommData data, AntData ant) {

        AntAction action = new AntAction(AntActionType.STASIS);
        BLine myPath = new BLine();

        LinkedList<AntAction> myActionQueue;
        myActionQueue = commandAnts.commandMap.get(ant.id);

        //finish the move started.
        if (ant.ticksUntilNextAction > 0) {
            return ant.myAction;
        }

        //indicates ants carrying a load
        if (ant.carryUnits > 0) {
            if (TRACKACTION) {
                System.out.println("has food.." + ant.gridX + ";" + ant.gridY + "...................");
            }
            if (DRAWTASK) {
                antworld.drawMapCircle(ant.gridX, ant.gridY, carryRGB);
            }
        }

        //if a quest for dropping of food is not set, and the ant has food, a quest is set
        if (ant.carryUnits > 0 && commandAnts.questMapping.get(ant.id) == null) {

            commandAnts.commandMap.get(ant.id).clear();

            if (TRACKACTION) {
                System.out.println("dropoff food");
            }
            NodeData currentNode = MapControl.myMap.get(ant.gridY).get(ant.gridX);
            commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.DROP));
            commandAnts.commandMap.put(ant.id, myPath.findPath(currentNode, MapControl.myMap.get(centerY).get(centerX)));

        }

        if (ant.underground) {
            if (ant.health < 10)//do not emerge at less than half health
            {
                if (TRACKACTION) {
                    System.out.println("ant" + ant.id + " is underground with low health of " + ant.health);
                }
                AntAction healAction = new AntAction(AntActionType.HEAL);
                return healAction;
            }
            if (TRACKACTION) {
                System.out.println("Climbing out   " + ant.id);
            }
//exit nest
            AntAction exitAction = new AntAction(AntActionType.EXIT_NEST);
            exitAction.x = centerX - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
            exitAction.y = centerY - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
            return exitAction;
        }

        if ((Math.abs(centerY-ant.gridY)+Math.abs(centerY-ant.gridX))>19&&(ant.health < 10) && (commandAnts.questMapping.get(ant.id) == null || commandAnts.questMapping.get(ant.id).type != AntActionType.HEAL))//test to see if ant needs to return home to heal
        {
            if (TRACKACTION) {
                System.out.println("ant" + ant.id + " has low health, returning to base");
            }
            AntAction currentQuest = commandAnts.questMapping.get(ant.id);

            if (TRACKACTION) {
                System.out.println("quest set to heal");
            }
            NodeData currentNode = MapControl.myMap.get(ant.gridY).get(ant.gridX);
            NodeData homeNode = MapControl.myMap.get(centerY).get(centerX);
            commandAnts.commandMap.put(ant.id, myPath.findPath(currentNode, homeNode));
            commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.HEAL));//builds a list of actions to return and heal

        }

//build food array
        HashSet<FoodData> food = data.foodSet;
        FoodData[] myFoodArray = new FoodData[food.size()];
        food.toArray(myFoodArray);

   //finds ants to pick up food
        if (commandAnts.commandMap.get(ant.id) == null
                || commandAnts.commandMap.get(ant.id).isEmpty()
                || commandAnts.questMapping.get(ant.id) == null
                || commandAnts.questMapping.get(ant.id).type == AntActionType.MOVE
                || commandAnts.questMapping.get(ant.id).type == AntActionType.ENTER_NEST) {
            for (FoodData f : myFoodArray) {
      //all ants within 5 pixels of the food will go pick it up.
                //   System.out.println(Math.abs(f.gridX-ant.gridX)+Math.abs(f.gridY-ant.gridY));
                if (Math.abs(f.gridX - ant.gridX) + Math.abs(f.gridY - ant.gridY) < 10 && f.foodType != FoodType.WATER && ant.carryUnits < 1) {
                    if (TRACKACTION) {
                        System.out.println("food seen:" + ant.id);
                    }
                    AntAction currentQuest = commandAnts.questMapping.get(ant.id);//if the ant is within 10 units of foos AND its current action is not to go get that, then build and action list to do so
                    if (TRACKACTION) {
                        System.out.println("food seen" + currentQuest);
                    }
                    if (currentQuest == null || currentQuest.type == AntActionType.MOVE) {
                        if (TRACKACTION) {
                            System.out.println("starting food quest");
                        }
                        NodeData foodNode = MapControl.myMap.get(f.gridY).get(f.gridX);
                        commandAnts.commandMap.put(ant.id, commandAnts.collectFood(ant, foodNode));//builds a list of actions to go get food and return with it
                        commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.PICKUP));
                        break;//break in case an ant is within 5 units of multiple food sources, it'll just go to the first on on the list
                    }
                    //else keep doing the quest you were doing
                }
            }
        }

        if (commandAnts.questMapping.get(ant.id) != null)//then it has been sent on a quest
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
            } else if ((commandAnts.questMapping.get(ant.id).type == AntActionType.PICKUP) && !myActionQueue.isEmpty())//if that quest was to pick up food
            {
                if (TRACKACTION) {
                    System.out.println(ant.id + ":pickup");
                }
                AntAction nextActionFromList = myActionQueue.pop();//after list is built, get the first action on that list

                return nextActionFromList;
            } else if ((commandAnts.questMapping.get(ant.id).type == AntActionType.MOVE))//moving/exploring
            {

                if (isObstructed(ant, data)) {
                    randomTrack(ant);
                }
                NodeData currentNode = MapControl.myMap.get(ant.gridY).get(ant.gridX);

                //run away to home if there are baddies nearby
                boolean safeZone = true;
                for (AntData dickAnt : data.enemyAntSet) {

                    if ((Math.sqrt((dickAnt.gridX - ant.gridX) * (dickAnt.gridX - ant.gridX) + (dickAnt.gridY - ant.gridY) * (dickAnt.gridY - ant.gridY)) < 3)) {
                        safeZone = false;
                    }
                }
           //an Elevationof100 is set for ants living on top of a hill. that 
                //we meant to keep for ourselves.
                if (currentNode.getElevation() < 100 || !safeZone) {

                    commandAnts.commandMap.put(ant.id, myPath.findPath(currentNode, MapControl.myMap.get(centerY).get(centerX)));
                    commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.EXIT_NEST));
                }

          //this is the move that is ment to be returned
                //it is assigned in random track
                return commandAnts.questMapping.get(ant.id);

            } else if ((commandAnts.questMapping.get(ant.id).type == AntActionType.DROP)) {//if they are on a drop off quest, go home.

                if (commandAnts.commandMap == null || commandAnts.commandMap.get(ant.id) == null || commandAnts.commandMap.get(ant.id).isEmpty() || commandAnts.commandMap.isEmpty()) {
                    NodeData currentNode = MapControl.myMap.get(ant.gridY).get(ant.gridX);
                    commandAnts.commandMap.put(ant.id, myPath.findPath(currentNode, MapControl.myMap.get(centerY).get(centerX)));
                    if (TRACKACTION) {
                        System.out.println(ant.gridY + ";" + ant.gridX + commandAnts.commandMap.get(ant.id));
                    }

                }

                if (commandAnts.commandMap.get(ant.id).isEmpty()) {

                    commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.STASIS));
                    return new AntAction(AntActionType.STASIS);
                }

                action = commandAnts.commandMap.get(ant.id).pop();

                return action;
            } else if ((commandAnts.questMapping.get(ant.id).type == AntActionType.ATTACK)) {//if they are attacking find the nearest bad ant and attack
//draw attacks
                if (DRAWTASK) {
                    antworld.drawMapCircle(ant.gridX, ant.gridY, stalkRGB);
                }
                if (getObstructedDir(ant, antsToKill) != null) {
                    action.type = AntActionType.ATTACK;
                    action.direction = getObstructedDir(ant, antsToKill);
                    System.out.println("Attack attempted");

                    if (DRAWTASK) {
                        antworld.drawMapCircle(ant.gridX, ant.gridY, boomRGB);
                    }
                    return action;

                }
                if (commandAnts.commandMap.get(ant.id).isEmpty()) {
                    attackIntruders(ant, antsToKill);
                }
                return commandAnts.commandMap.get(ant.id).pop();
            } else if ((commandAnts.questMapping.get(ant.id).type == AntActionType.ATTACK)) {
                return commandAnts.commandMap.get(ant.id).pop();

            }

        }

        myActionQueue = commandAnts.commandMap.get(ant.id);

        if (myActionQueue == null || myActionQueue.isEmpty()) {

            if (TRACKACTION) {
                System.out.println("running random track:" + ant.gridX + ";" + ant.gridY);
            }
            randomTrack(ant);

            return commandAnts.questMapping.get(ant.id);
        } else {
            action = myActionQueue.pop();
            if (TRACKACTION) {
                System.out.println(ant.id + ":getting action" + action.type + " from queue   ");
            }
            if (action.type == AntActionType.DROP) {
                commandAnts.questMapping.put(ant.id, null);
            }
 
        }
        if (TRACKACTION) {
            System.out.println(ant.id + ":at the bottom with job:" + action.type + ";" + action.direction);
        }
        if (TRACKACTION) {
            System.out.println("on quest:" + commandAnts.questMapping.get(ant.id));
        }
        
        //if stasus is returned in the event of getting to the end
        return action;
    }
    
    
    
//send all ants to collect food
    private void collectFood(FoodData[] food, CommData data) {
        for (FoodData fd : food) {
            if (fd.getCount() < 6 && fd.foodType == FoodType.BASIC) {
                continue;
            }
            if (TRACKACTION) {
                System.out.println("getting Food");
            }
            if ((Math.abs(fd.gridX - getCenterX()) + Math.abs(fd.gridY - getCenterY())) < 200) {
                ArrayList<AntData> antListToCollectFood = new ArrayList<AntData>();
                NodeData closestfoodNode = MapControl.myMap.get(fd.gridY).get(fd.gridX);//consider making this an algorithm, this is closest to Bullet base
                ArrayList<AntData> mySortedAntList = data.myAntList;//
                DistanceCompare myDistComp = new DistanceCompare();//SET the compare node in this class!!!      
                int numberOfAntsToCollectfood = 1;
                myDistComp.goalNode = closestfoodNode;//now it is set

                Collections.sort(mySortedAntList, myDistComp);//sortedAntList now sorted

                boolean getAnt;
                int j;
                for (int i = 0; i < numberOfAntsToCollectfood; i++) {
                    getAnt = true;
                    j = 0;
                    while (getAnt && (i + j) < mySortedAntList.size()) {
                        AntData ant = mySortedAntList.get(i + j);
                        if (ant.myAction.type != AntActionType.BIRTH && (commandAnts.questMapping == null || commandAnts.questMapping.isEmpty() || commandAnts.questMapping.get(ant.id) == null || (commandAnts.questMapping.get(ant.id).type) == AntActionType.MOVE || commandAnts.questMapping.get(ant.id).type == AntActionType.ENTER_NEST)) {
                            antListToCollectFood.add(ant);
                            if (TRACKACTION) {
                                System.out.println("assign this ant");
                            }
                            getAnt = false;
                        }
                        j++;
                    }

                }
                if (antListToCollectFood.size() != numberOfAntsToCollectfood) {
                } else {
                    if (TRACKACTION) {
                        System.out.println("getting food as assigned:" + fd.foodType);
                    }
                }
                //now we have 10 ants closest to water
                for (AntData ant : antListToCollectFood) {
                    commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.PICKUP));
                    commandAnts.commandMap.put(ant.id, commandAnts.collectFood(ant, closestfoodNode));//tells the ants to collect the water
                }
            }
        }
    }
//send all attack ants to go attack
    private void attackIntruders(CommData data, ArrayList<AntData> targets) {
        System.out.println("attackqueue");
        BLine bline = new BLine();

        for (AntData ant : data.myAntList) {

            NodeData currentNode = MapControl.myMap.get(ant.gridY).get(ant.gridX);

            DistanceCompare myDistComp = new DistanceCompare();//SET the compare node in this class!!!      

            myDistComp.goalNode = currentNode;//now it is set

            Collections.sort(targets, myDistComp);//sortedAntList now sorted

            if (!targets.isEmpty()) {
                AntData target = targets.get(0);
                targets.remove(0);

                if (ant.antType == AntType.ATTACK && ((Math.abs(ant.gridX - getCenterX()) + Math.abs(ant.gridY - getCenterY())) < 700)) {
                    commandAnts.commandMap.put(ant.id, bline.findPath(currentNode, MapControl.myMap.get(target.gridY).get(target.gridX)));
                    commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.ATTACK));
                }
            }
        }

    }

    ///attack instructions for one ant.
    private void attackIntruders(AntData ant, ArrayList<AntData> targets) {
        BLine bline = new BLine();

        NodeData currentNode = MapControl.myMap.get(ant.gridY).get(ant.gridX);

        DistanceCompare myDistComp = new DistanceCompare();//SET the compare node in this class!!!      

        myDistComp.goalNode = currentNode;//now it is set

        Collections.sort(targets, myDistComp);//sortedAntList now sorted

        if (!targets.isEmpty()) {
            AntData target = targets.get(0);
            targets.remove(0);

            if (ant.antType == AntType.ATTACK) {
                commandAnts.commandMap.put(ant.id, bline.findPath(currentNode, MapControl.myMap.get(target.gridY).get(target.gridX)));
                commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.ATTACK));

            }
        }
    }
    
//sends a number of ants to collect water from a hardcoded point
    private void collectWater(CommData data) {
        if (TRACKACTION) {
            System.out.println("getting Water");
        }
        ArrayList<AntData> antListToCollectFood = new ArrayList<AntData>();
        NodeData closestfoodNode = MapControl.myMap.get(2382).get(2064);//consider making this an algorithm, this is closest to Bullet base

        int numberOfAntsToCollectfood = Math.min(2, (int) data.myAntList.size() / 10);
        int i = 0;
        for (AntData ant : data.myAntList)//for each up until numberOfAntsToCOllectFood
        {
            antListToCollectFood.add(ant);
            i++;
            if (i == numberOfAntsToCollectfood) {
                break;
            }
        }

        for (AntData ant : antListToCollectFood) {
            commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.PICKUP));
            commandAnts.commandMap.put(ant.id, commandAnts.collectFood(ant, closestfoodNode));//tells the ants to collect the water
        }

    }
//selects a random action for the ants to move in, sets a path that is 50
    //actions of move in that direction
    //
    public void randomTrack(AntData ant) {
        BLine bline = new BLine();
        if (random.nextDouble() < 0.0) {
            NodeData location = sweetSpotInt.get(0);
            commandAnts.commandMap.put(ant.id, bline.findPath(MapControl.myMap.get(ant.gridY).get(ant.gridX), location));
            commandAnts.questMapping.put(ant.id, new AntAction(AntActionType.ENTER_NEST));
        } else {

            AntAction action = new AntAction(AntActionType.MOVE);

            action.direction = Direction.getRandomDir();
            LinkedList<AntAction> explore = new LinkedList<>();
            for (int i = 0; i < 50; i++) {
                explore.add(action);
            }
            commandAnts.commandMap.put(ant.id, explore);

            commandAnts.questMapping.put(ant.id, action);
        }

    }

    public void drawAnts(CommData data) throws IOException {
    //AntWorld 
        //antworld = new GraphicContainer(data);

    }

    public static void main(String[] args) throws IOException {
        String serverHost = "b146-76";
        System.out.println(args.length);
        if (args.length > 0) {
            serverHost = args[0];
        }

        myClient = new ClientControl(serverHost, Constants.PORT);
        gameBoard.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {

                if (e.isControlDown() && e.getKeyCode() == VK_X) {
                    myClient.closeAll();
                }
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

    private boolean isObstructed(AntData ant, CommData data) {

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (MapControl.myMap.get(i + ant.gridY).get(j + ant.gridX).getElevation() == Integer.MAX_VALUE) {
                    return true;
                }
                ArrayList<AntData> antList = new ArrayList<>();
                antList.addAll(data.myAntList);
                antList.addAll(data.enemyAntSet);
                antList.remove(ant);
                for (AntData otherAnt : antList) {
                    if (ant.gridX + i == otherAnt.gridX && ant.gridY + j == otherAnt.gridY) {
                        return true;
                    }

                }

            }
        }
        return false;

    }

    private Direction getObstructedDir(AntData ant, ArrayList<AntData> antList) {

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {

                for (AntData otherAnt : antList) {
                    if (ant.gridX + i == otherAnt.gridX && ant.gridY + j == otherAnt.gridY) {
                        return GetDirection.returnDirEnum(j, i);
                    }

                }

            }
        }
        return null;

    }

    public static boolean doDraw() {
        return DRAW;
    }

    public int getCenterX() {
        //centerX is not updating correctly, hardcode
//    return centerX;
        int x = 2064;
        return x;
    }

    public int getCenterY() {
        //centerX is not updating correctly, hardcode
//    return centerY;
        int y = 2125;
        return y;
    }

}
