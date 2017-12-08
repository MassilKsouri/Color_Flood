package p8.demo.colorflood;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Class of a color button
 * Found in : https://stackoverflow.com/questions/5779215/androidhow-to-add-a-button-in-surface-view
 */

class MenuButton {
    RectF btn_rect;
    private Matrix btn_matrix = new Matrix();

    private float width;
    private float height;
    private Bitmap bg;

    MenuButton(float width, float height, Bitmap bg) {
        this.width = width;
        this.height = height;
        this.bg = bg;
        btn_rect = new RectF(0, 0, width, height);
    }
    void setPosition(float x, float y) {
        btn_matrix.setTranslate(x, y);
        btn_matrix.mapRect(btn_rect);
    }
    void draw(Canvas canvas) {
        canvas.drawBitmap(bg, btn_matrix, null);
    }

}
