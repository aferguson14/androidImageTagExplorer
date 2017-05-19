package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.Manifest;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    public final String APP_TAG = "ImageTagExplorer";

    //CAMERA VARS
    private static final int REQ_CODE_TAKE_PICTURE = 1034;
    private static final int READ_REQUEST_CODE = 42;
    private String photoFileName;
    private File mediaStorageDir;

    private FilterTagAdapter tagButtonAdapter;
    private RecyclerView tagFilterRecycler;
    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private List<String> tagSortList = new ArrayList<String>();
    private int numImages;
    private ImageTagDatabaseHelper db;
    private TagSuggestionAdapter tagSuggestionAdapter;
    private AutoCompleteTextView mainTagTextView;

    private List<String> dbTags = new ArrayList<String>();
    private List<Integer> tagIdSortList = new ArrayList<Integer>();

    private int dbNumImages;
    private int dbNumTags;

    private int curImageNum;
    private Calendar calendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //database
        ImageTagDatabaseHelper.Initialize(this);
        db = ImageTagDatabaseHelper.GetInstance();

//        db.deleteAll();

        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FABs
        FloatingActionButton cameraButton = (FloatingActionButton) findViewById(R.id.camera_button);
        FloatingActionButton galleryButton = (FloatingActionButton) findViewById(R.id.gallery_button);


        //CAMERA
        mediaStorageDir = new
                File( getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);
        calendar = Calendar.getInstance();
        cameraButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                MainActivityPermissionsDispatcher.startCameraWithCheck(MainActivity.this);
            }
        });


        //GALLERY
        galleryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, READ_REQUEST_CODE);
            }
        });




        //MAIN IMAGE FILTER SUGGESTIONS
//        if(db.getAllTags() != null){
//            dbTags = db.getAllTags();
//        }
//        else{
//            dbTags.add("");
//        }
//        dbTags = new ArrayList<>();
//        dbTags.addAll(db.getAllTags());
//        tagSuggestionAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_dropdown_item_1line, db.getAllTags()); //db.getAllTags()
//        tagSuggestionAdapter = new TagSuggestionAdapter(MainActivity.this, db.getAllTags(), db);

        tagSuggestionAdapter = new TagSuggestionAdapter(this,
                R.id.tag_filter_list, dbTags, db); //android.R.layout.simple_spinner_dropdown_item
        mainTagTextView = (AutoCompleteTextView) findViewById(R.id.main_tag_text);
        mainTagTextView.setAdapter(tagSuggestionAdapter);
//        db.Subscribe(tagSuggestionAdapter);


        //tag filter reycler
//        tagSortList = new ArrayList<String>();
        tagFilterRecycler = (RecyclerView)findViewById(R.id.tag_filter_recycler);
//        final FilterTagAdapter tagButtonAdapter = new FilterTagAdapter(tagSortList);
//        db.Subscribe(tagButtonAdapter); //listen for db changes
//        tagSuggestionRecycler.setAdapter(tagButtonAdapter);


        //handle autocomplete click
//        mainTagTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
//            @Override
//            public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
//                tagSortList[0] = (String)parent.getItemAtPosition(position);
//            }
//            @Override
//            public void onNothingSelected (AdapterView<?> parent) {
//            }
//        });
//TODO

        tagButtonAdapter = new FilterTagAdapter(tagSortList);
        db.Subscribe(tagButtonAdapter); //listen for db changes
        tagFilterRecycler.setAdapter(tagButtonAdapter);
        tagFilterRecycler.addOnItemTouchListener(
                new RecyclerItemClickListener(MainActivity.this, tagFilterRecycler ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String tag = tagSortList.get(position);
                        //remove from query
                        tagSortList.remove(position);
                        tagButtonAdapter.notifyDataSetChanged();
                        updateImageAdapter();
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );


        mainTagTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mainTagTextView.setText("");
                tagSortList.add(0, (String)parent.getItemAtPosition(position));
                updateImageAdapter();
                tagButtonAdapter.notifyDataSetChanged();
                imageAdapter.notifyDataSetChanged();

            }
        });





        //image recycler view
        imageRecyclerView = (RecyclerView) findViewById(R.id.image_recycler);
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this,2));

        imageRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, imageRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String imageUri = imageAdapter.getImageUriAtPosition(position);
                        int imageId = db.getImageIdByUri(imageUri);
                        openPhotoDialog(imageUri, db.getTagsForImageById(imageId)); //SQL auto increment starts at 1
                    } //db.getImageUriById(position+1), db.getTagsForImageById(position+1)

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

        //TODO
