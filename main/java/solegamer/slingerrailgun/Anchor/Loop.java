package solegamer.slingerrailgun.Anchor;

import solegamer.slingerrailgun.Coordinate;
import solegamer.slingerrailgun.Util;

/**
 * Created by Joel on 4/14/2017.
 *
 * Anchor Movement Pattern that moves the anchor in circles
 */

public class Loop implements AnchorMovementPattern {

    /* The dimensions of the screen */
    private final int screenWidth, screenLength;

    /* The center of the circle movement pattern */
    private double turningX, turningY;

    /* Radius of the loop's movement*/
    private double radius;

    /* Dimensions of the context class' sprite */
    private Coordinate ctxSpriteDimensions;

    /* Keep track of the angle of the loop pattern*/
    private double angleTracker = 0;

    /* Estimation to where the client coordinate should be*/
    private Coordinate expectedCoordinate;

    /* The rate at which the loop movement pattern moves along its circumference */
    private double angularVelocity = Util.getRandomNum(0.6, 1, 100);

    /* Ensured that the initCoordinate has been called once */
    private boolean initialized = false;

    /**
     *
     * @param screenWidth width of the Android screen
     * @param screenLength length of the Android screen
     * @param spriteBoundary two coordinate point that indicates the dimensions of the spriteBoundary,
     *                       where x is the length and y is the height
     */
    public Loop (int screenWidth, int screenLength, Coordinate spriteBoundary){
        this.screenLength = screenLength;
        this.screenWidth = screenWidth;
        ctxSpriteDimensions = spriteBoundary;

        /* 50/50 chance of the loop turning clockwise or counterclockwise */
        angularVelocity *= Util.rollDie(2) % 2 == 0 ? -1 : 1;
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

        float newX = 0;
        float newY = 0;

        if (initialized) {
            double changeInAngle = Math.toRadians(angularVelocity) / fps;

            /* The current Position of the Anchor has changed since the last time */
            turningX += (currentPosition.x - expectedCoordinate.x);
            turningY += (currentPosition.y - expectedCoordinate.y);
            angleTracker = (angleTracker + changeInAngle) % (Math.PI * 2);

            newX = (float) (Math.cos(angleTracker) * radius + turningX);
            newY = (float) (Math.sin(angleTracker) * radius + turningY);

            expectedCoordinate = new Coordinate(newX, newY);
        }

        return new Coordinate (newX, newY);
    }

    /**
     * Given the screen dimensions in the form of a Point object and the y-position of the
     * center of the Anchor's center point, the AnchorMovementPattern state will dictate where the
     * initial coordinates of the Anchor should be
     *
     * @param centerY          y-position of the center of the Anchor's center point
     * @return initial coordinate of the center of the Anchor's sprite boundary
     */
    public Coordinate initCoordinate (float centerY) {

        initialized = true;

        angleTracker = 0;

        /* Roll a dice to decide at what coordinate the Anchor will be located */
        switch (Util.rollDie(3)){
            case 1:
                /* The turning radius is at a quarter of the screen */
                turningX = screenWidth / 4;
                radius = Util.getRandomNum(0.5, 0.8, (screenWidth / 4) - (int) (ctxSpriteDimensions.x / 2));
                break;
            case 2:
                /* The turning radius at half the screen */
                turningX = screenWidth / 2;
                radius = Util.getRandomNum(0.5, 0.8, screenWidth / 4 - (int) (ctxSpriteDimensions.x / 2));
                break;
            case 3:
                /* The turning radius is at three-quarters of the screen */
                turningX = 3 * screenWidth / 4;
                radius = Util.getRandomNum(0.5, 0.8, (screenWidth / 4) - (int) (ctxSpriteDimensions.x / 2));
                break;
        }

        /* The starting position is always at 3 o'clock */
        turningY = centerY;

        expectedCoordinate = new Coordinate((float) (turningX + radius), (float) turningY);

        return new Coordinate ((float) (turningX + radius), (float) turningY);
    }

    /**
     *@return name of movement pattern
     */
    public movementPatternName getMovementPatternName (){
        return movementPatternName.Loop;
    }
}
