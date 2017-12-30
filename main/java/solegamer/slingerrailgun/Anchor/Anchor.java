package solegamer.slingerrailgun.Anchor;

/**
 * Created by Joel on 2/20/2017.
 *
 * anchorInanimate and static attachable that simply lets the
 * player anchor onto the sprite before launching to the next
 * destination
 *
 * The logic of spawning Anchor objects is left entirely to the static class
 */

import android.graphics.RectF;

import solegamer.slingerrailgun.Coordinate;
import solegamer.slingerrailgun.ObjectT;
import solegamer.slingerrailgun.Util;

public class Anchor implements ObjectT{

    /* Keep track of the bounds of the screen that the anchor can operate in */
    private int screen_width, screen_length;

    /* Represent the dimensions of the anchor sprite*/
    private int length, height;

    /* Represents the point of the center of the anhor sprite */
    private float centerX, centerY;

    /* Android screen to sprite size ratio*/
    private int ratio = 13;

    /* Unique ID */
    private long tag;

    /* Spatial boundary of Anchor's sprite */
    private RectF anchorSpriteBoundary = new RectF();

    /* Movement pattern that dictates how the Anchor behaves */
    private AnchorMovementPattern anchorMovementPattern;

    /* Alerted that something has attached to this Anchor instance */
    private boolean isAttachedToPlayer = false;

    /**
     * Receive an instance of the Anchor class
     *
     * @param screenX the width of the Android screen
     * @param screenY the length of the Android screen
     * @param centerY the initial centerY-position of the central point of the Anchor sprite
     */
    public Anchor (int screenX, int screenY, float centerY, long ID) {
        /* Initialize the dimension trackers*/
        screen_width = screenX;
        screen_length = screenY;

        /* Unique identifier that distinguishes one instance from another */
        tag = ID;

        /* Initialize the dimensions of the sprite*/
        height = length = screen_width / ratio;

        /* Initialize the height of the centerY-position */
        this.centerY = centerY;
        centerX = Util.getRandomNum(0.2, 0.8, screen_width);

        /* The default movement pattern is inanimate Delegate the movement patterns to the mediator*/
        this.setToInanimateAnchor();
        }

    /**
     * * Updates the anchor's position relative to the atom object
     *
     * @param fps the frames per second at a given instant
     * @param scrollDistance the distance that the object is expected to move in a single frame; i.e.
     *                       must give the (total distance / fps)
     */
    public void updateScroll (long fps, float scrollDistance){
        Coordinate currPosition = new Coordinate (centerX, centerY + scrollDistance);
        currPosition = anchorMovementPattern.updateMovementPattern(fps, currPosition);

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

        anchorSpriteBoundary.left = this.centerX - length/2;
        anchorSpriteBoundary.right = this.centerX + length/2;
        anchorSpriteBoundary.top = this.centerY - height/2;
        anchorSpriteBoundary.bottom = this.centerY + height/2;
    }

    /**
     * Automatically updates both the Anchor and its sprite's positions
     * @param pt the new position of the center of the Anchor's sprite boundary
     */
    private void updatePosition (Coordinate pt){
        this.centerX = pt.x;
        this.centerY = pt.y;

        anchorSpriteBoundary.left = this.centerX - length/2;
        anchorSpriteBoundary.right = this.centerX + length/2;
        anchorSpriteBoundary.top = this.centerY - height/2;
        anchorSpriteBoundary.bottom = this.centerY + height/2;
    }

    /**
     * Sets the Anchor to a defined position and movement pattern, as is required of the initAnchor
     */
    public void setToInanimateInitAnchor () {
        this.updatePosition(screen_width/2 , (float) (0.6 * screen_length));
        anchorMovementPattern =
            new anchorInanimate(screen_width, screen_length, new Coordinate (length, height));
    }

    public void setToInanimateAnchor () {
        Coordinate initPosition = new Coordinate(centerX, centerY);
        anchorMovementPattern = new anchorInanimate(screen_width, screen_length, new Coordinate (length, height));

        this.updatePosition(initPosition.x, initPosition.y);
    }

    public void setToLoopAnchor () {
        anchorMovementPattern = new Loop(screen_width, screen_length, new Coordinate (length, height));
        Coordinate initPosition = anchorMovementPattern.initCoordinate(this.centerY);

        this.updatePosition(initPosition.x, initPosition.y);
    }

    public void setToSliderAnchor () {
        anchorMovementPattern = new anchorSlider(screen_width, screen_length, new Coordinate (length, height));
        Coordinate initPosition = anchorMovementPattern.initCoordinate(this.centerY);

        this.updatePosition(initPosition.x, initPosition.y);
    }

    public void setToBumbleBeeAnchor () {
        anchorMovementPattern = new BumbleBee(screen_width, screen_length, new Coordinate(length, height));
        Coordinate initPosition = anchorMovementPattern.initCoordinate(this.centerY);

        this.updatePosition(initPosition.x, initPosition.y);
    }

    /* Return the coordinate of the center of the Anchor */
    public float getX () { return  centerX;}
    public float getY () { return  centerY;}
    public Coordinate getCoordinate () {return new Coordinate ( centerX, centerY);}

    public RectF getSpriteBoundary() { return new RectF( anchorSpriteBoundary ); }

    public long getTag () { return tag; }

    public void playerLatchedOn () {
        isAttachedToPlayer = true;

        if (anchorMovementPattern instanceof BumbleBee){
            ((BumbleBee) anchorMovementPattern).setTooHeavy();
        }
    }

    public void playerReleased () {
        isAttachedToPlayer = false;

        if (anchorMovementPattern instanceof  BumbleBee){
            ((BumbleBee) anchorMovementPattern).setFree();
        }
    }

    public AnchorMovementPattern.movementPatternName getMovementPatternType () {
        return anchorMovementPattern.getMovementPatternName();
    }

    @Override
    public boolean equals(Object object){
        if (object == null) return false;

        if (object instanceof  Anchor){
            Anchor that = (Anchor) object;
            //return (this.getXCenter() == that.getXCenter() && this.getYCenter() == that.getYCenter());
            return (this.tag == that.getTag());
        }
        return false;
    }
}
