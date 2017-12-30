package solegamer.slingerrailgun;

import android.graphics.RectF;

import java.lang.*;

import solegamer.slingerrailgun.Actor;

/**
 * Created by Joel on 4/8/2017.
 *
 * Class of enemy AIs that can range from tracking the player to being
 * hiding Anchors that will kill the player if touched
 */

public class Thrashers implements ObjectT{
    /* Enemy AIs */

    private float x, y;

    private int length = 100, height = 100;

    private RectF ThrasherSpriteBoundary = new RectF();



    public void updateAutonomousMovement (long fps) {

        }

    /**
     * The camera of the game will always follow the player. Therefore, we require all
     * Objects to scroll relative to the player's position
     *
     * @param fps the frames per second at a given instant
     * @param distance how much to scroll the ObjectT relative to the player
     */
    public void updateScroll (long fps, float distance){

    }

    // SUPPORT FUNCTIONS
    public RectF getSpriteBoundary ()
        {
        return new RectF(ThrasherSpriteBoundary);
        }

    public Coordinate getCoordinate () { return new Coordinate (x, y); }
    /**
     * Automatically updates both the Thrasher and its sprite's positions
     * @param x must be greater than or equal to zero and less than or equal to boundX
     * @param y must be greater than or equal to zero and less than or equal to boundY
     */
    private void updatePosition (float x, float y)
    {
        this.x = x;
        this.y = y;

        ThrasherSpriteBoundary.left = this.x - length/2;
        ThrasherSpriteBoundary.right = this.x + length/2;
        ThrasherSpriteBoundary.top = this.y - height/2;
        ThrasherSpriteBoundary.bottom = this.y + height/2;
    }
}
