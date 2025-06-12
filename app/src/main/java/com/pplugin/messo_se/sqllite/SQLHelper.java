package com.pplugin.messo_se.sqllite;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "messo_se.db";
    private static final int DATABASE_VERSION = 2;

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables here
        db.execSQL("CREATE TABLE IF NOT EXISTS public_keys (" +
                "user_id TEXT NOT NULL, " +
                "public_key TEXT NOT NULL, " +
                "PRIMARY KEY(user_id)) ");

        db.execSQL("CREATE TABLE IF NOT EXISTS messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sender_id TEXT NOT NULL, " +
                "recipient_id TEXT NOT NULL, " +
                "message TEXT NOT NULL, " +
                "type TEXT NOT NULL, " + // 'INCOMING' or 'OUTGOING'
                "timestamp INTEGER NOT NULL)");
    }

    // Store public key for user
    public void insertOrUpdatePublicKey(String userId, String publicKey) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT OR REPLACE INTO public_keys (user_id, public_key) VALUES (?, ?)",
                new Object[]{userId, publicKey});
    }

    // Retrieve public key for user
    public String getPublicKey(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery(
                "SELECT public_key FROM public_keys WHERE user_id = ?",
                new String[]{userId});
        String publicKey = null;
        if (cursor.moveToFirst()) {
            publicKey = cursor.getString(0);
        }
        cursor.close();
        return publicKey;
    }

    public void insertMessage(String senderId, String recipientId, String message, String type, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO messages (sender_id, recipient_id, message, type, timestamp) VALUES (?, ?, ?, ?, ?)",
                new Object[]{senderId, recipientId, message, type, timestamp});
    }

    public java.util.List<com.pplugin.messo_se.ui.messages.Message> getMessages(String userId, String recipientId) {
        java.util.List<com.pplugin.messo_se.ui.messages.Message> messages = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery(
                "SELECT sender_id, message, type FROM messages WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?) ORDER BY timestamp ASC",
                new String[]{userId, recipientId, recipientId, userId});
        while (cursor.moveToNext()) {
            String sender = cursor.getString(0);
            String msg = cursor.getString(1);
            String type = cursor.getString(2);
            com.pplugin.messo_se.ui.messages.Message.Type msgType = type.equals("OUTGOING") ? com.pplugin.messo_se.ui.messages.Message.Type.OUTGOING : com.pplugin.messo_se.ui.messages.Message.Type.INCOMING;
            messages.add(new com.pplugin.messo_se.ui.messages.Message(msg, sender, msgType));
        }
        cursor.close();
        return messages;
    }

    // Get all conversations for the logged-in user
    public java.util.List<com.pplugin.messo_se.model.Conversation> getConversations(String userId) {
        java.util.List<com.pplugin.messo_se.model.Conversation> conversations = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // This query finds the latest message for each conversation (user pair)
        String sql = "SELECT other_user, message, timestamp FROM (" +
                "  SELECT CASE WHEN sender_id = ? THEN recipient_id ELSE sender_id END AS other_user, " +
                "         message, timestamp " +
                "  FROM messages " +
                "  WHERE sender_id = ? OR recipient_id = ? " +
                "  ORDER BY timestamp DESC" +
                ") GROUP BY other_user ORDER BY timestamp DESC";
        android.database.Cursor cursor = db.rawQuery(sql, new String[]{userId, userId, userId});
        while (cursor.moveToNext()) {
            String otherUserId = cursor.getString(0);
            String latestMessage = cursor.getString(1);
            long timestamp = cursor.getLong(2);
            // For demo, username and avatarUrl are set as userId and null. Replace with real lookup if available.
            String username = otherUserId;
            String avatarUrl = null;
            conversations.add(new com.pplugin.messo_se.model.Conversation(otherUserId, username, avatarUrl, latestMessage, timestamp));
        }
        cursor.close();
        return conversations;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS public_keys");
        db.execSQL("DROP TABLE IF EXISTS messages");
        onCreate(db);
    }
}
