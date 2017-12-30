package solegamer.slingerrailgun.MovementPatterns;

import solegamer.slingerrailgun.Anchor.AnchorMovementPattern;
import solegamer.slingerrailgun.Coordinate;
import solegamer.slingerrailgun.Util;

/**
 * Created by Joel on 4/4/2017.
 *
 * Anchor Movement Pattern that moves the anchor in horizontal slides
 */

public class Slider implements MovementPattern {

    /* The dimensions of the screen */
    private int screenWidth, screenLength;

    /* Dimensions of the context class' sprite */
    private Coordinate ctxSpriteDimensions;

    /* The rate at which the sliding moves along its horizontal line */
    private int xVelocity = Util.getRandomNum(0.8, 1, 350);

    /**
     *
     * @param screenWidth width of the Android screen
     * @param screenLength length of the Android screen
     * @param spriteBoundary two coordinate point that indicates the dimensions of the spriteBoundary,
     *                       where x is the length and y is the height
     */
    public Slider(int screenWidth, int screenLength, Coordinate spriteBoundary){
        this.screenLength = screenLength;
        this.screenWidth = screenWidth;
        ctxSpriteDimensions = spriteBoundary;

        xVelocity *= Util.rollDie(2) % 2 == 0 ? -1 : 1;
    }

    /**
     *
     * @param screenWidth width of the Android screen
     * @param spriteBoundary two coordinate point that indicates the dimensions of the spriteBoundary,
     *                       where x is the length and y is the height
     */
    public Slider(int screenWidth, Coordinate spriteBoundary){
        this.screenWidth = screenWidth;
        ctxSpriteDimensions = spriteBoundary;

        xVelocity *= Util.rollDie(2) % 2 == 0 ? -1 : 1;
    }

    /**
     * Given both the frames per second and the current position of the anchor,
     * the updateMovementPattern will calculate where the Anchor object is now located
     *
     * @param fps             the frames per second at a given instant
     * @param currentPosition point of the center of the RectF sprite boundary that contains
     *                        the Anchor object
     * @return new point coordinate to where the center of the Anchor should be
     */
    public Coordinate updateMovementPattern(long fps, Coordinate currentPosition) {

        /* The left- and rightmost points of the sprite boundaries*/
        float left = currentPosition.x - ctxSpriteDimensions.x/2;
        float right = currentPosition.x + ctxSpriteDimensions.x/2;

        float tempX;

        //UPDATE THE HORIZONTAL COORDINATES
        //Make sure that the sprite remains within the bounds of the view from the lefts
        if ( left + xVelocity/fps > 0 && right + xVelocity / fps < screenWidth) {
            tempX = currentPosition.x + (xVelocity / fps);
        }
        else {
            tempX = (xVelocity < 0) ? ctxSpriteDimensions.x/2 : screenWidth - ctxSpriteDimensions.x/2;
            reverseXVelocity();
        }

        return new Coordinate (tempX, currentPosition.y);
    }

    public movementPatternName getMovementPatternName () { return movementPatternName.Slider; }

    private void reverseXVelocity (){
        xVelocity *= -1;
    }

}
