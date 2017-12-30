package solegamer.slingerrailgun;

import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import solegamer.slingerrailgun.Anchor.Anchor;
import solegamer.slingerrailgun.Anchor.AnchorMovementPattern;
import solegamer.slingerrailgun.MovementPatterns.MovementPattern;
import solegamer.slingerrailgun.PlayerAggregate.AtomSlinger;
import solegamer.slingerrailgun.PowerUps.Explosive;
import solegamer.slingerrailgun.PowerUps.Item;
import solegamer.slingerrailgun.Wall.Wall;

/**
 * Created by Joel on 4/14/2017.
 *
 * Mediator object responsible for handling the interactions between the non-player objects
 */

public class ObjectManager {

    /* Keep track of the dimensions of the screen*/
    private int screen_width, screen_length;

    /* The object manager will generally be unable to do anything unless it is initialized*/
    private boolean initialized = false;

    /* Track all objects, spawning and removing objects as they come. Sorted in descending */
    private ArrayList<ObjectT> Object_Registry = new ArrayList<>();

    /* Track all Anchor objects */
    private ArrayList<Anchor> Anchor_Registry = new ArrayList<>();

    /* Track all Wall objects */
    private ArrayList<Wall> Wall_Registry = new ArrayList<>();

    /* Track all Item objects */
    private ArrayList<Item> Item_Registry = new ArrayList<>();

    /* Keep track of the number of anchors on the field*/
    private int AnchorCounter = 0;

    /* Keep track of the number of objects ever spawned */
    private long noOfAnchorsEverSpawned = 0;

    /* The sole instance of the player*/
    private AtomSlinger player;

    /* The mediator keeps track of which anchor the player is attached to*/
    private Anchor attachedAnchor;

    /* Score accumulates as the player ascends, successfully latches onto anchors, and collects items*/
    private long score = 0;

    /* Minimum distance between anchors as a perctange of a the screen */
    private float minDistanceBetweenAnchors;

    /* Ensure that if the player has intersected a wall once, the frame rate does not interfere with the physics */
    private boolean intersectedWallAlready = false;

    private static class ObjectManagerContainer {
        private static final ObjectManager _instance = new ObjectManager();
    }

    /* There should only ever be one instance of the Object Manager class */
    static ObjectManager Get_Instance() {
        return ObjectManagerContainer._instance;
    }

    /**
     * Initialize the object manager with the parameters it needs to perform its operations
     *
     * @param screen_width  screen x
     * @param screen_length screen y
     */
    void InitObjectManager(int screen_width, int screen_length) {
        this.screen_length = screen_length;
        this.screen_width = screen_width;

        double distanceBetweenAnchorsPercentage = 0.625;
        minDistanceBetweenAnchors = (float) (distanceBetweenAnchorsPercentage * screen_length);
        /* Once the Object Manager has its requisite parameters, it can execute its operations */
        initialized = true;
    }

    //ACTION EVENT FUNCTIONS

    /**
     * At runtime, should the player lose, then the anchors need to be reinitialized
     * Creates all the necessary objects
     */
    void GameOver_Objects_Reset() {
        Object_Registry.clear();
        Anchor_Registry.clear();
        Wall_Registry.clear();
        Item_Registry.clear();
        score = noOfAnchorsEverSpawned = AnchorCounter = 0;

        int placeOfInitWall = Util.rollDie(3) + 5, placeOfInitItem;
        do {
            placeOfInitItem = Util.rollDie(6) + 2;
        } while (placeOfInitItem == placeOfInitWall);

        for (int count = 0; count < 8; count++) {
            spawnAnchor();

            /* Initialize the player and the anchor to which it will be attached*/
            if (count == 0){
                attachedAnchor = (Anchor) Object_Registry.get(0);
                player = AtomSlinger.Get_Instance();
                player.Init_Player(screen_width, screen_length, attachedAnchor.getSpriteBoundary());
            }
            if (count == placeOfInitWall){
                spawnWall();
            }
            if (count == placeOfInitItem){
                spawnItem();
            }
        }
    }

