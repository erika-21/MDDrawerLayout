package com.erikbuttram.dlmasterdetail;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.erikbuttram.dlmasterdetail.thirdparty.CameraView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *  Utilizes the {@link com.erikbuttram.dlmasterdetail.thirdparty.CameraView} library
 *  to render and take a picture.
 */
public class CameraFragment extends Fragment {

    private static final String TAG = CameraFragment.class.getPackage() + " " +
            CameraFragment.class.getSimpleName();

    public static interface PictureTakenCallback {
        public void onPictureTaken(String filename);
    }

    private CameraView mCameraView;
    private Button mButton;
    private PictureTakenCallback mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CameraFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        mCameraView = (CameraView)rootView.findViewById(R.id.camera_view);
        mButton = (Button)rootView.findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPictureTaken("Nothing");
                /*
                mCameraView.takePicture(new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {
                            File outputFile = new File(getActivity().getExternalCacheDir(), "temp.jpg");
                            FileOutputStream stream = new FileOutputStream(outputFile);
                            stream.write(data);
                            stream.close();
                        } catch (FileNotFoundException fileEx) {
                            Log.d(TAG, "Unable to capture image: file not found");
                        } catch (IOException ioEx) {
                            Log.d(TAG, String.format("IOException occurred while capturing image: %s", ioEx.getMessage()));
                        }
                    }
                });
                */
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PictureTakenCallback)activity;
        } catch (ClassCastException classEx) {
            throw new ClassCastException("Classes implementing this must implement the PictureTakenCallback Interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
