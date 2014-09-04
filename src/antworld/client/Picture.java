package antworld.client;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class Picture extends JFrame implements ComponentListener
{ 
  public static final String VERSION = "Picture() Version 2013.4.16";
  
  private int imageWidth, imageHeight;
  private BufferedImage offScreenImage,savedImage;
  private ComponentListener resizeListenter;
  private DrawPane drawPane;
  public Double zoomLvl;
  private boolean error = false;
  
  
  //==================================================================
  //Constructor to create an empty picture of a specified inside size
  //  A JPanel is created that fills the inside of the area of the 
  //  JFrame.
  //  This also creates an offscreen BufferedImage for users of this
  //  class to draw on.
  //
  //When repaint on the JFrame, the offscreen buffered is copied
  //  to the JPanel filling this JFrame.
  //
  //==================================================================
  public Picture(int insideWidth, int insideHeight)
  {
    zoomLvl=1.0;
    savedImage=offScreenImage;
    System.out.println(VERSION);
    this.setTitle(VERSION);
    
    imageWidth = insideWidth;
    imageHeight = insideHeight;
    offScreenImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

    
    this.setResizable(false);
    this.setVisible(true);
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    Container contantPane = this.getContentPane();
    contantPane.setLayout(null);
    drawPane = new DrawPane();
    contantPane.add(drawPane);
    
    
    addSpaceToFrameForBoarder();
  }
  
  //==================================================================
  //Constructor that displays a file dialog box for the 
  //  user to select an image file. The selected image is loaded 
  //  into a JFrame with inside space equal to the image size.
  //==================================================================
  public Picture()
  {
    System.out.println(VERSION);
    String userFilePath = pickFile();
    
    if (userFilePath == null) {error = true; return;}
    else if (userFilePath.length() < 1) {error = true; return;}
    
    setupWindowWithImageFromFile(userFilePath);
  }
  
  
  //==================================================================
  //Constructor expects to be given an image path.
  //  The path can either be absolute or relative
  //  The image found at the given path is loaded.
  //  into a JFrame with inside space equal to the image size.
  //==================================================================
  public Picture(String path)
  {
    setupWindowWithImageFromFile(path);
  }
  
  
  void setupWindowWithImageFromFile(String path)
  {
    zoomLvl=1.0;
    savedImage=offScreenImage;
    offScreenImage = loadImage(path, this);
    if (offScreenImage == null)
    { error = true; 
      return;
    }
    
    this.setTitle(path);
    imageWidth = offScreenImage.getWidth();
    imageHeight = offScreenImage.getHeight();
    this.setResizable(true);
    this.setVisible(true);
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    Container contantPane = this.getContentPane();
    contantPane.setLayout(null);
    drawPane = new DrawPane();
   
    contantPane.add(drawPane);
    
    
    addSpaceToFrameForBoarder();
  }
  
  //==================================================================
  //addSpaceToFrameForBoarder()
  //
  // Makes the frame large enough for the frame boarders not to take
  //   away form the inside space.
  //==================================================================
  private void addSpaceToFrameForBoarder()
  {
    this.setSize(imageWidth, imageHeight); 
    
    Insets inset = this.getInsets();
    int frameWidth = imageWidth + inset.left + inset.right;
    int frameHeight = imageHeight + inset.top + inset.bottom;
    this.setSize(frameWidth, frameHeight);

    drawPane.setBounds(0, 0, imageWidth, imageHeight);

  }
  
  
  //===============================================================
  //listenForResize(ComponentListener listenter)
  //
  // Call this method to make the window resizeable.
  // This method requires a ComponentListener. 
  // To use this, the class calling this method must:
  // 1) implements ComponentListener.
  // 2) pass the instance of itself to listenForResize.
  // This is needed so that when the window is resized, the Picture 
  // class can call back to the class that created the Picture.
  // That class needs to know about the resizing so it can get a
  // new copy of the offscreen buffered image and so that it
  // knows it now has a different drawing space.
  //===============================================================
  public void listenForResize(ComponentListener listenter)
  { resizeListenter = listenter;
    this.addComponentListener(this);
    this.setResizable(true);
    
    //Note: resizable windows have larger boarders
    addSpaceToFrameForBoarder();
  }
  
  public boolean isError()
  { return error;
  }
  
  //=========================================================================
  // pickFile()
  //=========================================================================
  private static String pickFile()
  {
    String dir = System.getProperty("user.dir");
    JFileChooser fileChooser = new JFileChooser(dir);
    int returnVal = fileChooser.showOpenDialog(null);
    
    if (returnVal == JFileChooser.APPROVE_OPTION) 
    {
      File file = fileChooser.getSelectedFile();
      String imagePath = file.getPath();
      System.out.println("You selected file: ["+imagePath+"]");
      return imagePath; 
    }
    
    return null;
  }
  
  
  
  //=========================================================================
  // loadImage(String imagePath, Component window)
  //
  // load each image and register it, 
  // using the MediaTracker.addImage (Image, int) method. 
  // It takes as its first parameter an image, 
  // and the idcode of the image as its second parameter. 
  // The idcode can be used to inquire about the status of 
  // a particular image, rather than a group of images.
  //=========================================================================
  private static BufferedImage loadImage(String imagePath, Container widgit)
  {
    if (imagePath == null) return null;
    if (widgit == null)
    {
      widgit = new Container();
    }
    
    // Create a MediaTracker instance, to montior loading of images
    MediaTracker tracker = new MediaTracker(widgit);

    BufferedImage loadedImage = null;
    URL fileURL = null;

    // Load the image
    
    System.out.println("Picture.loadImage(imagePath="+imagePath+")");
    
    try
    {
      fileURL = new URL("file:" + imagePath);
      loadedImage = ImageIO.read(fileURL);

    }
    catch (MalformedURLException e1)
    {
      e1.printStackTrace();
    }
    
    catch (IOException e)
    {
      e.printStackTrace();
    }
    

    // Register it with media tracker
    tracker.addImage(loadedImage, 1);
    try
    { tracker.waitForAll();
    }
    catch (Exception e){}
    
    int width = loadedImage.getWidth(null);
    int height = loadedImage.getHeight(null);
    if (width <=0 || height <=0) return null;
    
    BufferedImage imageBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics g = imageBuffer.getGraphics();
    g.drawImage(loadedImage, 0, 0, null);
    
    return imageBuffer; 
  }
  
  
  
  //=========================================================================
  // getOffScreenGraphics()
  //=========================================================================
  public Graphics getOffScreenGraphics()
  { if (error) return null;
    return offScreenImage.getGraphics();
  }
  
  
  //=========================================================================
  // getImageWidth()
  //=========================================================================
  public int getImageWidth()
  { return offScreenImage.getWidth();
  }
  
  
  //=========================================================================
  // getImageHeight()
  //=========================================================================
  public int getImageHeight()
  { return offScreenImage.getHeight();
  }
  
  
  
  //=========================================================================
  // getRed(int x, int y)
  //=========================================================================
  public int getRed(int x, int y)
  {
    int rgb = offScreenImage.getRGB(x, y);
    int red = (rgb & 0x00FF0000) >> 16;
    return red;
  }
  
  
  //=========================================================================
  // getGreen(int x, int y)
  //=========================================================================
  public int getGreen(int x, int y)
  {
    int rgb = offScreenImage.getRGB(x, y);
    int green = (rgb & 0x0000FF00) >> 8;
    return green;
  }
  
  
  //=========================================================================
  // getBlue(int x, int y)
  //=========================================================================
  public int getBlue(int x, int y)
  {
    int rgb = offScreenImage.getRGB(x, y);
    int blue = rgb & 0x000000FF;
    return blue;
  }
  
  
  
  
  //=========================================================================
  // setRGB(int x, int y, int r, int g, int b)
  //=========================================================================
  public void setRGB(int x, int y, int r, int g, int b)
  {
    if (x<0) return;
    if (y<0) return;
    if (x>imageWidth) return;
    if (y>imageHeight) return;
    if (r<0 || g<0 || b<0) return;
    if (r>255 || g>255 || b>255) return;
    
    int rgb = (r<<16) | (g<<8) | b;
    offScreenImage.setRGB(x, y, rgb);
  }
  
  
  //=========================================================================
  // setColor(int x, int y, Color c)
  //=========================================================================
  public void setColor(int x, int y, Color c)
  {
    setRGB(x, y, c.getRed(), c.getGreen(), c.getBlue());
  }
  
  
  //=========================================================================
  // saveImage()
  //=========================================================================
  public void saveImage()
  {
    JFileChooser fileChooser = new JFileChooser();

    int returnValue = fileChooser.showSaveDialog(null);

    if (returnValue != JFileChooser.APPROVE_OPTION) return;

    File inputFile = fileChooser.getSelectedFile();
    String path = inputFile.getAbsolutePath();
    if ((path.endsWith(".png") == false) && (path.endsWith(".PNG") == false))
    { path = path+".png";
    }
    
    File myFile = new File(path); 
    try
    { ImageIO.write(offScreenImage, "png", myFile);
    }
    catch (Exception e){ e.printStackTrace();}
  }
  
  public void setCenter(int x, int y){
    // int h=drawPane.getHeight();
     //int w=drawPane.getWidth();
 //drawPane.setBounds(0,0,x+w,y+h);
drawPane.setLocation(x, y);
 System.out.println("setCenter"+x+";"+y);
  
  }
  public void resize(float zoomLevel){
   /*   Insets inset = this.getInsets();
      int frameWidth = this.getWidth();
      int frameHeight = this.getHeight();
      imageHeight = frameHeight - inset.top - inset.bottom;
      imageWidth = frameWidth - inset.left - inset.right;
      float newImageWidth = imageWidth * zoomLevel;
      float newImageHeight = imageHeight * zoomLevel;
      System.out.println(newImageWidth + ";" + newImageHeight);
      offScreenImage = new BufferedImage((int) newImageWidth, (int) newImageHeight, BufferedImage.TYPE_INT_RGB);
//offScreenImage.
      drawPane.setBounds(0, 0, imageWidth, imageHeight);
 */
      
      setupWindowWithImageFromFile("AntWorld.PNG");
      BufferedImage before = offScreenImage;
int w = before.getWidth();
int h = before.getHeight();
//image is reloaded to prevent a loss of detail.
BufferedImage after = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
AffineTransform at = new AffineTransform();
at.scale(zoomLevel, zoomLevel);
AffineTransformOp scaleOp = 
   new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
after = scaleOp.filter(before, after);
     
      
      offScreenImage=after;
      
       //drawPane.setBounds(0, 0, 1920, 1080);
     // System.out.println(w/2+","+ h/2);
       //drawPane.setLocation(500, 700);
    //This is the callback to class that created this Picture
  //  resizeListenter.componentResized(arg0);
  }
  
 public void zoom(char ch,Point p){
      
      if(ch=='i'){zoomLvl=zoomLvl*2;}
      if(ch=='o'){zoomLvl=zoomLvl/2;System.out.println("zoomout"+zoomLvl);}
      int zoomx=(int) Math.round(imageWidth*zoomLvl);
      int zoomy=(int) Math.round(imageHeight*zoomLvl);
      System.out.println(zoomx+";"+zoomy+":"+zoomLvl);

      offScreenImage.flush();
      offScreenImage=getScaledImage(savedImage, zoomx, zoomy);
      drawPane.setBounds(0,0,zoomx,zoomy);
     drawPane.setLocation(-p.x*zoomx,-p.y*zoomy);
  drawPane.repaint();
  }
  public Point getDrawPaneLocation(){
  
  return drawPane.getLocation();
  }

  public void componentHidden(ComponentEvent arg0){}
  public void componentMoved(ComponentEvent arg0) {}
  public void componentShown(ComponentEvent arg0) {}
  public void componentResized(ComponentEvent arg0) 
  { 
    
    Insets inset = this.getInsets();
    int frameWidth = this.getWidth();
    int frameHeight = this.getHeight();
    
    System.out.println("Picture::Resized ("+frameWidth+", "+frameHeight+")");
    imageHeight = frameHeight - inset.top - inset.bottom;
    imageWidth = frameWidth - inset.left - inset.right;
    
    offScreenImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
    drawPane.setBounds(0, 0, imageWidth, imageHeight);
    
    //This is the callback to class that created this Picture
    resizeListenter.componentResized(arg0);
    
  }
  
 // public static void main(String[] args)
 // { new Picture();
  //  new Picture("Smaug.gif");
 // }
  
  
  
  //=========================================================================
  // DrawPane
  // This is an inner class it is just used to copy the offscren buffer
  // to the JPanel that fills the JFrame window.
  //=========================================================================
  class DrawPane extends JPanel
  {
    public void paintComponent(Graphics canvas)  
    { 
      canvas.drawImage(offScreenImage, 0, 0, null);
    }
  }
    private BufferedImage getScaledImage(BufferedImage srcImg, int w, int h){
    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
    Graphics2D g2 = resizedImg.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(srcImg, 0, 0, w, h, null);
    g2.dispose();
    return resizedImg;
}
}