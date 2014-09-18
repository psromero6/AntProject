/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antworld.client;

import antworld.data.AntData;
import antworld.data.AntType;
import antworld.data.CommData;
import antworld.data.FoodData;
import antworld.data.NestData;
import antworld.data.NestNameEnum;
import antworld.data.TeamNameEnum;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_X;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 *
 * @author Stephen
 */
public class AntWorld
{

    //static Node[][] globalNodeMap;
  public static Picture gameBoard;
 // ArrayList<NodeData> paintNodes=new ArrayList<>();
    //static Point startClick = new Point(0, 0);

//keeping track of the numer of turns
  //add more catagories as needed. ie solder, medic etc.
  /**
   * @param args the command line arguments
   */
  public AntWorld(CommData data) throws IOException
  {
    //globalNodeMap= buildMap(readImage());
      
    //NestData myNest = new NestData(NestNameEnum.ACORN, TeamNameEnum.Buffalograss, 0, 0);//
    gameBoard = new Picture("AntWorld.png");
    gameBoard.setResizable(true);
    gameBoard.setSize(gameBoard.getImageWidth() / 2, gameBoard.getImageHeight() / 2);
    gameBoard.setCenter(-1500, -1700);
    gameBoard.addMouseMotionListener(new MouseMotionListener()
    {
      Point start;
      Point dpPoint;

      @Override
      public void mouseDragged(MouseEvent e)
      {
        int x = e.getX();
        int y = e.getY();
        gameBoard.setCenter(x - start.x, y - start.y);
      }

      @Override
      public void mouseMoved(MouseEvent e)
      {

        dpPoint = gameBoard.getDrawPaneLocation();
        start = new Point(e.getPoint().x - dpPoint.x, e.getPoint().y - dpPoint.y);

      }

    });

  }

  public static int[][] readImage() throws IOException
  {
    File fle = new File("AntWorld.png");
    BufferedImage img = ImageIO.read(fle);
    Raster rstr = img.getData();
    int imagearray[][] = new int[rstr.getWidth()][rstr.getHeight()];
    int pixeldata[] = new int[3];
    for (int i = 0; i < rstr.getWidth(); i++)
    {
      for (int j = 0; j < rstr.getHeight(); j++)
      {
        rstr.getPixel(i, j, pixeldata);
        imagearray[i][j] = pixeldata[1];
        // System.out.print(imagearray[i][j]);
      }
      // System.out.println("");
    }
    //build map as a list of nodes
    return imagearray;

  }

  public void resetPic()
  {
    gameBoard.dispose();

    //globalNodeMap= buildMap(readImage());
    NestData myNest = new NestData(NestNameEnum.ACORN, TeamNameEnum.Buffalograss, 0, 0);//
    gameBoard = new Picture("AntWorld.png");
    gameBoard.setResizable(true);
    gameBoard.setSize(gameBoard.getImageWidth() / 2, gameBoard.getImageHeight() / 2);
    gameBoard.setCenter(-1500, -1700);
    gameBoard.addMouseMotionListener(new MouseMotionListener()
    {
      Point start;
      Point dpPoint;

      @Override
      public void mouseDragged(MouseEvent e)
      {
        int x = e.getX();
        int y = e.getY();
        gameBoard.setCenter(x - start.x, y - start.y);
      }

      @Override
      public void mouseMoved(MouseEvent e)
      {

        dpPoint = gameBoard.getDrawPaneLocation();
        start = new Point(e.getPoint().x - dpPoint.x, e.getPoint().y - dpPoint.y);

      }

    });

  }

  public void draw(CommData data)
  {

    gameBoard.refresh();
    for (AntData ant : data.myAntList)
    {
      drawMyAnt(ant.gridX, ant.gridY);

    }
    for (AntData ant : data.enemyAntSet)
    {
      drawOtherAnt(ant.gridX, ant.gridY);

    }

    for (FoodData food : data.foodSet)
    {
      drawFood(food.gridX, food.gridY, food.foodType.getColor());

    }

  }

  public void drawFood(int x, int y, int argb)
  {
    int size = 4;
    int r = (argb) & 0xFF;
    int g = (argb >> 8) & 0xFF;
    int b = (argb >> 16) & 0xFF;

    for (int i = 0; i < size; i++)
    {
      for (int j = 0; j < size; j++)
      {

        //gameBoard.setColor(x, y, color);
         // paintNodes.add(Control.myMap.get(j).get(i));
        gameBoard.setRGB(x + i - size, y + j - size, r, g, b);
      }
    }

  }

  public void drawOtherAnt(int x, int y) 
{ 
int size = 3; 
for (int i = 0; i < size; i++) 
{ 
for (int j = 0; j < size; j++) 
{ 
gameBoard.setRGB(x + i - size, y + j - size, 0, 0, 0); 
if ((j > 0) && (j + 1 < size) && (i > 0) && (i + 1 < size)) 
{ 
gameBoard.setRGB(x + i - size, y + j - size, 0, 200, 200); 
}
//paintNodes.add(Control.myMap.get(j).get(i));
} 
} 
} 

public void drawMyAnt(int x, int y) 
{ 
int size = 3; 
for (int i = 0; i < size; i++) 
{ 
for (int j = 0; j < size; j++) 
{ 
//int blue=gameBoard.getBlue(x+i-size, y+j-size); 
gameBoard.setRGB(x + i - size, y + j - size, 255, 255, 255); 
if ((j > 0) && (j + 1 < size) && (i > 0) && (i + 1 < size)) 
{ 
gameBoard.setRGB(x + i - size, y + j - size, 255, 0, 0); 
}
//paintNodes.add(Control.myMap.get(j).get(i));
} 
} 
} 

  public void drawMapPixel(int x, int y, int hVal)
  {
    int size = 1;
    for (int i = 0; i < size; i++)
    {
      for (int j = 0; j < size; j++)
      {
       //   paintNodes.add(Control.myMap.get(j).get(i));
        gameBoard.setRGB(x + i - size, y + j - size, 255 - (hVal % 255), (hVal % 255), 0);
      }
    }
  }
 public void drawMapCircle(int x, int y, int[] RGB)
  {
    int size = 6;
    for (int i = 0; i < size; i++)
    {
      for (int j = 0; j < size; j++)
      {
       if(Math.abs(i+j)>=6) gameBoard.setRGB(x + i - size, y + j - size, RGB[0], RGB[1], RGB[2]);
      // paintNodes.add(Control.myMap.get(j).get(i));
      }
    }
  }
  public void setLocation(int x, int y)
  {

    gameBoard.setLocation(x, y);
  }

//  public void flash(){
//  for(NodeData node:paintNodes){
//  int x=node.colID;
//  int y=node.rowID;
//  
//  //node.
//  
// // gameBoard.setRGB(x , y , 255 - (hVal % 255), (hVal % 255), 0);
//  }
  
  
  
  }
  
  
  
  

