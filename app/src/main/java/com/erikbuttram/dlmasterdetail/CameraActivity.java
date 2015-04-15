package com.erikbuttram.dlmasterdetail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;


/**
 * An Activity that utilizes a camera view, which is used to take a picture
 * and display the list of meta data in the list fragment.
 * <p/>
 * The activity makes heavy use of fragments. The Camera view is in the
 * {@link CameraFragment} and the item detail list
 * (if present) is a {@link com.erikbuttram.dlmasterdetail.ExifMetaDetailFragment} that
 * expands from the right programmatically.
 * <p/>
 * This activity also implements the required
 * {@link com.erikbuttram.dlmasterdetail.CameraFragment.PictureTakenCallback} interface
 * to listen for when the {@link com.erikbuttram.dlmasterdetail.thirdparty.CameraView} takes a picture
 * However, this could be for any event, this needs to be done in this fragment
 * so it can be properly propagated to {@link ExifMetaDetailFragment}
 */
public class CameraActivity extends FragmentActivity implements CameraFragment.PictureTakenCallback {

    public static final String FILE_URI_KEY = "key_file_uri";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private DrawerLayout drawerLayout;//this is the root on tablets

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (findViewById(R.id.exifmeta_detail_container) != null) {
            mTwoPane = true;
            drawerLayout = (DrawerLayout)findViewById(R.id.drawer);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        } else {
            drawerLayout = null;
        }
    }

    @Override
    public void onPictureTaken(String filename) {
        if (mTwoPane) {
            ExifMetaDetailFragment fragment = ExifMetaDetailFragment.createInstance("nothing");
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.exifmeta_detail_container, fragment)
                    .commit();
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            drawerLayout.openDrawer(Gravity.RIGHT);
        } else {
            Intent intent = new Intent(this, ExifMetaDetailActivity.class);
            intent.putExtra(FILE_URI_KEY, "nothing");
            startActivity(intent);
        }

    }
}