//        tagSortList = new ArrayList<>();
//        tagSortList.add("dog");
//        tagSortList.add("chihuahua");
//        tagSortList.add("picture");

        //db.getFilteredImages(tagstoTagIds(tagSortList))

        tagIdSortList.addAll(tagstoTagIds(tagSortList));
        imageAdapter = new ImageAdapter(MainActivity.this, db, tagstoTagIds(tagSortList));
        imageRecyclerView.setAdapter(imageAdapter);
        db.Subscribe(imageAdapter);


        //images
        //final TextView textView = (TextView)findViewById(R.id.textView);
        //final ImageView imageView = (ImageView)findViewById(R.id.imageView);

////UPDATED
//        TaggedImageRetriever.getNumImages(new TaggedImageRetriever.ImageNumResultListener() {
//            @Override
//            public void onImageNum(int num) {
//                //textView.setText(textView.getText() + "\n\n" + num);
//                numImages = num;
//                for (int i = 0; i < num; i++) {
//
//                    final int I_CLOSURE = i;
//                    // this is referred to as an inner class closure. See, e.g. discussion at
//                    // http://stackoverflow.com/questions/2804923/how-does-java-implement-inner-class-closures
//
//                    TaggedImageRetriever.getTaggedImageByIndex(i, new TaggedImageRetriever.TaggedImageResultListener() {
//                        @Override
//                        public void onTaggedImage(TaggedImageRetriever.TaggedImage image) {
//                            if (image != null) {
//                                String fname = "Test"+ I_CLOSURE + ".jpg";
//                                db.addImage(fname);
////                                try (FileOutputStream stream = openFileOutput(fname, Context.MODE_PRIVATE)) {
////                                    image.image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
////                                    image.image.recycle();
////                                } catch (IOException e) {
////                                }
//                                //Picasso.with(MainActivity.this).load(getFileStreamPath(fname)).into(imageView);
//                                // Careful! Picasso is using a worker thread. So this is creating more asynchronicity!
//                                StringBuilder tagList = new StringBuilder();
//                                for (String p : image.tags) {
//                                    tagList.append(p + "\n");
//
//                                    db.addTag(p); //test
//                                    tagSuggestionAdapter.notifyDataSetChanged();
//                                }
//                                //textView.setText(textView.getText() + "\n\n" + tagList.toString());
//                            }
//                        }
//                    });
//                }
//            }
//        });

        //SKELETON
