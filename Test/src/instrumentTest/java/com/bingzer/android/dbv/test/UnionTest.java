package com.bingzer.android.dbv.test;

import android.content.Context;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.bingzer.android.dbv.DbQuery;
import com.bingzer.android.dbv.IDatabase;
import com.bingzer.android.dbv.IEntity;
import com.bingzer.android.dbv.IEntityList;
import com.bingzer.android.dbv.IQuery;
import com.bingzer.android.dbv.sqlite.SQLiteBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ricky on 8/16/13.
 */
public class UnionTest extends AndroidTestCase {

    IDatabase db;

    @Override
    protected void setUp() throws Exception {
        db = DbQuery.getDatabase("UnionTest");
        db.open(1, new SQLiteBuilder() {
            @Override
            public Context getContext() {
                return UnionTest.this.getContext();
            }

            @Override
            public void onModelCreate(IDatabase database, IDatabase.Modeling modeling) {
                modeling.add("Student")
                        .addPrimaryKey("Id")
                        .addText("Name")
                        .index("Name");

                modeling.add("Employee")
                        .addPrimaryKey("Id")
                        .addText("Name")
                        .index("Name");
            }
        });

        // delete all
        db.get("Student").delete();
        db.get("Employee").delete();

        // bulk insert
        db.get("Student").insert("Name").val("Student 1");
        db.get("Student").insert("Name").val("Student 2");
        db.get("Student").insert("Name").val("Student 3");
        db.get("Student").insert("Name").val("John");
        db.get("Student").insert("Name").val("Dave");
        // employee
        db.get("Employee").insert("Name").val("Employee 1");
        db.get("Employee").insert("Name").val("Employee 2");
        db.get("Employee").insert("Name").val("Employee 3");
        db.get("Student").insert("Name").val("John");
        db.get("Student").insert("Name").val("Dave");
    }

    public void testUnion_Simple(){
        IQuery.Select select = db.get("Student").select().columns("Name");
        Cursor cursor = db.get("Employee")
                            .union(select)
                            .select().columns("Name").query();

        assertEquals(cursor.getCount(),8);
    }

    public void testUnion_Simple_Entity(){
        TinyPersonList list = new TinyPersonList();

        IQuery.Select select = db.get("Student").select().columns("Name");
        db.get("Employee")
                .union(select)
                .select().columns("Name").query(list);

        assertEquals(list.size(),8);
    }

    public void testUnionAll_Simple(){
        IQuery.Select select = db.get("Student").select().columns("Name");
        Cursor cursor = db.get("Employee")
                .unionAll(select)
                .select().columns("Name").query();

        assertEquals(cursor.getCount(), 10);
    }

    public void testUnionAll_Simple_Entity(){
        TinyPersonList list = new TinyPersonList();

        IQuery.Select select = db.get("Student").select().columns("Name");
        db.get("Employee")
                .unionAll(select)
                .select().columns("Name").query(list);

        assertEquals(list.size(),10);
    }


    static class TinyPersonList extends LinkedList<TinyPerson> implements IEntityList<TinyPerson>{

        @Override
        public List<TinyPerson> getEntityList() {
            return this;
        }

        @Override
        public TinyPerson newEntity() {
            return new TinyPerson();
        }
    }

    static class TinyPerson implements IEntity{
        private int id;
        private String name;

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void map(Mapper mapper) {
            mapper.mapId(new Action<Integer>(Integer.class) {
                @Override
                public void set(Integer value) {
                    setId(value);
                }

                @Override
                public Integer get() {
                    return getId();
                }
            });

            mapper.map("Name", new Action<String>(String.class){

                @Override
                public void set(String value) {
                    setName(value);
                }

                @Override
                public String get() {
                    return getName();
                }
            });
        }
    }
}
