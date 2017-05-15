package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel on 5/2/2017.
 */

public class ImageTagDatabaseHelper extends SQLiteOpenHelper {
    private static final String CreateImageTable = "CREATE TABLE Image (Id integer PRIMARY KEY AUTOINCREMENT, Uri text NOT NULL UNIQUE);";
    private static final String CreateTagTable = "CREATE TABLE Tag (Id integer PRIMARY KEY AUTOINCREMENT, Text text NOT NULL UNIQUE);";
    private static final String CreateLinkTable =
            "CREATE TABLE Link (ImageId integer, TagId integer, PRIMARY KEY (ImageId, TagId), " +
                    "FOREIGN KEY (ImageId) REFERENCES Image (Id) ON DELETE CASCADE ON UPDATE NO ACTION, " +
                    "FOREIGN KEY (TagId) REFERENCES Tag (Id) ON DELETE CASCADE ON UPDATE NO ACTION);";
    private static final String DatabaseName = "ImageTagDatabase.db";
    private static ImageTagDatabaseHelper Instance;
    private List<OnDatabaseChangeListener> Listeners;

    private ImageTagDatabaseHelper(Context context) {
        super(context, DatabaseName, null, 1);
        Listeners = new ArrayList<>();
    }

    public static void Initialize(Context context) {
        Instance = new ImageTagDatabaseHelper(context);
    }

    public static ImageTagDatabaseHelper GetInstance() {
        return Instance;
    }

    public void Subscribe(OnDatabaseChangeListener listener) {
        Listeners.add(listener);
    }

    private boolean TryUpdate(Cursor cursor) {
        try {
            cursor.moveToFirst();
        } catch (SQLiteConstraintException exception) {
            return false;
        } finally {
            cursor.close();
        }
        NotifyListeners();
        return true;
    }

    private void NotifyListeners() {
        for (OnDatabaseChangeListener listener : Listeners) {
            listener.OnDatabaseChange();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CreateImageTable);
        db.execSQL(CreateTagTable);
        db.execSQL(CreateLinkTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public interface OnDatabaseChangeListener {
        void OnDatabaseChange();
    }


    /* CRUD OPERATIONS */

    //IMAGES
    public void addImage(ImageObject imageObject){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //values.put("Id", imageObject.getID());
        values.put("Uri", imageObject.getUri());

        db.insert("Image", null, values);
        db.close();
    }

    ImageObject getImage(String uri){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("Image", new String[] {"Uri"}, "Uri" + "=?", new String[]{uri}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        ImageObject imageObject = new ImageObject(Integer.parseInt(cursor.getString(0)),cursor.getString(1));
        return imageObject;
    }

    public  List<String> getAllImages(){
        List<String> imageList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + "Image";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding image to list
                imageList.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // return image list
        return imageList;
    }

    // Deleting single image
//    public void deleteImage(String uri) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete("Image", "Id" + " = ?", uri);
//        db.close();
//    }

    //TAGS
    public void addTag(String tag){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Text", tag);

        db.insert("Tag", null, values);
        db.close();
    }

    public  List<String> getAllTags(){
        List<String> tagList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + "Tag";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding tag to list
                tagList.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // return tag list
        return tagList;
    }

    // Deleting single tag
//    public void deleteTag(String tag) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete("Tag", "Id" + " = ?", tag);
//        db.close();
//    }

}