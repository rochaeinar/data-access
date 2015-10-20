package com.erc.dataaccesslayer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.erc.dal.Aggregation;
import com.erc.dal.ExpresionOperator;
import com.erc.dal.Log;
import com.erc.dal.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Marcador t = new Marcador(getApplicationContext());
        t.date = new Date();
        t.save();

        Marcador m2 = t.getById(1);
        Log.w(m2.toString());

        ArrayList<Marcador> entities = t.getAll();
        for (Marcador entity : entities) {
            Log.w(entity.toString());
        }

        Options options = new Options();
        options.and("description", "");
        options.and("code", "");
        options.and("status", false);
        options.and("date", null);
        //options.and("description", "%escrip%", ExpresionOperator.like());
        options.orderBy("id", true);
        options.distinct(true);
        options.in("id", new ArrayList(Arrays.asList(5, 30)));
        options.limit(5);
        entities = t.getAll(options);
        for (Marcador entity : entities) {
            Log.w(entity.toString());
        }

        Log.i("count: " + t.calculate(Aggregation.count()) + "");

        Termino termino = new Termino(getApplicationContext());
        termino.save();
        termino.description = "test";
        termino.save();

        Log.w(termino.getById(1).toString());

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