    //ONMOTIONEVENT FUNCTIONS. SHOULD ONLY CONCERN THE PLAYER
    /**
     * An OnMotionEvent function: call the methods when the player is touching the screen
     *
     * @param playerNewX the latest x-coordinate of the pointer interaction
     * @param playerNewY the latest y-coordinate of the pointer interaction
     * @return true if the player's movement is registered; else, false
     */
    boolean updateWhenUserTouchesScreen(float playerNewX, float playerNewY) {
        boolean userInputPhase = false;

        if (player.checkAnchorState()) {
            userInputPhase = true;

            player.updateUserDrag(playerNewX, playerNewY, attachedAnchor.getSpriteBoundary());
        }

        return userInputPhase;
    }

    /**
     * An OnMotionEvent function: call the method when the player has released his pointer
     * from the screen
     */
    void updateWhenUserReleasesScreen() {
        /* If the player has launched */
        if (player.releaseAnchor()) {
            /* Notify the anchor it was attached to that it has released*/
            attachedAnchor.playerReleased();
        }
    }


    /**
     * An OnMotionEvent function: call the method when the player has kept his pointer in a given
     * position for more than a threshold time; a long press
     */
    void updateWhenUserLongPresses () { player.callExplode();}

    //GAME LOGIC FUNCTIONS

    /**
     * Call the method when the user is able to interact with the atom sprite and all objects
     * need to be updated in order to ensure the game ecosystem continues regardless of user
     * interaction
     *
     * @param fps instantaneous frames per second of the Android
     */
    void updateDuringUserInputPhase(long fps) {
        if (player.checkAnchorState()) {
            /* Determine how much to scroll the screen so that the camera is focused on the player */
            float adjustHeight = player.getDistanceToCompensate(fps, attachedAnchor.getY());

            /* Adjust the distance to center the player*/
            this.updateObjectPosition(adjustHeight, fps);

            /* Ensure that the player is also scrolled with the other objects */
            player.updateScroll(adjustHeight);

            /* Ensure that the player is also updated if its current anchor is moving */
            player.updateUserDrag(attachedAnchor.getSpriteBoundary());
        }
    }

    /**
     * Call the method when the user cannot interact with the player and the
     * atom is currently flying through the screen
     *
     * @param fps the instantaneous frames per second; must not be zero
     */
    void updateNotInUserInputPhase(long fps) {
        /* Iterate through the objects in the registry and check if the player has intersected any of them*/
        handleCollision();

        /* If the player is not attached to some anchor, the it should move autonomously */
        if (!player.checkAnchorState()) {

            /* Update the player's position */
            player.moveAsProjectile(fps);

            /* Update the objects' positions*/
            this.updateObjectPosition(player.getAscentDistance(fps), fps);

            /* Reset the game if the player has gone too far below the screen */
            if (player.getBottomRectF() > 1.3 * screen_length)
                this.GameOver_Objects_Reset();
        }
        /*
        If the player is in fact anchored but the user is simply not interacting with the
        screen, then just return the atom to its default position
        */
        else {
            /* After updating all objects */
            this.updateObjectPosition(player.getDistanceToCompensate(fps, attachedAnchor.getY()), fps);

            /*
             Alert the player that the anchor to which it has bas been attached could have
             moved, so let it update itself and its own objects
            */
            player.oscillate(fps, attachedAnchor.getSpriteBoundary());
        }
    }

    // SUPPORT FUNCTIONS for the updates

