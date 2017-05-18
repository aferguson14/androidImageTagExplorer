package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.app.DialogFragment;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.RuntimePermissions;

/**
 * Created by Ferg on 5/16/17.
 */
public class EditPhotoDialogFragment extends DialogFragment {

    private ImageView imageView;
    private AutoCompleteTextView textField;
    private String mUri;
    private RecyclerView tagFilterRecycler;
    private FilterTagAdapter tagButtonAdapter;
    private ImageTagDatabaseHelper mdb;
    private List<String> mTags;

    static EditPhotoDialogFragment newInstance(String uri, List<String> tags) {
        EditPhotoDialogFragment frag = new EditPhotoDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("tags", (ArrayList)tags);
        args.putString("uri", uri);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_dialog_fragment, container, false); //need XML file for each fragment

        Bundle bundle = this.getArguments();

//        if (bundle != null) {
//            uri = bundle.getString("image", null);
//        }
        if (bundle != null) {
            mUri = bundle.getString("uri");
            mTags = (List)bundle.getParcelableArrayList("tags");
        }

        imageView = (ImageView)view.findViewById(R.id.fragment_image_view);


        Uri imageUri = Uri.parse(mUri);
        Picasso.with(getContext()).load(imageUri).into(imageView);


//        Picasso.with(getContext()).load(getContext().getFileStreamPath(mUri)).into(imageView);

        textField = (AutoCompleteTextView) view.findViewById(R.id.main_tag_text);

        tagFilterRecycler = (RecyclerView)view.findViewById(R.id.tag_filter_recycler_fragment);
        tagButtonAdapter = new FilterTagAdapter(mTags);
        //db.Subscribe(tagButtonAdapter); //listen for db changes
        tagFilterRecycler.setAdapter(tagButtonAdapter);
        tagFilterRecycler.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), tagFilterRecycler ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String tag = mTags.get(position);
                        ((MainActivity)getActivity()).deleteTagFromImage(mUri, tag);
                        mTags.remove(position);
                        tagButtonAdapter.notifyDataSetChanged();
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
        return view;
    }


//    public void saveState(Bundle bundle) {
//        bundle.putString(TextExtra, textField.getText().toString());
//    }
//
//    public void restoreState(Bundle bundle) {
//        if (bundle != null) {
//            text = bundle.getString(TextExtra);
//        }
//    }
}
