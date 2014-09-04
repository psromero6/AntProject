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
public class AntWorld {
   //static Node[][] globalNodeMap;
   static ArrayList<ArrayList<NodeData>> yourMap;
   static ArrayList<AntData> scoutList;
   static ArrayList<AntData> gathererList;
   static int nturns;
   static Picture gameBoard;
   static float zoomLevel =1.0f;
   static Point startClick=new Point(0,0);
//keeping track of the numer of turns
   //add more catagories as needed. ie solder, medic etc.
   
   
    /**
     * @param args the command line arguments
     */
    public AntWorld(CommData data) throws IOException {
        scoutList = new ArrayList<AntData>();
        //Control comptrol=
        //need to initalize global myMap
        yourMap=new Control().myMap;
       //globalNodeMap= buildMap(readImage());
       NestData myNest=new NestData(NestNameEnum.ACORN,TeamNameEnum.Buffalograss,0,0);//
       ArrayList<NodeData> patrolNodeList =  getPatrolNodes(myNest.centerX, myNest.centerY,10);
       gameBoard=new Picture("AntWorld.png");
       gameBoard.setResizable(true);
       gameBoard.setSize(1920,1080);
       //gameBoard.setLocation(0,0);
        AntData testAnt=new AntData(1, AntType.BASIC, NestNameEnum.ACORN, TeamNameEnum.Buffalograss);
       AntData testAnt1=new AntData(2, AntType.BASIC, NestNameEnum.ACORN, TeamNameEnum.Buffalograss);
       testAnt.gridY=240;
       testAnt.gridX=700;
       testAnt1.gridY=240;
       testAnt1.gridX=1175;
       scoutList.add(testAnt);
       scoutList.add(testAnt1);
       draw(data);

       
        
    gameBoard.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches=e.getWheelRotation();
                if(notches<0) {drawPaneResize(gameBoard,'u');}
                if(notches>0) {drawPaneResize(gameBoard,'d');}
                //gameBoard.setCenter(0,0);
            }
        });
       
    gameBoard.addMouseListener(new MouseListener(){
      Point startClick;
            @Override
            public void mouseClicked(MouseEvent e) {
               System.out.println("Click");
            }

            @Override
            public void mousePressed(MouseEvent e) {
                startClick=e.getPoint();
                System.out.println("StartClick:"+startClick);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //ispressed=false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                System.out.println("Enter");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                System.out.println("Exit");
            }
    
    
    });
       
   gameBoard.addMouseMotionListener(new MouseMotionListener(){
            Point start;
            Point dpPoint;
            @Override
            public void mouseDragged(MouseEvent e) {
                //Point pnt=getStart();
                int x=e.getX();
                int y=e.getY();
                System.out.println(x+";"+y);
                gameBoard.setCenter(x-start.x,y-start.y);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             
            //start=e.getPoint();
               dpPoint=gameBoard.getDrawPaneLocation();
            start=new Point(e.getPoint().x-dpPoint.x,e.getPoint().y-dpPoint.y);
           // System.out.println("start:   "+start);
          //  System.out.println("gameBoard:   "+gameBoard.getDrawPaneLocation());
            }
            
            public void setStart(MouseEvent e){
           
            
            }
            
            public Point getStart(MouseEvent e){
            
            return new Point();
            }
            
        });
    
    
    }
    
    public static void drawPaneResize(Picture pic,char in){
        if (in=='u') zoomLevel=zoomLevel * 2;
        if (in=='d') zoomLevel=zoomLevel / 2;
        
        gameBoard.resize(zoomLevel);
        //gameBoard.setupWindowWithImageFromFile("AntWorld.PNG");
    gameBoard.setSize(1920,1080);
   // gameBoard.setCenter(2500,-1250);
            
        //gameBoard.repaint();
    }
    
    
    public static int[][] readImage() throws IOException{
        File fle=new File("AntWorld.png");
        BufferedImage img= ImageIO.read(fle);
        Raster rstr=img.getData();
        int imagearray[][]=new int[rstr.getWidth()][rstr.getHeight()];
        int pixeldata[]=new int[3];
        for(int i=0;i<rstr.getWidth();i++){
            for(int j=0;j<rstr.getHeight();j++){
                rstr.getPixel(i, j, pixeldata);
                imagearray[i][j]=pixeldata[1];
               // System.out.print(imagearray[i][j]);
                    }
                   // System.out.println("");
                }
        //build map as a list of nodes
        return imagearray;
    
    
    }
    
//    public static NodeDaty[][] buildMap(int[][] intMap){
//        int l=intMap.length;
//        int w=intMap[0].length;
//        Node[][] nodeMap=new Node[l][intMap[0].length];
//        for(int i=0;i<l;i++){
//            for(int j=0;j<w;j++){
//             nodeMap[i][j]=new Node(i,j,intMap[i][j]);
//            }
//        }
//    
//    return nodeMap;
//    }
    
    
        public ArrayList<NodeData> getPatrolNodes(int centerX,int centerY,int radius){
            ArrayList<NodeData> patrolNodeList= new ArrayList<>();
            for(int i=-radius;i<radius;i++){
            int j=radius-Math.abs(i);
            //myMap hasnt been declared yet.
               if(i>-1&&j>-1) patrolNodeList.add(yourMap.get(j).get(i));
           
        }
            return patrolNodeList;
        }
        public void draw(CommData data){

System.out.println("myAnts drawn");
for (AntData ant:data.myAntList){
       drawMyAnt(ant.gridX,ant.gridY); 
      
       
       }
for (AntData ant:data.enemyAntSet){
    drawOtherAnt(ant.gridX,ant.gridY);
    
    
}

for (FoodData food:data.foodSet){
    drawFood(food.gridX,food.gridY,food.foodType.getColor());

    
}
        
        }
 public void drawFood(int x,int y,int argb){
        int size=10;
        int r = (argb)&0xFF;
        int g = (argb>>8)&0xFF;
        int b = (argb>>16)&0xFF;
        
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                
                //gameBoard.setColor(x, y, color);
             gameBoard.setRGB(x+i-size, y+j-size, r, g, b);
            }
        }
        
        }
        
        
        public void drawOtherAnt(int x,int y){
        int size=10;
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                
             gameBoard.setRGB(x+i-size, y+j-size, 0, 0, 0);
            }
        }
        
        }
        
        
        
        
        
        
        public void drawMyAnt(int x,int y){
        int size=10;
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                int blue=gameBoard.getBlue(x+i-size, y+j-size);
             gameBoard.setRGB(x+i-size, y+j-size, 255, 255, blue++);
            }
        }
        
        
        
        
        }
        
        
}