    /**
     * Update the positions of all instances of Anchor relative to the change in position of the Atom
     *
     * @param fps            the instantaneous frames per second at the time of the function call
     * @param scrollDistance must not be null; the change in position of the player
     */
    private void updateObjectPosition(float scrollDistance, long fps) {

        ArrayList <ObjectT> listOfObjectsToRemove = new ArrayList<>();

        for (int index = 0; index < Object_Registry.size(); index++){
            ObjectT o = Object_Registry.get(index);
            if (o.getSpriteBoundary().top > 1.2 * screen_length){
                if (o instanceof Anchor){
                    Anchor_Registry.remove(o);
                    AnchorCounter--;
                    if (AnchorCounter < 6) spawnAnchor();
                }

                if (o instanceof Wall){
                    Wall_Registry.remove(o);
                }

                if (o instanceof Item){
                    Item_Registry.remove(o);
                }

                listOfObjectsToRemove.add(o);
            }
            else{
                o.updateScroll(fps, scrollDistance);
            }
        }

        if (Wall_Registry.size() < Wall.getMaxNoOfWallsThatCanFitScreen()) {
            spawnWall();
        }

        if (Item_Registry.size() < 1) {
            spawnItem();
        }

        Object_Registry.removeAll(listOfObjectsToRemove);
    }

    /**
     * Given access to the Object_Registry, iterate through it and handle the logic of
     * when the player collides with other objects
     */
    private void handleCollision () {
        /* Iterate through the objects in the registry and check if the player has intersected them*/
        RectF overlappingRegion = null;
        boolean intersectedWall = false;

        ArrayList <ObjectT> listOfObjectsToRemove = new ArrayList<>();

        for (ObjectT o : Object_Registry){
            if (RectF.intersects(player.getSpriteBoundary(), o.getSpriteBoundary())) {

                if (o instanceof Anchor && !o.equals(attachedAnchor)) {
                    player.latchOntoAnchor(o.getSpriteBoundary());

                /* Update the global variable of the player's attached anchor*/
                    attachedAnchor = (Anchor) o;

                /* Notify the anchor that the player has latched on*/
                    attachedAnchor.playerLatchedOn();

                /* Increase the score every time the player successfully latches on */
                    score++;

                    break;
                }

                if (o instanceof Wall) {
                    /* If the player has activated the explosive power up, then any wall it touches gives way*/
                    if (player.checkExplosiveState()){
                        Wall_Registry.remove(o);
                        listOfObjectsToRemove.add(o);
                    } else {
                        intersectedWall = true;

                        /* Already computed physics so no point in going through the calculations again */
                        if (intersectedWallAlready) continue;

                        RectF temp = Util.getOverlappingRectF(getPlayerSpriteBoundary(), o.getSpriteBoundary());

                        if (overlappingRegion == null) {
                            overlappingRegion = new RectF(temp);
                        } else {
                            overlappingRegion.union(temp);
                        }

                        /* Found the whole overlapping region*/
                        if (overlappingRegion.width() == player.getLength() ||
                                overlappingRegion.height() == player.getHeight())
                            break;
                    }
                }

                if (o instanceof Item){
                    if (o instanceof Explosive) {
                        player.powerUp(Item.items.Explosive);
                    } else{
                        player.powerUp(Item.items.Sling);
                    }

                    listOfObjectsToRemove.add(o);
                    Item_Registry.remove(o);
                }
            }
        }

        if (intersectedWall && !intersectedWallAlready) {
            if (overlappingRegion.width() >= overlappingRegion.height()) {
                player.reverseYVelocity();
            } else {
                player.reverseXVelocity();
            }
        }

        intersectedWallAlready = intersectedWall;

        Object_Registry.removeAll(listOfObjectsToRemove);
    }

    // SPAWNING FUNCTIONS

