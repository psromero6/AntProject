package antworld.client;
/****************************************************************************
   *  Authors: Hans Weeks and Stephen Romero
   * 
   * This class is instantiated by ClientControl. It builds a double arrayList
   * of NodeData's, each node representing 1 pixel of the map. This arrayList is
   * used throughout the program to reference locations on the map as nodes.
   * accessed as MapControl.myMap.get(row).get(col) of location. note that
   * row=y and col=x here, thus access is matrix style, NOT CARTESIAN COORD STYLE
  ****************************************************************************/

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class MapControl
{

    public static BufferedImage img;
    public static ArrayList<ArrayList<NodeData>> myMap = new ArrayList<ArrayList<NodeData>>();

  /****************************************************************************
   *Constructor
   *  input: reads png image of map, called from AntWorld
   *  description: calls read image method in this class to access map image
  ****************************************************************************/
    public MapControl()
    {

        readImage();
    }

    /****************************************************************************
   *readImage
   *  input:none
   *  output:none
   *  description: accesses image file to build double ArrayList my map,
   *  which contains each pixel of the image as a node data. This object is accessed
   *  by the rest of the program to specify nodes (i.e. currentNode, startNode,
   *  goalNode, neighborNodes
  ****************************************************************************/
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
        int[] waterData = new int[3];
        rstr.getPixel(10, 10, waterData);

        //\\populate myMap from image
        for (int row = 0; row < rstr.getHeight(); row++)
        {
            ArrayList<NodeData> tempNodeArr = new ArrayList<NodeData>();//
            for (int col = 0; col < rstr.getWidth(); col++)
            {
                int pixelData[] = new int[3];
                rstr.getPixel(col, row, pixelData);//accessing image as (col,row) like (x,y), but access map as (row,col) like a matrix
                if (pixelData[0] == waterData[0] && pixelData[1] == waterData[1] && pixelData[2] == waterData[2])
                {
                    NodeData tempNode = new NodeData(row, col, Integer.MAX_VALUE);//impassable terrain, water
                    tempNodeArr.add(tempNode);
                } else
                {
                    NodeData tempNode = new NodeData(row, col, pixelData[1]);//pixelData[1] contains green value, we use for elevation
                    tempNodeArr.add(tempNode);
                }

            }
            myMap.add(tempNodeArr);
        }
        //TODO set base movecost for water blocks as INT_MAX
        int thisIsABreakPoint = 0;
        thisIsABreakPoint = thisIsABreakPoint++;
    }
}