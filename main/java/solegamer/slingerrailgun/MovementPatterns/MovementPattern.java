package solegamer.slingerrailgun.MovementPatterns;

import solegamer.slingerrailgun.Coordinate;

/**
 * Created by Joel on 5/2/2017.
 *
 * Interface for agreeing the basic functions needed to change movements
 */

public interface MovementPattern {

    public enum movementPatternName {
        Bumblebee,
        Inanimate,
        Loop,
        Slider
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
    Coordinate updateMovementPattern (long fps, Coordinate currentPosition);

    /**
     *@return name of movement pattern
     */
    movementPatternName getMovementPatternName ();
}