    /**
     * Creates an anchor object
     */
    private void spawnAnchor() {
        if (initialized) {
            float y = (AnchorCounter == 0) ?
                    (float) 0.61 * screen_length :
                    this.getHighestAnchorYCoordinate() - minDistanceBetweenAnchors;

            Anchor a = new Anchor(screen_width, screen_length, y, noOfAnchorsEverSpawned);

            /* Set the first Anchor ever created to be the init Anchor*/
            if (AnchorCounter == 0) {
                a.setToInanimateInitAnchor();
            }

            /*
            If the player has passed a minimum of 20 objects, then begin spawning
            the more difficult loop movement patterns
            */

//            if (/*score > 8 &&*/ Util.rollDie(4) > 2) {
            if (score > 8 && Util.rollDie(4) > 2) {
                a.setToSliderAnchor();
            }
//            if (/*score > 16 &&*/ Util.rollDie(5) <= 2){
            if (score > 16 && Util.rollDie(5) <= 2){
                a.setToLoopAnchor();
            }
//            if (/*score > 20&&*/ Util.rollDie(5) == 2){
            if (score > 20&& Util.rollDie(5) == 2){
                a.setToBumbleBeeAnchor();
            }

            Object_Registry.add(a);
            Anchor_Registry.add(a);

            noOfAnchorsEverSpawned++;
            AnchorCounter++;
        }
    }

    /**
     * Creates a row of wall objects
     */
    private void spawnWall() {
        if (initialized) {
            boolean createSliderWallsFlag = false;
            int noOfWallsToAddInRow = 0, lengthBetweenAnchors = 0;

            spawnAnchor();

            Anchor anchorAboveWall = getHighestAnchor();
            Anchor anchorBelowWall = Anchor_Registry.get(Anchor_Registry.size() - 2);

            float y = anchorAboveWall.getY() + minDistanceBetweenAnchors/2;
            Wall referenceWall = new Wall(screen_width,y);
            float x = referenceWall.getCoordinate().x;

            if (anchorAboveWall.getMovementPatternType() != MovementPattern.movementPatternName.Bumblebee) {
                /* In this case, the player can pass through any obstacle so construct a full wall */
                if (player.checkIfPlayerHoldsExplosive() &&
                        anchorBelowWall.getMovementPatternType() == MovementPattern.movementPatternName.Inanimate) {
                    noOfWallsToAddInRow = Wall.getMaxNoOfWallsThatCanFitScreen();
                    lengthBetweenAnchors = 1;
                }
                /* If the Anchor can slide, then arrange the walls with two gates*/
                else if (anchorAboveWall.getMovementPatternType()
                        == AnchorMovementPattern.movementPatternName.Slider) {
                    noOfWallsToAddInRow = 4;
                    lengthBetweenAnchors = 3;
                }
                /* If the player has sufficiently progressed then create slider walls*/
                else if (score > 16 && Util.rollDie(6) >= 5) {
                    noOfWallsToAddInRow = Util.rollDie(4);
                    lengthBetweenAnchors = 5;
                    createSliderWallsFlag = true;
                }
                /* Else, construct a regular row of walls that only occupies the center of the screen*/
                else {
                    noOfWallsToAddInRow = 4;
                    lengthBetweenAnchors = 1;
                    x = referenceWall.getCoordinate().x
                            + referenceWall.getLength() *
                            (Wall.getMaxNoOfWallsThatCanFitScreen() - noOfWallsToAddInRow) / 2;
                    referenceWall = new Wall(screen_width, new Coordinate(x, y));
                    x = referenceWall.getCoordinate().x;
                }
            }

            /* This is the production floor, where the walls are actually created */
            for (int count = noOfWallsToAddInRow; count > 0; count--){
                Wall temp = new Wall (screen_width, new Coordinate (x,y) );

                /* If the walls slide, then set the movement pattern now*/
                if (createSliderWallsFlag) temp.setToSlider();

                /* Ensure that the Object_Registry remains sorted */
                int indexOfHighestAnchor = y > anchorAboveWall.getY() ?
                        Object_Registry.indexOf(anchorAboveWall) :
                        Object_Registry.size() - 1;

                Object_Registry.add(indexOfHighestAnchor, temp);
                Wall_Registry.add(temp);

                /* Determine the next x position of the Anchor from left to right*/
                x = x + referenceWall.getLength()*lengthBetweenAnchors;
            }
        }
    }

