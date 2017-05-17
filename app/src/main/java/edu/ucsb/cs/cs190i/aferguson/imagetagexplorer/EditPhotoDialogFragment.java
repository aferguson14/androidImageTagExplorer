package edu.ucsb.cs.cs190i.aferguson.imagetagexplorer;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Ferg on 5/16/17.
 */

public class EditPhotoDialogFragment extends Fragment {

    private ImageView imageView;
    private AutoCompleteTextView textField;
    private String uri;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_dialog_fragment, container, false); //need XML file for each fragment


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            uri = bundle.getString("image", null);
        }

        if(uri!=null){
            Log.d("photoFrag", uri);
        }
        else{
            Log.d("photoFrag", "Uri Null");
        }

        imageView = (ImageView)view.findViewById(R.id.fragment_image_view);
        
        Picasso.with(getContext()).load(getContext().getFileStreamPath(uri)).into(imageView);

        textField = (AutoCompleteTextView) view.findViewById(R.id.main_tag_text);
//        if (text != null) {
//            textField.setText(text);
//        }


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
