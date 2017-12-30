package solegamer.slingerrailgun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import solegamer.slingerrailgun.Anchor.Anchor;
import solegamer.slingerrailgun.Anchor.AnchorMovementPattern;
import solegamer.slingerrailgun.MovementPatterns.MovementPattern;
import solegamer.slingerrailgun.PlayerAggregate.AtomSlinger;
import solegamer.slingerrailgun.PowerUps.Explosive;
import solegamer.slingerrailgun.PowerUps.Item;
import solegamer.slingerrailgun.Wall.Wall;

/**
 * The SlingerRailGunView acts as a Mediator object for all the different components and
 * objects that populate the game
 */

public class SlingerRailGunView extends SurfaceView implements Runnable{

    Context ctx;

    private Thread gameThread = null;

    private SurfaceHolder gameHolder;

    private volatile boolean playing;

    //Checks whether the user can interact with the screen
    private boolean UserInputPhase = true;

    //The framerate at which the game will run
    private long fps = 60;

    //Dimensions of the screen
    private final int screen_width, screen_length;

    //Represents the main character;
    //There should only ever be one AtomSlinger in the whole
    //game
    private Bitmap atomSprite;

    //Keep track of all the anchors in and out of the game
    private Bitmap anchorSprite;

    //Graphic representation of walls inside the screen
    private Bitmap wallSprite;

    //Graphic representation of items inside the screen
    private Bitmap explosiveSprite;

    //Graphic representation of when the player is charging
    private Bitmap atomChargingSprite;

    //Graphic representation of when the player is fully charged
    private Bitmap atomChargedSprite;

    //Graphic representation of the backgrounds
    private Bitmap beginnings;
    private Bitmap beginningsBlueTree;
    private Bitmap blueTreeJourney;
    private Bitmap greenTreeJourney;

    //Point where the user made the initial touch
    private Coordinate mDown = new Coordinate (0,0);

    // If the player is charging, then use it to activate the charging animation
    private boolean playerIsCharging = false;

    /* Use the system time variables to determine long presses */
    private long startLongPress = System.currentTimeMillis();

    /* Time it takes to fully activate the Explosive ability*/
    private final int timeToActivateTheLongPress = 1500;

    /* Track all objects, spawning and removing objects as they come. Sorted in descending */
    private ArrayList<ObjectT> Object_Registry = new ArrayList<>();

    /* Track all Anchor objects */
    private ArrayList<Anchor> Anchor_Registry = new ArrayList<>();

    /* Track all Wall objects */
    private ArrayList<Wall> Wall_Registry = new ArrayList<>();

    /* Track all Item objects */
    private ArrayList<Item> Item_Registry = new ArrayList<>();

    /* Track all the backgrounds in use */
    private ArrayList<Background> Background_Registry = new ArrayList<>();

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

    public SlingerRailGunView (Context ctx, int screenX, int screen_length){

        super(ctx); this.ctx = ctx;

        playing = true;

        gameHolder = getHolder();

        //Let the View class know the dimensions of the screen
        this.screen_width = screenX; this.screen_length = screen_length;

        /* Make sure to call the Reset method to initialize all the objects in the game*/
        GameOver_Objects_Reset();

        anchorSprite = BitmapFactory.decodeResource(this.getResources(), R.drawable.anchor);

        atomSprite = BitmapFactory.decodeResource(this.getResources(), R.drawable.atom);

        atomChargingSprite = BitmapFactory.decodeResource(this.getResources(), R.drawable.atom_charging);

        atomChargedSprite = BitmapFactory.decodeResource(this.getResources(), R.drawable.atom_exploding);

        wallSprite = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall);

        explosiveSprite = BitmapFactory.decodeResource(this.getResources(), R.drawable.dynamite);

//        beginnings = BitmapFactory.decodeResource(this.getResources(), R.drawable.beginning);
//
//        beginningsBlueTree = BitmapFactory.decodeResource(this.getResources(), R.drawable.beginning_blue_tree);
//
        blueTreeJourney = BitmapFactory.decodeResource(this.getResources(), R.drawable.blue_tree_journey);

