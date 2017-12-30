package solegamer.slingerrailgun;

import android.graphics.RectF;

/**
 * Created by Joel on 5/11/2017.
 *
 * Handle the placement of the background
 */

public class Background {

    private final int screenLength, screenWidth;

    /* The containing RectF */
    private RectF backgroundSprite;

    /*
    As the background scrolls down, a portion of it will need to be replaced by another
    image of itself. Notify the mediator object the height to transpose with another
    */
    private int yClip;

    /* The player to background ratio in which the background will move relative to the player */
    private int speedFactor;

    /* Alert the mediator if the Background has been crossed over more than once*/
    private boolean noLongerAtBeginnings;

    Background (int screenWidth, int screenLength, int speedFactor){
        this.screenLength = screenLength;
        this.screenWidth = screenWidth;
        this.speedFactor = speedFactor;

        yClip = 0;

        noLongerAtBeginnings = false;

        backgroundSprite = new RectF(0,0,screenWidth,screenLength);
    }

    /**
     * Call this method to scroll the background down relative to the player
     * @param distance the amount of pixels to compensate in a single frame, not second; i.e.
     *                 the fps has already been calculated
     */
    public void updateScroll (float distance){
        yClip += (distance/speedFactor);
        backgroundSprite.top += (distance/speedFactor);
        backgroundSprite.bottom += (distance/speedFactor);

        if (yClip >= screenLength){
            noLongerAtBeginnings = true;
            backgroundSprite = new RectF (0, yClip - screenLength, screenWidth, yClip);
            yClip = 0;
        }
    }

    public void updatePosition (RectF newPosition){
        backgroundSprite = newPosition;
    }

    /**
     * @return distance at which the Background has been clipped in the y-direction
     */
    public int getyClip () { return yClip; }

    /**
     * @return true if the Background has crossed over the entire Android screen once;
     * else, false
     */
    public boolean isNoLongerAtBeginnings () { return noLongerAtBeginnings; }

    public RectF getSpriteBoundary () { return new RectF (backgroundSprite); }
}
