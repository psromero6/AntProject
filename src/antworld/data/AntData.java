package antworld.data;

import java.io.Serializable;
import antworld.data.AntAction.AntActionType;

// TODO: Auto-generated Javadoc
/**
 *!!!!!!!!!! DO NOT MODIFY ANYTHING IN THIS CLASS !!!!!!!!!!<br>
 * This class is serialized across a network socket. Any modifications will
 * prevent the server from being able to read this class.<br><br>
 * 
 * 
 * AntData contains all data about an ant agent that is exchanged between client and server.
 * 
 */

public class AntData implements Comparable<AntData>, Serializable
{
  private static final long serialVersionUID = Constants.VERSION;

  /** The nest name. */
  public final NestNameEnum nestName;
  
  /** The team name. */
  public final TeamNameEnum teamName;

  /** The id. */
  public int id = Constants.UNKNOWN_ANT_ID; //Use whenever you birth an ant

  /** The grid y. */
  public int gridX, gridY;
  
  /** The alive. */
  public boolean alive = true;

  /** The ant type. */
  public AntType antType;
  
  /** The carry type. */
  public FoodType carryType = null;
  
  /** The carry units. */
  public int carryUnits = 0;

  /** The my action. */
  public AntAction myAction;

  /** The ticks until next action. */
  public int ticksUntilNextAction = 0;

  /** The health. */
  public int health;

  /** The underground. */
  public boolean underground = true;

  /**
   * Instantiates a new ant data.
   *
   * @param id the id
   * @param type the type
   * @param nestName the nest name
   * @param teamName the team name
   */
  public AntData(int id, AntType type, NestNameEnum nestName, TeamNameEnum teamName)
  {
    this.id = id;
    antType = type;
    this.nestName = nestName;
    this.teamName = teamName;
    health = type.getMaxHealth();
    myAction = new AntAction(AntActionType.BIRTH);
  }
  
  
  /**
   * Instantiates a new ant data.
   *
   * @param source the source
   */
  public AntData(AntData source)
  {
    id = source.id;
    nestName = source.nestName;
    teamName = source.teamName;
    
    gridX = source.gridX;
    gridY = source.gridY;
    alive = source.alive;

    antType = source.antType;
    carryType = source.carryType;
    carryUnits = source.carryUnits;

    myAction = new AntAction(source.myAction);

    ticksUntilNextAction = source.ticksUntilNextAction;

    health = source.health;

    underground = source.underground;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    String out = "AntData: [id=" + id + ", nest=" + nestName + ", team=" + teamName + ", " + antType + ", health="
        + health + ", " + myAction;
    if (carryUnits > 0) out += ", carry: [" + carryType + ", " + carryUnits + "]";
    if (underground) out += ", underground ]";
    else out += ", x=" + gridX + ", y=" + gridY + "]";

    return out;
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(AntData otherAnt)
  {
    return id - otherAnt.id;
  }

}
