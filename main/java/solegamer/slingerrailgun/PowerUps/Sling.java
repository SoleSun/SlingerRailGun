package solegamer.slingerrailgun.PowerUps;

import android.graphics.RectF;

import solegamer.slingerrailgun.Coordinate;
import solegamer.slingerrailgun.MovementPatterns.Inanimate;
import solegamer.slingerrailgun.MovementPatterns.MovementPattern;
import solegamer.slingerrailgun.Util;

/**
 * Created by Joel on 5/1/2017.
 *
 * Enables the player to deploy a rappelling hook that can latch onto an anchor
 * and save the player from death
 */

public class Sling implements Item {

    /* Coordinates of the Explosive Item*/
    private float centerX, centerY;

    /* The spatial boundaries of the explosive sprite*/
    private RectF explosiveSpriteBoundary = new RectF();

    /* The dimensions of the sprite boundary */
    private final int length, height;

    /* The screen to sprite ratio */
    private final int ratio = 14;

    private MovementPattern movementPattern = new Inanimate();

    public Sling (int screen_width, float centerY) {
        this.centerY = centerY;
        centerX = Util.getRandomNum(0.2, 0.8, screen_width);

        length = height = screen_width / ratio;

        this.updatePosition(centerX, this.centerY);
    }

    /**
     * @return the RectF object that serves as the boundary of the Object's sprite
     */
    public RectF getSpriteBoundary (){
        return new RectF (explosiveSpriteBoundary);
    }

    /**
     * @return the point of the center of the Object's sprite
     */
    public Coordinate getCoordinate (){
        return new Coordinate (centerX, centerY);
    }

    /**
     * The camera of the game will always follow the player. Therefore, we require all
     * Objects to scroll relative to the player's position
     *
     * @param fps the frames per second at a given instant
     * @param distance how much to scroll the ObjectT relative to the player
     */
    public void updateScroll (long fps, float distance){
        Coordinate currPosition = new Coordinate(centerX, centerY + distance);
        currPosition = movementPattern.updateMovementPattern(fps, currPosition);
        this.updatePosition(currPosition);
    }

    /**
     * Automatically updates both the Anchor and its sprite's positions
     * @param centerX must be greater than or equal to zero and less than or equal to boundX
     * @param centerY must be greater than or equal to zero and less than or equal to boundY
     */
    private void updatePosition (float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;

        explosiveSpriteBoundary.left = this.centerX - length/2;
        explosiveSpriteBoundary.right = this.centerX + length/2;
        explosiveSpriteBoundary.top = this.centerY - height/2;
        explosiveSpriteBoundary.bottom = this.centerY + height/2;
    }

    /**
     * Automatically updates both the Anchor and its sprite's positions
     * @param pt the new position of the center of the Anchor's sprite boundary
     */
    private void updatePosition (Coordinate pt){
        this.centerX = pt.x;
        this.centerY = pt.y;

        explosiveSpriteBoundary.left = this.centerX - length/2;
        explosiveSpriteBoundary.right = this.centerX + length/2;
        explosiveSpriteBoundary.top = this.centerY - height/2;
        explosiveSpriteBoundary.bottom = this.centerY + height/2;
    }
}
