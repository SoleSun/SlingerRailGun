package solegamer.slingerrailgun.Anchor;

import solegamer.slingerrailgun.Coordinate;
import solegamer.slingerrailgun.Util;

/**
 * Created by Joel on 4/20/2017.
 *
 * Falls if the Anchor has been attached to because it
 * cannot support both the weight of the player and itself
 */

public class BumbleBee implements AnchorMovementPattern {

    /* The dimensions of the screen */
    private final int screenWidth, screenLength;

    /* Dimensions of the context class' sprite */
    private Coordinate ctxSpriteDimensions;

    /* Should the bumblebee begin falling? */
    private boolean tooHeavy = false;

    /* If so, at what rate? */
    private float yVelocity = Util.getRandomNum(0.8, 0.9, 150);

    /**
     *
     * @param screenWidth width of the Android screen
     * @param screenLength length of the Android screen
     * @param spriteBoundary two coordinate point that indicates the dimensions of the spriteBoundary,
     *                       where x is the length and y is the height
     */
    public BumbleBee (int screenWidth, int screenLength, Coordinate spriteBoundary){
        this.screenLength = screenLength;
        this.screenWidth = screenWidth;
        ctxSpriteDimensions = spriteBoundary;
    }

    /**
     * Given both the frames per second and the current position of the anchor,
     * the updateMovementPattern will calculate where the Anchor object is now located
     *
     * @param fps the frames per second at a given instant
     * @param currentPosition point of the center of the RectF sprite boundary that contains
     *                        the Anchor object
     * @return new point coordinate to where the center of the Anchor should be
     */
    public Coordinate updateMovementPattern (long fps, Coordinate currentPosition){
        return !tooHeavy ?
                currentPosition :
                new Coordinate (currentPosition.x, currentPosition.y + yVelocity/fps);
    }

    /**
     * Given the screen dimensions in the form of a Point object and the y-position of the
     * center of the Anchor's center point, the AnchorMovementPattern state will dictate where the
     * initial coordinates of the Anchor should be
     *
     * @param centerY y-position of the center of the Anchor's center point
     *
     * @return initial coordinate of the center of the Anchor's sprite boundary
     */
    public Coordinate initCoordinate (float centerY){
        return new Coordinate (Util.getRandomNum(0.2, 0.8, screenWidth), (int) centerY);
    }

    /**
     * @return name of movement pattern
     */
    public movementPatternName getMovementPatternName (){
        return movementPatternName.Bumblebee;
    }

    void setTooHeavy () {
        tooHeavy = true;
    }

    void setFree () {
        tooHeavy = false;
    }
}
