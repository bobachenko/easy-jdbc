package org.bobachenko.easyjdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents parameter that contains many values.
 * For example for IN clause.
 * @param <T>
 */
public class Array<T> {

    private List<T> values = new ArrayList<>();
    private final String dbDatatype;

    private Array(String dbDatatype) {
        this.dbDatatype = dbDatatype;
    }

    private Array(String dbDatatype, Collection<T> values) {
        this.dbDatatype = dbDatatype;
        this.values = new ArrayList<>(values);
    }

    /**
     * Create array parameter.
     * @param dbDatatype data type name for the target field of the database's table
     * @param <T> java type of values
     * @return Array object
     */
    public static <T> Array<T> of(final String dbDatatype) {
        return new Array<T>(dbDatatype);
    }

    /**
     * Create array parameter.
     * @param dbDatatype data type name for the target field of the database's table
     * @param values Collection of values
     * @param <T> java type of values
     * @return Array object
     */
    public static <T> Array<T> of(final String dbDatatype, Collection<T> values) {
        return new Array<>(dbDatatype, values);
    }

    /**
     * add value to this parameter
     */
    public Array<T> add(T item) {
        values.add(item);
        return this;
    }

    /**
     * Values of this parameter
     */
    Object[] getValues() {
        return values.toArray();
    }

    /**
     * Database's data type.
     */
    String getDbDatatype() {
        return dbDatatype;
    }
}
