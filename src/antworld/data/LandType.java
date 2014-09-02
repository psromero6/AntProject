package antworld.data;

public enum LandType
{
  NEST
  { public int getMapColor() {return 0xF0E68C;}
  },
  
  GRASS
  { 
    //Note: grass land only uses the green color channel.
    //   Thus, the client AI can use the red and blue channels to store
    //   other data, such as something that takes the role of a pheromone trail
    public int getMapColor() {return 0x283724;}
    public int getMapHeight(int rgb)
    {
      int g = (rgb & 0x0000FF00) >> 8;
      return g - 55;
    }
  },
  
  
  WATER
  { public int getMapColor() {return 0x1E90FF;}
  };
  
  public abstract int getMapColor();
  public int getMapHeight(int rgb) {return 0;}
  public static int getMaxMapHeight() {return 200;}
 
  
}
