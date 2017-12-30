package solegamer.slingerrailgun.PlayerAggregate;

import android.graphics.RectF;
import android.util.Log;

import solegamer.slingerrailgun.Coordinate;
import solegamer.slingerrailgun.PowerUps.Item;

public class AtomSlinger {

    private int spriteLength, spriteHeight;

    /* Keep track of the Anroid phone's dimensions */
    private int screen_width, screen_length;

    /* Coordinates of the CENTER of the sprite*/
    private float centerX, centerY;

    /* Vectors of the player's movement in the x and y directions
     * Will be zero if attached to an anchor */
    private float xVelocity = 0, yVelocity = 100;

    /*
     * Gravitational acceleration in the vertical direction
     * The greater the g force, the faster the atom accelerates downward
     */
    private static long g = 2000;

    /* Mass of the AtomSlinger to calculate the velocity it will launch from the sling */
    private static final long mass = 250;

    //The minimum rectangular dimension needed to contain the atom's bitmap sprite
    private RectF atomSpriteBoundary = new RectF();

    /*
    The reference angle is directly down the Android screen in portrait mode
    Keeps track of the angle the atom slinger makes with its attached anchor
    */
    private double angleWithAnchor, distance;

    //This is the magnitude at which the atomsprite will be shown
    private final int ratio = 12;

    //The AtomicSlinger can only ever be attached to one ElasticSling */
    private ElasticSling Tether;

    //Checks if the player is anchored
    private boolean isAnchored = false;

    //Checks if the player is ready to be launched
    private boolean readyToLaunch = false;

    //Checks if the player has delegated
    private boolean moveOtherObjects = false;

    //Check if the player has any explosives to activate this ability
    private final int maxNoOfExplosives = 3;
    private int noOfExplosives = 0;

    //Check if the player can deploy a rapel to save himself
    private final int maxNoOfRapels = 1;
    private int noOfRapels = 0;

    //The player has activated the Explosive ability to burst through walls
    private boolean isExploding = false;

    /*
    Default y-coordinate of AtomSlinger as percentange of screen
    The AtomSlinger will alert its manager when to scroll other objects
    */
    private double defaultYCoordinateAsPerctangeOfScreen = 0.65;

    private AtomSlinger ()
    {
        screen_width = screen_length = spriteLength = spriteHeight = 0;
    }

    private static class AtomSlingerContainer {
        private static final  AtomSlinger _instance = new AtomSlinger();
    }

    /**
     * Ensure that only one instance of the AtomSlinger player can be instantiated
     *
     * @return sole instance of AtomSlinger object
     */
    static public AtomSlinger Get_Instance ()
    {
        return AtomSlingerContainer._instance;
    }

    /**
     * Used when starting or restarting the game to initialize the player's condition
     *
     * @param screenX
     * @param screenY
     * @param initAnchor
     */
    public void Init_Player (int screenX, int screenY, RectF initAnchor) {
        Log.i("Player screen length", "" + screenY);
        screen_width = screenX;
        screen_length = screenY;

        spriteLength = screenX / ratio;
        spriteHeight = screenX / ratio;

        atomSpriteBoundary = new RectF();

        isExploding = false;

        /* At restart, ensure that the player has no items left over from the last play*/
        noOfRapels = noOfExplosives = 0;

        /* Ensure the previous position is also not saved*/
        distance = angleWithAnchor = 0;

        xVelocity *= 1/4;
        yVelocity *= 1/4;

        this.updatePosition(initAnchor.centerX(), initAnchor.centerY());
        this.latchOntoAnchor(initAnchor);
    }

