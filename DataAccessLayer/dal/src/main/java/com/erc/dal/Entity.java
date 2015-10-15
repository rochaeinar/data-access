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
        options_.setEntity(this);
        StringBuffer sb = new StringBuffer();
        String selectAll = QueryBuilder.getAllQuery(this.getClass(), options_.getDistinct());
        if (!Util.isNullOrEmpty(options_.getSelect())) {
            selectAll = selectAll.replace("*", options_.getSelect());
        }
        sb.append(selectAll);
        String expresions = options_.getExpressions();
        if (expresions.length() > 0) {
            sb.append(Ctt.WHERE);
            sb.append(expresions);
        }
        sb.append(options_.getOrderBy());
        sb.append(options_.getLimit());
        sb.append(Ctt.SEMICOLON);
        exec(sb.toString());
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
        Log.i(sql);
        return 0;
    }
}
