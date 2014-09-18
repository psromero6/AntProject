package antworld.client;


import antworld.data.AntAction;
import antworld.data.AntAction.AntActionType;
import java.util.ArrayList;
import java.util.LinkedList;




public class AStar 
{

    public static ArrayList<ArrayList<NodeData>> myMap = Control.myMap;
    LinkedList<NodeData> openList = new LinkedList<NodeData>();
    LinkedList<NodeData> closedList = new LinkedList<NodeData>();
    ArrayList<NodeData> path = new ArrayList<NodeData>();
    LinkedList<Character> solvedMovedList;
    NodeData startLoc;
    NodeData goalLoc;

    
    
  
    
    
    
  ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    public AStar()
    {
    //TODO need a way to create this object for each ant
        //default constructor is fine
    }
  ///////////////////////////////////////////////////////////////////////////
    //////////////////////////////FINDPATH/////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public LinkedList<Character> findPath(NodeData myStartLoc, NodeData myGoalLoc)
    {
        int tick=0;//debug
        startLoc = myStartLoc;
        goalLoc = myGoalLoc;
        NodeData currentNode = null;
        ArrayList<NodeData> neighborList = null;

        insertSorted(startLoc, openList);
        startLoc.calcG(startLoc);
        startLoc.calcH(goalLoc);
        startLoc.calcF();

        while (openList != null)
        {
            currentNode = openList.getFirst();
            neighborList = findNeighbors(currentNode);

      //TODO dangerous to flag goal found with a null list. Is it possible for there to be a nodes with no neighbors?
            //probably not, as the nodes are removed from this list after the findNeighbor method
            if (neighborList == null)
            {
                return solvedMovedList;
            }//neighbor list returns null once path is found

            int[][] ID = new int[1][2];//debug variable, currentNodeID variable
            ID[0][0] = currentNode.getRowID();//debug
            ID[0][1] = currentNode.getColID();//debug

            openList.remove(currentNode);
            insertSorted(currentNode, closedList);

            neighborList = updateNeighborVal(neighborList, currentNode, goalLoc);
            neighborList = compareNeighborList(neighborList, openList);//remove duplicate
            neighborList = compareNeighborList(neighborList, closedList);
         if( ClientRandomWalk.doDraw())  drawOpenList(closedList);
            
            if(closedList.size()>27)//debug
            {
                if((closedList.get(27).getRowID()==242)&&(closedList.get(27).getColID()==726))
                {
                    if(closedList.get(28).getRowID()==242&&closedList.get(28).getColID()==726)
                    {
                        int wait=0;
                    }
                }
            }
            
            /*debug
            System.out.println("size of closed list:"+closedList.size()+", size of open list:"+openList.size());
            if(closedList.size()>1000)
            {
                int i=0;
                for(NodeData closedNode : closedList)
                {
                    int closedRow=closedNode.getRowID();
                    int closedCol=closedNode.getColID();
                    System.out.println(i+":("+closedRow+","+closedCol+")");
                    i++;
                }
                int wait=0;
            }        //*/    

            while (neighborList.size() != 0)
            {
                insertSorted(neighborList.get(0), openList);
                neighborList.remove(0);
            }
            tick++;
        }
        return null;
    }

  ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    public NodeData findLowF()
    {
    //TODO make this favor nodes in the correct direction first
        //TODO keep the nodes ordered by f value then by h value
        NodeData lowF = openList.getFirst();
        int fVal = lowF.getF();
        for (int i = 0; i < openList.size(); i++)
        {
            if (lowF.getF() > openList.get(i).getF())
            {
                lowF = openList.get(i);
                fVal = lowF.getF();
                i = -1;
            }
        }
        return lowF;
    }

  ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    public void insertSorted(NodeData currentNode, LinkedList<NodeData> myList)//add to open or closed lists
    {
       
    //lowest F is the first value in the list
        //if list is empty, add to beginning
        if (myList.size() == 0)
        {
            myList.add(currentNode);
            return;
        }
       
        int currentF = currentNode.getF();
        int listF = myList.getFirst().getF();
        int listIndex = 0;//index within myList
        
        if(currentF==373)
        {
            int wait=0;
        }

        while (currentF > listF)
        {
            if (listIndex + 1 == myList.size())
            {
                myList.add(currentNode);
                return;
                //append node to end
            }
            listIndex++;
            listF = myList.get(listIndex).getF();
        }
        //here, currentF<=listF
        if (currentF == listF)
        {
            //tie breaker on H value (favor nodes closest to goal)
            int currentH = currentNode.getH();
            int listH = myList.get(listIndex).getH();
            if (currentH < listH)
            {
                myList.add(listIndex, currentNode);
                return;
            }
            while (currentF == listF&&currentH >= listH)//if H value also equal, add after listH's value (arbitrary)//fuck this terrible terrible while loop which will place smaller F after larger!!//fixed now
            {                
                 if (listIndex + 1 == myList.size())
                {
                    myList.add(currentNode);
                    return;
                    //append node to end
                }                
                listIndex++;
                listH = myList.get(listIndex).getH();
                listF=myList.get(listIndex).getF();
            }
            myList.add(listIndex, currentNode);//else
            return;
        }
        //else currentNode<listF
        {//consider making this an else/if (but the code should only get this facr if current is smaller than listF
            myList.add(listIndex, currentNode);
            return;
        }
    //else add current node before (currentF<listF)

    //add a break to add at the end if currentF is larger than any value in the
        //list
    }

