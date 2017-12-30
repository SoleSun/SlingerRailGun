package solegamer.slingerrailgun;

import android.graphics.Point;
import android.graphics.RectF;

/**
 * Created by Joel on 4/15/2017.
 */

public interface ObjectT {

    /**
     * @return the RectF object that serves as the boundary of the Object's sprite
     */
    RectF getSpriteBoundary ();

    /**
     * @return the point of the center of the Object's sprite
     */
    Coordinate getCoordinate ();

    /**
     * The camera of the game will always follow the player. Therefore, we require all
     * Objects to scroll relative to the player's position
     *
     * @param fps the frames per second at a given instant
     * @param distance how much to scroll the ObjectT relative to the player
     */
    void updateScroll (long fps, float distance);
}
