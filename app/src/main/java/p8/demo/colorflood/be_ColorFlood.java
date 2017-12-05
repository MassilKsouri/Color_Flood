package p8.demo.colorflood;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

// declaration de notre activity héritée de Activity
public class be_ColorFlood extends Activity {

    private ColorFlood mColorFlood;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // initialise notre activity avec le constructeur parent    	
        super.onCreate(savedInstanceState);
        //chargement des textes
        final TextView turnCounter = (TextView)findViewById(R.id.turnCounter);
        // charge le fichier main.xml comme vue de l'activité
        setContentView(R.layout.main);
        // recuperation de la vue une voie cree à partir de son id
        mColorFlood = (ColorFlood)findViewById(R.id.SokobanView);
        // rend visible la vue
        mColorFlood.setVisibility(View.VISIBLE);
    }
}