//        TaggedImageRetriever.getNumImages(new TaggedImageRetriever.ImageNumResultListener() {
//            @Override
//            public void onImageNum(int num) {
//                //textView.setText(textView.getText() + "\n\n" + num);
//            }
//        });
//
//        TaggedImageRetriever.getTaggedImageByIndex(0, new TaggedImageRetriever.TaggedImageResultListener() {
//            @Override
//            public void onTaggedImage(TaggedImageRetriever.TaggedImage image) {
//                if (image != null) {
//                    try (FileOutputStream stream = openFileOutput("Test.jpg", Context.MODE_PRIVATE)){
//                        image.image.compress(Bitmap.CompressFormat.JPEG, 100, stream); //"" + image.image.hashCode().toString + ".jpg", bring in current system time to make sure unique
//                        image.image.recycle();
//                    } catch (IOException e) {
//                    }
//                    Picasso.with(MainActivity.this).load(getFileStreamPath("Test.jpg")).resize(500,500).centerCrop().into(imageView);
//                    // imageView.setImageBitmap(image.image);
//                    StringBuilder tagList = new StringBuilder();
//                    for (String p : image.tags) {
//                        tagList.append(p + "\n");
//                    }
//                    //textView.setText(textView.getText() + "\n\n" + tagList.toString());
//                }
//            }
//        });
    }

    //resource used: http://stackoverflow.com/questions/31231609/creating-a-button-in-android-toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_populate_db:
                Log.d("optionItemSelected", "inpopulateDB");
                TaggedImageRetriever.getNumImages(new TaggedImageRetriever.ImageNumResultListener() {
                    @Override
                    public void onImageNum(int num) {
                        //textView.setText(textView.getText() + "\n\n" + num);
                        numImages = num;
                        for (int i = 0; i < num; i++) {

                            final int I_CLOSURE = i;
                            // this is referred to as an inner class closure. See, e.g. discussion at
                            // http://stackoverflow.com/questions/2804923/how-does-java-implement-inner-class-closures

                            TaggedImageRetriever.getTaggedImageByIndex(i, new TaggedImageRetriever.TaggedImageResultListener() {
                                @Override
                                public void onTaggedImage(TaggedImageRetriever.TaggedImage image) {
                                    if (image != null) {
                                        String fname = "Test"+ I_CLOSURE + ".jpg";

                                        if (isExternalStorageAvailable()) {
                                            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                                                Log.d(APP_TAG, "failed to create directory");
                                            }
                                        // Return the file target for the photo based on filename
                                            File file = new File(mediaStorageDir.getPath() + File.separator + fname);
                                            Log.d("photouri", "Inside getPhotoFileURI created file");
                                    // wrap File object into a content provider, required for API >= 24
                                            Uri takenPhotoUri = FileProvider.getUriForFile(MainActivity.this, "com.codepath.fileprovider", file);

                                            try{
                                                OutputStream outStream = new FileOutputStream(file);
                                                image.image.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                                                image.image.recycle();
                                                outStream.flush();
                                                outStream.close();
                                            } catch (IOException e) {
                                            }
                                            db.addImage(takenPhotoUri.toString());
                                            int imageId = db.getImageIdByUri(takenPhotoUri.toString());
                                            imageAdapter.notifyDataSetChanged();

                                            StringBuilder tagList = new StringBuilder();
                                            for (String p : image.tags) {
                                                tagList.append(p + "\n");

                                                db.addTag(p); //test
                                                int tagId = db.getTagId(p);
                                                db.addImageTagLink(imageId, tagId);
                                                tagSuggestionAdapter.notifyDataSetChanged();

                                            }
                                        }
                                    }
                                }
                            });
                        }
//                        dbTags.clear();
//                        dbTags.addAll(db.getAllTags());
                    }

                });
//                tagSuggestionAdapter.clear();
//                tagSuggestionAdapter.addAll(db.getAllTags());
//                dbTags.clear();
//                dbTags.addAll(db.getAllTags());
//                Log.d("dbTags", dbTags.get(0));
//                tagSuggestionAdapter.updateTagsList(db.getAllTags());
//                Log.d("dbTags", "Pop" + dbTags.get(0) + "");
                tagSuggestionAdapter.notifyDataSetChanged();
                imageAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_clear_db:
                Log.d("optionItemSelected", "inclearDB");
                db.deleteAll();
//                dbTags.clear();
//                dbTags.addAll(db.getAllTags());
//                tagSuggestionAdapter.updateTagsList(db.getAllTags());
//                tagSuggestionAdapter.clear();
//                tagSuggestionAdapter.addAll(db.getAllTags());
                tagSuggestionAdapter.notifyDataSetChanged();
                imageAdapter.notifyDataSetChanged();

