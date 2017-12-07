package p8.demo.colorflood;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ColorFlood extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    // Declaration des images
    private Bitmap vide;
    private Bitmap win;
    private Bitmap red;
    private Bitmap blue;
    private Bitmap green;
    private Bitmap yellow;
    private Bitmap redButton;
    private Bitmap blueButton;
    private Bitmap greenButton;
    private Bitmap yellowButton;
    private int level = 1;

    // Declaration des objets Ressources et Context permettant d'accéder aux ressources de notre application et de les charger
    private Resources mRes;
    private Context mContext;

    // array representing the board
    private Case[][] carte;
    private Queue<Case> caseActive;
    private List<Case> caseFound;
    private int nbCaseToFind;

    // ancres pour pouvoir centrer la carte du jeu
    private int carteTopAnchor;                   // coordonnées en Y du point d'ancrage de notre carte
    private int carteLeftAnchor;                  // coordonnées en X du point d'ancrage de notre carte

    // width and height of the board
    private static final int carteWidth = 6;
    private static final int carteHeight = 6;
    // size of each case in the board
    private static final int carteTileSize = 40;

    // constant representing the color of each case
    Map<Integer, Bitmap> cst2Bitmap = new HashMap<>();
    Map<Integer, Bitmap> cst2ButtonBitmap = new HashMap<>();
    private static final int CST_red = 0;
    private static final int CST_vide = 1;
    private static final int CST_green = 2;
    private static final int CST_yellow = 3;
    private static final int CST_blue = 4;

    private int[] colors = {CST_red, CST_blue, CST_green, CST_yellow};
    private int nbOfColors = colors.length;

    // the clickable buttons
    private ColorButton colorButtons[] = new ColorButton[nbOfColors];
    // array containing the color for each case in the board
    private int[][] ref = createLevel(colors);
    // thread
    private boolean in = true;
    private Thread cv_thread;
    SurfaceHolder holder;
    Paint paint;

    /**
     * The constructor called from the main JetBoy activity
     *
     * @param context the context
     * @param attrs the attributes
     */
    public ColorFlood(Context context, AttributeSet attrs) {
        super(context, attrs);
        // permet d'ecouter les surfaceChanged, surfaceCreated, surfaceDestroyed
        holder = getHolder();
        holder.addCallback(this);

        // load images
        mContext = context;
        mRes = mContext.getResources();
        red = BitmapFactory.decodeResource(mRes, R.drawable.red);
        green = BitmapFactory.decodeResource(mRes, R.drawable.green);
        yellow = BitmapFactory.decodeResource(mRes, R.drawable.yellow);
        blue = BitmapFactory.decodeResource(mRes, R.drawable.blue);
        redButton = BitmapFactory.decodeResource(mRes, R.drawable.red_button);
        greenButton = BitmapFactory.decodeResource(mRes, R.drawable.green_button);
        yellowButton = BitmapFactory.decodeResource(mRes, R.drawable.yellow_button);
        blueButton = BitmapFactory.decodeResource(mRes, R.drawable.blue_button);
        vide = BitmapFactory.decodeResource(mRes, R.drawable.vide);
        win = BitmapFactory.decodeResource(mRes, R.drawable.win);

        cst2Bitmap.put(CST_red, red);
        cst2Bitmap.put(CST_green, green);
        cst2Bitmap.put(CST_yellow, yellow);
        cst2Bitmap.put(CST_blue, blue);
        cst2ButtonBitmap.put(CST_red, redButton);
        cst2ButtonBitmap.put(CST_green, greenButton);
        cst2ButtonBitmap.put(CST_yellow, yellowButton);
        cst2ButtonBitmap.put(CST_blue, blueButton);
        // init game parameters
        initparameters(level);
        // create the thread
        cv_thread = new Thread(this);
        // prise de focus pour gestion des touches
        setFocusable(true);
    }

    /**
     * Generates a grid filled with random colors
     *
     * @param colors the colors to chose from to fill the grid
     * @return a colored grid
     */
    private int[][] createLevel(int[] colors) {
        Random random_color = new Random();
        int ref[][] = new int[carteWidth][carteHeight];
        for (int i = 0; i < carteWidth; i++) {
            for (int j = 0; j < carteHeight; j++) {
                // select a random color
                int color = colors[random_color.nextInt(colors.length)];
                ref[i][j] = color;
            }
        }
        return ref;
    }


    /**
     * Initializes the level parameters
     * @param a the level
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void initparameters(int a) {
        paint = new Paint();
        paint.setColor(0xff0000);
        paint.setDither(true);
        paint.setColor(0xFFFFFF00);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        paint.setTextAlign(Paint.Align.LEFT);

        // create the board
        carte = new Case[carteHeight][carteWidth];
        caseActive = new ConcurrentLinkedQueue<>();
        caseFound = new ArrayList<>();
        loadlevel(a);

        // create the first active case
        caseActive.add(carte[0][0]);
        // find all the first active cases
        regroup();
        carteTopAnchor = (getHeight() - carteHeight * carteTileSize) / 2;
        carteLeftAnchor = (getWidth() - carteWidth * carteTileSize) / 2;

        if ((cv_thread != null) && (!cv_thread.isAlive())) {
            cv_thread.start();
            Log.e("-FCT-", "cv_thread.start()");
        }

    }

    /**
     * Create the board
     * @param a the level of the board
     */
    private void loadlevel(int a) {
        // lvl 1
        if (a == 1) {
            // create the board
            for (int i = 0; i < carteHeight; i++) {
                for (int j = 0; j < carteWidth; j++) {
                    Case newCase = new Case(ref[j][i], j, i);
                    carte[j][i] = newCase;
                }
            }
            nbCaseToFind = carteHeight * carteWidth;
        }
        // lvl 2
        else {
            for (int i = 0; i < carteHeight; i++) {
                for (int j = 0; j < carteWidth; j++) {
                    Case newCase = new Case(ref[j][i], j, i);
                    carte[j][i] = newCase;
                }
            }
        }
    }

    // draw win if won
    private void paintwin(Canvas canvas) {
        canvas.drawBitmap(win, carteLeftAnchor + 3 * carteTileSize, carteTopAnchor + 4 * carteTileSize, null);
    }

    // draw the board
    private void paintcarte(Canvas canvas) {
        for (int i = 0; i < carteHeight; i++) {
            for (int j = 0; j < carteWidth; j++) {
                Bitmap currentBitmap = cst2Bitmap.get(carte[i][j].CSTcolor);
                canvas.drawBitmap(currentBitmap, carteLeftAnchor + j * carteTileSize, carteTopAnchor + i * carteTileSize, null);
            }
        }
        // creates the clickable buttons
        for (int i = 0; i < colors.length; i ++) {
            ColorButton colorButton = new ColorButton(80, 80, cst2ButtonBitmap.get(colors[i]), colors[i]);
            colorButton.setPosition(carteTileSize * 2 * i, getHeight() - carteTileSize * 2);
            colorButton.draw(canvas);
            this.colorButtons[i] = colorButton;
        }
    }

    // game is won if every case is found (ie of the same color as the others)
    private boolean isWon() {
        return (caseFound.size() == nbCaseToFind);
    }

    // draw the board
    private void nDraw(Canvas canvas) {
        canvas.drawRGB(44, 44, 44);
        if (isWon()) {
            paintcarte(canvas);
            paintwin(canvas);
            // si gagnant, on incr la valeur du niveau de 1
            level = level + 1;

        } else {
            paintcarte(canvas);
        }

    }

    // callback sur le cycle de vie de la surfaceview
    // TODO gérer le cas ou l'utilisateur pause l appli puis la relance car dans notre cas il n'y a aucune sauvegarde de la situation avant la pause
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("-> FCT <-", "surfaceChanged " + width + " - " + height);
        initparameters(level);
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        Log.i("-> FCT <-", "surfaceCreated");
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.i("-> FCT <-", "surfaceDestroyed");
    }

    /**
     * run (run du thread cr��)
     * on endort le thread, on modifie le compteur d'animation, on prend la main pour dessiner et on dessine puis on lib�re le canvas
     */
    // lancé automatiquement car SokobanView implements Runnable
    public void run() {
        Canvas c = null;
        while (in) {
            try {
                cv_thread.sleep(40);
                try {
                    c = holder.lockCanvas(null);
                    nDraw(c);
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
            } catch (Exception e) {
                Log.e("-> RUN <-", "PB DANS RUN");
            }
        }
    }

    /**
     * Change the color of every found case
     * @param colorID the color to change the cases to
     */
    private void changeColor(int colorID) {
        for (Case current_case : caseFound) {
            current_case.CSTcolor = colorID;
            caseActive.add(current_case);
        }
    }

    /**
     * Regroup all the cases of the same color and near the actives cases
     * and add them to the caseFound group
     */
    private void regroup() {
        while (this.caseActive.peek() != null) {
            Case current_case = caseActive.remove();
            if (!caseFound.contains(current_case)) caseFound.add(current_case);
            if (current_case.x < carteWidth - 1) {
                Case neighboor_down = carte[current_case.x + 1][current_case.y];
                if (current_case.CSTcolor == neighboor_down.CSTcolor && !caseFound.contains(neighboor_down)) {
                    caseActive.add(neighboor_down);
                }
            }
            if (current_case.x > 0) {
                Case neighboor_up = carte[current_case.x - 1][current_case.y];
                if (current_case.CSTcolor == neighboor_up.CSTcolor && !caseFound.contains(neighboor_up)) {
                    caseActive.add(neighboor_up);
                }
            }
            // si il est possible d avoir un voisin a gauche
            if (current_case.y > 0) {
                Case neighboor_left = carte[current_case.x][current_case.y - 1];
                if (current_case.CSTcolor == neighboor_left.CSTcolor && !caseFound.contains(neighboor_left)) {
                    caseActive.add(neighboor_left);
                }
            }
            if (current_case.y < carteHeight - 1) {
                Case neighboor_right = carte[current_case.x][current_case.y + 1];
                if (current_case.CSTcolor == neighboor_right.CSTcolor && !caseFound.contains(neighboor_right)) {
                    caseActive.add(neighboor_right);
                    caseFound.add(neighboor_right);
                }
            }
        }
    }

    /**
     * One turn of the game
     *
     * @param colorID the color chosen by the user
     */
    private void oneTurn(int colorID) {
        changeColor(colorID);
        regroup();
    }

    // fonction permettant de recuperer les evenements tactiles
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("-> FCT <-", "onTouchEvent: " + event.getX());
        float pos_x = event.getX();
        float pos_y = event.getY();

        // itere through every button to detect a touch event
        for (int i = 0; i < colorButtons.length; i++) {
            if (colorButtons[i].btn_rect.contains(pos_x, pos_y)) {
                int userColor = colorButtons[i].colorID;
                oneTurn(userColor);
            }
        }
        // check if the game is won
        if (isWon()) {
            int x = (getWidth() / 2) - (win.getWidth() / 2);
            int y = (getHeight() / 2) - (win.getHeight() / 2);
            // vérif que l'utilisateur appuie sur le bouton gagné pour lancer le niveau suivant
            if (event.getX() > x && event.getX() < x + win.getWidth() && event.getY() > y && event.getY() < y + win.getHeight()) {
                initparameters(level);
            }
        }
        return super.onTouchEvent(event);
    }
}