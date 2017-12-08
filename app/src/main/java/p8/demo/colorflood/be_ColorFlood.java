package p8.demo.colorflood;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


// declaration de notre activity héritée de Activity
public class be_ColorFlood extends Activity {
    private ColorFlood mColorFlood;

    /**
     * The menu
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        final Button buplay = (Button) findViewById(R.id.buplay);
        final Button buinfo = (Button) findViewById(R.id.buinfo);


        // launch the game
        buplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.main);
                //recuperation de la vue une voie cree à partir de son id
                mColorFlood = (ColorFlood) findViewById(R.id.ColorFloodView);
                // rend visible la vue
                mColorFlood.setVisibility(View.VISIBLE);
            }
        });

        // display credits
        buinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                CharSequence text = "Made By: BOUDJOGHRA & KSOURI";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}