    /**
     * When the Slinger is attached to an anchor and when the user is
     * dragging the AtomSlinger prior to launching
     *
     * @param x must be greater than or equal to zero; represents the new center x-coordinate of the atom sprite
     * @param y must be greater than or equal to zero; represents the new center y-coordinate of the atom sprite
     * @param attachedAnchor spatial boundary of the attached anchor's sprite
     */
    public void updateUserDrag (float x , float y, RectF attachedAnchor){

        //Check to make sure that the AtomSlinger is indeed achored
        if (isAnchored) {
            Coordinate anchorPosition = convertRectFToCoordinate(attachedAnchor);

            /* The current Anchor might have been moved, so ensure the Tether keeps up with the Anchor*/
            Tether.updateAnchorPosition( anchorPosition );

            /* Ensure that the player can never launch the atom downwards */
            this.updatePosition
                    ( x, (y <= anchorPosition.y) ? anchorPosition.y : y );

            double tempVelocity;

            switch ( Tether.stretch( centerX, centerY ) ) {
                case READYTOLAUNCH:
                    readyToLaunch = true;
                    tempVelocity = Math.sqrt(Tether.getPotentialEnergy() * 2 / mass);
                    xVelocity = (float) (Tether.getCos() * tempVelocity);
                    yVelocity = (float) (Tether.getSin() * tempVelocity);
                    break;

                case MAXIMUMPOWER:
                    readyToLaunch = true;
                    tempVelocity = Math.sqrt(Tether.getPotentialEnergy() * 2 / mass);
                    xVelocity = (float) (Tether.getCos() * tempVelocity);
                    yVelocity = (float) (Tether.getSin() * tempVelocity);
                    //enforce the maximum stretching by receiving the Tather object's end coordinates
                    this.updatePosition( Tether.getStopX(), Tether.getStopY());
                    break;

                case NOTENOUGHENERGY:
                    readyToLaunch = false;
                    xVelocity = yVelocity = 0;
                    break;
            }

            double  dx = centerX - attachedAnchor.centerX(),
                    dy = centerY - attachedAnchor.centerY();

            angleWithAnchor = Math.atan(dx/dy);
            distance = Math.sqrt(dx*dx + dy*dy);
        }
    }

    /**
     * Call this method when the user still has his pointer on the screen but not moving. This will
     * alert the AtomSlinger to any changes in position of its attached Anchor. The atom will automatically
     * maintain the angle at which it was previously attached to the anchor.
     *
     * @param attachedAnchor spatial boundary of the attached Anchor
     */
    public void updateUserDrag (RectF attachedAnchor){

        //Check to make sure that the AtomSlinger is indeed achored
        if (isAnchored) {

            Coordinate anchorPosition = convertRectFToCoordinate(attachedAnchor);

            /* The current Anchor might have been moved, so ensure the Tether keeps up with the Anchor*/
            Tether.updateAnchorPosition(anchorPosition);

            float   anchorX = attachedAnchor.centerX(),
                    anchorY = attachedAnchor.centerY(),
                    dx = (float)(distance * Math.sin(angleWithAnchor)),
                    dy =  (float)(distance * Math.cos(angleWithAnchor));

            /* Ensure that the player can never launch the atom downwards */
            this.updatePosition
                    ( anchorX + dx, anchorY + dy );

            Tether.updatePlayerPosition(atomSpriteBoundary);

//            double tempVelocity;

//            switch ( Tether.stretch( centerX, centerY ) ) {
//                case READYTOLAUNCH:
//                    readyToLaunch = true;
//                    tempVelocity = Math.sqrt(Tether.getPotentialEnergy() * 2 / mass);
//                    xVelocity = (float) (Tether.getCos() * tempVelocity);
//                    yVelocity = (float) (Tether.getSin() * tempVelocity);
//                    break;
//
//                case MAXIMUMPOWER:
//                    readyToLaunch = true;
//                    tempVelocity = Math.sqrt(Tether.getPotentialEnergy() * 2 / mass);
//                    xVelocity = (float) (Tether.getCos() * tempVelocity);
//                    yVelocity = (float) (Tether.getSin() * tempVelocity);
//                    //enforce the maximum stretching by receiving the Tather object's end coordinates
//                    this.updatePosition( Tether.getStopX(), Tether.getStopY());
//                    break;
//
//                case NOTENOUGHENERGY:
//                    readyToLaunch = false;
//                    xVelocity = yVelocity = 0;
//                    break;
//            }
        }
    }

    /**
     * Updates the horizontal movement while the vertical movement remains static
     *
     * @return true if the vertical position has relatively gone beyond 45/100 of the screen
     *          and the atom remains fixated while everything else around it should move;
     *          else, false and the atom will update its own movement
     */
    public boolean moveAsProjectile(long fps){

        boolean Delegate_Movement = false;

        float tempX;

        //UPDATE THE HORIZONTAL COORDINATES
        //Make sure that the sprite remains within the bounds of the view from the lefts
        if ( this.getLeftRectF() + xVelocity/fps > 0 && this.getRightRectF() + xVelocity / fps < screen_width) {
            tempX = centerX + (xVelocity / fps);
        }
        else {
            tempX = (xVelocity < 0) ? spriteLength/2 : screen_width - spriteLength/2;
            this.reverseXVelocity();
        }

        float tempY;
        long instFps = (long)(fps*0.9);
        yVelocity += (g/instFps);

        /* If the atom has begun to descend, then its explosive power has worn off*/
        if (yVelocity > 0) isExploding = false;

        //UPDATE THE VERTICAL COORDINATES OR DELEGATE
        //if the atom sprite is below the thresold of the screen size or it's descending,
        // it can update its vertical position
        // on its own without having to delegate the task to the viewport.
        if ( centerY >= screen_length*(defaultYCoordinateAsPerctangeOfScreen)|| yVelocity > 0){
            tempY = centerY + (yVelocity / fps);
            moveOtherObjects = false;
        }
        //Hold the atom's vertical position and instead delegate to the viewport;
        //if done correctly, the viewport should scroll the other objects to give
        //the illusion of moving
        else {
            tempY = centerY;
            moveOtherObjects = true;
            Delegate_Movement = true;
        }

        this.updatePosition( tempX , tempY);

        return Delegate_Movement;
    }

