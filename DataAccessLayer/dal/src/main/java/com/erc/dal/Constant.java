package com.erc.dal;

/**
 * Created by einar on 8/31/2015.
 */
public class Constant {
    public static final String TABLE = "%t";
    public static final String FIELD = "%f";
    public static final String VALUE = "%v";
    public static final String VALUES = "%vs";
    public static final String KEYS = "%ks";
    public static final String FIELDS = "%fs";
    public static final String PAIRS = "%ps";
    public static final String VALUE_QUOTES = "'" + VALUE + "'";
    public static final String SEMICOLON = ";";
    public static final String TABLE_PAIR = TABLE + "." + FIELD + " = " + VALUE + " ";
    public static final String PAIR = FIELD + " = " + VALUE + " ";
    public static final String PAIR_QUOTE = FIELD + " = '" + VALUE + "' ";
    public static final String TABLE_PAIR_QUOTES = TABLE + "." + FIELD + " = '" + VALUE + "' ";
    public static final String WHERE = "WHERE ";
    public static final String SELECT = "SELECT";
    public static final String SELECT_FROM = "SELECT * FROM " + TABLE + " ";
    public static final String INSERT = "INSERT INTO " + TABLE + " (" + FIELDS + ") VALUES (" + VALUES + ");";
    public static final String UPDATE = "UPDATE " + TABLE + " SET " + PAIRS + " WHERE " + KEYS + ";";
    public static final String DELETE = "DELETE FROM " + TABLE + " WHERE " + KEYS + ";";
    public static final String CREATE = "CREATE TABLE  " + TABLE + " (" + FIELDS + ");";
    public static final String ORDER_BY = " ORDER BY ";
    public static final String DESC = " DESC ";
    public static final String ASC = " ASC ";
    public static final String DISTINCT = " DISTINCT";
    public static final String LIMIT = " LIMIT ";

    public static final String TAG = "DATA_ACCESS";
    public static final int LENGTH_TEXT_ID = 10;
}
