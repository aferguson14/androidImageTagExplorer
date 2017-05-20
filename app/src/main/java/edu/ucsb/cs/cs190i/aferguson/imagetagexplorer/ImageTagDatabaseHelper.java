package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.Image;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        } catch (Exception e) {
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
    public void addImage(String image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //values.put("Id", imageObject.getID());
        values.put("Uri", image);

        db.insert("Image", null, values);
        db.close();
        NotifyListeners();
    }

    public int getImageIdByUri(String uri){
        SQLiteDatabase db = this.getReadableDatabase();
        int imageId;

        //Cursor cursor = db.query("Image", new String[] {"Id"}, "Id" + "=?", new String[]{Integer.toString(id)}, null, null, null, null);
        String selectQuery = "SELECT Id FROM Image WHERE Uri=?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{uri});
        try{
            cursor.moveToFirst();
            imageId = cursor.getInt(cursor.getColumnIndex("Id"));
            return imageId;
        }finally{
            cursor.close();
            db.close();
        }
    }

    public String getImageUriById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        String imageUri = "";
        Log.d("dbquery", Integer.toString(id));

        //Cursor cursor = db.query("Image", new String[] {"Id"}, "Id" + "=?", new String[]{Integer.toString(id)}, null, null, null, null);
        String selectQuery = "SELECT Uri FROM Image WHERE Id=?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{Integer.toString(id)});
        try{
            cursor.moveToFirst();
            imageUri = cursor.getString(cursor.getColumnIndex("Uri"));
            Log.d("imageUri", imageUri);
            return imageUri;
        }finally{
            cursor.close();
            db.close();
        }
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

        cursor.close();
        db.close();
        // return image list
        return imageList;
    }

    //resource used: http://stackoverflow.com/questions/9203261/android-sqlite-in-clause-using-values-from-array
    public  List<String> getFilteredImages(List<Integer> tagIds){
        List<Integer> imageIdList = new ArrayList<>();
        List<String> imageIdStr = new ArrayList<>();
        List<String> tagIdsStr = new ArrayList<>();
        List<String> imageUriList = new ArrayList<>();
        if(tagIds.size()== 0 || tagIds == null){
            return getAllImages();
        }

        for(int s : tagIds) tagIdsStr.add(Integer.toString(s));
        String[] tagIdArray = tagIdsStr.toArray(new String[tagIdsStr.size()]);
//        tagIdArray[tagIdsStr.size()] = Integer.toString(tagIds.size());

//        for(String s: tagIdArray){
//            Log.d("filter", "tagId: " + s);
//        }

//        Log.d("filteredimages", "countSize: " + tagIdArray[tagIdsStr.size()]);


        String selectQuery = "SELECT ImageId FROM Link WHERE TagId IN (" + makePlaceholders(tagIdArray.length) + ")";
//        String selectQuery = "SELECT ImageId FROM Link WHERE TagId IN (" + makePlaceholders(tagIdArray.length-1) + ") "
//                + "GROUP BY ImageId HAVING COUNT(ImageId)=?";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, tagIdArray); //new String[]{tagsIdClause}

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding image to list
                imageIdList.add(cursor.getInt(cursor.getColumnIndex("ImageId")));
                Log.d("filteredimages", "imageIdList: " + cursor.getInt(cursor.getColumnIndex("ImageId")));
            } while (cursor.moveToNext());
        }

        if(imageIdList.size()==0 || imageIdList == null){
            return imageUriList; //empty list
        }

        Map<Integer, Integer> tempMap = new HashMap();
        List<Integer>temp = new ArrayList();

        if(tagIds.size() <= 1){ //one tag
            for(int i : imageIdList) imageIdStr.add(Integer.toString(i));
        }
        else {

            for (int i = 0; i < imageIdList.size(); i++) {
                tempMap.put(imageIdList.get(i), 1);
            }

            for (int i = 0; i < imageIdList.size(); i++) {
                Log.d("filteredimages", "temp size: " + temp.size());
                if (temp.contains(imageIdList.get(i))) {
                    int val = tempMap.get(imageIdList.get(i));
                    val++;
                    tempMap.put(imageIdList.get(i), val);
                    if(tempMap.get(imageIdList.get(i)) == tagIds.size())
                        imageIdStr.add(Integer.toString(imageIdList.get(i)));
                }
                temp.add(imageIdList.get(i));
                Log.d("filteredimages", "addingtoTemp");
//            Log.d("filteredimages", Integer.toString(temp.get(i)));
            }
        }

