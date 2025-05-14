import java.util.*;
import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class MyWorld here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class MyWorld extends World {
    public static int screenWidth = 1200;
    public static int screenHeight = 800;

    public static long time = 0;
    
    private List<Ant> antPopulation;

    /**
     * Constructor for objects of class MyWorld.
     *
     */
    public MyWorld() {
        // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
        super(screenWidth, screenHeight, 1);
        
        antPopulation = new ArrayList<>();

        antPopulation.add(new Ant());
        antPopulation.forEach(ant -> addObject(ant, screenWidth/2, screenHeight/2));
    }
    
    @Override
    public void act() {
        antPopulation.forEach(Ant::act);
        
        
    }
}
