package antworld.client;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Control
{
  public static BufferedImage img;
  public static ArrayList<ArrayList<NodeData>> myMap = new ArrayList<ArrayList<NodeData>>();

  ///////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////
  
  public Control(){
  
  readImage();
  }
  
  
  
  
  
  
  public static void readImage()
  {
    File fle = new File("AntWorld.png");
    //BufferedImage img;
    try
    {
      img = ImageIO.read(fle);
    } catch (IOException e)
    {
      e.printStackTrace();
      return;
    }
    
    Raster rstr = img.getData();
    //displayImage(img);
    int[] waterData=new int[3];
    rstr.getPixel(10, 10, waterData);       
    
    //\\populate myMap from image
    for(int row=0;row<rstr.getHeight();row++)
    {
      ArrayList<NodeData> tempNodeArr=new ArrayList<NodeData>();//
      for(int col=0;col<rstr.getWidth();col++)
      {        
        int pixelData[]=new int[3];
        rstr.getPixel(col, row, pixelData);//accessing image as (col,row) like (x,y), but access map as (row,col) like a matrix
        if(pixelData[0]==waterData[0]&&pixelData[1]==waterData[1]&&pixelData[2]==waterData[2])
        {
          NodeData tempNode=new NodeData(row,col,Integer.MAX_VALUE);//impassable terrain, water
          tempNodeArr.add(tempNode);
        }
        else
        {
          NodeData tempNode=new NodeData(row,col,pixelData[1]);//pixelData[1] contains green value, we use for elevation
          tempNodeArr.add(tempNode);
        }
        
        
      }
      myMap.add(tempNodeArr);
    }
    //TODO set base movecost for water blocks as INT_MAX
    int thisIsABreakPoint=0;
    thisIsABreakPoint=thisIsABreakPoint++;
  }
  ///////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////
  public static void displayImage(BufferedImage img)
  {
    Image scaledImg=img.getScaledInstance(1728, 972, 0);
    JFrame myFrame=new JFrame();
    JLabel label=new JLabel(new ImageIcon(scaledImg));
    myFrame.getContentPane().setLayout(new FlowLayout());
    myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    myFrame.getContentPane().add(label);
    myFrame.pack();
    myFrame.setLocation(100,100);
    myFrame.setVisible(true);
  }
    
  ///////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////
  public static NodeData[] findNests()
  {
    //first iteration this method only searches for first 2 nests to draw a line between them
    NodeData[] topNests=new NodeData[2];
    int count=0;
    for(int row=0;row<myMap.size();row++)
    {
      for(int col=0;col<myMap.get(row).size();col++)
      {
        if(myMap.get(row).get(col).getElevation()==0)
        {
          if(count==2){break;}
          topNests[count]=myMap.get(row).get(col);
          count++;          
        }
      }
    }
    return topNests;
  }
  
  ///////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////
  public static void findPath(NodeData startLoc,NodeData goalLoc)
  {
//    .NodeData[] topNests;
    readImage();
//    topNests=findNests();
//    NodeData startLoc=topNests[0];
//    NodeData goalLoc=topNests[1];
//    //debug, a really close point
    
    
    //NodeData startLoc=myMap.get(240).get(700);
    //NodeData goalLoc=myMap.get(240).get(874);
    
//    NodeData startLoc=myMap.get(240).get(700);
//    NodeData goalLoc=myMap.get(300).get(800);
    
    
    AStar myPath=new AStar();    
    final long startTime = System.currentTimeMillis();
    myPath.findPath(startLoc,goalLoc);
    final long endTime = System.currentTimeMillis();
    System.out.println("Total execution time: " + (endTime - startTime));
  }

}