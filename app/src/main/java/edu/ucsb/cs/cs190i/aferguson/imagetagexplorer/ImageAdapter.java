package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by Ferg on 5/14/17.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>
        implements ImageTagDatabaseHelper.OnDatabaseChangeListener{
    private List<String> mDataset;
    private List<Integer> mTagIds;
    private Context mContext;
    private ImageTagDatabaseHelper mDb;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView mImage;
        public ViewHolder(ImageView v) {
            super(v);
            mImage = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ImageAdapter(Context context, ImageTagDatabaseHelper db, List<Integer> tagIdList) {
        mContext = context;
        //mDataset = myDataset;
        mDb = db;
        mTagIds = tagIdList;
        mDataset = db.getFilteredImages(tagIdList);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view (inflate layout)
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_imageview, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    //This is where we load image w/Picasso, store ImageView into ViewHolder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

//        File f = new File(mDataset.get(position));
        Uri imageUri = Uri.parse(mDataset.get(position));

        Log.d("imageAdapter", imageUri.toString());

//        Picasso.with(mContext).load(mContext.getFileStreamPath(mDataset.get(position))).into(holder.mImage);
        Picasso.with(mContext).load(imageUri).into(holder.mImage);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void OnDatabaseChange(){
        mDataset = mDb.getFilteredImages(mTagIds);
    }

}
