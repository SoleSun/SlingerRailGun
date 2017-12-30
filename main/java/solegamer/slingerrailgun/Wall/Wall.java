package solegamer.slingerrailgun.Wall;

import android.graphics.RectF;

import solegamer.slingerrailgun.Coordinate;
import solegamer.slingerrailgun.MovementPatterns.Inanimate;
import solegamer.slingerrailgun.MovementPatterns.MovementPattern;
import solegamer.slingerrailgun.MovementPatterns.Slider;
import solegamer.slingerrailgun.ObjectT;

/**
 * Created by Joel on 4/23/2017.
 *
 * Barriers or extra bounds off which the player can bounce.
 * anchorInanimate and do not move at all
 */

public class Wall implements ObjectT {

    private final int screenWidth;

    private RectF wallSpriteBoundary = new RectF();

    private float x, y;

    /* Screen to sprite ratio */
    private static int ratio = 10;

    /* Dimensions of the sprite object */
    private int spriteLength, spriteHeight;

    /* How the wall will move to obstruct the player */
    private MovementPattern movementPattern = new Inanimate();

    /**
     * Constructur with the coordinate well defined
     * @param screen_width horizontal dimension of the Android screen in Portrait mode
     * @param location coordinate of the center of the sprite boundary
     */
    public Wall (int screen_width, Coordinate location){
        screenWidth = screen_width;

        spriteLength = spriteHeight = (screen_width/ratio);

        updatePosition(location);
    }

    /**
     * If no x-coordinate is provided, then the Wall will locate itself along the left side of the
     * screen
     * @param screen_width the horizontal dimension of the Android screen in Portrait mode
     * @param centerY y-coordinate of the center of the sprite boundary
     */
    public Wall (int screen_width, float centerY){
        screenWidth = screen_width;

        spriteLength = spriteHeight = (screen_width/ratio);

        updatePosition( spriteLength/2, centerY);
    }

    /**
     * The camera of the game will always follow the player. Therefore, we require all
     * Objects to scroll relative to the player's position
     *
     * @param fps the frames per second at a given instant
     * @param distance how much to scroll the ObjectT relative to the player
     */
    public void updateScroll (long fps, float distance) {
        Coordinate currPosition = new Coordinate(x, y + distance);
        currPosition = movementPattern.updateMovementPattern(fps, currPosition);
        this.updatePosition(currPosition);
    }

    /**
     * Change the movement pattern of the wall to a horizontally sliding one
     */
    public void setToSlider () {
        movementPattern = new Slider(screenWidth, new Coordinate(spriteLength, spriteHeight)); }

    /**
     * @return the dimensions of the Wall's sprite, where the x is the length and y is the height
     */
    public int getLength() { return spriteLength; }

    /**
     * @return the RectF object that serves as the boundary of the Object's sprite
     */
    public RectF getSpriteBoundary () { return wallSpriteBoundary; }

    /**
     * @return the point of the center of the Object's sprite
     */
    public Coordinate getCoordinate () { return new Coordinate (x, y); }

    /**
     * Automatically updates both the Wall and its sprite's positions
     * @param pt the new position of the center of the Anchor's sprite boundary
     */
    private void updatePosition (Coordinate pt){
        this.x = pt.x;
        this.y = pt.y;

        wallSpriteBoundary.left = this.x - spriteLength/2;
        wallSpriteBoundary.right = this.x + spriteLength/2;
        wallSpriteBoundary.top = this.y - spriteHeight/2;
        wallSpriteBoundary.bottom = this.y + spriteHeight/2;
    }

    /**
     * Automatically updates both the Wall and its sprite's positions
     * @param centerX must be greater than or equal to zero and less than or equal to boundX
     * @param centerY must be greater than or equal to zero and less than or equal to boundY
     */
    private void updatePosition (float centerX, float centerY){
        this.x = centerX;
        this.y = centerY;

        wallSpriteBoundary.left = this.x - spriteLength/2;
        wallSpriteBoundary.right = this.x + spriteLength/2;
        wallSpriteBoundary.top = this.y - spriteHeight/2;
        wallSpriteBoundary.bottom = this.y + spriteHeight/2;
    }

    public static int getMaxNoOfWallsThatCanFitScreen () { return ratio; }
}
