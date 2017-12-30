package solegamer.slingerrailgun.PlayerAggregate;

/**
 * Created by Joel on 2/19/2017.
 */

import android.graphics.RectF;
import android.util.Log;

import solegamer.slingerrailgun.Coordinate;

public class ElasticSling {

    private int screen_width, screen_length;

    private double maxHeight = 1.3;

    /*
     * When the ElasticSling is stretched, it can achieve various states
     */
    public enum  elasticState
        {
        NOTENOUGHENERGY, /* sling lacks enough energy to propel player a meaningful distance*/
        READYTOLAUNCH, /* sling has enough energy to propel player */
        MAXIMUMPOWER /* sling has achieved its maximum length and will restrict the player's position */
        }

    private final long k, g = AtomSlinger.g(), m = AtomSlinger.mass();

    /* Elastic constant */
    private final float c = m;

    //Represents the elastic potential energy stored when the user pulls back the character
    private double potentialEnergy;

    /*
     * One end of the ElasticSling will always be anchored to a Anchor, the coordinates at the
     * center. Likewise, the other end of the ElasticSling will be tethered to the
     * AtomSlinger. Whenever the user draws and releases the AtomSlinger, both the
     * Sling and the Slinger will coil back towards the Anchor
     * Natural length of the Elastic Sling
     */
    private final float length;

    /* Calculating minimum energy to propel atom a meaningful distance and maximum to how much it can stretch */
    private final long Minimum_Energy, Maximum_Energy;

    public final float Maximum_Length;

    private float startX, startY, stopX, stopY;

    /* Zero degrees is from the x-axis as normal but turns clockwise*/
    private double sin, cos;

    /* Keep track of the velocity of the spring */
    private double xVelocity = 0, yVelocity = 0;

    /* Keep track of the acceleration of the spring */
    private double xAcceleration = 0, yAcceleration = 0;

    public ElasticSling (RectF Attachable, RectF AtomSlingerObject, Coordinate screenDimensions){
        startX = Attachable.centerX();
        startY = Attachable.centerY();
        stopX = AtomSlingerObject.centerX();
        stopY = AtomSlingerObject.centerY();

        screen_width = (int) screenDimensions.x;
        screen_length = (int) screenDimensions.y;

        length = (float)((13/100)*screen_length);
        Maximum_Energy = m * g * ((long)(maxHeight*screen_length));
        Minimum_Energy = (long) (0.8* Maximum_Energy);

        k = (50 *Maximum_Energy) / (screen_length * screen_length);
        Maximum_Length = (float) (Math.sqrt(2 * Maximum_Energy / k) + length);
    }

    public float getStartX () { return startX; }
    public float getStartY () { return startY; }
    public float getStopX () { return stopX; }
    public float getStopY () { return stopY; }

    public double getSin () { return sin; }
    public double getCos () { return cos; }
    public double getPotentialEnergy () { return potentialEnergy; }


    /**
     * Updates the sling on where its atom object is
     *
     * @param AtomSlingerObject valid RectF object representing the boundary of the atom object
     *
     * @return NOTENOUGHENERGY if the player has not been stretched to a sufficient distance;
     *          READYTOLAUNCH if the player has been stretched to a sufficient distance;
     *          MAXIMUMPOWER if the player has been stretched to the sling's maximum tensile
     *          strength and is now being restricted in position
     *
     */
    public elasticState stretch (RectF AtomSlingerObject){
        xVelocity = yVelocity = xAcceleration = yAcceleration = 0;

        stopX = AtomSlingerObject.centerX();
        stopY = AtomSlingerObject.centerY();

        double distFromAnchor = calculateLength(startX, startY, stopX, stopY);

        cos = (startX - stopX) / distFromAnchor;
        sin = - Math.abs(stopY - startY) / distFromAnchor;

        // The Elastic Sling must be stretched a distance greater than its equilibrium length
        potentialEnergy =
                distFromAnchor > length ?
                        0.5 * k * (distFromAnchor - length) * (distFromAnchor - length) :
                        0;

        //What tensile state is the elatic sling in currently
        if (potentialEnergy < Minimum_Energy)
        {
            return elasticState.NOTENOUGHENERGY;
        }
        else if (potentialEnergy > Maximum_Energy)
        {
            potentialEnergy = Maximum_Energy;
            stopX = (float) cos * Maximum_Length;    stopY = (float) sin * Maximum_Length;
            return elasticState.MAXIMUMPOWER;
        }
        else
        {
            return elasticState.READYTOLAUNCH;
        }
    }

