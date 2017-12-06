package com.github.shadowjonathan.automatiaapp.ffnet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.github.shadowjonathan.automatiaapp.background.Download;
import com.github.shadowjonathan.automatiaapp.global.Downloads;
import com.github.shadowjonathan.automatiaapp.global.Helper;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.shadowjonathan.automatiaapp.background.Modules.ffnet;

public class Story {
    public static final int UNDOWNLOADED = 0;
    public static final int DOWNLOADED = 1337;
    public static final int DOWNLOADING = 9001;
    public static final int UPDATE_READY = 42;
    private static Map<String, Story> Stories = new HashMap<>();
    private static String TAG = "STORY";
    public String ID;
    public Registry.RegistryEntry info;
    public Integer state;
    public int p_state = 0;
    public int p_total = 0;
    protected ProgressState progress;
    private Archive from;
    private boolean downloadQueried = false;
    private Boolean downloaded;

    public Story(String ID, Registry.RegistryEntry re) {
        this.ID = ID;
        this.info = re;
    }

    public Story(String ID) {
        this.ID = ID;
        info = findEntry();
        if (info == null) {
            requestEntry();
        }
    }

    Story(String ID, boolean downloaded) {
        this.downloaded = downloaded;
        this.ID = ID;
        info = findEntry();
        if (info == null) {
            requestEntry();
        }
    }

    public static ArrayList<Story> getList() {
        ArrayList<Story> list = new ArrayList<>();
        SQLiteDatabase Db = ffnet.getDB().getReadableDatabase();

        String[] projection = {
                StoryContract.SEntry._ID,
                StoryContract.SEntry.COLUMN_NAME_ID,
                StoryContract.SEntry.COLUMN_NAME_DOWNLOADED,
        };

        Cursor cursor = Db.query(
                StoryContract.SEntry.TABLE_NAME,
                projection,
                null, null, null, null, null
        );


        while (cursor.moveToNext())
            list.add(Story.getStory(
                    cursor.getString(cursor.getColumnIndex(StoryContract.SEntry.COLUMN_NAME_ID)),
                    cursor.getInt(cursor.getColumnIndex(StoryContract.SEntry.COLUMN_NAME_DOWNLOADED)) == 1)
            );

        cursor.close();

        for (Story s : Stories.values()) {
            if (s.getState() > 0)
                if (!list.contains(s))
                    list.add(s);
        }
        Log.d(TAG, "getList: LEN " + list.size());
        return list;
    }

    private static Story getStory(String ID, boolean downloaded) {
        if (Stories.containsKey(ID))
            return Stories.get(ID).stateDownloaded(downloaded);
        Story s = new Story(ID, downloaded);
        Stories.put(ID, s);
        return s;
    }

    public static Story getStory(String ID) {
        if (Stories.containsKey(ID))
            return Stories.get(ID);
        Story s = new Story(ID);
        Stories.put(ID, s);
        return s;
    }

    public static Story getStory(String ID, Registry.RegistryEntry re) {
        if (Stories.containsKey(ID))
            return Stories.get(ID);
        Story s = new Story(ID, re);
        Stories.put(ID, s);
        return s;
    }

    public Archive from() {
        if (from == null) {
            for (Map.Entry<String, Category> entry : Category.Categories.entrySet()) {
                if (entry.getValue().hasArchive(info.archive))
                    from = entry.getValue().registerArchive(info.archive);
            }
            return from;
        }
        return from;
    }

    public int getState() {
        if (state == null)
            state = getInternalState();
        return state;
    }

    private int getInternalState() {
        if (downloaded == null) {
            getIsDownloaded();
            if (downloaded == null)
                return UNDOWNLOADED;
            else
                return getState();
        } else {
            if (downloaded) {
                if (updateReady())
                    return UPDATE_READY;
                else
                    return DOWNLOADED;
            } else {
                return DOWNLOADING;
            }
        }
    }

