package com.example.p2pchat.db;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.example.p2pchat.model.ChatHistoryEntity;
import com.example.p2pchat.model.MessageEntity;

import java.util.List;

public class MessageRepository {
    private static MessageRepository INSTANCE = new MessageRepository();
    private MessageDao messageDao;

    public static MessageRepository getInstance() {
        return INSTANCE;
    }

    private MessageRepository() {
        MessagesDatabase db = MessagesDatabase.getInstance();
        messageDao = db.messageDao();
    }

    public void update(MessageEntity message) {
        new UpdateAsyncTask(messageDao).execute(message);
    }

    public void insert(MessageEntity message) {
        new InsertAsyncTask(messageDao).execute(message);
    }

    public void delete(MessageEntity message) {
        new DeleteAsyncTask(messageDao).execute(message);
    }

    public void deleteAllFrom(String addressee) {
        new DeleteAllFromAsyncTask(messageDao).execute(addressee);
    }

    public void deleteAll() {
        new DeleteAllAsyncTask(messageDao).execute();
    }


    public LiveData<List<MessageEntity>> getAllMessages(String addressee) {
        return messageDao.getAllMessages(addressee);
    }

    public LiveData<List<ChatHistoryEntity>> getAllChats() {
        return messageDao.getAllChats();
    }

    private static class InsertAsyncTask extends AsyncTask<MessageEntity, Void, Void> {
        private MessageDao messageDao;

        private InsertAsyncTask(MessageDao dao) {
            this.messageDao = dao;
        }

        @Override
        protected Void doInBackground(MessageEntity... messages) {
            messageDao.insert(messages[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<MessageEntity, Void, Void> {
        private MessageDao messageDao;

        private UpdateAsyncTask(MessageDao dao) {
            this.messageDao = dao;
        }

        @Override
        protected Void doInBackground(MessageEntity... messages) {
            messageDao.update(messages[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<MessageEntity, Void, Void> {
        private MessageDao messageDao;

        private DeleteAsyncTask(MessageDao dao) {
            this.messageDao = dao;
        }

        @Override
        protected Void doInBackground(MessageEntity... messages) {
            messageDao.delete(messages[0]);
            return null;
        }
    }

    private static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private MessageDao messageDao;

        private DeleteAllAsyncTask(MessageDao dao) {
            this.messageDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            messageDao.deleteAll();
            return null;
        }
    }

    private static class DeleteAllFromAsyncTask extends AsyncTask<String, Void, Void> {
        private MessageDao messageDao;

        private DeleteAllFromAsyncTask(MessageDao dao) {
            this.messageDao = dao;
        }

        @Override
        protected Void doInBackground(String... addressees) {
            messageDao.deleteAllFrom(addressees[0]);
            return null;
        }
    }

}