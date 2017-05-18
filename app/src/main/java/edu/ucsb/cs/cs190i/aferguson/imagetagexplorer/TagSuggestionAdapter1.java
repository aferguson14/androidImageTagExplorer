package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ferg on 5/12/17.
 */


//resource used: http://stackoverflow.com/questions/26245139/how-to-create-recyclerview-with-multiple-view-type

public class TagSuggestionAdapter1 extends BaseAdapter implements Filterable, ImageTagDatabaseHelper.OnDatabaseChangeListener{
    private List<String> filteredData;
    private List<String> originalData;
    private ImageTagDatabaseHelper mDb;
    private Context context;
    private LayoutInflater layoutInflater;
    private ItemFilter mFilter = new ItemFilter();

    // Provide a suitable constructor (depends on the kind of dataset)
    public TagSuggestionAdapter1(Context context, List<String> myDataset, ImageTagDatabaseHelper db) {
        this.context = context;
        this.mDb = db;
        this.originalData = myDataset;
        this.filteredData = myDataset;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null && filteredData.size()!=0) {
            view = LayoutInflater.from(context).inflate(R.layout.recycler_tag_suggestion, null);
        }
        if(filteredData.size()!=0){
            TextView tag = (TextView) view.findViewById(R.id.recycler_tag_suggestion);
            tag.setText(filteredData.get(position));
        }
        return view;
    }

    @Override
    public int getCount() {
        return filteredData.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position); //returns list item at the specified position
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<String> list = originalData;

            int count = list.size();
            final List<String> nlist = new ArrayList<String>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }

    }

    //TODO
    @Override
    public void OnDatabaseChange(){
        originalData = mDb.getAllTags();
        filteredData = mDb.getAllTags();
    }



}


