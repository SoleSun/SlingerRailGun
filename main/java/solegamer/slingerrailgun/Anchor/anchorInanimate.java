package solegamer.slingerrailgun.Anchor;

import solegamer.slingerrailgun.Coordinate;
import solegamer.slingerrailgun.MovementPatterns.Inanimate;
import solegamer.slingerrailgun.Util;

/**
 * Created by Joel on 4/14/2017.
 *
 * Movement Pattern: inanimate and does not move at all
 */

public class anchorInanimate extends Inanimate implements AnchorMovementPattern {

    /* The dimensions of the screen */
    private final int screenWidth, screenLength;

    /**
     *
     * @param screenWidth width of the Android screen
     * @param screenLength length of the Android screen
     * @param spriteBoundary two coordinate point that indicates the dimensions of the spriteBoundary,
     *                       where x is the length and y is the height
     */
    public anchorInanimate(int screenWidth, int screenLength, Coordinate spriteBoundary){
        super (screenWidth, screenLength, spriteBoundary);
        this.screenLength = screenLength;
        this.screenWidth = screenWidth;
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

}