    /**
     * Creates an item
     */
    private void spawnItem (){
        if (initialized) {

            spawnAnchor();
            float potentialY = getHighestAnchorYCoordinate() + minDistanceBetweenAnchors / 2;

            Item item;

            switch (Util.rollDie(4)) {
//                case 2:
//                    item = new Sling(screen_width, potentialY);
//                    break;
                default:
                    item = new Explosive(screen_width, potentialY);
                    break;
            }

            Item_Registry.add(item);
            Object_Registry.add(item);
        }
    }


    // GETTER METHODS

    //SPATIAL TRACKING FUNCTIONS

    /**
     * @return y-ooordinate of the highest ObjectT in the scroller game
     */
    private float getHighestAnchorYCoordinate() {
        return getHighestAnchor().getY();
    }

    /**
     * @return Anchor object at game time located at the least y-coordinate i.e. highest point
     * considering this is an upwards scrolling game
     */
    private Anchor getHighestAnchor () {
        return Anchor_Registry.get(Anchor_Registry.size() - 1);
    }

    //ACCESSOR METHODS FOR DRAWING THE GRAPHICS
    /**
     * @return the instantaneous score the player has accumulated
     */
    long getScore () { return score; }

    /**
     * @return retrieve the sprite boundaries of all Anchors
     */
    ArrayList <RectF> getListOfAnchorSpriteBoundaries () {
        ArrayList <RectF> tempArr = new ArrayList<>();

        for (ObjectT o: Anchor_Registry){
            tempArr.add( o.getSpriteBoundary() );
        }

        return tempArr;
    }

    /**
     * @return return arraylist of sprite boundaries of all Walls
     */
    ArrayList <RectF> getListOfWallSpriteBoundaries () {
        ArrayList <RectF> tempArr = new ArrayList<>();

        for (Wall w: Wall_Registry){
            tempArr.add(w.getSpriteBoundary());
        }

        return tempArr;
    }

    /**
     * @return return arraylist of sprite boundaries of all Items
     */
    ArrayList <RectF> getListOfItemSpriteBoundaries (){
        ArrayList <RectF> tempArr = new ArrayList<>();

        for (Item i: Item_Registry){
            tempArr.add(i.getSpriteBoundary());
        }

        return tempArr;
    }

    /**
     * Retrieve the spatial boundary of the atom object
     * @return RectF that reprsents the spatial boundary of the player's atom object
     */
    RectF getPlayerSpriteBoundary () { return player.getSpriteBoundary();}

    /**
     * @return RectF object that represents the two coordinates that denote the ends of the
     *              Tether object
     */
    RectF getTether () {
        RectF tetherCoordinates = new RectF(0,0,0,0);
        if (player.checkAnchorState()) {
            tetherCoordinates =
                    new RectF
                    (player.getTetherStartX(),
                    player.getTetherStartY(),
                    player.getTetherStopX(),
                    player.getTetherStopY());
        }

        return tetherCoordinates;
    }

    // GETTER METHODS TO CHECK THE PLAYER'S STATE
    /**
     * @return true if the player indeed has the Explosive powerup
     */
    boolean checkIfPlayerHasExplosive () { return player.checkIfPlayerHoldsExplosive(); }

    /**
     * @return true if the player indeed has the Explosive powerup
     */
    boolean checkIfPlayerExploding () { return player.checkExplosiveState(); }

    /**
     * @return number of Explosive items that the player has successfully captured
     */
    int checkNoOfExplosivePlayerOwns () { return player.checkNoOfExplosivesPlayerHolds(); }

    /**
     * @return number of Rapel items that the player has successfully captured
     */
    int checkNoOfRapelsPlayerOwns () { return player.checkNoOfRapelPlayerHolds(); }
}
