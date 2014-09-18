/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package antworld.client;

import java.util.LinkedList;

/**
 *
 * @author Stephen
 */
public class PathThread extends Thread{
    public NodeData antNode,goalNode;

    public PathThread(NodeData start,NodeData end){
        antNode=start;
        goalNode=end;
        
        
    }

public void run(){
 AStar search=new AStar();
 LinkedList<Character> list=search.findPath(antNode, goalNode);
 System.out.println(list);





    
    
}}
