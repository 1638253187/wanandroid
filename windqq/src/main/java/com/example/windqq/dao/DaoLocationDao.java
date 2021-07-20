package com.example.windqq.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.example.windqq.bean.DaoLocation;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "location".
*/
public class DaoLocationDao extends AbstractDao<DaoLocation, Long> {

    public static final String TABLENAME = "location";

    /**
     * Properties of entity DaoLocation.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property User = new Property(1, String.class, "user", false, "USER");
        public final static Property UserId = new Property(2, String.class, "userId", false, "USER_ID");
        public final static Property Lat = new Property(3, double.class, "lat", false, "LAT");
        public final static Property Lng = new Property(4, double.class, "lng", false, "LNG");
    }


    public DaoLocationDao(DaoConfig config) {
        super(config);
    }
    
    public DaoLocationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"location\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"USER\" TEXT," + // 1: user
                "\"USER_ID\" TEXT," + // 2: userId
                "\"LAT\" REAL NOT NULL ," + // 3: lat
                "\"LNG\" REAL NOT NULL );"); // 4: lng
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"location\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DaoLocation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String user = entity.getUser();
        if (user != null) {
            stmt.bindString(2, user);
        }
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(3, userId);
        }
        stmt.bindDouble(4, entity.getLat());
        stmt.bindDouble(5, entity.getLng());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DaoLocation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String user = entity.getUser();
        if (user != null) {
            stmt.bindString(2, user);
        }
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(3, userId);
        }
        stmt.bindDouble(4, entity.getLat());
        stmt.bindDouble(5, entity.getLng());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DaoLocation readEntity(Cursor cursor, int offset) {
        DaoLocation entity = new DaoLocation( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // user
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // userId
            cursor.getDouble(offset + 3), // lat
            cursor.getDouble(offset + 4) // lng
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DaoLocation entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUser(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setUserId(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setLat(cursor.getDouble(offset + 3));
        entity.setLng(cursor.getDouble(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DaoLocation entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DaoLocation entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DaoLocation entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
