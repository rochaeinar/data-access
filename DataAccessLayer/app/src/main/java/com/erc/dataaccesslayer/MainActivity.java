package com.erc.dataaccesslayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.erc.dal.DB;
import com.erc.dal.Log;
import com.erc.dal.Options;
import com.erc.dal.upgrade.DBConfig;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity {
    long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Marcador marker = new Marcador();
        marker.id = 7;
        marker.description = "\\test";
        DB db = new DB(getApplicationContext());
        measurement("save");
        db.save(marker);
        measurement("save end");

        Marcador marker2 = db.getById(Marcador.class, 7);
        measurement("getById end");
        if (marker2 != null) {
            Log.w("GET BY ID: " + marker2.toString());
        }

        measurement("getAll start");
        ArrayList<Marcador> entities = db.getAll(Marcador.class);
        measurement("getAll end");
        for (Marcador entity : entities) {
            Log.w("GET ALL: " + entity.toString());
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
        measurement("getAll options start");
        entities = db.getAll(Marcador.class, options);
        measurement("getAll options end");
        showEntities(entities, "GET ALL Options: ");

        //Testing offset
        marker2.id = 8;
        db.save(marker2);
        marker2.id = 9;
        db.save(marker2);
        options = new Options();
        options.limit(1);
        options.offset(2);
        entities = db.getAll(Marcador.class, options);
        showEntities(entities, "OFFSET TEST: ");


        /*Log.i("count: " + db.calculate(Marcador.class, Aggregation.count()) + "");

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

        Log.i("newId: " + db.calculate(Marcador.class, Aggregation.max("ID")) + "");*/

        measurement("Upgrade start");
        //Upgrade default DB example
        DBConfig dbConfig = new DBConfig(getApplicationContext(), "", 1, "");
        dbConfig.setOnUpgradeListener(new UpgradeExample());
        db = new DB(dbConfig);
        db.getAll(Termino.class).size();
        measurement("Upgrade end");
        showTableStructure(db, "SETTINGS");


        measurement("Upgrade external start");
        //Upgrade external DB example
        dbConfig = new DBConfig(getApplicationContext(), "external.db", 1, Util.getAppPath(getApplicationContext()) + "test");
        dbConfig.setOnUpgradeListener(new UpgradeExample());
        DB externalDb = new DB(dbConfig);
        externalDb.getAll(SETTINGS.class);
        measurement("Upgrade external end");
        showTableStructure(externalDb, "SETTINGS");
        externalDb.save(new SETTINGS());

        Log.w("External Upgrade existing db");
        dbConfig = new DBConfig(getApplicationContext(), "external.db", (int) (Math.random() * 1000000), Util.getAppPath(getApplicationContext()) + "test");
        dbConfig.setOnUpgradeListener(new UpgradeExample());
        externalDb = new DB(dbConfig);
        externalDb.getAll(SETTINGS_UPGRADE.class);
        //externalDb.save(new SETTINGS());
        showTableStructure(externalDb, "SETTINGS");

        Log.w("DB_TIME endTime: " + (System.nanoTime() - startTime));
    }

    private static void showEntities(ArrayList<Marcador> entities, String label) {
        for (Marcador entity : entities) {
            Log.w(label + entity.toString());
        }
    }

    private void measurement(String tag) {
        long currentTime = System.nanoTime();
        if (startTime != 0) {
            Log.i("DB_TIME " + tag + ": " + java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(currentTime - startTime));
        }
        startTime = currentTime;
    }

    @SuppressLint("Range")
    private void showTableStructure(DB db, String tableName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ");");
        while (cursor != null && cursor.moveToNext()) {
            if (cursor.getColumnIndex("name") >= 0) {
                Log.w(cursor.getString(cursor.getColumnIndex("name")));
            }
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
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
