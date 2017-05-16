package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainTagAdapter tagButtonAdapter;
    private RecyclerView tagSuggestionRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //database TODO
        ImageTagDatabaseHelper.Initialize(this);
        final ImageTagDatabaseHelper db = ImageTagDatabaseHelper.GetInstance();

        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FABs
        FloatingActionButton cameraButton = (FloatingActionButton) findViewById(R.id.camera_button);
        FloatingActionButton galleryButton = (FloatingActionButton) findViewById(R.id.gallery_button);

        //tag suggest drop down
        final ArrayAdapter<String> tagSuggestionAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, db.getAllTags());
        final AutoCompleteTextView mainTagTextView = (AutoCompleteTextView) findViewById(R.id.main_tag_text);
        mainTagTextView.setAdapter(tagSuggestionAdapter);
        //db.Subscribe(tagSuggestionAdapter);


        db.addTag("Octopus"); //test tag

        //tag filter reycler
        final String[] tagSortArray = new String[1];
        tagSuggestionRecycler = (RecyclerView)findViewById(R.id.tag_suggestion_recycler);
//        final MainTagAdapter tagButtonAdapter = new MainTagAdapter(tagSortArray);
//        db.Subscribe(tagButtonAdapter); //listen for db changes
//        tagSuggestionRecycler.setAdapter(tagButtonAdapter);


        Log.d("test", "test");
        //handle autocomplete click
        mainTagTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
                Log.d("tagSortArray", "onitemselect");
                tagSortArray[0] = (String)parent.getItemAtPosition(position);
                //mainTagTextView.dismissDropDown();
                //tagButtonAdapter.notifyDataSetChanged();
            }
            @Override
            public void onNothingSelected (AdapterView<?> parent) {
            }
        });

        mainTagTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("tagSortArray", "onitemclick");
                mainTagTextView.setText("");
                Log.d("tagSortArray", (String)parent.getItemAtPosition(position));
                Log.d("tagSortArray", "test");
                tagSortArray[0] = (String)parent.getItemAtPosition(position);
                tagButtonAdapter = new MainTagAdapter(tagSortArray);
                db.Subscribe(tagButtonAdapter); //listen for db changes
                tagSuggestionRecycler.setAdapter(tagButtonAdapter);
                tagButtonAdapter.notifyDataSetChanged();

            }
        });


        //images
        final TextView textView = (TextView)findViewById(R.id.textView);
        final ImageView imageView = (ImageView)findViewById(R.id.imageView);


        TaggedImageRetriever.getNumImages(new TaggedImageRetriever.ImageNumResultListener() {
            @Override
            public void onImageNum(int num) {
                textView.setText(textView.getText() + "\n\n" + num);

                for (int i = 0; i < num; i++) {

                    final int I_CLOSURE = i;
                    // this is referred to as an inner class closure. See, e.g. discussion at
                    // http://stackoverflow.com/questions/2804923/how-does-java-implement-inner-class-closures

                    TaggedImageRetriever.getTaggedImageByIndex(i, new TaggedImageRetriever.TaggedImageResultListener() {
                        @Override
                        public void onTaggedImage(TaggedImageRetriever.TaggedImage image) {
                            if (image != null) {
                                String fname = "Test"+ I_CLOSURE + ".jpg";
                                try (FileOutputStream stream = openFileOutput(fname, Context.MODE_PRIVATE)) {
                                    image.image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    image.image.recycle();
                                } catch (IOException e) {
                                }
                                Picasso.with(MainActivity.this).load(getFileStreamPath(fname)).into(imageView);
                                // Careful! Picasso is using a worker thread. So this is creating more asynchronicity!
                                StringBuilder tagList = new StringBuilder();
                                for (String p : image.tags) {
                                    tagList.append(p + "\n");

                                    db.addTag(p); //test
                                    tagSuggestionAdapter.notifyDataSetChanged();
                                }
                                textView.setText(textView.getText() + "\n\n" + tagList.toString());
                            }
                        }
                    });
                }
            }
        });

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
                //fill in
                return true;
            case R.id.action_clear_db:
                //fill in
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


}