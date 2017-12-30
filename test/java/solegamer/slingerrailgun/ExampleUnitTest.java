package solegamer.slingerrailgun;

import android.graphics.RectF;

import org.junit.Test;

import java.util.ArrayList;

import solegamer.slingerrailgun.Wall.Wall;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        ArrayList <Wall> registry = new ArrayList<>();

        registry.add(new Wall (1440, new Coordinate(648, 500)));
        registry.add(new Wall (1440, new Coordinate(792, 500)));

        RectF player = new RectF(648, 450, 792, 594);

        RectF overlappingRegion = new RectF(-1,-1,-1,-1);

        System.out.println("Player Coordinate x: " + Util.getRectFCenterX(player));
        System.out.println("Player Coordinate y: " + Util.getRectFCenterY(player));

        for (Wall w : registry){
            float dx, dy;
                    /* Determine the new coordinates of the overlapping rectangle*/
            float left, top, right, bottom;

            System.out.println("Wall Coordinate x: " + w.getCoordinate().x);
            System.out.println("Wall Coordinate y: " + w.getCoordinate().y);

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

            System.out.println("Overlapping region left: " + overlappingRegion.left);
            System.out.println("Overlapping region top: " + overlappingRegion.top);
            System.out.println("Overlapping region bottom: " + overlappingRegion.bottom);
            System.out.println("Overlapping region right: " + overlappingRegion.right);

            /* Found the whole overlapping region*/
            if (Util.getRectFLength(overlappingRegion) == 144 ||
                    Util.getRectFHeight(overlappingRegion) == 144){
                break;
            }
        }

        System.out.println("Overlapping region left: " + overlappingRegion.left);
        System.out.println("Overlapping region top: " + overlappingRegion.top);
        System.out.println("Overlapping region bottom: " + overlappingRegion.bottom);
        System.out.println("Overlapping region right: " + overlappingRegion.right);

        if (Util.getRectFHeight(overlappingRegion) >= Util.getRectFLength(overlappingRegion)) {
            System.out.println("Reversing y-velocity");
        } else {
            System.out.println("Reversing x-velocity");
        }
    }


    }
