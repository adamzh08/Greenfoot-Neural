import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class MyWorld here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MyWorld extends World
{

    public static int amount_of_ants = 15;

    public static int screenWidth = 1200;
    public static int screenHeight = 800;

    public static long time = 0;

    public static void time_avarage()
    {
        time = time / 10;
        System.out.println("Time: " + time);
    }

    /**
     * Constructor for objects of class MyWorld.
     * 
     */
    public MyWorld()
    {    
        // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
        super(screenWidth, screenHeight, 1); 
        prepare();
    }
    /**
     * Prepare the world for the start of the program.
     * That is: create the initial objects and add them to the world.
     */
    private void prepare()
    {    
        Ant[] ants_array = new Ant[amount_of_ants*amount_of_ants];
        
        for(int i = 0; i < amount_of_ants*amount_of_ants; i++)
        {
            ants_array[i] = new Ant();
            addObject(ants_array[i], (int)((screenWidth/amount_of_ants)*(int)(i/amount_of_ants)), (int)((screenHeight/amount_of_ants)*(i%amount_of_ants)));
            //addObject(ants_array[i], 10, 10);
        }
    }
}
