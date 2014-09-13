/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package antworld.client;

/**
 *
 * @author 363620
 */
public class OrderedPair
{
    public int row, col;
    public OrderedPair(int row, int col)
    {
        this.row=row;
        this.col=col;
    }
    
    public int getX()
    {
        return col;
    }
    
    public int getY()
    {
        return row;
    }
            
    
}
