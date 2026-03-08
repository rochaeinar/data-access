package com.erc.dataaccesslayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.erc.dal.Aggregation;
import com.erc.dal.DALException;
import com.erc.dal.DB;
import com.erc.dal.DBs;
import com.erc.dal.ExpresionOperator;
import com.erc.dal.FieldParam;
import com.erc.dal.Function;
import com.erc.dal.Group;
import com.erc.dal.Log;
import com.erc.dal.LogicalOperator;
import com.erc.dal.Options;
import com.erc.dal.upgrade.DBConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class MainActivity extends Activity {
    long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ── 1. CRUD BÁSICO ────────────────────────────────────────────────────
        try {
            DB db = new DB(getApplicationContext());

            // Save — inserta porque el ID no existe aún
            Marcador marker = new Marcador();
            marker.id = 7;
            marker.description = "Hello DAL";
            marker.code = 'A';
            marker.language = 1;
            marker.status = true;
            marker.date = new Date();
            marker.myShort = 10;
            marker.myDouble = 3.14;
            marker.myFloat = 1.5f;
            measurement("save");
            db.save(marker);
            measurement("save end");

            // Update — save detecta que el ID ya existe y hace UPDATE
            marker.description = "Updated description";
            db.save(marker);

            // Get by ID
            Marcador found = db.getById(Marcador.class, 7);
            measurement("getById end");
            if (found != null) {
                Log.w("GET BY ID: " + found);
            }

            // Get All sin filtros
            measurement("getAll start");
            ArrayList<Marcador> all = db.getAll(Marcador.class);
            measurement("getAll end");
            for (Marcador m : all) {
                Log.w("GET ALL: " + m);
            }

            // Remove
            db.remove(Marcador.class, 7);
            Log.w("Removed id=7, count after: " + db.getAll(Marcador.class).size());

        } catch (DALException e) {
            Log.e("CRUD básico falló", e);
        }


        // ── 2. OPTIONS — TODOS LOS OPERADORES ────────────────────────────────
        try {
            DB db = new DB(getApplicationContext());

            // Poblar datos de prueba
            for (int i = 1; i <= 10; i++) {
                Marcador m = new Marcador();
                m.id = i;
                m.description = "Item " + i;
                m.language = i % 3;         // 0, 1, 2, 0, 1, 2 ...
                m.status = (i % 2 == 0);    // true para pares
                m.myDouble = i * 1.5;
                m.myShort = (short) i;
                db.save(m);
            }

            // EQUALS (default)
            Options opEquals = new Options();
            opEquals.and("language", 1);
            logAll(db.getAll(Marcador.class, opEquals), "EQUALS language=1");

            // NOT_EQUAL_TO
            Options opNeq = new Options();
            opNeq.and("language", 0, ExpresionOperator.NOT_EQUAL_TO);
            logAll(db.getAll(Marcador.class, opNeq), "NOT_EQUAL language!=0");

            // GREATER_THAN
            Options opGt = new Options();
            opGt.and("ID", 5, ExpresionOperator.GREATER_THAN);
            logAll(db.getAll(Marcador.class, opGt), "GREATER_THAN id>5");

            // LESS_THAN
            Options opLt = new Options();
            opLt.and("ID", 4, ExpresionOperator.LESS_THAN);
            logAll(db.getAll(Marcador.class, opLt), "LESS_THAN id<4");

            // GREATER_THAN_OR_EQUAL_TO
            Options opGte = new Options();
            opGte.and("ID", 8, ExpresionOperator.GREATER_THAN_OR_EQUAL_TO);
            logAll(db.getAll(Marcador.class, opGte), "GTE id>=8");

            // LESS_THAN_OR_EQUAL_TO
            Options opLte = new Options();
            opLte.and("ID", 3, ExpresionOperator.LESS_THAN_OR_EQUAL_TO);
            logAll(db.getAll(Marcador.class, opLte), "LTE id<=3");

            // NOT_GREATER_THAN (!>)  — equivale a <=
            Options opNgt = new Options();
            opNgt.and("ID", 5, ExpresionOperator.NOT_GREATER_THAN);
            logAll(db.getAll(Marcador.class, opNgt), "NOT_GREATER_THAN id!>5");

            // NOT_LESS_THAN (!<) — equivale a >=
            Options opNlt = new Options();
            opNlt.and("ID", 7, ExpresionOperator.NOT_LESS_THAN);
            logAll(db.getAll(Marcador.class, opNlt), "NOT_LESS_THAN id!<7");

            // NOT_EQUAL_TO_DIFF (!=)
            Options opNeqDiff = new Options();
            opNeqDiff.and("language", 2, ExpresionOperator.NOT_EQUAL_TO_DIFF);
            logAll(db.getAll(Marcador.class, opNeqDiff), "NOT_EQUAL_DIFF language!=2");

            // LIKE
            Options opLike = new Options();
            opLike.and("description", "Item 1%", ExpresionOperator.LIKE);
            logAll(db.getAll(Marcador.class, opLike), "LIKE description LIKE 'Item 1%'");

            // IN con AND (default)
            Options opIn = new Options();
            opIn.in("ID", new ArrayList<>(Arrays.asList(2, 4, 6, 8)));
            logAll(db.getAll(Marcador.class, opIn), "IN id IN (2,4,6,8)");

            // IN con OR
            Options opInOr = new Options();
            opInOr.and("language", 0);
            opInOr.in("ID", new ArrayList<>(Arrays.asList(3, 7)), LogicalOperator.OR);
            logAll(db.getAll(Marcador.class, opInOr), "IN OR language=0 OR id IN (3,7)");

            // OR entre condiciones
            Options opOr = new Options();
            opOr.and("language", 0);
            opOr.or("language", 2);
            logAll(db.getAll(Marcador.class, opOr), "OR language=0 OR language=2");

            // ORDER BY ASC
            Options opAsc = new Options();
            opAsc.orderBy("description", true);
            logAll(db.getAll(Marcador.class, opAsc), "ORDER BY description ASC");

            // ORDER BY DESC
            Options opDesc = new Options();
            opDesc.and("status", true);
            opDesc.orderBy("ID", false);
            logAll(db.getAll(Marcador.class, opDesc), "ORDER BY id DESC");

            // DISTINCT
            Options opDistinct = new Options();
            opDistinct.distinct(true);
            opDistinct.and("language", 1, ExpresionOperator.NOT_EQUAL_TO);
            logAll(db.getAll(Marcador.class, opDistinct), "DISTINCT language!=1");

            // LIMIT
            Options opLimit = new Options();
            opLimit.limit(3);
            opLimit.orderBy("ID", true);
            logAll(db.getAll(Marcador.class, opLimit), "LIMIT 3");

            // LIMIT + OFFSET
            Options opOffset = new Options();
            opOffset.orderBy("ID", true);
            opOffset.limit(3);
            opOffset.offset(4);
            logAll(db.getAll(Marcador.class, opOffset), "LIMIT 3 OFFSET 4");

        } catch (DALException e) {
            Log.e("Options falló", e);
        }


        // ── 3. AGGREGATION — TODOS LOS OPERADORES ────────────────────────────
        try {
            DB db = new DB(getApplicationContext());

            // COUNT(*)
            Long count = db.calculate(Marcador.class, Aggregation.count());
            Log.w("COUNT(*) = " + count);

            // MAX
            Long maxId = db.calculate(Marcador.class, Aggregation.max("ID"));
            Log.w("MAX(ID) = " + maxId);

            // MIN
            Long minId = db.calculate(Marcador.class, Aggregation.min("ID"));
            Log.w("MIN(ID) = " + minId);

            // SUM
            Long sumLang = db.calculate(Marcador.class, Aggregation.sum("language"));
            Log.w("SUM(language) = " + sumLang);

            // AVG
            Float avgLang = db.calculate(Marcador.class, Aggregation.avg("language"));
            Log.w("AVG(language) = " + avgLang);

            // Aggregation con Options (filtro previo)
            Options opAgg = new Options();
            opAgg.and("status", true);
            Long countActive = db.calculate(Marcador.class, Aggregation.count(), opAgg);
            Log.w("COUNT(*) WHERE status=true = " + countActive);

            Float avgActive = db.calculate(Marcador.class, Aggregation.avg("language"), opAgg);
            Log.w("AVG(language) WHERE status=true = " + avgActive);

        } catch (DALException e) {
            Log.e("Aggregation falló", e);
        }


        // ── 4. GROUP — EXPRESIONES AGRUPADAS CON PARÉNTESIS ──────────────────
        // SQL equivalente:
        //   WHERE language = 1 AND ( id >= 3 AND id <= 7 )
        try {
            DB db = new DB(getApplicationContext());

            Options options = new Options();
            options.and("language", 1);

            Group group = new Group(options);
            group.and("ID", 3, ExpresionOperator.GREATER_THAN_OR_EQUAL_TO);
            group.and("ID", 7, ExpresionOperator.LESS_THAN_OR_EQUAL_TO);
            options.and(group);

            logAll(db.getAll(Marcador.class, options), "GROUP AND: language=1 AND (id>=3 AND id<=7)");

        } catch (DALException e) {
            Log.e("Group AND falló", e);
        }

        // SQL equivalente:
        //   WHERE status = true AND ( id IN (1,2) OR id IN (8,9,10) )
        try {
            DB db = new DB(getApplicationContext());

            Options options = new Options();
            options.and("status", true);

            Group group = new Group(options);
            group.in("ID", new ArrayList<>(Arrays.asList(1, 2)));
            group.in("ID", new ArrayList<>(Arrays.asList(8, 9, 10)), LogicalOperator.OR);
            options.and(group);

            logAll(db.getAll(Marcador.class, options), "GROUP IN OR: status=true AND (id IN (1,2) OR id IN (8,9,10))");

        } catch (DALException e) {
            Log.e("Group IN OR falló", e);
        }

        // SQL equivalente:
        //   WHERE ( language = 0 OR language = 2 ) OR ( status = true AND id > 5 )
        try {
            DB db = new DB(getApplicationContext());

            Options options = new Options();

            Group g1 = new Group(options);
            g1.and("language", 0);
            g1.or("language", 2);
            options.and(g1);

            Group g2 = new Group(options);
            g2.and("status", true);
            g2.and("ID", 5, ExpresionOperator.GREATER_THAN);
            options.or(g2);

            logAll(db.getAll(Marcador.class, options), "MULTI GROUP: (lang=0 OR lang=2) OR (status=true AND id>5)");

        } catch (DALException e) {
            Log.e("Multi-Group falló", e);
        }


        // ── 5. FUNCTION — LOWER, UPPER, REPLACE ──────────────────────────────
        try {
            DB db = new DB(getApplicationContext());

            // Guardar datos con mayúsculas/minúsculas mixtas
            Termino t1 = new Termino();
            t1.description = "Hello World";
            db.save(t1);
            Termino t2 = new Termino();
            t2.description = "HELLO JAVA";
            db.save(t2);

            // LOWER — buscar "hello" sin importar capitalización
            Function fnLower = Function.lower(FieldParam.getInstance("description"));
            Options opLower = new Options();
            opLower.and(fnLower, "%hello%", ExpresionOperator.LIKE);
            ArrayList<Termino> resLower = db.getAll(Termino.class, opLower);
            Log.w("LOWER LIKE '%hello%' count=" + resLower.size());

            // UPPER — buscar "WORLD"
            Function fnUpper = Function.upper(FieldParam.getInstance("description"));
            Options opUpper = new Options();
            opUpper.and(fnUpper, "%WORLD%", ExpresionOperator.LIKE);
            ArrayList<Termino> resUpper = db.getAll(Termino.class, opUpper);
            Log.w("UPPER LIKE '%WORLD%' count=" + resUpper.size());

            // REPLACE — reemplazar espacios por guiones antes de comparar
            Function fnReplace = Function.replace(
                    FieldParam.getInstance("description"), " ", "-");
            Options opReplace = new Options();
            opReplace.and(fnReplace, "Hello-World", ExpresionOperator.EQUALS);
            ArrayList<Termino> resReplace = db.getAll(Termino.class, opReplace);
            Log.w("REPLACE spaces->'-' EQUALS 'Hello-World' count=" + resReplace.size());

        } catch (DALException e) {
            Log.e("Function falló", e);
        }


        // ── 6. SAVE CON OPTIONS ───────────────────────────────────────────────
        try {
            DB db = new DB(getApplicationContext());

            // Insertar registro base
            Marcador m = new Marcador();
            m.id = 99;
            m.description = "Original";
            m.language = 5;
            db.save(m);

            // Actualizar por condición (no por PK)
            m.description = "Updated by Options";
            Options condition = new Options();
            condition.and("language", 5);
            Marcador updated = db.save(m, condition);
            Log.w("Save con Options: " + updated);

        } catch (DALException e) {
            Log.e("Save con Options falló", e);
        }


        // ── 7. RAWQUERY / EXECSQL DIRECTO ─────────────────────────────────────
        try {
            DB db = new DB(getApplicationContext());

            // execSQL — DDL o DML arbitrario
            db.execSQL("UPDATE Marcador SET language = 99 WHERE ID = 1");
            Log.w("execSQL UPDATE OK");

            // rawQuery — SELECT arbitrario con Cursor
            Cursor cursor = db.rawQuery("SELECT ID, description FROM Marcador WHERE language = 99");
            while (cursor != null && cursor.moveToNext()) {
                Log.w("rawQuery row: id=" + cursor.getLong(0) + " desc=" + cursor.getString(1));
            }
            if (cursor != null && !cursor.isClosed()) cursor.close();

            // PRAGMA — inspección de estructura
            showTableStructure(db, "Marcador");

        } catch (DALException e) {
            Log.e("rawQuery/execSQL falló", e);
        }


        // ── 8. DBCONFIG — URL EXTERNO + PACKAGE FILTER ───────────────────────
        try {
            measurement("External DB start");
            DBConfig dbConfig = new DBConfig(
                    getApplicationContext(),
                    "external.db",
                    1,
                    Util.getAppPath(getApplicationContext()) + "test");
            dbConfig.setOnUpgradeListener(new UpgradeExample());
            // Limitar el escaneo de entidades a un sub-paquete concreto
            dbConfig.setPackageFilter("com.erc.dataaccesslayer");

            DB externalDb = new DB(dbConfig);
            externalDb.getAll(SETTINGS.class);
            measurement("External DB end");

            SETTINGS s = new SETTINGS();
            s.NAME = "theme";
            s.VALUE = "dark";
            externalDb.save(s);

            ArrayList<SETTINGS> settings = externalDb.getAll(SETTINGS.class);
            for (SETTINGS setting : settings) {
                Log.w("SETTINGS: " + setting);
            }

        } catch (DALException e) {
            Log.e("External DBConfig falló", e);
        }


        // ── 9. DBs SINGLETON — MÚLTIPLES BASES ABIERTAS ──────────────────────
        try {
            // DBs gestiona un pool por nombre de archivo; no crea instancias duplicadas
            DBConfig config1 = new DBConfig(getApplicationContext(), null, 1, null);
            DBConfig config2 = new DBConfig(
                    getApplicationContext(), "second.db", 1,
                    Util.getAppPath(getApplicationContext()) + "test");

            DB db1 = DBs.getInstance().getDB(config1);
            DB db2 = DBs.getInstance().getDB(config2);

            db1.getAll(Marcador.class);
            db2.getAll(SETTINGS.class);
            Log.w("DBs singleton OK — db1 y db2 activos");

            // Eliminar del pool cuando ya no se necesite
            DBs.getInstance().removeDB(config2);

        } catch (DALException e) {
            Log.e("DBs singleton falló", e);
        }


        // ── 10. UPGRADE ───────────────────────────────────────────────────────
        try {
            measurement("Upgrade default start");
            DBConfig dbConfig = new DBConfig(getApplicationContext(), "", 1, "");
            dbConfig.setOnUpgradeListener(new UpgradeExample());
            DB db = new DB(dbConfig);
            db.getAll(Termino.class);
            measurement("Upgrade default end");
            showTableStructure(db, "SETTINGS");

        } catch (DALException e) {
            Log.e("Upgrade default falló", e);
        }

        try {
            // Forzar versión aleatoria para disparar onUpgrade
            DBConfig dbConfig = new DBConfig(
                    getApplicationContext(), "external.db",
                    (int) (Math.random() * 1000000),
                    Util.getAppPath(getApplicationContext()) + "test");
            dbConfig.setOnUpgradeListener(new UpgradeExample());
            DB externalDb = new DB(dbConfig);
            externalDb.getAll(SETTINGS_UPGRADE.class);
            showTableStructure(externalDb, "SETTINGS");

        } catch (DALException e) {
            Log.e("Upgrade externo falló", e);
        }


        // ── 11. EJEMPLO COMPLEJO — BÚSQUEDA CON NORMALIZACIÓN DE TEXTO ───────
        // Equivalente al patrón findInVersion del proyecto real:
        // busca en "description" ignorando tildes y capitalización,
        // filtrando por rango de language (capítulos) y/o lista de IDs marcados,
        // con límite configurable.
        try {
            String rawText = "Héllo";   // texto del usuario, con tildes
            int initialLang = 1;
            int endLang = 5;
            boolean limitResults = true;
            String savedIds = "2,4,7";  // IDs seleccionados manualmente

            searchNormalized(new DB(getApplicationContext()),
                    rawText, initialLang, endLang, limitResults, savedIds);

        } catch (DALException e) {
            Log.e("Búsqueda compleja falló", e);
        }


        Log.w("DB_TIME endTime: " + (System.nanoTime() - startTime));
    }

    /**
     * Búsqueda full-text normalizada con Group + Function + in() OR.
     *
     * SQL generado aproximado:
     *   SELECT * FROM Marcador
     *   WHERE REPLACE(REPLACE(REPLACE(LOWER(description),'á','a'),'é','e'),'ó','o')
     *         LIKE '%hello%'
     *     AND ( language >= 1 AND language <= 5
     *           OR id IN (2, 4, 7) )
     *   LIMIT 100;
     */
    private void searchNormalized(DB db, String rawText,
                                  int initialLang, int endLang,
                                  boolean limitResults, String savedIds) {
        // Normalizar el texto buscado (quitar tildes, minúsculas)
        String textNormalized = rawText.toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u")
                .replace(",", "").replace(";", "");

        // Cadena de funciones anidadas sobre la columna
        Function f1 = Function.lower(FieldParam.getInstance("description"));
        Function f2 = Function.replace(f1, "á", "a");
        Function f3 = Function.replace(f2, "é", "e");
        Function f4 = Function.replace(f3, "í", "i");
        Function f5 = Function.replace(f4, "ó", "o");
        Function f6 = Function.replace(f5, "ú", "u");
        Function f7 = Function.replace(f6, ",", "");
        Function f8 = Function.replace(f7, ";", "");

        Options options = new Options();
        options.and(f8, "%" + textNormalized + "%", ExpresionOperator.LIKE);

        // Grupo: rango de language + OR lista de IDs favoritos
        Group group = new Group(options);
        group.and("language", initialLang, ExpresionOperator.GREATER_THAN_OR_EQUAL_TO);
        group.and("language", endLang, ExpresionOperator.LESS_THAN_OR_EQUAL_TO);

        if (!savedIds.isEmpty()) {
            ArrayList<Integer> idList = new ArrayList<>();
            for (String id : savedIds.split(",")) {
                idList.add(Integer.parseInt(id.trim()));
            }
            group.in("ID", idList, LogicalOperator.OR);
        }

        options.and(group);

        if (limitResults) {
            options.limit(100);
        }

        ArrayList<Marcador> results = db.getAll(Marcador.class, options);
        Log.w("searchNormalized: " + results.size() + " resultado(s)");
        for (Marcador m : results) {
            Log.w("  → " + m);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void logAll(ArrayList<Marcador> list, String label) {
        Log.w(label + " (" + list.size() + ")");
        for (Marcador m : list) {
            Log.w("  " + m);
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
                Log.w(tableName + ".col: " + cursor.getString(cursor.getColumnIndex("name")));
            }
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
