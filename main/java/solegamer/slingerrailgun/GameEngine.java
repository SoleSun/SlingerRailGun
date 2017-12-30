package solegamer.slingerrailgun;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;


public class GameEngine extends Activity{

    SlingerRailGunView GameView;

    @TargetApi(17)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Get the smartphone's details
        Display display = getWindowManager().getDefaultDisplay();
        //The resolution of the points needs to go into a Point object
        Point pt = new Point();
        try {
            display.getRealSize(pt);
        } catch (NoSuchMethodError e) {
            display.getSize(pt);
        }

        // Initialize gameView and set it as the view
        GameView = new SlingerRailGunView(this, pt.x, pt.y);
        setContentView(GameView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        GameView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        GameView.pause();
    }
}
