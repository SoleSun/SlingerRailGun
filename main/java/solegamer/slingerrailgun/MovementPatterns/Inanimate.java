package solegamer.slingerrailgun.MovementPatterns;

import solegamer.slingerrailgun.Anchor.AnchorMovementPattern;
import solegamer.slingerrailgun.Coordinate;
import solegamer.slingerrailgun.Util;

/**
 * Created by Joel on 4/14/2017.
 */

public class Inanimate implements MovementPattern {

    /* The dimensions of the screen */
    private int screenWidth, screenLength;

    /* Dimensions of the context class' sprite */
    private Coordinate ctxSpriteDimensions;

    public Inanimate () {
    }

    /**
     *
     * @param screenWidth width of the Android screen
     * @param screenLength length of the Android screen
     * @param spriteBoundary two coordinate point that indicates the dimensions of the spriteBoundary,
     *                       where x is the length and y is the height
     */
    public Inanimate(int screenWidth, int screenLength, Coordinate spriteBoundary){
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
        return currentPosition;
    }

    /**
     *@return name of movement pattern
     */
    public movementPatternName getMovementPatternName (){
        return movementPatternName.Inanimate;
    }
}
