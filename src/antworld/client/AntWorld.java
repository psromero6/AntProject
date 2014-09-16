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

    static ArrayList<ArrayList<NodeData>> yourMap;
    static ArrayList<AntData> scoutList;
    static ArrayList<AntData> gathererList;
    static int nturns;
    public static Picture gameBoard;
    static float zoomLevel = 1.0f;
    static Point startClick = new Point(0, 0);

//keeping track of the numer of turns
    //add more catagories as needed. ie solder, medic etc.
    /**
     * @param args the command line arguments
     */
    public AntWorld(CommData data) throws IOException
    {
        scoutList = new ArrayList<AntData>();
        //Control comptrol=
        //need to initalize global myMap
        yourMap = new Control().myMap;
        //globalNodeMap= buildMap(readImage());
        NestData myNest = new NestData(NestNameEnum.ACORN, TeamNameEnum.Buffalograss, 0, 0);//
        gameBoard=new Picture("AntWorld.PNG");
        gameBoard.setResizable(true);
        gameBoard.setSize(gameBoard.getImageWidth()/2,gameBoard.getImageHeight()/2);


//        gameBoard.addMouseWheelListener(new MouseWheelListener()
//        {
//
//            @Override
//            public void mouseWheelMoved(MouseWheelEvent e)
//            {
//                int notches = e.getWheelRotation();
//
//                if (notches < 0)
//                {
//                    gameBoard.zoom('i', e.getPoint());
//                }
//                if (notches > 0)
//                {
//                    gameBoard.zoom('o', e.getPoint());
//                }
//                //gameBoard.setCenter(0,0);
//            }
//        });

        

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



    public void draw(CommData data)
    {

        gameBoard.refresh("AntWorld.png");
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
                gameBoard.setRGB(x + i - size, y + j - size, r, g, b);
            }
        }

    }

    public void drawOtherAnt(int x, int y)
    {
        int size = 4;
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {

                gameBoard.setRGB(x + i - size, y + j - size, 0, 0, 0);
            }
        }

    }

    public void drawMyAnt(int x, int y)
    {
        int size = 4;
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                //int blue=gameBoard.getBlue(x+i-size, y+j-size);
                gameBoard.setRGB(x + i - size, y + j - size, 255, 255, 255);
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
                gameBoard.setRGB(x + i - size, y + j - size, 255 - (hVal % 255), (hVal % 255), 0);
            }
        }
    }
    
    public void setLocation(int x, int y){
    
    gameBoard.setLocation(x, y);
    }

}