    private Story stateDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
        return this;
    }

    private boolean updateReady() {
        Date downloadedDate = downloadedDate();
        Minutes mins = Minutes.minutesBetween(new DateTime(info.updated), new DateTime(downloadedDate));
        return (mins.getMinutes() >= 0 ? mins : mins.negated()).isGreaterThan(Minutes.minutes(5));
    }

    private Date downloadedDate() {
        Cursor cursor = ffnet.getDB().getReadableDatabase().query(
                StoryContract.SEntry.TABLE_NAME,
                new String[]{
                        StoryContract.SEntry._ID,
                        StoryContract.SEntry.COLUMN_NAME_ID,
                        StoryContract.SEntry.COLUMN_NAME_LATEST_UPDATE,
                },
                StoryContract.SEntry.COLUMN_NAME_ID + "=?",
                new String[]{ID},
                null, null, null, null);
        if (cursor != null && cursor.getCount() == 1)
            cursor.moveToFirst();
        else {
            Log.w(TAG, "downloadedDate: DATE IS NULL");
            return null;
        }
        Date last = Helper.parseDate(cursor.getString(cursor.getColumnIndex(StoryContract.SEntry.COLUMN_NAME_LATEST_UPDATE)));
        cursor.close();
        Log.d(TAG, "downloadedDate: DD is " + last);
        return last;
    }

    private void getIsDownloaded() {
        Cursor cursor = ffnet.getDB().getReadableDatabase().query(
                StoryContract.SEntry.TABLE_NAME,
                new String[]{
                        StoryContract.SEntry._ID,
                        StoryContract.SEntry.COLUMN_NAME_ID,
                        StoryContract.SEntry.COLUMN_NAME_DOWNLOADED,
                },
                StoryContract.SEntry.COLUMN_NAME_ID + "=?",
                new String[]{ID},
                null, null, null, null);
        if (cursor != null && cursor.getCount() == 1)
            cursor.moveToFirst();
        else
            return;
        boolean downloaded = cursor.getInt(cursor.getColumnIndex(StoryContract.SEntry.COLUMN_NAME_DOWNLOADED)) == 1;
        cursor.close();
        this.downloaded = downloaded;
    }

    public void markDownloaded(Date updateDate, String storyContentFile, Context context) {
        Log.d(TAG, "markDownloaded: MARKING DOWNLOADED FOR " + storyContentFile + " " + ID);
        SQLiteDatabase db = ffnet.getDB().getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(StoryContract.SEntry.COLUMN_NAME_ID, ID);
        values.put(StoryContract.SEntry.COLUMN_NAME_DOWNLOADED, 1);
        values.put(StoryContract.SEntry.COLUMN_NAME_LATEST_UPDATE, Helper.TSUtils.getISO8601(updateDate));

        int id = (int) db.insertWithOnConflict(
                StoryContract.SEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
        if (id == -1) {
            db.update(
                    StoryContract.SEntry.TABLE_NAME,
                    values,
                    StoryContract.SEntry.COLUMN_NAME_ID + "=?",
                    new String[]{ID}
            );
        }

        try {
            Download.move(Uri.parse(storyContentFile), new File(ffnet.getContext().getFilesDir(), "stories/" + ID + ".epub"), context);
            changeState(DOWNLOADED);
        } catch (IOException io) {
            Log.e(TAG, "markDownloaded: ERROR MOVING FILE", io);
        }
    }

    public boolean copyTo(String dir) {
        try {
            Download.copy(new File(ffnet.getContext().getFilesDir(), "stories/" + ID + ".epub"), new File(dir, (info.title + " - " + info.author).replaceAll("[^ a-zA-Z0-9.-]", "_") + ".epub"));
            return true;
        } catch (IOException io) {
            Toast.makeText(ffnet.getContext(), "Error copying story...", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "copyTo: ERROR COPYING STORY:", io);
            return false;
        }
    }

    private Helper.JSONConstructor baseMSG() {
        return new Helper.JSONConstructor()
                .i("story_id", this.ID);
    }

    private Registry.RegistryEntry findEntry() {
        return Registry.requestStorySearch(this.ID);
    }

    private void requestEntry() {
        ffnet.sendMessage(baseMSG().i("meta", true));
    }

    public boolean putDownload() {
        if (!downloadQueried) {
            ffnet.sendMessage(baseMSG().i("download", true));
            downloadQueried = true;
            SQLiteDatabase db = ffnet.getDB().getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(StoryContract.SEntry.COLUMN_NAME_ID, ID);
            values.put(StoryContract.SEntry.COLUMN_NAME_DOWNLOADED, 0);

            int id = (int) db.insertWithOnConflict(
                    StoryContract.SEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
            );
            if (id == -1) {
                db.update(
                        StoryContract.SEntry.TABLE_NAME,
                        values,
                        StoryContract.SEntry.COLUMN_NAME_ID + "=?",
                        new String[]{ID}
                );
            }

            changeState(DOWNLOADING);

            db.close();

            Log.d(TAG, "putDownload");
            return true;
        } else
            return false;
    }

    private void changeState(int newstate) {
        if (progress != null) {
            progress.stateChange(newstate);
        }
        state = newstate;
    }

    private void showProgress(int total, int progress) {
        if (progress > p_state) {
            p_state = progress;
            p_total = total;
            if (this.progress != null)
                this.progress.showProgress(total, progress);
        }
        if (progress == 0) {
            p_state = 0;
            p_total = total;
        }
    }

    protected void registerProgress(ProgressState p) {
        this.progress = p;
    }

    public void onMessage(JSONObject o) {
        if (o.has("meta")) {
            try {
                info = new Registry.RegistryEntry(o.optJSONObject("meta"));
                info.register(ffnet.getDB());
            } catch (JSONException je) {
                Log.e(TAG, "onMessage: JSON ERROR", je);
            }
        } else if (o.has("finished")) {
            downloadQueried = false;
            long id = Download.fromAutomatia(o.optString("file_name")).setTo("story/download").start();
            new Downloads().newEntry("ffnet", id, ID);
            Log.d(TAG, "onMessage: START DOWNLOAD: " + id);
        } else if (o.has("_total")) {
            showProgress(o.optInt("_total", 0), o.optInt("_current", 0));
        } else {
            Log.d(TAG, "onMessage: MISSED: " + o);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " " + (info != null ? info.title : "INFONULL");
    }

    public static abstract class ProgressState {
        abstract void showProgress(int total, int progress);

        abstract void stateChange(int newState);
    }
}
