package antworld.data;

import java.io.Serializable;

public class FoodData  implements Serializable 
{
  private static final long serialVersionUID = Constants.VERSION;

  public FoodType foodType;
  public int gridX, gridY;
  protected int count;
  
  public FoodData(FoodType foodType, int x, int y, int count)
  {
    this.foodType = foodType;
    this.gridX = x;
    this.gridY = y;
    this.count = count;
  }
  public int getCount() {return count;}
}