//        for(int i : imageIdList) imageIdStr.add(Integer.toString(i));
        String[] imageIdArray = imageIdStr.toArray(new String[imageIdStr.size()]);
//        Log.d("filteredimages", "imageIdList: " + imageIdArray[0]);

        cursor.close();

        //SECOND QUERY
        //String selectQuery1 = "SELECT Uri FROM Image WHERE Id IN (" + makePlaceholders(imageIdArray.length)+ ") "
        String selectQuery1 = "SELECT Uri FROM Image WHERE Id IN (" + makePlaceholders(imageIdArray.length) + ")";

        Cursor cursor1 = db.rawQuery(selectQuery1, imageIdArray);
        // looping through all rows and adding to list
        if (cursor1.moveToFirst()) {
            Log.d("filteredimages", "inside cursor1if");
            do {
                // Adding image to list
                imageUriList.add(cursor1.getString(cursor1.getColumnIndex("Uri")));
                Log.d("filteredimages", "imageUriList: " + cursor1.getString(cursor1.getColumnIndex("Uri")));
            } while (cursor1.moveToNext());
        }
        db.close();
        cursor1.close();

        return imageUriList;
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
        NotifyListeners();
    }

    public int getTagId(String tag){
        SQLiteDatabase db = this.getReadableDatabase();
        int tagId;

        //Cursor cursor = db.query("Image", new String[] {"Id"}, "Id" + "=?", new String[]{Integer.toString(id)}, null, null, null, null);
        String selectQuery = "SELECT Id FROM Tag WHERE Text=?";
        Cursor cursor = null;
        cursor = db.rawQuery(selectQuery, new String[]{tag});
//        if(cursor.getCount() == 0){
//            return -1;
//            Toast
//        }
        try{
            cursor.moveToFirst();
            tagId = cursor.getInt(cursor.getColumnIndex("Id"));
            return tagId;

//        }catch(Exception e) { return null;
        }finally{
            cursor.close();
            db.close();
        }
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
        cursor.close();
        db.close();
        // return tag list
        return tagList;
    }

    public List<String> getTagsForImageById(int imageId){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> tagList = new ArrayList<String>();

        String selectQuery = "SELECT Text FROM Tag WHERE Tag.Id in ( " +
                                "SELECT TagId FROM Link WHERE ImageId=?)";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{Integer.toString(imageId)});

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding image to list
                tagList.add(cursor.getString(cursor.getColumnIndex("Text")));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return tagList;
    }


    public void addImageTagLink(int imageId, int tagId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ImageId", imageId);
        values.put("TagId", tagId);

        db.insert("Link", null, values);
        db.close();
        NotifyListeners();
    }

    public void deleteImageTagLink(int imageId, int tagId){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM Link WHERE imageId=" + imageId + " AND tagId=" + tagId);

        db.close();
        NotifyListeners();
    }

    // Deleting single tag
//    public void deleteTag(String tag) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete("Tag", "Id" + " = ?", tag);
//        db.close();
//    }

    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE Image");
        db.execSQL("DROP TABLE Tag");
        db.execSQL("DROP TABLE Link");
        db.execSQL(CreateImageTable);
        db.execSQL(CreateTagTable);
        db.execSQL(CreateLinkTable);
        db.close();
        NotifyListeners();
    }


    //HELPER
    //taken from: http://stackoverflow.com/questions/27807659/query-a-sqlite-database-using-an-array-android
    String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            //throw new RuntimeException("No placeholders");
            return"";
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }
}