  /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    public ArrayList<NodeData> findNeighbors(NodeData currentNode)
    {
        //neighbors will be total of 8 nodes
        int colID;
        int rowID;
        ArrayList<NodeData> neighborList = new ArrayList<NodeData>();

        colID = currentNode.getColID();
        rowID = currentNode.getRowID();

        for (int rowMod = -1; rowMod < 2; rowMod++)
        {
            for (int colMod = -1; colMod < 2; colMod++)
            {
                if (rowMod == colMod && colMod == 0)
                {
                    colMod++;
                }//don't add currentNode to neighborList
                boolean inBounds = true;
                if ((rowID + rowMod < 0) || (rowID + rowMod > myMap.size()))
                {
                    inBounds = false;
                }//check row+mod for in bounds
                if (inBounds)//if it is
                {
                    if ((colID + colMod < 0) || (colID + colMod > myMap.get(rowID + rowMod).size()))
                    {
                        inBounds = false;
                    }//then check col+mod for in bounds
                }
                if (inBounds)//if it passes both inBounds tests, we add it as a neighbor
                {
                    neighborList.add(myMap.get(rowID + rowMod).get(colID + colMod));
                }
            }
        }

        //add parent to nodes with no parent, test for goal as neighbor
        for (int i = 0; i < neighborList.size(); i++)
        {
            //TODO would this be the best place to check for a reparent? not if g/h not calced yet
            if (neighborList.get(i).getParentNode() == null)
            {
                currentNode.setParent(neighborList.get(i));
            }
            if (neighborList.get(i).isGoal(goalLoc))
            {
                //returnPath returns a linkedList, unnecessary in this implementation
//        solvedMovedList=returnPath(neighborList.get(i));
                goalLoc.calcG(startLoc);
                returnPath(neighborList.get(i));

                return null;
            }
        }
        return neighborList;
    }
  /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    public ArrayList<NodeData> compareNeighborList(
            ArrayList<NodeData> neighborList, LinkedList<NodeData> myList)//myList=open or closedList
    {
    // n variables correspond to neighborList, m variables to myList
        //n variables correspond to neighborList, m variables to myList
        int neighborF, nRow, nCol;
        int myF, mRow, mCol;
        ArrayList<OrderedPair> trashList=new ArrayList<OrderedPair>();//list of nodes to be removed from neighborList

        if (myList.size() == 0 || neighborList.size() == 0)
        {
            return neighborList;
        }//base case

        for (int nIndex = 0; nIndex < neighborList.size(); nIndex++)//these don't need to be for loops, as the nested while loops increment/return
        {
            for (int mIndex = 0; mIndex < myList.size(); mIndex++)//
            {
                neighborF = neighborList.get(nIndex).getF();
                myF = myList.get(mIndex).getF();
                while (neighborF > myF)//myList ordered smallest to largest
                {
                    mIndex++;
                    if (mIndex == myList.size())//mIndex already incremented, no mIndex+1==
                    {
                        break;//
                    }
                    //update values for new indices for next while() test
                    neighborF = neighborList.get(nIndex).getF();
                    myF = myList.get(mIndex).getF();
                }
                if (neighborF == myF)
                {
                    nRow = neighborList.get(nIndex).getRowID();
                    nCol = neighborList.get(nIndex).getColID();
                    mRow = myList.get(mIndex).getRowID();
                    mCol = myList.get(mIndex).getColID();
                    
                    //debug
                    if(mRow==242&&mCol==726)
                    {
                        int wait=0;
                        if(nRow==242&&nCol==726)
                        {
                            wait++;
                        }
                    }
                    
                    if (nRow == mRow && nCol == mCol)
                    {
                        //instead of decrement, mark nodes for deletion, delete them all at the end.
                        //neighborList.remove(nIndex);
                        //nIndex--;//neighborList.size() just shrank by 1
                        OrderedPair pair=new OrderedPair(nRow,nCol);
                        trashList.add(pair);
                        break;
                    }
                } else if (neighborF < myF)
                {
                    break;//myF only get bigger as we increment, so break and compare next neighbor
                }
            }
        }
        
        for(OrderedPair deleteThis : trashList)
        {
            for (int nIndex = 0; nIndex < neighborList.size(); nIndex++)//these don't need to be for loops, as the nested while loops increment/return
            {   
                nRow=neighborList.get(nIndex).getRowID();
                nCol=neighborList.get(nIndex).getColID();
                if(nRow==deleteThis.row&&nCol==deleteThis.col)
                {
                    neighborList.remove(nIndex);
                    break;
                }
            }
        }

        for (int i = 0; i < neighborList.size(); i++)
        {
            //TODO get terrain, implement it here!
            if (neighborList.get(i).getElevation() == Integer.MAX_VALUE)
            {
                neighborList.remove(i);
                i--;//because the list just shrank
            }
        }

        return neighborList;
    }

