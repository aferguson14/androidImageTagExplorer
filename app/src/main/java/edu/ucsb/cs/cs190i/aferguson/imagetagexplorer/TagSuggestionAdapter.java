package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ferg on 5/16/17.
 */

public class TagSuggestionAdapter extends ArrayAdapter<String> {//implements ImageTagDatabaseHelper.OnDatabaseChangeListener{

    private LayoutInflater layoutInflater;
    private List<String> mDataset;
//    private int viewResourceId;
    private ImageTagDatabaseHelper mDb;
    private Context mContext;


    public TagSuggestionAdapter(Context context, int viewResourceId, List<String> dbTags, ImageTagDatabaseHelper db) {
        super(context, viewResourceId, dbTags);
        Log.d("tagsuggestion", "in Constructor");
//        Log.d("tagsuggestion", Integer.toString(dbTags.size()));
        mContext = context;
        mDb = db;
        mDataset = dbTags;
        layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //this.itemsAll = (ArrayList<Customer>) items.clone();
        //this.suggestions = new ArrayList<Customer>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("tagsuggestion", "in getView");
//        Log.d("tagsuggestion", Integer.toString(mDataset.size()));

//        Log.d("tagsuggestion", "first entry: " + mDataset.get(0));

        View view = convertView;

        if (view == null && mDataset.size()!=0) {
            view = layoutInflater.inflate(R.layout.recycler_tag_suggestion, null); //null
        }
        if(mDataset.size()!=0){
            TextView tag = (TextView) view.findViewById(R.id.recycler_tag_suggestion);
            tag.setText(mDataset.get(position));
        }
        return view;
    }

//    @Override
//    public void OnDatabaseChange(){
////        Log.d("tagsuggestion", "onDatabaseChange");
////        this.clear();
////        this.addAll(mDb.getAllTags());
////        this.notifyDataSetChanged();
////        ((MainActivity)mContext).updateTagSuggestionAdapter();
//    }

    public void updateTagsList(List<String> newList){
        Log.d("tagsuggestion", "updateTagsLIst");
        mDataset.clear();
        mDataset.addAll(newList);
        Log.d("tagsuggestion", Integer.toString(mDataset.size()));
        this.notifyDataSetChanged();
    }
}