//                Log.d("database", Integer.toString(db.getAllTags().size()));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageTagDatabaseHelper.GetInstance().close();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Show your dialog here (this is called right after onActivityResult)
    }


    public void openPhotoDialog(String uri, List<String> tags){
        //FragmentManager fragMan = getFragmentManager();
        //FragmentTransaction fragTransaction = fragMan.beginTransaction();

        DialogFragment photoFrag = EditPhotoDialogFragment.newInstance(uri, tags);//new EditPhotoDialogFragment();

//        Bundle bundle = new Bundle();
//        bundle.putString("image", uri);
//        photoFrag.setArguments(bundle);

        photoFrag.show(getFragmentManager(), "dialog");

        //fragTransaction.add(R.id.photo_fragment_container, photoFrag).commit();

        if ( photoFrag.getDialog() != null )
            photoFrag.getDialog().setCanceledOnTouchOutside(true);

    }

    public List<Integer> tagstoTagIds(List<String> tags){
        List<Integer> convertedList = new ArrayList<>();
        Log.d("filteredimages", Integer.toString(tags.size()));
        if(tags!=null){
            if(tags.size()!= 0) {
                Log.d("filteredimages", "TAGS SIZE" + Integer.toString(tags.size()));
                for (int i = 0; i < tags.size(); i++) {
                    convertedList.add(db.getTagId(tags.get(i)));
                }
            }
        }
        return convertedList;
    }

    public void addTagToImage(String uri, String tag){
        int imageId = db.getImageIdByUri(uri);
        db.addTag(tag);
        int tagId = db.getTagId(tag);
        db.addImageTagLink(imageId, tagId);

    }

    public void deleteTagFromImage(String uri, String tag){
        int imageId = db.getImageIdByUri(uri);
        int tagId = db.getTagId(tag);
        db.deleteImageTagLink(imageId, tagId);
    }


    public void updateTagSuggestionAdapter(){
//        tagSuggestionAdapter.clear();
//        dbTags.clear();
//        dbTags.addAll(db.getAllTags());
//        tagSuggestionAdapter.notifyDataSetChanged();
        TagSuggestionAdapter tagSuggestionAdapter1 = new TagSuggestionAdapter(this,
                R.id.tag_filter_list, db.getAllTags(), db);
        mainTagTextView.setAdapter(tagSuggestionAdapter1);
//        db.Subscribe(tagSuggestionAdapter1);
    }

    public void updateImageAdapter(){
//        tagIdSortList.clear();
//        tagIdSortList.addAll(tagstoTagIds(tagSortList));
        imageAdapter= new ImageAdapter(MainActivity.this, db, tagstoTagIds(tagSortList));
        imageRecyclerView.setAdapter(imageAdapter);
        db.Subscribe(imageAdapter);
    }
    //For Camera & Gallery Intents
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

//        if(requestCode == REQ_CODE_TAKE_PICTURE){
//            if(resultCode == RESULT_OK){
//                Log.d("camera", "resultCode ok");
////                ArrayList<String> list = intent.getStringArrayListExtra("SOMETHING");
//
//                Uri takenPhotoUri = getPhotoFileUri(photoFileName);
//                db.addImage(takenPhotoUri.toString());
//                imageAdapter.notifyDataSetChanged();
//            }
//            else{
//                //toast
//            }
//        }

        if (requestCode == REQ_CODE_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri takenPhotoUri = getPhotoFileUri(photoFileName);
                db.addImage(takenPhotoUri.toString());
                imageAdapter.notifyDataSetChanged();
                List<String> emptyTempList = new ArrayList<String>();
                openPhotoDialog(takenPhotoUri.toString(), emptyTempList);

//// by this point we have the camera photo on disk
//                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
//// RESIZE BITMAP (if desired)
//// Load the taken image into a preview
//                ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
//                ivPreview.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (intent != null) {
                uri = intent.getData();
                db.addImage(uri.toString());
                imageAdapter.notifyDataSetChanged();
                List<String> emptyTempList = new ArrayList<String>();
                openPhotoDialog(uri.toString(), emptyTempList);
            }
        }


    }




    //CAMERA STUFF
    @NeedsPermission(Manifest.permission.CAMERA)
    public void startCamera(){
        Intent picIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFileName = "ImageTagExplorer_" + calendar.getTime(); //db.getAllImages().size()+1
        picIntent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName));
        startActivityForResult(picIntent, REQ_CODE_TAKE_PICTURE);
    }

    // Returns the Uri for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {

        Log.d("photouri", "Inside getPhotoFileURI");
// Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
// Get safe storage directory for photos
// Use `getExternalFilesDir` on Context to access package-specific directories.
// This way, we don't need to request external read/write runtime permissions.
//            File mediaStorageDir = new
//                    File( getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);
// Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(APP_TAG, "failed to create directory");
            }
// Return the file target for the photo based on filename
            File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
            Log.d("photouri", "Inside getPhotoFileURI created file");
// wrap File object into a content provider, required for API >= 24
            return FileProvider.getUriForFile(this, "com.codepath.fileprovider", file);
        }
        return null; }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

}