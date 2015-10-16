package com.erc.dataaccesslayer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.erc.dal.ExpresionOperator;
import com.erc.dal.Options;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Marcador t = new Marcador(getApplicationContext());
        t.save();
        t.save();
        t.get(1445006285274L);
        t.getAll();
        t.remove(5);

        Options options = new Options();
        options.and("description", "this is a test");
        options.and("code", "A");
        options.and("id", "123");
        options.or("id", "5");
        options.or("description", "%escrip%", ExpresionOperator.like());
        options.orderBy("id", true);
        options.distinct(true);
        options.in("code", new ArrayList(Arrays.asList(5, 6)));
        options.select("code", "id");
        options.limit(5);
        options.avg("code");
        options.count();

        t.getAll(options);
        t.execSQL("select * from fsdgsdf where asdfgsdfg sdfg sdfg");

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
