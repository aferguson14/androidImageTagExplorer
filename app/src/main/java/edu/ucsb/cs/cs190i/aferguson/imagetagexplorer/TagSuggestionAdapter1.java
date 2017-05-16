package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ferg on 5/12/17.
 */


//resource used: http://stackoverflow.com/questions/26245139/how-to-create-recyclerview-with-multiple-view-type

public class TagSuggestionAdapter1 extends RecyclerView.Adapter<TagSuggestionAdapter1.ViewHolder> implements ImageTagDatabaseHelper.OnDatabaseChangeListener{
    private List<String> mDataset;
    private ImageTagDatabaseHelper mDb;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mText;
        public ViewHolder(TextView v) {
            super(v);
            mText = v;

//            mButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                }
//            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TagSuggestionAdapter1(List<String> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TagSuggestionAdapter1.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view (inflate layout)
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_tag_suggestion, parent, false);

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
        holder.mText.setText(mDataset.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    //TODO
    @Override
    public void OnDatabaseChange(){
        mDataset = mDb.getAllImages();
    }



}


