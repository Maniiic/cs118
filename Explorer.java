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
    public int nonwallExits(IRobot robot) {
        int nonwalls = 0;
        for (int i = 0; i < 4; i++) {
			if (robot.look(IRobot.AHEAD + i) != IRobot.WALL) {
                nonwalls++;
            }
        }
        return nonwalls;
    }

    public int deadEnd(IRobot robot) {
        int direction;
        System.out.println("Dead end");
        for (int i = 0; i < 4; i++) {
			if (robot.look(IRobot.AHEAD + i) != IRobot.WALL) {
                direction = IRobot.AHEAD + i
            }
        }

        return direction;
    }

    public int corridor(IRobot robot) {
        int direction = IRobot.AHEAD;
        System.out.println("Corridor");
        for (int i = 0; i < 3; i++) { // i < 3 to avoid choosing backwards direction
			if (robot.look(IRobot.LEFT + i) != IRobot.WALL)
                direction = IRobot.LEFT + i;
        }
        return direction;
    }

    public int junction(IRobot robot) {
        int direction;
        List<Integer> possibleDirections = new ArrayList<>();
        List<Integer> prioritisedDirections = new ArrayList<>();
        System.out.println("Junction");    
        for (int i = 0; i < 4; i++) {
			if (robot.look(IRobot.AHEAD + i) == IRobot.PASSAGE) {  
                prioritisedDirections.add(IRobot.AHEAD + i);
            }
            else if (robot.look(IRobot.AHEAD + i) != IRobot.WALL) {
                possibleDirections.add(IRobot.AHEAD + i);
            }
        }

        return direction;
    }

    public int crossroads(IRobot robot) {
        int direction;
        System.out.println("Crossroads");
        return direction;
    }

    public int genRandomDirection() {
		int randno;
		int direction;

		// Select a random number 0-3
		randno = (int) Math.floor(Math.random()*3);

		// Convert this to a direction
        direction = IRobot.AHEAD + randno;
		return direction;
    }

    public void controlRobot(IRobot robot) {
        int exits;
        int direction;

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
    }



}
