package com.example.p2pchat.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.p2pchat.App;
import com.example.p2pchat.converters.DateConverter;
import com.example.p2pchat.model.MessageEntity;


@Database(entities = {MessageEntity.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class MessagesDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "app_database";

    private static MessagesDatabase INSTANCE;

    private static final Object lock = new Object();

    public abstract MessageDao messageDao();

    static MessagesDatabase getInstance() {
        synchronized (lock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                        App.getContext(),
                        MessagesDatabase.class,
                        DATABASE_NAME)
                        .allowMainThreadQueries()
                        .build();
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new populateAsyncTask(INSTANCE).execute();
        }
    };

    private static class populateAsyncTask extends AsyncTask<Void, Void, Void> {
        private MessageDao dao;

        populateAsyncTask(MessagesDatabase db) {
            this.dao = db.messageDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            return null;
        }
    }
}
