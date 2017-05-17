package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FilterTagAdapter tagButtonAdapter;
    private RecyclerView tagFilterRecycler;
    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private String[] tagSortArray;
    private int numImages;
    private ImageTagDatabaseHelper db;
    private TagSuggestionAdapter tagSuggestionAdapter;
    private AutoCompleteTextView mainTagTextView;

    private List<String> dbTags;

    private int dbNumImages;
    private int dbNumTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //database TODO
        ImageTagDatabaseHelper.Initialize(this);
        db = ImageTagDatabaseHelper.GetInstance();



        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FABs
        FloatingActionButton cameraButton = (FloatingActionButton) findViewById(R.id.camera_button);
        FloatingActionButton galleryButton = (FloatingActionButton) findViewById(R.id.gallery_button);

        //tag suggest drop down
//        tagSuggestionAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_dropdown_item_1line, dbTags); //db.getAllTags()
        //tagSuggestionAdapter = new TagSuggestionAdapter1(db.getAllTags());
        this.dbTags = db.getAllTags();
        tagSuggestionAdapter = new TagSuggestionAdapter(MainActivity.this,
                android.R.layout.simple_dropdown_item_1line, dbTags); //db.getAllTags()
        mainTagTextView = (AutoCompleteTextView) findViewById(R.id.main_tag_text);
        mainTagTextView.setAdapter(tagSuggestionAdapter);
        db.Subscribe(tagSuggestionAdapter);


        //tag filter reycler
        tagSortArray = new String[1];
        tagFilterRecycler = (RecyclerView)findViewById(R.id.tag_filter_recycler);
//        final FilterTagAdapter tagButtonAdapter = new FilterTagAdapter(tagSortArray);
//        db.Subscribe(tagButtonAdapter); //listen for db changes
//        tagSuggestionRecycler.setAdapter(tagButtonAdapter);


        //handle autocomplete click
//        mainTagTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
//            @Override
//            public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
//                tagSortArray[0] = (String)parent.getItemAtPosition(position);
//            }
//            @Override
//            public void onNothingSelected (AdapterView<?> parent) {
//            }
//        });

        mainTagTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mainTagTextView.setText("");
                tagSortArray[0] = (String)parent.getItemAtPosition(position);
                tagButtonAdapter = new FilterTagAdapter(tagSortArray);
                db.Subscribe(tagButtonAdapter); //listen for db changes
                tagFilterRecycler.setAdapter(tagButtonAdapter);
                tagButtonAdapter.notifyDataSetChanged();

            }
        });



        //image recycler view
        imageRecyclerView = (RecyclerView) findViewById(R.id.image_recycler);
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this,2));

        imageRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, imageRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        openPhotoDialog(db.getImageById(position+1)); //SQL auto increment starts at 1
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

        imageAdapter = new ImageAdapter(MainActivity.this, db);
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
                                        db.addImage(fname);
                                        imageAdapter.notifyDataSetChanged();
                                try (FileOutputStream stream = openFileOutput(fname, Context.MODE_PRIVATE)) {
                                    image.image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    image.image.recycle();
                                } catch (IOException e) {
                                }
                                        //Picasso.with(MainActivity.this).load(getFileStreamPath(fname)).into(imageView);
                                        // Careful! Picasso is using a worker thread. So this is creating more asynchronicity!
                                        StringBuilder tagList = new StringBuilder();
                                        for (String p : image.tags) {
                                            tagList.append(p + "\n");

                                            db.addTag(p); //test
                                            //tagSuggestionAdapter.notifyDataSetChanged();

                                        }
                                        //textView.setText(textView.getText() + "\n\n" + tagList.toString());
                                    }
                                }
                            });
                        }
                    }
                });
                tagSuggestionAdapter.updateTagsList(db.getAllTags());
//                tagSuggestionAdapter.notifyDataSetChanged();
                imageAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_clear_db:
                Log.d("optionItemSelected", "inclearDB");
                db.deleteAll();
                dbTags.clear();
                dbTags.addAll(db.getAllTags());
                tagSuggestionAdapter.updateTagsList(db.getAllTags());
//                tagSuggestionAdapter.notifyDataSetChanged();
                imageAdapter.notifyDataSetChanged();

                Log.d("database", Integer.toString(db.getAllTags().size()));

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

    public void updateTagsList(List<String> newList){
        dbTags.clear();
        dbTags.addAll(newList);

    }

    public void openPhotoDialog(String uri){
        FragmentManager fragMan = getFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();

        DialogFragment photoFrag = EditPhotoDialogFragment.newInstance();//new EditPhotoDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putString("image", uri);
        photoFrag.setArguments(bundle);

        photoFrag.show(getFragmentManager(), "dialog");

        //fragTransaction.add(R.id.photo_fragment_container, photoFrag).commit();

        if ( photoFrag.getDialog() != null )
            photoFrag.getDialog().setCanceledOnTouchOutside(true);
//        photoFrag.dismiss();

    }

}