package solegamer.slingerrailgun;

import android.graphics.RectF;

import java.util.Random;

/**
 * Created by Joel on 4/16/2017.
 *
 * Util class that keeps a library of static methods that assist
 * in running the game engine
 */

public class Util {

    private static Random rand = new Random (System.currentTimeMillis());

    /**
     * Retrieved a random number between a certain percentage range of a number
     *
     *@param low lower bound percentage of the num that can be returned
     *
     *@param high higher bound percentage of the num that can be returned
     *
     *@param num The number to set as the maximum number possible
     *
     *@return A truncated integer a given percentage of the passed in number;
     *        lower and upper bounds limit the min and max number that is passed
     *
     */
    public static int getRandomNum (double low, double high, int num) {
        //Random number generator
        int upper = (int) (num * (high - low));
        return Math.abs(rand.nextInt(upper) + (int) (num * low));
    }

    public static int rollDie (int numOfSides){
        return rand.nextInt(numOfSides) + 1;
    }

    // RECTF operations
    public static float getRectFLength (RectF r) { return r.width();}

    public static float getRectFHeight (RectF r) { return r.height();}

    public static float getRectFCenterX (RectF r) { return getRectFLength(r)/2 + r.left; }

    public static float getRectFCenterY (RectF r) { return getRectFHeight(r)/2 + r.top; }

    /**
     * @param one valid RectF object with no negative coordinates
     * @param two valid RectF object with no negative coordinates
     * @return RectF object whose bounds represent the overlapping region between RectF objects one
     * and two
     */
    public static RectF getOverlappingRectF (RectF one, RectF two){
        float dx, dy; /* The length and height of the overlapping regions*/
        float left, right, top, bottom; /* The float coordinates of the overlapping regions */

        /* Player is right more*/
        if (one.centerX() >= two.centerX()) {
            dx = two.right - one.left;
            left = one.left;
        }
        else {
            dx = one.right - two.left;
            left = two.left;
        }

        right = left + dx;

        /* Player is bottom more*/
        if (one.centerY() >= two.centerY()) {
            dy = two.bottom - one.top;
            top = one.top;
        }
        else {
            dy = one.bottom - two.top;
            top = two.top;
        }

        bottom = top + dy;

        /* Determine the new dimensions of the overlapping RectF region*/
        return new RectF(left, top, right, bottom);
    }

}