    // CAMERA VIEW FUNCTIONS
    /**
     * If the player is Anchored and the camera is not ocused on the player,
     * then this function alerts the client how much to adjust the camera i.e.
     * adjust the positions of all other objects
     *
     * @param must be greater than zero; the frames per second that the screen is
     *             updating at
     * @return number of pixels to adjust the camera in a single frame
     */
    public float getDistanceToCompensate ( long fps, float attachedAnchorY) {
        float distanceToCompensate = 0;
        if (moveOtherObjects){
            distanceToCompensate =
                    (float) (defaultYCoordinateAsPerctangeOfScreen * screen_length) - attachedAnchorY;
            distanceToCompensate /= fps;
        }
        return distanceToCompensate;
    }

    /**
     * @param fps must be greater than zero; the frames per second that the screen
     *            is updating at
     * @return  returns 0 if the atom is still able to fully update its movement on its own;
     *          else, returns the number of pixels that the atom has climbed in a specific instant
     *
     */
    public float getAscentDistance (long fps) {
        return (moveOtherObjects && yVelocity < 0) ? Math.abs(yVelocity /fps) : 0;
    }

    //PLAYER STATE MUTATOR FUNCTIONS

    public void reverseXVelocity() {
        xVelocity *= -1;
    }

    public void reverseYVelocity () { yVelocity *= -1;}

    /**
     * Restores the AtomSlinger to equilibrium position
     * Can only be invoked if the AtomSlinger is attached to an anchor
     *
     * @param anchor the spatial boundaries of the anchor to which the atom slinger is attached
     */
    public void oscillate(long fps, RectF attachedAnchor){
        //Check that the atom is anchored
        if (isAnchored) {
            /* Update the position of the AtomSlinger */
            this.updatePosition( Tether.oscillate(fps, attachedAnchor));
        }
    }

    /**
     * Attaches the atom to the specified Anchor object
     *
     * @param anchor the spatial boundaries of the anchor to which the atom slinger is attached
     *
     * @return distance to how much the screen needs to be adjusted to accomodate the player's position
     */
    public float latchOntoAnchor (RectF anchorSpriteBoundary){
        readyToLaunch = false;
        isAnchored = true;
        isExploding = false;
        Tether = new ElasticSling(anchorSpriteBoundary, atomSpriteBoundary, new Coordinate(screen_width, screen_length));

        /* Pass the parameters of motion to the sling*/
        Tether.setInitialConditions(xVelocity/2, 3*yVelocity/10);

        /* Since the playeris now slinged, it carries no autonomous motion of its own*/
        xVelocity = yVelocity = 0;

        float distanceToCompensate =
                (float) (defaultYCoordinateAsPerctangeOfScreen * screen_length) - anchorSpriteBoundary.centerY();

        /* If the player is out of a certain range, then don't bother compensating */
        moveOtherObjects = distanceToCompensate  - screen_length / 50 == 0?
                false : true;

        return distanceToCompensate;
    }

    /**
     * When the atom is ready to be launched and the player releases
     * the sprite, the atom no longer attaches to its current anchor
     * and moves autonomously
     *
     * @return true if the atom has launched; else, false and the atom object still
     *          remains latched onto its Anchor object
     */
    public boolean releaseAnchor (){
        boolean Launched = false;

        if (this.readyToLaunch) {
            Launched = true;
            isAnchored = false;
            moveOtherObjects = false;
        }

        return Launched;
    }

    /**
     * @param TypeOfItem enumerated item inside the Item Interface lets the player know that it has
     *                   changed state
     */
    public void powerUp (Item.items TypeOfItem) {
        switch (TypeOfItem){
            case Sling:
                if (noOfRapels <= maxNoOfRapels) noOfRapels++;
                break;
            case Explosive:
                if (noOfExplosives <= maxNoOfExplosives) noOfExplosives++;
                break;
        }
    }

