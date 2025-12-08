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
 * For a corridor or corner, I need to find the direction of a nonwall which is not from the direction the robot just came from so I changed the amount i iterate through and started so that it wouldn't check behind.
 * For a junction and crossroads, I want to check the number of passages and choose randomly from them and if there are none I chose randomly from the nonwalls.
 * To choose them randomly, I added the directions to an array.
 * 
 * 
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Explorer
{
    public List<Integer> getPrioritisedDirections(IRobot robot) {
        List<Integer> prioritisedDirections = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            if (robot.look(IRobot.AHEAD + i) == IRobot.PASSAGE) {  
                prioritisedDirections.add(IRobot.AHEAD + i);
            }
        }
        
        return prioritisedDirections;
    }

    public List<Integer> getPossibleDirections(IRobot robot) {
        List<Integer> possibleDirections = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            if (robot.look(IRobot.AHEAD + i) != IRobot.WALL) {
                possibleDirections.add(IRobot.AHEAD + i);
            }
        }
        return possibleDirections;
    }

    public int nonwallExits(IRobot robot) {
        List<Integer> possibleDirections = getPossibleDirections(robot);
        int nonwalls = possibleDirections.size();
        return nonwalls;
    }

    public int passageExits(IRobot robot) {
        List<Integer> prioritisedDirections = getPrioritisedDirections(robot);
        int passages = prioritisedDirections.size();
        return passages;
    }

    public int chooseRandDir(List<Integer> directions) {
		int randno;
		int direction;

		// Select a random integer corresponding to each element in the array
		randno = (int) Math.floor(Math.random()*directions.size());

		// Choose the associated direction
        direction = directions.get(randno);
		return direction;
    }

    public int deadEnd(IRobot robot) {
        int direction = IRobot.AHEAD;
        System.out.println("Dead end");
        direction = getPossibleDirections(robot).get(0);
        return direction;
    }

    public int corridor(IRobot robot) {
        int direction = IRobot.AHEAD;
        List<Integer> possibleDirections = getPossibleDirections(robot);
        possibleDirections.remove(Integer.valueOf(IRobot.BEHIND));
        direction = possibleDirections.get(0);
        System.out.println("Corridor");
        return direction;
    }

    public int junction(IRobot robot) {
        int direction = IRobot.AHEAD;
        List<Integer> possibleDirections = getPossibleDirections(robot);
        possibleDirections.remove(Integer.valueOf(IRobot.BEHIND));
        List<Integer> prioritisedDirections = getPrioritisedDirections(robot);
        System.out.println("Junction");    
        
        if (prioritisedDirections.size() != 0) {
            direction = chooseRandDir(prioritisedDirections);
        }
        else if (possibleDirections.size() != 0) {
            direction = chooseRandDir(possibleDirections);
        }

        return direction;
    }

    public int crossroads(IRobot robot) {
        int direction = IRobot.AHEAD;
        List<Integer> possibleDirections = getPossibleDirections(robot);
        possibleDirections.remove(Integer.valueOf(IRobot.BEHIND));
        List<Integer> prioritisedDirections = getPrioritisedDirections(robot);
        System.out.println("Crossroads");
        
        if (prioritisedDirections.size() != 0) {
            direction = chooseRandDir(prioritisedDirections);
        }
        else if (possibleDirections.size() != 0) {
            direction = chooseRandDir(possibleDirections);
        }
        
        return direction;
    }


    public void controlRobot(IRobot robot) {
        int exits;
        int direction = IRobot.AHEAD;

        exits = nonwallExits(robot);
        System.out.println(exits);

        if (exits == 1)
            direction = deadEnd(robot);
        else if (exits == 2)
            direction = corridor(robot);
        else if (exits == 3)
            direction = junction(robot);
        else if (exits == 4)
            direction = crossroads(robot);
        else
            System.out.println("Case not found");

        robot.face(direction);

    }
    
}
