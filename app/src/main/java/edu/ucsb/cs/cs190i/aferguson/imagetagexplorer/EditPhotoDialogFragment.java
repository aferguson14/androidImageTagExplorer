package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.app.DialogFragment;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

        if (bundle != null) {
            mUri = bundle.getString("uri");
            mTags = (List)bundle.getParcelableArrayList("tags");
        }

        imageView = (ImageView)view.findViewById(R.id.fragment_image_view);


        Uri imageUri = Uri.parse(mUri);
        Picasso.with(getContext()).load(imageUri).into(imageView);


        textField = (AutoCompleteTextView) view.findViewById(R.id.fragment_tag_text);
        textField.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_GO ||
                        actionId == EditorInfo.IME_ACTION_NEXT){
                    ((MainActivity)getActivity()).addTagToImage(mUri, textField.getText().toString());
                    mTags.add(textField.getText().toString());
                    textField.setText("");
                    tagButtonAdapter.notifyDataSetChanged();
                    ((MainActivity)getActivity()).updateImageAdapter();
                    return true;
                }
                return false;
            }
        });


        tagFilterRecycler = (RecyclerView)view.findViewById(R.id.tag_filter_recycler_fragment);
        tagButtonAdapter = new FilterTagAdapter(mTags);
        tagFilterRecycler.setAdapter(tagButtonAdapter);
        tagFilterRecycler.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), tagFilterRecycler ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String tag = mTags.get(position);
                        ((MainActivity)getActivity()).deleteTagFromImage(mUri, tag);
                        mTags.remove(position);
                        ((MainActivity)getActivity()).updateImageAdapter();
                        tagButtonAdapter.notifyDataSetChanged();
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

        return view;
    }
}
