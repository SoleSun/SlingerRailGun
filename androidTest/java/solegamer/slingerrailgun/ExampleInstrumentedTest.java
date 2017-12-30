package solegamer.slingerrailgun;

import android.content.Context;
import android.graphics.RectF;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import solegamer.slingerrailgun.Wall.Wall;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("solegamer.slingerrailgun", appContext.getPackageName());
    }

    @Test
    public void addition_isCorrect() throws Exception {

        ArrayList<Wall> registry = new ArrayList<>();

        registry.add(new Wall (1440, new Coordinate(648, 500)));
        registry.add(new Wall (1440, new Coordinate(792, 500)));

        RectF player = new RectF(648, 500, 792, 644);

        RectF overlappingRegion = new RectF(-1,-1,-1,-1);

        for (Wall w : registry){
            float dx, dy;
                    /* Determine the new coordinates of the overlapping rectangle*/
            float left, top, right, bottom;

            /* Player is right more*/
            if (Util.getRectFCenterX(player) >= w.getCoordinate().x) {
                dx = w.getSpriteBoundary().right - player.left;
                left = player.left;
                right = player.left + dx;
            }
            else {
                dx = player.right - w.getSpriteBoundary().left;
                left = w.getSpriteBoundary().left;
                right = w.getSpriteBoundary().left + dx;
            }

            /* Player is bottom more*/
            if (Util.getRectFCenterY(player) >= w.getCoordinate().y) {
                dy = w.getSpriteBoundary().bottom - player.top;
                assertTrue(dy == 72);
                top = player.top;
                bottom = player.top + dy;
            }
            else {
                dy = player.bottom - w.getSpriteBoundary().top;
                top = w.getSpriteBoundary().top;
                bottom = w.getSpriteBoundary().top + dy;
            }

                    /* Determine the new dimensions of the overlapping RectF region*/
            overlappingRegion.left =
                    overlappingRegion.left <= left && overlappingRegion.left > 0 ? overlappingRegion.left : left;
            overlappingRegion.top =
                    overlappingRegion.top <= top && overlappingRegion.top > 0? overlappingRegion.top : top;
            overlappingRegion.bottom =
                    overlappingRegion.bottom >= bottom && overlappingRegion.bottom > 0 ? overlappingRegion.bottom : bottom;
            overlappingRegion.right =
                    overlappingRegion.right >= right && overlappingRegion.right > 0? overlappingRegion.right : right;

            /* Found the whole overlapping region*/
            if (Util.getRectFLength(overlappingRegion) == 144 ||
                    Util.getRectFHeight(overlappingRegion) == 144){
                break;
            }
        }

        if (Util.getRectFHeight(overlappingRegion) >= Util.getRectFLength(overlappingRegion)) {
            System.out.println("Reversing y-velocity");
        } else {
            System.out.println("Reversing x-velocity");
        }

        RectF tempOne = new RectF (0,0,100,100);
        RectF tempTwo = new RectF (50,0,150,100);

        tempOne.union(tempTwo);

        assertTrue(tempOne.left == 0);
        assertTrue(tempOne.top == 0);
        assertTrue(tempOne.right == 150);
        assertTrue(tempOne.bottom == 100);
    }
}