  /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    public ArrayList<NodeData> updateNeighborVal(ArrayList<NodeData> neighborList, NodeData currentNode, NodeData goalLoc)
    {
        //set and test tentative parents
        for (int i = 0; i < neighborList.size(); i++)
        {
            boolean betterParent = false;//flag to see if parent was reset
            currentNode.setTentativeParent(neighborList.get(i));
            neighborList.get(i).calcG(startLoc);
            betterParent = neighborList.get(i).testG(startLoc);//this function will reset the parent if it finds a better parent
            neighborList.get(i).calcG(startLoc);
            neighborList.get(i).calcH(goalLoc);
            neighborList.get(i).calcF();
        }
        return neighborList;
    }

  /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    public LinkedList<AntAction> returnPath(NodeData goalLoc)
    {
        LinkedList<AntAction> moveDirections=new LinkedList<AntAction>();
        int columnDiff, rowDiff;
        int deltaX, deltaY;//difference in col, difference in row
        int currentX, currentY, nextX, nextY;
        NodeData nextNode;
        
        //LinkedList<Character> moveList=new LinkedList<Character>();
        //String moveList = "";
        NodeData currentNode = goalLoc;
        NodeData previousNode = goalLoc;//place holder
        Object[] pathArr = new Object[2];

        while (currentNode.isStart(startLoc) == false)
        {
            path.add(currentNode);
            currentNode = currentNode.getParentNode();
        }
        path.add(currentNode);
        
        for(int i=0;i<path.size()-1;i++)
        {
            currentNode=path.get(i);
            if(i+1==path.size())
            {
             nextNode=path.get(i+1);            
             moveDirections.add(new AntAction(AntActionType.STASIS));
            }
            else //if(i+1<path.size())
            {
            nextNode=path.get(i+1);
            currentX=currentNode.getColID();
            currentY=currentNode.getRowID();
            nextX=nextNode.getColID();
            nextY=nextNode.getRowID();
            deltaX=nextX-currentX;
            deltaY=nextY-currentY;
            AntAction tempAction=new AntAction(AntActionType.MOVE,deltaX,deltaY);
            moveDirections.add(tempAction);
            }
        }
//    System.out.println(moveList.size());//print number of moves
//    System.out.println("Number of moves=" + (path.size() - 1));
//    System.out.println("move cost="+goalLoc.getParentNode().getG());
//        System.out.println("move cost=" + goalLoc.getG());
//        System.out.println("start elevation=" + startLoc.getElevation());
//        System.out.println("goal elevation=" + goalLoc.getElevation());

        //System.out.println(moveList);

        return moveDirections;
    }

   /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    public void drawOpenList(LinkedList<NodeData> openList)
    {
        for (NodeData currentNode : openList)
        {
            int currRow = currentNode.getRowID();
            int currCol = currentNode.getColID();
            int currH = currentNode.getH();
           ClientRandomWalk.myClient.antworld.drawMapPixel(currCol, currRow, currH);
            //antworld.drawAnt(currCol,currRow);
        }
    }
}