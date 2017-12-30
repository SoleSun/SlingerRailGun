package solegamer.slingerrailgun.Anchor;

import solegamer.slingerrailgun.Coordinate;
import solegamer.slingerrailgun.MovementPatterns.MovementPattern;

/**
 * Created by Joel on 4/14/2017.
 *
 * Movement Pattern interface unique to the Anchor class
 */

public interface AnchorMovementPattern extends MovementPattern{

    /**
     * Given the screen dimensions in the form of a Point object and the y-position of the
     * center of the Anchor's center point, the AnchorMovementPattern state will dictate where the
     * initial coordinates of the Anchor should be
     *
     * @param centerY y-position of the center of the Anchor's center point
     *
     * @return initial coordinate of the center of the Anchor's sprite boundary
     */
    Coordinate initCoordinate (float centerY);
}
