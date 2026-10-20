package hutech.com.example.projecthoctienganh.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import hutech.com.example.projecthoctienganh.model.Topic;
import hutech.com.example.projecthoctienganh.model.Vocabulary;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EnglishLearning.db";
    private static final int DATABASE_VERSION = 2;

    // Filter Constants
    public static final int FILTER_ALL = 0;
    public static final int FILTER_UNLEARNED = 1;
    public static final int FILTER_LEARNED = 2;

    // Table Topics
    private static final String TABLE_TOPICS = "topics";
    private static final String KEY_TOPIC_ID = "id";
    private static final String KEY_TOPIC_NAME = "name";
    private static final String KEY_TOPIC_DESC = "description";
    private static final String KEY_TOPIC_ICON = "icon_name";

    // Table Vocabularies
    private static final String TABLE_VOCABULARIES = "vocabularies";
    private static final String KEY_VOCAB_ID = "id";
    private static final String KEY_VOCAB_TOPIC_ID = "topic_id";
    private static final String KEY_VOCAB_WORD = "word";
    private static final String KEY_VOCAB_PHONETIC = "phonetic";
    private static final String KEY_VOCAB_MEANING = "meaning";
    private static final String KEY_VOCAB_EXAMPLE = "example";
    private static final String KEY_VOCAB_ICON = "icon_name";
    private static final String KEY_VOCAB_IS_LEARNED = "is_learned";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TOPICS_TABLE = "CREATE TABLE " + TABLE_TOPICS + "("
                + KEY_TOPIC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TOPIC_NAME + " TEXT,"
                + KEY_TOPIC_DESC + " TEXT,"
                + KEY_TOPIC_ICON + " TEXT" + ")";

        String CREATE_VOCABULARIES_TABLE = "CREATE TABLE " + TABLE_VOCABULARIES + "("
                + KEY_VOCAB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_VOCAB_TOPIC_ID + " INTEGER,"
                + KEY_VOCAB_WORD + " TEXT,"
                + KEY_VOCAB_PHONETIC + " TEXT,"
                + KEY_VOCAB_MEANING + " TEXT,"
                + KEY_VOCAB_EXAMPLE + " TEXT,"
                + KEY_VOCAB_ICON + " TEXT,"
                + KEY_VOCAB_IS_LEARNED + " INTEGER DEFAULT 0,"
                + "FOREIGN KEY(" + KEY_VOCAB_TOPIC_ID + ") REFERENCES " + TABLE_TOPICS + "(" + KEY_TOPIC_ID + ") ON DELETE CASCADE" + ")";

        db.execSQL(CREATE_TOPICS_TABLE);
        db.execSQL(CREATE_VOCABULARIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOCABULARIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOPICS);
        onCreate(db);
    }

    // TOPIC CRUD & STATS
    public List<Topic> getAllTopics() {
        List<Topic> topics = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TOPICS, null);

        if (cursor.moveToFirst()) {
            do {
                Topic topic = new Topic();
                topic.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TOPIC_ID)));
                topic.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TOPIC_NAME)));
                topic.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TOPIC_DESC)));
                topic.setIconName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TOPIC_ICON)));

                int[] stats = getTopicStats(topic.getId());
                topic.setTotalWords(stats[0]);
                topic.setLearnedWords(stats[1]);

                topics.add(topic);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return topics;
    }

    public Topic getTopicById(int topicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TOPICS, null, KEY_TOPIC_ID + "=?",
                new String[]{String.valueOf(topicId)}, null, null, null);

        Topic topic = null;
        if (cursor != null && cursor.moveToFirst()) {
            topic = new Topic();
            topic.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TOPIC_ID)));
            topic.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TOPIC_NAME)));
            topic.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TOPIC_DESC)));
            topic.setIconName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TOPIC_ICON)));

            int[] stats = getTopicStats(topic.getId());
            topic.setTotalWords(stats[0]);
            topic.setLearnedWords(stats[1]);
            cursor.close();
        }
        return topic;
    }

    public long addTopic(Topic topic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TOPIC_NAME, topic.getName());
        values.put(KEY_TOPIC_DESC, topic.getDescription());
        values.put(KEY_TOPIC_ICON, topic.getIconName());
        return db.insert(TABLE_TOPICS, null, values);
    }

    public int updateTopic(Topic topic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TOPIC_NAME, topic.getName());
        values.put(KEY_TOPIC_DESC, topic.getDescription());
        values.put(KEY_TOPIC_ICON, topic.getIconName());

        return db.update(TABLE_TOPICS, values, KEY_TOPIC_ID + "=?",
                new String[]{String.valueOf(topic.getId())});
    }

    public void deleteTopic(int topicId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VOCABULARIES, KEY_VOCAB_TOPIC_ID + "=?", new String[]{String.valueOf(topicId)});
        db.delete(TABLE_TOPICS, KEY_TOPIC_ID + "=?", new String[]{String.valueOf(topicId)});
    }

    public int[] getTopicStats(int topicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int total = 0;
        int learned = 0;

        Cursor totalCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VOCABULARIES + " WHERE " + KEY_VOCAB_TOPIC_ID + "=?", new String[]{String.valueOf(topicId)});
        if (totalCursor.moveToFirst()) {
            total = totalCursor.getInt(0);
        }
        totalCursor.close();

        Cursor learnedCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VOCABULARIES + " WHERE " + KEY_VOCAB_TOPIC_ID + "=? AND " + KEY_VOCAB_IS_LEARNED + "=1", new String[]{String.valueOf(topicId)});
        if (learnedCursor.moveToFirst()) {
            learned = learnedCursor.getInt(0);
        }
        learnedCursor.close();

        return new int[]{total, learned};
    }

    // VOCABULARY CRUD & FILTERING
    public List<Vocabulary> getAllVocabularies(int filterType) {
        List<Vocabulary> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = null;
        if (filterType == FILTER_UNLEARNED) {
            selection = KEY_VOCAB_IS_LEARNED + "=0";
        } else if (filterType == FILTER_LEARNED) {
            selection = KEY_VOCAB_IS_LEARNED + "=1";
        }

        Cursor cursor = db.query(TABLE_VOCABULARIES, null, selection,
                null, null, null, KEY_VOCAB_ID + " DESC");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Vocabulary vocab = new Vocabulary();
                    vocab.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOCAB_ID)));
                    vocab.setTopicId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOCAB_TOPIC_ID)));
                    vocab.setWord(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOCAB_WORD)));
                    vocab.setPhonetic(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOCAB_PHONETIC)));
                    vocab.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOCAB_MEANING)));
                    vocab.setExample(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOCAB_EXAMPLE)));
                    vocab.setIconName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOCAB_ICON)));
                    vocab.setLearned(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOCAB_IS_LEARNED)) == 1);

                    list.add(vocab);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public List<Vocabulary> getAllVocabularies() {
        return getAllVocabularies(FILTER_ALL);
    }

    public List<Vocabulary> getVocabulariesByTopic(int topicId, int filterType) {
        List<Vocabulary> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = KEY_VOCAB_TOPIC_ID + "=?";
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(topicId));

        if (filterType == FILTER_UNLEARNED) {
            selection += " AND " + KEY_VOCAB_IS_LEARNED + "=0";
        } else if (filterType == FILTER_LEARNED) {
            selection += " AND " + KEY_VOCAB_IS_LEARNED + "=1";
        }

        Cursor cursor = db.query(TABLE_VOCABULARIES, null, selection,
                selectionArgs.toArray(new String[0]), null, null, KEY_VOCAB_ID + " DESC");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Vocabulary vocab = new Vocabulary();
                    vocab.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOCAB_ID)));
                    vocab.setTopicId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOCAB_TOPIC_ID)));
                    vocab.setWord(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOCAB_WORD)));
                    vocab.setPhonetic(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOCAB_PHONETIC)));
                    vocab.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOCAB_MEANING)));
                    vocab.setExample(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOCAB_EXAMPLE)));
                    vocab.setIconName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOCAB_ICON)));
                    vocab.setLearned(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOCAB_IS_LEARNED)) == 1);

                    list.add(vocab);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public long addVocabulary(Vocabulary vocab) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_VOCAB_TOPIC_ID, vocab.getTopicId());
        values.put(KEY_VOCAB_WORD, vocab.getWord());
        values.put(KEY_VOCAB_PHONETIC, vocab.getPhonetic());
        values.put(KEY_VOCAB_MEANING, vocab.getMeaning());
        values.put(KEY_VOCAB_EXAMPLE, vocab.getExample());
        values.put(KEY_VOCAB_ICON, vocab.getIconName() != null ? vocab.getIconName() : "ic_word_default");
        values.put(KEY_VOCAB_IS_LEARNED, vocab.isLearned() ? 1 : 0);

        return db.insert(TABLE_VOCABULARIES, null, values);
    }

    public int updateVocabulary(Vocabulary vocab) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_VOCAB_WORD, vocab.getWord());
        values.put(KEY_VOCAB_PHONETIC, vocab.getPhonetic());
        values.put(KEY_VOCAB_MEANING, vocab.getMeaning());
        values.put(KEY_VOCAB_EXAMPLE, vocab.getExample());
        values.put(KEY_VOCAB_ICON, vocab.getIconName());
        values.put(KEY_VOCAB_IS_LEARNED, vocab.isLearned() ? 1 : 0);

        return db.update(TABLE_VOCABULARIES, values, KEY_VOCAB_ID + "=?",
                new String[]{String.valueOf(vocab.getId())});
    }

    public void updateLearnedStatus(int vocabId, boolean isLearned) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_VOCAB_IS_LEARNED, isLearned ? 1 : 0);
        db.update(TABLE_VOCABULARIES, values, KEY_VOCAB_ID + "=?",
                new String[]{String.valueOf(vocabId)});
    }

    public void deleteVocabulary(int vocabId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VOCABULARIES, KEY_VOCAB_ID + "=?", new String[]{String.valueOf(vocabId)});
    }
}