        greenTreeJourney = BitmapFactory.decodeResource(this.getResources(), R.drawable.green_tree_journey);
    }

    /**
     * At runtime, should the player lose, then the anchors need to be reinitialized
     * Creates all the necessary objects
     */
    void GameOver_Objects_Reset() {
        double distanceBetweenAnchorsPercentage = 0.625;
        minDistanceBetweenAnchors = (float) (distanceBetweenAnchorsPercentage * screen_length);

        Object_Registry.clear();
        Anchor_Registry.clear();
        Wall_Registry.clear();
        Item_Registry.clear();
        Background_Registry.clear();
        score = noOfAnchorsEverSpawned = AnchorCounter = 0;

        int placeOfInitWall = Util.rollDie(3) + 5, placeOfInitItem;
        do {
            placeOfInitItem = Util.rollDie(6) + 2;
        } while (placeOfInitItem == placeOfInitWall);

        /* Spawn the necessary items */
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

        /* Initialize the backgrounds */
        /* Add the beginning */
        Background_Registry.add( new Background(screen_width,screen_length,1));

        /* Add the beginning_blue_tree */
        Background_Registry.add( new Background(screen_width,screen_length,4));
    }

    @Override
    public void run(){
        while (playing){

            long startFrameTime = System.currentTimeMillis();

            update();

            draw();

            long timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1){
                fps = 1000 / timeThisFrame;
            }
        }
    }

    /**
     * The update method accounts for two states: 1. when the atom is attached to an achor,
     * reverting the atom's position back to an "equilibrium" state; 2. when the atom is
     * released, the sprite moves by itself
     *
     * WARNING! The only times updates to the atom's velocity are called are in this method
     */
    public void update(){

        //Get the instantaneous frames per second
        long instFPS = fps;

        /* If the user is able to interact with the player sprite*/
        if (UserInputPhase) {
            /* Make sure all objects all updated regardless */
            updateDuringUserInputPhase(instFPS);
        }
        /* Otherwise, make */
        else {
            updateNotInUserInputPhase(instFPS);
        }
    }

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

        for (int index = 0; index < Background_Registry.size(); index++){
            Background_Registry.get(index).updateScroll(scrollDistance);
        }
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

                        RectF temp = Util.getOverlappingRectF(player.getSpriteBoundary(), o.getSpriteBoundary());

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

    /**
     * Creates a row of wall objects
     */
    private void spawnWall() {
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

    /**
     * Creates an item
     */
    private void spawnItem (){
        spawnAnchor();
        float potentialY = getHighestAnchorYCoordinate() + minDistanceBetweenAnchors / 2;

        Item item;

        switch (Util.rollDie(4)) {
//            case 2:
//                item = new Sling(screen_width, potentialY);
//                break;
            default:
                item = new Explosive(screen_width, potentialY);
                break;
        }

        Item_Registry.add(item);
        Object_Registry.add(item);
    }

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

    /**
     *  Update the graphics of the game
     */
    public void draw(){
        if (gameHolder.getSurface().isValid()){
            Paint paint = new Paint();

            Canvas canvas = gameHolder.lockCanvas();

            //Color for the background
            canvas.drawColor(Color.argb(255, 255, 255, 255));

//            /* Draw the foreground */
//            Background greenTree = Background_Registry.get(0);
//
//            RectF originalRectF = new RectF(0,greenTree.getyClip(), screen_width,screen_length);
//            Rect originalRectFDrawn = new Rect (0,0, screen_width, screen_length - greenTree.getyClip());
//
//            RectF mirrorRectF = new RectF (0, 0, screen_width, greenTree.getyClip());
//            Rect mirrorRectFDrawn = new Rect (0, greenTree.getyClip(), screen_width, screen_length);
//
//            /* First, draw the normal background*/
//            canvas.drawBitmap(greenTreeJourney, originalRectFDrawn, originalRectF, paint);
//
//                /* Then draw its mirror to fill in the gap*/
//            canvas.drawBitmap(greenTreeJourney, mirrorRectFDrawn, mirrorRectF, paint);

//            if (greenTree.isNoLongerAtBeginnings()){
//                /* First, draw the normal background*/
//                canvas.drawBitmap(greenTreeJourney, originalRectFDrawn, originalRectF, paint);
//
//                /* Then draw its mirror to fill in the gap*/
//                canvas.drawBitmap(greenTreeJourney, mirrorRectFDrawn, mirrorRectF, paint);
//            } else {
//                /* First, draw the normal background*/
//                canvas.drawBitmap(beginnings, originalRectFDrawn, originalRectF, paint);
//
//                /* Then draw its mirror to fill in the gap*/
//                canvas.drawBitmap(greenTreeJourney, mirrorRectFDrawn, mirrorRectF, paint);
//            }
//
            /* Draw the background*/
//            Background blueTree = Background_Registry.get(1);
//
//            originalRectF = new RectF(0,blueTree.getyClip(), screen_width,screen_length);
//            originalRectFDrawn = new Rect (0,0, screen_width, screen_length - blueTree.getyClip());
//
//            mirrorRectF = new RectF (0, 0, screen_width, blueTree.getyClip());
//            mirrorRectFDrawn = new Rect (0, blueTree.getyClip(), screen_width, screen_length);
//
//            /* First, draw the normal background*/
//            canvas.drawBitmap(blueTreeJourney, originalRectFDrawn, originalRectF, paint);
//
//            /* Then draw its mirror to fill in the gap*/
//            canvas.drawBitmap(blueTreeJourney, mirrorRectFDrawn, mirrorRectF, paint);
//
//            if (blueTree.isNoLongerAtBeginnings()){
//                /* First, draw the normal background*/
//                canvas.drawBitmap(blueTreeJourney, originalRectFDrawn, originalRectF, paint);
//
//                /* Then draw its mirror to fill in the gap*/
//                canvas.drawBitmap(blueTreeJourney, mirrorRectFDrawn, mirrorRectF, paint);
//            } else {
//                /* First, draw the normal background*/
//                canvas.drawBitmap(beginningsBlueTree, originalRectFDrawn, originalRectF, paint);
//
//                /* Then draw its mirror to fill in the gap*/
//                canvas.drawBitmap(greenTreeJourney, mirrorRectFDrawn, mirrorRectF, paint);
//            }


            //Draw the tether if it exists
            paint.setColor(Color.argb(255,  249, 129, 0));

            RectF tetherCoordinates = player.getTether();
            canvas.drawLine(tetherCoordinates.left, tetherCoordinates.top,
                        tetherCoordinates.right, tetherCoordinates.bottom, paint);

            //Draw anchors
            for (Anchor a : Anchor_Registry ){
                canvas.drawBitmap(atomChargingSprite, null, a.getSpriteBoundary(), paint);
            }

            //Draw walls
            for (Wall w : Wall_Registry ){
                canvas.drawBitmap(wallSprite, null, w.getSpriteBoundary(), paint);
            }

            //Draw items
            for (Item i : Item_Registry ){
                canvas.drawBitmap(explosiveSprite, null, i.getSpriteBoundary(), paint);
            }

            //Draw the number of items that the player owns
            float side = screen_width /20;
            float   left = 85 * screen_width / 100,
                    top = screen_length / 50,
                    right = left + side,
                    bottom = top + side;

            RectF dimensionsOfItemTracker = new RectF(left, top, right, bottom);

            canvas.drawBitmap(explosiveSprite, null, dimensionsOfItemTracker, paint);

            paint.setTextSize(side);
            canvas.drawText(": " + player.checkNoOfExplosivesPlayerHolds(),
                    dimensionsOfItemTracker.right + 5, dimensionsOfItemTracker.bottom, paint);

            //Draw the player
            /* If the player has released in the explosive state or it has fully charged*/
            if (player.checkExplosiveState() ||
                    (playerIsCharging && System.currentTimeMillis() - startLongPress >= timeToActivateTheLongPress)){
                canvas.drawBitmap(atomChargedSprite, null, player.getSpriteBoundary(), paint);
            } else {
                if (playerIsCharging && System.currentTimeMillis() - startLongPress >= 750) {
                    canvas.drawBitmap(atomChargingSprite, null, player.getSpriteBoundary(), paint);
                } else {
                    canvas.drawBitmap(atomSprite, null, player.getSpriteBoundary(), paint);
                }
            }

            // Make the text a bit bigger
            paint.setTextSize(side);

            // Display the current fps on the screen
            canvas.drawText( "" + score , 50, 100, paint);

            paint = null;

            gameHolder.unlockCanvasAndPost(canvas);
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

    @Override
    public boolean onTouchEvent(MotionEvent mtnEvent) {

        switch (mtnEvent.getAction() & MotionEvent.ACTION_MASK) {

            /* The user has made the first click */
            case MotionEvent.ACTION_DOWN:
                /* If the player is anchored, then the UserInputPhase will be true */
                UserInputPhase = updateWhenUserTouchesScreen(mtnEvent.getX(), mtnEvent.getY());

                mDown = new Coordinate(mtnEvent.getX(), mtnEvent.getY());

                if (player.checkIfPlayerHoldsExplosive()) {
                    startLongPress = System.currentTimeMillis();
                    playerIsCharging = true;
                }
                break;

            /* The user is moving his pointer along the screen */
            case MotionEvent.ACTION_MOVE:
                int scrollThreshold = 8;
                float dx = Math.abs(mDown.x - mtnEvent.getX()), dy = Math.abs(mDown.y - mtnEvent.getY()),
                      distance = (float) Math.sqrt(dx*dx + dy*dy);

                boolean movementDetected = distance > scrollThreshold;

                if (movementDetected) {
                    UserInputPhase = updateWhenUserTouchesScreen(mtnEvent.getX(), mtnEvent.getY());

                    /* Reset the parameters that determine */
                    startLongPress = System.currentTimeMillis();
                    mDown = new Coordinate(mtnEvent.getX(), mtnEvent.getY());
                }

                playerIsCharging = player.checkIfPlayerHoldsExplosive() && !movementDetected;


                break;

            case MotionEvent.ACTION_UP:
                UserInputPhase = false;
                /* If the player has kept his pointer in one position for a certain time*/
                if (playerIsCharging && System.currentTimeMillis() - startLongPress >= timeToActivateTheLongPress){
                    /* then power-up the player*/
                    updateWhenUserLongPresses();
                }

                playerIsCharging = false;
                updateWhenUserReleasesScreen();
                break;

        }
        return true;
    }

    // If SimpleGameEngine Activity is paused/stopped
    // shutdown our thread.
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    // If SimpleGameEngine Activity is started then
    // start our thread.
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}
