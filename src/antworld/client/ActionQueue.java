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
    //need a way to add remove queues and ants if they are born or if they die

    public ActionQueue(CommData myComm)
    {
        int i = 0;
        for (AntData myAntData : myComm.myAntList)
        {
            LinkedList antActionQueue = new LinkedList<AntAction>();//each ant will have a queue of actions to take. we want it to work like a queue, except that we wont "pop"
            //the queue until we get confirmation that the previous move happened.
            antList.add(myAntData.id);
            commandMap.put(myAntData.id, antActionQueue);
        }
    }

    public void updateActionQueue(AntData ant)
    {
        LinkedList<AntAction> myActionQueue = commandMap.get(ant.id);//get the value for this key
        //This is adding a null move to a running action queue, preventing empty queues
       // AntAction myAction = new AntAction(AntActionType.MOVE, 0, -1);
      //  myActionQueue.add(myAction);
    }
}