    /**
     * The user has activated the player's Explode ability
     */
    public void callExplode () {
        if (noOfExplosives > 0){
            yVelocity -= 750;
            noOfExplosives--;
            isExploding = true;
        }
    }

    // POSITION GETTERS
    /* Method for getting the spatial boundary of the object */
    public RectF getSpriteBoundary() { return new RectF(atomSpriteBoundary); }

    /* Return the coordinates of the AtomSprite*/
    public float getLeftRectF () { return atomSpriteBoundary.left; }
    public float getTopRectF () { return atomSpriteBoundary.top; }
    public float getBottomRectF () { return atomSpriteBoundary.bottom; }
    public float getRightRectF () { return atomSpriteBoundary.right; }
    public Coordinate getCoordinate () { return new Coordinate( centerX, centerY); }

    /* Retrieving the dimensions of the */
    public int getLength () { return spriteLength; }
    public int getHeight () { return spriteHeight; }

    /* Retrieve the mass of the Atom */
    public static long mass () { return mass;}
    public static long g () { return g;}

    //POSITION STATE GETTERS
    /** Check whether the AtomicSlinger is anchored*/
    public boolean checkAnchorState (){ return isAnchored;}

    /** Check if the player is exploding */
    public boolean checkExplosiveState () { return isExploding;}

    /** Check if the player has an Explosive power up*/
    public boolean checkIfPlayerHoldsExplosive () { return noOfExplosives > 0; }

    /** Check how many explosives the player holds */
    public int checkNoOfExplosivesPlayerHolds () { return noOfExplosives; }

    /** Check if the player has a Sling powerup*/
    public boolean checkRapelState () { return noOfRapels > 0; }

    /** Check how many explosives the player holds */
    public int checkNoOfRapelPlayerHolds () { return noOfRapels; }

    /**
     * Automatically updates both the Atom and its sprite's positions
     * @param newCenterX must be greater than or equal to zero and less than or equal to screen_width
     * @param newCenterY must be greater than or equal to zero and less than or equal to screen_length
     */
    private void updatePosition (float newCenterX, float newCenterY) {
        centerX = newCenterX;
        centerY = newCenterY;

        atomSpriteBoundary.left = centerX - spriteLength/2;
        atomSpriteBoundary.right = centerX + spriteLength/2;
        atomSpriteBoundary.top = centerY - spriteHeight/2;
        atomSpriteBoundary.bottom = centerY + spriteHeight/2;
    }

    /**
     * Automatically updates both the Atom and its sprite's positions
     * @param newPosition Coordinate object with x and y values within the dimensions of the Android screen;
     *                    updates the coordinates of the AtomSlinger to those provided
     */
    private void updatePosition (Coordinate newPosition) {
        centerX = newPosition.x;
        centerY = newPosition.y;

        atomSpriteBoundary.left = centerX - spriteLength/2;
        atomSpriteBoundary.right = centerX + spriteLength/2;
        atomSpriteBoundary.top = centerY - spriteHeight/2;
        atomSpriteBoundary.bottom = centerY + spriteHeight/2;
    }

    /**
     * The camera of the game will always follow the player. Therefore, we require all
     * Objects to scroll relative to the player's position
     *
     * @param fps the frames per second at a given instant
     * @param distance how much to scroll the ObjectT relative to the player
     */
    public void updateScroll (float distance){
        this.updatePosition(centerX, centerY + distance);
    }

    // TETHER ACCESSOR METHODS
    /**The tether should be encapsulated and accessed only through the AtomSlinger itself */
    public float getTetherStartX () { return Tether.getStartX(); }
    public float getTetherStartY () { return Tether.getStartY(); }
    public float getTetherStopX () { return Tether.getStopX(); }
    public float getTetherStopY () { return Tether.getStopY(); }

    /**
     * @return The coordinates of the Tether from the attached anchor to the player;
     * will return a RectF of zero length and height located on the screen's topleft
     * corner if the player is not anchored
     */
    public RectF getTether () {
        RectF tetherCoordinates = new RectF(0,0,0,0);
        if (isAnchored) {
            tetherCoordinates =
                    new RectF
                        (getTetherStartX(),
                        getTetherStartY(),
                        getTetherStopX(),
                        getTetherStopY());
        }

        return tetherCoordinates;
    }

    private Coordinate convertRectFToCoordinate (RectF r){
        return new Coordinate (r.centerX(), r.centerY());
    }
}
