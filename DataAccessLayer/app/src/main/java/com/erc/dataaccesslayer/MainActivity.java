package com.erc.dataaccesslayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.erc.dal.Aggregation;
import com.erc.dal.DB;
import com.erc.dal.Log;
import com.erc.dal.Options;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DB db = new DB(getApplicationContext());

        Marcador t = new Marcador();
        t.id = 7;
        db.save(t);

        Marcador m2 = db.getById(Marcador.class, 7);
        if (m2 != null) {
            Log.w(m2.toString());
        }

        ArrayList<Marcador> entities = db.getAll(Marcador.class);
        for (Marcador entity : entities) {
            Log.w(entity.toString());
        }

        Options options = new Options();
        options.and("description", "");
        options.and("code", "");
        options.and("status", true);
        options.and("date", null);
        options.orderBy("id", true);
        options.distinct(true);
        options.in("id", new ArrayList(Arrays.asList(5, 7)));
        options.limit(5);
        entities = db.getAll(Marcador.class, options);
        for (Marcador entity : entities) {
            Log.w(entity.toString());
        }

        Log.i("count: " + db.calculate(Marcador.class, Aggregation.count()) + "");

        Termino termino = new Termino();
        db.save(termino);
        termino.description = "test";
        db.save(termino);

        ArrayList<Termino> terminos = db.getAll(Termino.class);
        for (Termino entity : terminos) {
            Log.w(entity.toString());
        }

        Termino termino1 = db.getById(Termino.class, 1);
        if (termino1 != null) {
            Log.w(termino1.toString());
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
