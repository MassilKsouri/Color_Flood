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

/**
 * CLass representing the Color Flood game
 */
public class ColorFlood extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Bitmap win;
    private Bitmap lost;


    // the current level
    private int level = 1;
    // the current turn
    private int turns = 0;

    // array representing the board
    private Case[][] carte;
    private Queue<Case> caseActive;
    private List<Case> caseFound;
    private int nbCaseToFind;

    // ancres pour pouvoir centrer la carte du jeu
    private int carteTopAnchor;                   // coordonnées en Y du point d'ancrage de notre carte
    private int carteLeftAnchor;                  // coordonnées en X du point d'ancrage de notre carte

    // width and height of the board
    private static final int carteWidth = 8;
    private static final int carteHeight = 8;
    // size of each case in the board
    private static final int carteTileSize = 40;

    // constant representing the color of each case
    // map each color code to a bitmap
    Map<Integer, Bitmap> cst2Bitmap = new HashMap<>();
    Map<Integer, Bitmap> cst2ButtonBitmap_lvl1 = new HashMap<>();
    Map<Integer, Bitmap> cst2ButtonBitmap_lvl2 = new HashMap<>();


    // color codes
    private static final int CST_red = 0;
    private static final int CST_green = 2;
    private static final int CST_yellow = 3;
    private static final int CST_blue = 4;
    private static final int CST_purple = 5;

    private int[] colors_lvl1 = {CST_red, CST_blue, CST_green, CST_yellow};
    private int nbOfColors_lvl1 = colors_lvl1.length;
    private int[] colors_lvl2 = {CST_red, CST_blue, CST_green, CST_yellow, CST_purple};
    private int nbOfColors_lvl2 = colors_lvl2.length;

    // the clickable buttons
    private ColorButton colorButtons_lvl1[] = new ColorButton[nbOfColors_lvl1];
    private ColorButton colorButtons_lvl2[] = new ColorButton[nbOfColors_lvl2];
    // array containing the color for each case in the board
    private int[][] ref_lvl1 = createLevel(colors_lvl1);
    private int[][] ref_lvl2 = createLevel(colors_lvl2);
    private Thread cv_thread;
    SurfaceHolder holder;
    Paint paint;
    int maxTurns = 20;
    int score = 1000;
    long timeBegin = System.currentTimeMillis();

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
        Resources mRes = context.getResources();
        Bitmap red = BitmapFactory.decodeResource(mRes, R.drawable.red);
        Bitmap green = BitmapFactory.decodeResource(mRes, R.drawable.green);
        Bitmap yellow = BitmapFactory.decodeResource(mRes, R.drawable.yellow);
        Bitmap blue = BitmapFactory.decodeResource(mRes, R.drawable.blue);
        Bitmap purple = BitmapFactory.decodeResource(mRes, R.drawable.purple);
        Bitmap redButton_lvl1 = BitmapFactory.decodeResource(mRes, R.drawable.red_button_lvl1);
        Bitmap greenButton_lvl1 = BitmapFactory.decodeResource(mRes, R.drawable.green_button_lvl1);
        Bitmap yellowButton_lvl1 = BitmapFactory.decodeResource(mRes, R.drawable.yellow_button_lvl1);
        Bitmap blueButton_lvl1 = BitmapFactory.decodeResource(mRes, R.drawable.blue_button_lvl1);
        Bitmap redButton_lvl2 = BitmapFactory.decodeResource(mRes, R.drawable.red_button_lvl2);
        Bitmap greenButton_lvl2 = BitmapFactory.decodeResource(mRes, R.drawable.green_button_lvl2);
        Bitmap yellowButton_lvl2 = BitmapFactory.decodeResource(mRes, R.drawable.yellow_button_lvl2);
        Bitmap blueButton_lvl2 = BitmapFactory.decodeResource(mRes, R.drawable.blue_button_lvl2);
        Bitmap purpleButton_lvl2 = BitmapFactory.decodeResource(mRes, R.drawable.purple_button_lvl2);
        win = BitmapFactory.decodeResource(mRes, R.drawable.win);
        lost = BitmapFactory.decodeResource(mRes, R.drawable.lost);


        // colors for the 1st level
        cst2Bitmap.put(CST_red, red);
        cst2Bitmap.put(CST_green, green);
        cst2Bitmap.put(CST_yellow, yellow);
        cst2Bitmap.put(CST_blue, blue);
        cst2Bitmap.put(CST_purple, purple);

        // bitmap for the buttons of the first level
        cst2ButtonBitmap_lvl1.put(CST_red, redButton_lvl1);
        cst2ButtonBitmap_lvl1.put(CST_green, greenButton_lvl1);
        cst2ButtonBitmap_lvl1.put(CST_yellow, yellowButton_lvl1);
        cst2ButtonBitmap_lvl1.put(CST_blue, blueButton_lvl1);

        // bitmap for the buttons of the second level
        cst2ButtonBitmap_lvl2.put(CST_red, redButton_lvl2);
        cst2ButtonBitmap_lvl2.put(CST_green, greenButton_lvl2);
        cst2ButtonBitmap_lvl2.put(CST_yellow, yellowButton_lvl2);
        cst2ButtonBitmap_lvl2.put(CST_blue, blueButton_lvl2);
        cst2ButtonBitmap_lvl2.put(CST_purple, purpleButton_lvl2);

        // init game parameters
        initparameters(level);
        // create the thread
        cv_thread = new Thread(this);
        // prise de focus pour gestion des touches
        setFocusable(true);
    }

    /**
     * Generates a grid filled with random colors_lvl1
     *
     * @param colors the colors_lvl1 to chose from to fill the grid
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
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(0xFFFFFF00);

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
                    Case newCase = new Case(ref_lvl1[j][i], j, i);
                    carte[j][i] = newCase;
                }
            }
            nbCaseToFind = carteHeight * carteWidth;
        }
        // lvl 2
        else if (a == 2){
            for (int i = 0; i < carteHeight; i++) {
                for (int j = 0; j < carteWidth; j++) {
                    Case newCase = new Case(ref_lvl2[j][i], j, i);
                    carte[j][i] = newCase;
                }
            }
        }
    }

    // draw win if won
    private void paintwin(Canvas canvas) {
        canvas.drawBitmap(win, carteLeftAnchor + 3 * carteTileSize, carteTopAnchor + 4 * carteTileSize, null);
    }
    private void paintlost(Canvas canvas) {
        canvas.drawBitmap(lost, carteLeftAnchor + 3 * carteTileSize, carteTopAnchor + 4 * carteTileSize, null);
    }

    // draw the board
    private void paintcarte(Canvas canvas) {
        if (level == 1) {
            for (int i = 0; i < carteHeight; i++) {
                for (int j = 0; j < carteWidth; j++) {
                    Bitmap currentBitmap = cst2Bitmap.get(carte[i][j].CSTcolor);
                    canvas.drawBitmap(currentBitmap, carteLeftAnchor + j * carteTileSize, carteTopAnchor + i * carteTileSize, null);
                }
            }
            // creates the clickable buttons
            for (int i = 0; i < colors_lvl1.length; i ++) {
                ColorButton colorButton = new ColorButton(80, 80, cst2ButtonBitmap_lvl1.get(colors_lvl1[i]), colors_lvl1[i]);
                colorButton.setPosition(80 * i, getHeight() - carteTileSize);
                colorButton.draw(canvas);
                this.colorButtons_lvl1[i] = colorButton;
            }
        }
        if (level == 2) {
            for (int i = 0; i < carteHeight; i++) {
                for (int j = 0; j < carteWidth; j++) {
                    Bitmap currentBitmap = cst2Bitmap.get(carte[i][j].CSTcolor);
                    canvas.drawBitmap(currentBitmap, carteLeftAnchor + j * carteTileSize, carteTopAnchor + i * carteTileSize, null);
                }
            }
            // creates the clickable buttons
            for (int i = 0; i < colors_lvl2.length; i ++) {
                ColorButton colorButton = new ColorButton(60, 60, cst2ButtonBitmap_lvl2.get(colors_lvl2[i]), colors_lvl2[i]);
                colorButton.setPosition(carteLeftAnchor + 60 * i, getHeight() - carteTileSize);
                colorButton.draw(canvas);
                this.colorButtons_lvl2[i] = colorButton;
            }
        }


    }

    // game is won if every case is found (ie of the same color as the others)
    private boolean isWon() {
        return (caseFound.size() == nbCaseToFind);
    }
    // game is lost if the number of turns exceeds maxturns
    private  boolean isLost() {
        return (turns > maxTurns);
    }

    // draw the board and the turns
    private void nDraw(Canvas canvas) {
        String turnsToDisplay = "TURNS : "
                + Integer.toString(turns) + " / "
                + Integer.toString(maxTurns);
        String levelToDisplay = "LEVEL : "
                + Integer.toString(level);
        String scoreToDisplay = "SCORE : "
                + Integer.toString(score);
        long timeNow = System.currentTimeMillis() - timeBegin;
        String timeToDisplay = "Time : "
                + Long.toString(timeNow / 1000) + "\"";
        canvas.drawRGB(44, 44, 44);
        canvas.drawText(levelToDisplay, 20, 20, paint);
        canvas.drawText(turnsToDisplay, 20, 40, paint);
        canvas.drawText(scoreToDisplay, 160, 20, paint);
        canvas.drawText(timeToDisplay, 160, 40, paint);


        if (isWon()) {
            //paintcarte(canvas);
            paintwin(canvas);
        } else if(isLost()) {
            //paintcarte(canvas);
            paintlost(canvas);
        }
        else {
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
        boolean in = true;
        while (in) {
            try {
                cv_thread.sleep(40);
                if (!isWon() & !isLost()) score -= 1;
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
        turns += 1;
    }


    // fonction permettant de recuperer les evenements tactiles
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("-> FCT <-", "onTouchEvent: " + event.getX());
        float pos_x = event.getX();
        float pos_y = event.getY();

        // check if the game is won
        if (isWon()) {
            int x = (getWidth() / 2) - (win.getWidth() / 2);
            int y = (getHeight() / 2) - (win.getHeight() / 2);
            // check that the user touch the button and launch the next level
            if (event.getX() > x && event.getX() < x + win.getWidth() && event.getY() > y && event.getY() < y + win.getHeight()) {
                level += 1;
                score += level * 1000;
                int turnsLeft = maxTurns - turns;
                maxTurns += turnsLeft;
                turns = 0;
                initparameters(level);
            }
        }
        // check if the game is lost
        else if (isLost()) {
            int x = (getWidth() / 2) - (lost.getWidth() / 2);
            int y = (getHeight() / 2) - (lost.getHeight() / 2);
            // check that the user touch the button and launch the first level
            if (event.getX() > x && event.getX() < x + lost.getWidth() && event.getY() > y && event.getY() < y + lost.getHeight()) {
                level = 1;
                maxTurns = 20;
                turns = 0;
                score = 1000;
                initparameters(level);
            }
        }
        // itere through every button to detect a touch event
        else if (level == 1) {
            for (ColorButton aColorButtons_lvl1 : colorButtons_lvl1) {
                if (aColorButtons_lvl1.btn_rect.contains(pos_x, pos_y)) {
                    int userColor = aColorButtons_lvl1.colorID;
                    oneTurn(userColor);
                }
            }
        }
        else if (level == 2) {
            for (ColorButton aColorButtons_lvl2 : colorButtons_lvl2) {
                if (aColorButtons_lvl2.btn_rect.contains(pos_x, pos_y)) {
                    int userColor = aColorButtons_lvl2.colorID;
                    oneTurn(userColor);
                }
            }
        }

        return super.onTouchEvent(event);
    }
}