    /**
     * Updates the sling on where the sling's atom object is
     *
     * @param x x value of the center of where the atom sprite is located
     * @param y y value of the center of where the atom sprite is located
     *
     * @return NOTENOUGHENERGY if the player has not been stretched to a sufficient distance;
     *          READYTOLAUNCH if the player has been stretched to a sufficient distance;
     *          MAXIMUMPOWER if the player has been stretched to the sling's maximum tensile
     *          strength and is now being restricted in position
     */
    public elasticState stretch (float x, float y) {
        xVelocity = yVelocity = xAcceleration = yAcceleration = 0;
        stopX = x;
        stopY = y;

        double distFromAnchor = calculateLength(startX, startY, stopX, stopY);

        cos = (startX - stopX) / distFromAnchor;
        sin = - Math.abs(stopY - startY) / distFromAnchor;

        // The Elastic Sling must be stretched a distance greater than its equilibrium length
        potentialEnergy =
            distFromAnchor > length ?
            0.5 * k * (distFromAnchor - length) * (distFromAnchor - length) :
            0;

        //What tensile state is the elatic sling in currently
        if (potentialEnergy < Minimum_Energy) {
            return elasticState.NOTENOUGHENERGY;
        }
        else if (potentialEnergy > Maximum_Energy) {
            potentialEnergy = Maximum_Energy;
            stopX = startX + (float) - (cos * Maximum_Length);
            stopY = startY + (float) Math.abs(sin * Maximum_Length);
            return elasticState.MAXIMUMPOWER;
        }
        else {
            return elasticState.READYTOLAUNCH;
        }
    }

    /**
     * Mimic the natural 2D oscillation of a spring
     * @param x the x-position of the mass
     * @param y the y-position of the mass
     * @return the new coordinates of the mass
     */
    public Coordinate oscillate (long fps, RectF anchorPosition) {
        float dxAnchor = anchorPosition.centerX() - startX, dyAnchor = anchorPosition.centerY() - startY;
        this.updateAnchorPosition(anchorPosition);

        /* Update the position of the bob relative to the anchor*/
        stopX += dxAnchor;
        stopY += dyAnchor;

        fps *= 0.25;

        double  dx = stopX - startX,
                dy = stopY - startY;

        float   elasticModulus = (float)(0.025)*k,
                acceleration = (float)(0.13*screen_length*elasticModulus)/m;


        /* Use Euler's Method to calculate the following positions*/
        xAcceleration = -(elasticModulus*dx + c*xVelocity) / m;
        yAcceleration = acceleration - (elasticModulus*dy + c*yVelocity) / m;

        xVelocity = xVelocity + xAcceleration/fps;
        yVelocity = yVelocity + yAcceleration/fps;

        stopX = (float) (stopX + xVelocity/fps);
        stopY = (float) (stopY + yVelocity/fps);

        return new Coordinate (stopX, stopY);
    }

    /**
     * Notify the sling of the initial velocities so that it can properly calculate the
     * oscillatory motion
     * @param xVelocity the given x-component of the bob at time zero
     * @param yVelocity the given y-component of the bob at time zero
     */
    public void setInitialConditions (float xVelocity, float yVelocity){
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }

    /**
     * Resets the start point to wherever the anchor is
     * @param startX
     * @param startY
     */
    public void updateAnchorPosition(Coordinate attachableCoordinate){
        startX = attachableCoordinate.x;
        startY = attachableCoordinate.y;
    }

    /**
     * Resets the start point to wherever the anchor is
     * @param startX
     * @param startY
     */
    public void updateAnchorPosition(RectF attachableCoordinate){
        startX = attachableCoordinate.centerX();
        startY = attachableCoordinate.centerY();
    }

    /**
     * Resets the start point to wherever the anchor is
     * @param startX
     * @param startY
     */
    public void updatePlayerPosition (RectF playerCoordinate){
        stopX = playerCoordinate.centerX();
        stopY = playerCoordinate.centerY();
    }

    /**
     * Return the Elastic Sling to an equilibrium position relative to where the starting
     * Anchor sprite and the ending Atom sprite are
     * @param anchorSprite
     * @param AtomSprite
     */
    public void returnToEquilibrium (Coordinate anchorSprite, Coordinate AtomSprite){
        startX = anchorSprite.x;
        startY = anchorSprite.y;
        stopX = AtomSprite.x;
        stopY = AtomSprite.y;

        potentialEnergy = 0;
    }

    private double calculateLength (float x1, float y1, float x2, float y2){
        return Math.sqrt( (x2 - x1)*(x2 - x1) +
                            (y2 - y1)*(y2 - y1));
    }

}
