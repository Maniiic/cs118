/*
 * File:    Broken	.java
 * Created: 7 September 2001
 * Author:  Stephen Jarvis
 */

/* 
 * Ex2 Preamble
 * I re-implemented the solution making use of a stack storing the heading as it arrived so that I could just store the potentially relevent information until the junction gets fully explored where it is no longer need and therefore removed from the stack.
 * The LIFO structure suits the design of a depth first search as you return to the latest junction.
 * This also saves space as the x, y coordinates are not necessary, only the heading is needed to be able to backtrack in the correct direction.
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

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

    // For when the robot is at a junction or crossroad
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
            robotData.pushHeading(robot.getHeading());
            
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
                previousHeading = robotData.popHeading();
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
            if (exits == 1) {
                direction = deadEnd(robot);
            }
            else {
                direction = corridor(robot);
            }
            robot.face(direction);
        }
    }

    // Reset the counter for junction and set to exploring
    public void reset() {
        robotData.resetStack();
        explorerMode = 1;
    }
}

class RobotData 
{
    Stack<Integer> headingStack;
    
    // Create an empty stack
    public RobotData() {
        headingStack = new Stack<>();
    }

    // Add heading onto stack
    public void pushHeading(int heading) {
        headingStack.push(heading);
        System.out.println("Pushing " + headingToString(heading));
    }
    
    // Removes and returns top of the stack
    public int popHeading() {
        int heading;
        if (!(isEmpty())) {
            heading = headingStack.pop();
            System.out.println("Poping " + headingToString(heading));
            return heading;
        }
        System.out.println("Nothing to pop");
        return -1;
    }

    public boolean isEmpty() {
        if (headingStack.isEmpty()) {
            System.out.println("Empty stack");
            return true;
        }
        return false;
    }

    // Set number of junctions to 0
    public void resetStack() {
        headingStack.clear();
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