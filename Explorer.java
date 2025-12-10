/*
 * File:    Broken	.java
 * Created: 7 September 2001
 * Author:  Stephen Jarvis
 */

/* 
 * Ex1 Preamble
 *
 * Both the nonwallExits and passageExits methods are handled similarly as I am iterating through each direction and checking if that direction corresponds to anything other than a wall for the nonwallExits and only a passage for the passageExits.
 * For each instance of nonwall/passage the exits variable that gets returned is incremented by 1.
 * Depending on the number of nonwalls around the robot, the corresponding method is called.
 * For a dead end, I was tempted to make it just go ahead if there isn't a wall and reverse if there is but for the start case the passage might not be behind the robot so I chose to iterate through the directions to find the only nonwall direction and go that way.
 * For a corridor or corner, I need to find the direction of a nonwall which is not from the direction the robot just came from so I checked the possible directions and removed the direction it just came from which wouldn't work if the robot could start at a corridor or corner.
 * For a junction and crossroads, the code is the same as I want to check the number of passages and choose randomly from them and if there are none I chose randomly from the nonwalls.
 * To choose them randomly, I added the directions to an array and choose the direction based of a random index.
 * The program could potentially made more efficient by combining the methods that count the number of nonwalls, passages and been before squares as the same squares are being checked.
 * The same is probably true for the prioritised and possible directions as they are achieving very similar goals but seperating the functions makes it more modular.
 * 
 * For the robot data class I created an array composed of objects that record the x, y and the heading the robot arrived from.
 * The robot data class is also responsible for getting the header from given x, y coordinates and is able to add newly visited junctions to the array.
 * This is very useful when backtracking as the most recent header is needed to be able to go back the opposite way it entered from once all paths had been explored.
 * 
 * The explorer will always find the target assuming the maze is non-loopy.
 * In the worse case scenarion each empty square can be traversed twice so that would be the upper bound for the number of steps it will take.
 * 
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Explorer
{
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private int explorerMode = 1; // 1 = explore, 0 = backtrack

    // Return the directions that are not walls and have not been to before
    public List<Integer> getPrioritisedDirections(IRobot robot) {
        List<Integer> prioritisedDirections = new ArrayList<>();
        // Iterate through the 4 directions
        for (int i = 0; i < 4; i++) {
            if (robot.look(IRobot.AHEAD + i) == IRobot.PASSAGE) {  
                prioritisedDirections.add(IRobot.AHEAD + i);
            }
        }
        
        return prioritisedDirections;
    }

    // Return the directions that are not walls
    public List<Integer> getPossibleDirections(IRobot robot) {
        List<Integer> possibleDirections = new ArrayList<>();
        // Iterate through the 4 directions
        for (int i = 0; i < 4; i++) {
            if (robot.look(IRobot.AHEAD + i) != IRobot.WALL) {
                possibleDirections.add(IRobot.AHEAD + i);
            }
        }
        return possibleDirections;
    }

    // Returns the number of non walls around current square
    public int nonwallExits(IRobot robot) {
        List<Integer> possibleDirections = getPossibleDirections(robot);
        int nonwalls = possibleDirections.size();
        return nonwalls;
    }

    // Returns the number of passages around current square
    public int passageExits(IRobot robot) {
        List<Integer> prioritisedDirections = getPrioritisedDirections(robot);
        int passages = prioritisedDirections.size();
        return passages;
    }

    // Returns the number of squares that have been previously explored
    public int beenbeforeExits(IRobot robot) {
        int beenbefores = 0;
        for (int i = 0; i < 4; i++) {
            if (robot.look(IRobot.AHEAD + i) == IRobot.BEENBEFORE) {
                beenbefores++;
            }
        }
        return beenbefores;
    }

    // Chooses a random direction from a given list
    public int chooseRandDir(List<Integer> directions) {
		int randno;
		int direction;

		// Select a random integer corresponding to each element in the array
		randno = (int) Math.floor(Math.random()*directions.size());

		// Choose the associated direction
        direction = directions.get(randno);
		return direction;
    }

    // For when the robot is at a dead end
    public int deadEnd(IRobot robot) {
        int direction = IRobot.AHEAD;
        // Check if dead end is not the starting one
        if (beenbeforeExits(robot) == 1) {
            explorerMode = 0;            
        }
        // Get the only possible direction
        direction = getPossibleDirections(robot).get(0);
        return direction;
    }

    // For when the robot is at a corridor or a corner
    public int corridor(IRobot robot) {
        int direction = IRobot.AHEAD;
        List<Integer> possibleDirections = getPossibleDirections(robot);
        // Choose the direction that the robot did not just come from
        possibleDirections.remove(Integer.valueOf(IRobot.BEHIND));
        direction = possibleDirections.get(0);
        return direction;
    }

    // For when the robot is at a corridor or a corner
    public int junctionOrCrossroad(IRobot robot) {
        int direction = IRobot.AHEAD;
        List<Integer> possibleDirections = getPossibleDirections(robot);
        // Don't let the robot backtrack as it needs to explore new squares
        possibleDirections.remove(Integer.valueOf(IRobot.BEHIND));
        List<Integer> prioritisedDirections = getPrioritisedDirections(robot); 
        
        // If there is are unexplored directions choose one of them randomly
        if (passageExits(robot) != 0) {
            direction = chooseRandDir(prioritisedDirections);
        }
        // If there aren't then choose one of the possible directions
        else {
            direction = chooseRandDir(possibleDirections);
        }

        // Check if junction is new
        if (beenbeforeExits(robot) <= 1) {
            robotData.recordJuntion(robot.getLocation().x, robot.getLocation().y, robot.getHeading());
            robotData.printJunction();
        }
        
        return direction;
    }

    public void controlRobot(IRobot robot) {
        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)) {
            robotData = new RobotData(); // Reset the data store
            explorerMode = 1;
        }
        pollRun++; // Increment pollRun so that the data is not reset each time the robot moves

        // Decide whether to backtrack or explore
        if (explorerMode == 1) { 
            exploreControl(robot);
        }
        else {
            backtrackControl(robot);
        }
    }

    // Decide what method to call based off surrounding exits
    public void exploreControl(IRobot robot) { 
        int exits = nonwallExits(robot);
        int direction = IRobot.AHEAD;
        if (exits == 1)
            direction = deadEnd(robot);
        else if (exits == 2)
            direction = corridor(robot);
        else
            direction = junctionOrCrossroad(robot);
        robot.face(direction);
    }

    // Backtrack to previous junction after a dead end
    public void backtrackControl(IRobot robot) {
        int exits = nonwallExits(robot);
        int direction = IRobot.AHEAD;
        int previousHeading;
        int newHeading;
        System.out.println("Backtracking");

        // Check if its a junction
        if (exits > 2) {
            if (passageExits(robot) != 0) {
                explorerMode = 1;
                direction = chooseRandDir(getPrioritisedDirections(robot));
                robot.face(direction);
                System.out.println("Exits > 2, junction");
            }
            else {
                previousHeading = robotData.searchJunction(robot.getLocation().x, robot.getLocation().y);
                // A shift by 2 gets opposite heading
                if (previousHeading <= IRobot.EAST) {
                    newHeading = previousHeading + 2;
                }
                else {
                    newHeading = previousHeading - 2;
                }
                robot.setHeading(newHeading);
                System.out.println("Exits <= 2, deadend, corridor");
            }
        }
        // If its not then continue down path
        else {
            if (exits == 1)
                direction = deadEnd(robot);
            else
                direction = corridor(robot);
            robot.face(direction);
        }
    }

    // Reset the counter for junction and set to exploring
    public void reset() {
        robotData.resetJunctionCounter();
        explorerMode = 1;
    }
}

class RobotData 
{
    private static int maxJunctions = 10000; // Max number likely to occur
    private static int junctionCounter; // No. of junctions stored
    private JunctionRecorder[] junctions; // Array of junctions
    
    // Create array and set counter to 0
    public RobotData() {
        junctions = new JunctionRecorder[maxJunctions];
        junctionCounter = 0;
    }

    // Find junction header corresponding with juncX and juncY
    public int searchJunction(int juncX, int juncY) {
        int heading = -1;
        JunctionRecorder checkJunciton;
        // Iterate through the junctions and check if it is the previous one
        for (int i = 0; i < junctionCounter; i++) {
            checkJunciton = junctions[i];
            if (checkJunciton.getJuncX() == juncX && checkJunciton.getJuncY() == juncY) {
                heading = checkJunciton.getArrived();
            }
        }
        return heading;
    }

    // Add junction to end of array
    public void recordJuntion(int juncX, int juncY, int arrived) {
        junctions[junctionCounter] = new JunctionRecorder(juncX, juncY, arrived);
        junctionCounter++;
    }
    
    // Output info about latest junction
    public void printJunction() { 
        JunctionRecorder previousJunction = junctions[junctionCounter - 1];
        System.out.println("Junction " + junctionCounter + " (x=" + previousJunction.getJuncX() + ",y=" + previousJunction.getJuncY() + ") heading " + headingToString(previousJunction.getArrived()) + previousJunction.getArrived());
    }

    // Set number of junctions to 0
    public void resetJunctionCounter() {
        junctionCounter = 0;
    }

    // Get the string corresponding to a heading
    public String headingToString(int heading) {
        String headingAsString = "-1";
        switch (heading) {
            case (IRobot.NORTH):
                headingAsString = "NORTH";
                break;
            case (IRobot.EAST):
                headingAsString = "EAST";
                break;
            case (IRobot.SOUTH):
                headingAsString = "SOUTH";
                break;
            case (IRobot.WEST):
                headingAsString = "WEST";
                break;
        }
        return headingAsString;
    }
}

class JunctionRecorder {
    private int juncX;
    private int juncY;
    private int arrived;

    // Record x,y and heading arrived from
    public JunctionRecorder(int juncX, int juncY, int arrived) {
        this.juncX = juncX;
        this.juncY = juncY;
        this.arrived = arrived;
    }

    // Getters
    public int getJuncX() {
        return juncX;
    }
    public int getJuncY() {
        return juncY;
    }
    public int getArrived() {
        return arrived;
    }
}