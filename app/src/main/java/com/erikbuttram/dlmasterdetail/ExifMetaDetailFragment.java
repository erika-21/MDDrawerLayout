package com.erikbuttram.dlmasterdetail;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import com.erikbuttram.dlmasterdetail.dummy.DummyContent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a single ExifMeta detail screen.
 * This fragment is either contained in a {@link CameraActivity}
 * in two-pane mode (on tablets) or a {@link ExifMetaDetailActivity}
 * on handsets.
 */
public class ExifMetaDetailFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).  Don't change this unless
     * you enjoy sadness
     */
    public ExifMetaDetailFragment() {
    }

    private String imagePath;
    private ListView listView;

    /**
     * Static factory to create new detail fragments
     * @param nothing nothing, but you can pass anything through here...so long as it's Parcelable
     * @return
     */
    public static ExifMetaDetailFragment createInstance(String nothing) {
        ExifMetaDetailFragment fragment = new ExifMetaDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CameraActivity.FILE_URI_KEY, nothing);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(CameraActivity.FILE_URI_KEY)) {
            //Take in the meta data information, but in a real world application
            //this could be anything.
            String uri = (String)getArguments().get(CameraActivity.FILE_URI_KEY);
            imagePath = uri;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exifmeta_detail, container, false);
        // Show the dummy content as text in a TextView.
        listView = (ListView)rootView.findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_1);
        adapter.addAll(createItemsFromInterface());
        listView.setAdapter(adapter);
        return rootView;
    }

    private List<String> createItemsFromInterface() {
        ArrayList<String> items = new ArrayList<>();
        items.add("Sorry : No Metadata here");
        items.add("Why : The Camera Library has a bug in it");
        items.add("BUG : That won't allow lollipop cameras to take photos");
        items.add("Maybe : next time though");
        return items;
    }

    private String getTag(String key, ExifInterface inter) {
        return String.format("%s : %s", key, inter.getAttribute(key));
    }
}
