package com.erc.dal;

import java.util.ArrayList;

/**
 * Created by einar on 8/31/2015.
 */
public abstract class Entity {

    public Entity save() {
        String sql = "";
        Pair pair = QueryBuilder.getPrimaryKey(this);
        if (pair.getValue().toString().isEmpty() || pair.getValue().toString().equals("0")) {
            QueryBuilder.setID(this);
            sql = QueryBuilder.getQueryInsert(this);
        } else {
            sql = QueryBuilder.getQueryUpdate(this);
        }
        exec(sql);
        return this;
    }

    public ArrayList<Entity> getAll(Options... options) {
        Options options_ = options.length == 0 ? new Options() : options[0];
        String selectAll = QueryBuilder.getAllQuery(this.getClass());
        selectAll = options_.getSql(this, selectAll) + Ctt.SEMICOLON;
        exec(selectAll);
        return new ArrayList<Entity>();
    }

    public Entity get(int id) {
        String sql = QueryBuilder.getQuery(this.getClass(), id);
        exec(sql);
        return this;
    }

    public boolean remove(int id) {
        String sql = QueryBuilder.getQueryRemove(this.getClass(), id);
        exec(sql);
        return true;
    }

    public int exec(String sql) {
        int res = 0;
        try {
            Log.i(sql);
        } catch (Exception e) {
            Log.e("Failed to execute SQL", e);
        }
        return res;
    }
}
