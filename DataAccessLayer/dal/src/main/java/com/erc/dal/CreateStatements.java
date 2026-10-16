package com.erc.dal;

import java.util.ArrayList;
import java.util.List;

/**
 * The statements of a database's create script, and the class loader its entities are found with.
 * Kept apart from Android so it can be unit tested.
 */
public final class CreateStatements {

    private CreateStatements() {
    }

    /**
     * The non-blank statements of a script joined by ";". A blank one ("" after the last ";", or
     * the whole script when no entity was found) made SQLiteDatabase.execSQL fail with
     * "not an error (code 0)".
     */
    public static List<String> split(String script) {
        List<String> statements = new ArrayList<>();
        if (script == null) return statements;
        for (String statement : script.split(";")) {
            if (!statement.trim().isEmpty()) statements.add(statement.trim());
        }
        return statements;
    }

    /**
     * The class loader that has the app's classes: the thread's when it has one, else the app's.
     * A background worker thread (the Drive sync's JobIntentService) has no context class loader,
     * and scanning it found no entity at all.
     */
    public static ClassLoader classLoader(ClassLoader thread, ClassLoader app) {
        return thread != null ? thread : app;
    }
}
