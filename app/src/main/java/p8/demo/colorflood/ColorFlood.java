package p8.demo.colorflood;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import java.util.Random;

public class ColorFlood extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // Declaration des images
    private Bitmap 		vide;
    private Bitmap 		win;
    private Bitmap      red;
    private Bitmap      blue;
    private Bitmap      green;
    private Bitmap      yellow;
    private Bitmap      redButton;
    private Bitmap      blueButton;
    private Bitmap      greenButton;
    private Bitmap      yellowButton;
    int level=1;


    // Declaration des objets Ressources et Context permettant d'accéder aux ressources de notre application et de les charger
    private Resources 	mRes;
    private Context 	mContext;

    // tableau modelisant la carte du jeu
    // TODO gérer plusieurs cartes avec carte[0][i][j] pour 1ere carte, carte[1][i][j] pour 2eme carte etc...
    int[][] carte;

    // ancres pour pouvoir centrer la carte du jeu
    int        carteTopAnchor;                   // coordonnées en Y du point d'ancrage de notre carte
    int        carteLeftAnchor;                  // coordonnées en X du point d'ancrage de notre carte

    // taille de la carte
    static final int    carteWidth    = 6;
    static final int    carteHeight   = 6;
    static final int    carteTileSize = 40;

    // constante modelisant les differentes types de cases
    static final int    CST_red = 0;
    static final int    CST_vide      = 1;
    static final int    CST_green = 2;
    static final int    CST_yellow = 3;
    static final int    CST_blue = 4;


    // l'ensemble des couleurs dans notre jeu
    private int[] colors = {CST_red, CST_blue, CST_green, CST_yellow};
    int nbOfColors = colors.length;
    // les boutons clickables
    ColorButton colorButtons[] = new ColorButton[nbOfColors];

    // tableau de reference du terrain
    int [][] ref    = createLevel(colors);

    int [][] ref2    = createLevel(colors);



    /* compteur et max pour animer les zones d'arriv�e des diamants */
    int currentStepZone = 0;
    int maxStepZone     = 4;

    // thread utiliser pour animer les zones de depot des diamants
    private     boolean in      = true;
    private     Thread  cv_thread;
    SurfaceHolder holder;

    Paint paint;

    /**
     * The constructor called from the main JetBoy activity
     *
     * @param context
     * @param attrs
     */
    public ColorFlood(Context context, AttributeSet attrs) {
        super(context, attrs);



        // permet d'ecouter les surfaceChanged, surfaceCreated, surfaceDestroyed
        holder = getHolder();
        holder.addCallback(this);

        // chargement des images
        mContext	= context;
        mRes 		= mContext.getResources();
        red = BitmapFactory.decodeResource(mRes, R.drawable.red);
        green = BitmapFactory.decodeResource(mRes, R.drawable.green);
        yellow = BitmapFactory.decodeResource(mRes, R.drawable.yellow);
        blue = BitmapFactory.decodeResource(mRes, R.drawable.blue);
        redButton = BitmapFactory.decodeResource(mRes, R.drawable.red_button);
        greenButton = BitmapFactory.decodeResource(mRes, R.drawable.green_button);
        yellowButton = BitmapFactory.decodeResource(mRes, R.drawable.yellow_button);
        blueButton = BitmapFactory.decodeResource(mRes, R.drawable.blue_button);
        vide 		= BitmapFactory.decodeResource(mRes, R.drawable.vide);
        win 		= BitmapFactory.decodeResource(mRes, R.drawable.win);


        // initialisation des parmametres du jeu
        initparameters(level);

        // creation du thread
        cv_thread   = new Thread(this);
        // prise de focus pour gestion des touches
        setFocusable(true);


    }

    /**
     * Generates a grid filled with random colors
     * @param colors the colors to chose from to fill the grid
     * @return a colored grid
     */
    private int[][] createLevel(int[] colors) {
        Random random_color = new Random();
        int ref[][] = new int[this.carteWidth][this.carteHeight];
        for (int i = 0; i < this.carteWidth; i++) {
            for (int j = 0; j < this.carteHeight; j ++) {
                // sélection d'une couleur aléatoire dans colors
                int color = colors[random_color.nextInt(colors.length)];
                ref[i][j] = color;
            }
        }
        return  ref;
    }

    // chargement du niveau a partir du tableau de reference du niveau
    private void loadlevel( int a) {
        // lvl 1
        if (a==1){
            for (int i=0; i< carteHeight; i++) {
                for (int j=0; j< carteWidth; j++) {
                    carte[j][i]= ref[j][i];
                }
            }
        }
        // lvl 2
        else{
            for (int i=0; i< carteHeight; i++) {
                for (int j=0; j< carteWidth; j++) {
                    carte[j][i]= ref2[j][i];
                }
            }

        }
    }


    // initialisation du jeu
    public void initparameters( int a) {
        paint = new Paint();
        paint.setColor(0xff0000);

        paint.setDither(true);
        paint.setColor(0xFFFFFF00);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        paint.setTextAlign(Paint.Align.LEFT);
        carte           = new int[carteHeight][carteWidth];
        loadlevel(a);
        carteTopAnchor  = (getHeight()- carteHeight*carteTileSize)/2;
        carteLeftAnchor = (getWidth()- carteWidth*carteTileSize)/2;

        if ((cv_thread!=null) && (!cv_thread.isAlive())) {
            cv_thread.start();
            Log.e("-FCT-", "cv_thread.start()");
        }

    }


    // dessin du gagne si gagne
    private void paintwin(Canvas canvas) {
        canvas.drawBitmap(win, carteLeftAnchor+ 3*carteTileSize, carteTopAnchor+ 4*carteTileSize, null);
    }

    // dessin de la carte du jeu
    private void paintcarte(Canvas canvas) {
        for (int i=0; i< carteHeight; i++) {
            for (int j=0; j< carteWidth; j++) {
                switch (carte[i][j]) {
                    case CST_red:
                        canvas.drawBitmap(red, carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                    case CST_vide:
                        canvas.drawBitmap(vide,carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                    case CST_blue:
                        canvas.drawBitmap(blue, carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                    case CST_green:
                        canvas.drawBitmap(green, carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                    case CST_yellow:
                        canvas.drawBitmap(yellow, carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                }
            }
        }
        // création des boutons sur lesquels l'utilisateur peut clicker
        ColorButton rButton = new ColorButton(80,80, redButton, CST_red);
        rButton.setPosition(0, getHeight() - carteTileSize * 2);
        rButton.draw(canvas);
        ColorButton bButton = new ColorButton(80,80, blueButton, CST_blue);
        bButton.setPosition(carteTileSize * 2, getHeight() - carteTileSize * 2);
        bButton.draw(canvas);
        ColorButton gButton = new ColorButton(80,80, greenButton, CST_green);
        gButton.setPosition(carteTileSize * 4, getHeight() - carteTileSize * 2);
        gButton.draw(canvas);
        ColorButton yButton = new ColorButton(80,80, yellowButton, CST_yellow);
        yButton.setPosition(carteTileSize * 6, getHeight() - carteTileSize * 2);
        yButton.draw(canvas);
        this.colorButtons[0] = rButton;
        this.colorButtons[1] = bButton;
        this.colorButtons[2] = gButton;
        this.colorButtons[3] = yButton;

    }


    // permet d'identifier si la partie est gagnee (tous les diamants à leur place)
    private boolean isWon() {
        return false;
    }

    // dessin du jeu (fond uni, en fonction du jeu gagne ou pas dessin du plateau et du joueur des diamants et des fleches)
    private void nDraw(Canvas canvas) {
        canvas.drawRGB(44,44,44);
        if (isWon()) {
            paintcarte(canvas);
            paintwin(canvas);
            // si gagnant, on incr la valeur du niveau de 1
            level=level+1;

        } else {
            paintcarte(canvas);
        }

    }

    // callback sur le cycle de vie de la surfaceview
    // TODO gérer le cas ou l'utilisateur pause l appli puis la relance car dans notre cas il n'y a aucune sauvegarde de la situation avant la pause
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("-> FCT <-", "surfaceChanged "+ width +" - "+ height);
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
                currentStepZone = (currentStepZone + 1) % maxStepZone;
                try {
                    c = holder.lockCanvas(null);
                    nDraw(c);
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
            } catch(Exception e) {
                Log.e("-> RUN <-", "PB DANS RUN");



            }
        }
    }


    // fonction permettant de recuperer les evenements tactiles
    public boolean onTouchEvent (MotionEvent event) {
        Log.i("-> FCT <-", "onTouchEvent: "+ event.getX());
        float pos_x = event.getX();
        float pos_y = event.getY();

        // on itere sur tous les boutons pour savoir si un a été clické
        for (int i = 0; i < colorButtons.length; i ++) {
            if (colorButtons[i].btn_rect.contains(pos_x, pos_y)) {
                Log.i("-> FCT <-", "id couleur: "+ colorButtons[i].colorID);
            }
        }
        // si la partie est gagnée
        if (isWon()){

            int x = (getWidth()/2)-(win.getWidth()/2);


            int y = (getHeight()/2)-(win.getHeight()/2);
            // vérif que l'utilisateur appuie sur le bouton gagné pour lancer le niveau suivant
            if (event.getX()> x && event.getX() < x+win.getWidth() && event.getY() > y && event.getY() <y+win.getHeight()){
                initparameters(level);
            }

        }
        return super.onTouchEvent(event);
    }
}