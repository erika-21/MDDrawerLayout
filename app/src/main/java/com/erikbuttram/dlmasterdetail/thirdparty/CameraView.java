package com.erikbuttram.dlmasterdetail.thirdparty;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import com.erikbuttram.dlmasterdetail.R;

import java.io.IOException;
import java.util.List;

/**
 * Created by erikb on 3/31/15.
 */
public class CameraView extends TextureView implements TextureView.SurfaceTextureListener,
        Camera.AutoFocusCallback {

    public static final String TAG = CameraView.class.getPackage() + " " +
            CameraView.class.getSimpleName();

    public static final int ID_NONE = -99;

    /**
     * {@link com.erikbuttram.dlmasterdetail.thirdparty.CameraView#mCurrentCamera}
     * @return the current camera that is in use.
     */
    public Camera getCurrentCamera() {
        return mCurrentCamera;
    }

    /**
     * {@link com.erikbuttram.dlmasterdetail.thirdparty.CameraView#mCameraInfo}
     * @return
     */
    public Camera.CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    /**
     * Set to enable the zoom feature on the camera, this will use the cameras built in max and
     * min zoom levels to intelligently set the zoom.  If zooming isn't supported,
     * this property is ignored
     * @param isEnabled
     */
    public void enableZoom(boolean isEnabled) {
        mZoomEnabled = isEnabled;
    }

    /**
     * Sets or unsets the auto focus, if set to false, there will be no auto focus
     * @param autoFocusEnabled
     */
    public void setAutoFocus(boolean autoFocusEnabled) {
        this.mAutoFocusEnabled = autoFocusEnabled;
    }

    /**
     * Used to set or unset the "shutter" sounds whenever a picture is taken with the camera.
     * This value is ignored in Jelly Bean (api 16) and below.
     * @param shutterEnabled
     */
    public void setShutterEnabled(boolean shutterEnabled) {
        this.mEnableShutter = shutterEnabled;
    }

    /**
     * Used to set the focus mode for the camera view.  The default is
     * {@link android.hardware.Camera.Parameters#FOCUS_MODE_AUTO}
     * @param mode
     */
    public void setFocusMode(String mode) {
        this.mFocusSetting = mode;
    }

    /**
     * The currently used camera being used by the TextureView;
     */
    private Camera mCurrentCamera;


    /**
     *  Returns the Camera Info of the current camera
     */
    private Camera.CameraInfo mCameraInfo;
    private boolean mIsCameraOpen;
    private CameraPosition mCurrentPos;
    private int mCurrentCameraId;
    private boolean mZoomEnabled = true;
    private String mFocusSetting;
    private boolean mAutoFocusEnabled = true;
    private boolean mEnableShutter = true;

    private ScaleGestureDetector mScaleGestureDetector;
    private CameraZoomListener mZoomListener;

    private void setAttributes(AttributeSet attributeSet) {
        TypedArray array = getContext().getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.CameraView,
                0,
                0);

        mZoomEnabled = array.getBoolean(R.styleable.CameraView_zoomEnabled, true);
        int currentPosInt = array.getInteger(R.styleable.CameraView_cameraPosition, 0);
        mCurrentPos = currentPosInt == 0 ? CameraPosition.Back : CameraPosition.Front;
        mAutoFocusEnabled = array.getBoolean(R.styleable.CameraView_autoFocus, true);
        mEnableShutter = array.getBoolean(R.styleable.CameraView_enableShutter, true);
    }

    //gets called before anything else
    private void init() {
        mCurrentCamera = null;
        mCurrentCameraId = ID_NONE;
        mCurrentPos = CameraPosition.Back;
        mCameraInfo = new Camera.CameraInfo();
        setSurfaceTextureListener(this);
        mZoomListener = new CameraZoomListener();
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), mZoomListener);
        mFocusSetting = Camera.Parameters.FOCUS_MODE_AUTO;
    }

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
        setAttributes(attributeSet);
    }

    public void releaseCamera() {
        mCurrentCamera.stopPreview();
        mCurrentCamera.release();
    }

    /**
     * Basically an internal api that returns the focus mode to use, if any.
     * @param supported
     * @return the supported {@link android.hardware.Camera.Parameters#getSupportedFocusModes()} that was specified,
     * the only focus mode available, AUTO if available, or an empty string (not to set focus mode)
     */
    private String setFocusMode(List<String> supported) {

        if (supported.size() == 1) {
            return supported.get(0);
        }

        if (supported.size() == 0) {
            return "";
        }

        if (supported.contains(mFocusSetting)) {
            return mFocusSetting;
        } else if (supported.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            return Camera.Parameters.FOCUS_MODE_AUTO;
        }

        return supported.get(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mZoomEnabled) {
            mScaleGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mCurrentCamera != null) {
            mCurrentCamera.release();
            mIsCameraOpen = false;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        if (mCurrentCamera == null) {
            setCameraPosition(mCurrentPos);
        } else {
            try {
                //we're resuming, just start the preview again
                mCurrentCamera = Camera.open(mCurrentCameraId);
                mIsCameraOpen = true;
                adjustRotation();
            } catch (RuntimeException sadDays) {
                mIsCameraOpen = false;
            }
        }
        try {
            if (mIsCameraOpen) {
                Camera.getCameraInfo(mCurrentCameraId, mCameraInfo);
                mCurrentCamera.setPreviewTexture(getSurfaceTexture());

                mCurrentCamera.startPreview();
            }
        } catch (IOException ioEx) {
            Log.e(TAG, String.format("Unable to initialize camera preview: %s", ioEx.getMessage()));
            mCurrentCamera = null;
            mIsCameraOpen = false;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mIsCameraOpen) {
            Camera.Parameters params = mCurrentCamera.getParameters();
            Camera.Size newSize = getPreviewSize(width, height);
            params.setPreviewSize(newSize.width, newSize.height);
            requestLayout();
            mCurrentCamera.setParameters(params);
            mCurrentCamera.startPreview();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        if (mIsCameraOpen) {
            releaseCamera();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }

    /**
     * returns the preview size to use
     * @param width
     * @param height
     * @return 2 index int array with int[0] = width int[1] = height
     */
    private Camera.Size getPreviewSize(int width, int height) {

        List<Camera.Size> previewSizes = mCurrentCamera.getParameters().getSupportedPreviewSizes();
        int[] areas = new int[previewSizes.size()];
        int i = 0;
        int area = width * height;
        //we'll go recursive, its not ideal but this set is miniscule
        for (Camera.Size size : previewSizes) {
            int localArea = size.height * size.width;
            areas[i] = localArea;
            i++;
        }
        int smallest = Integer.MAX_VALUE;
        int selectedIdx = -1;
        i = 0;
        for (int k : areas) {
            int difference = Math.abs(area - k);
            if (difference < smallest) {
                selectedIdx = i;
                smallest = difference;
            }
            i++;
        }
        return previewSizes.get(selectedIdx);
        /*
if (selSize.height == height && selSize.width == width) {
            //we're done here
            return new int[] { width, height};
        }

        float arSurface = (float)width / (float)height;
        float arPreview = (float)selSize.width / (float)selSize.height;
        int selHeight;
        int selWidth;

        if (selSize.height <= height) {
            selHeight = selSize.height;
            if (selSize.width == width) {
                return new int[] { selSize.width, selHeight };
            }
            selWidth = (int)(((float)selHeight / (float)selSize.width) * (float)selHeight);
        } else {
            selWidth = selSize.width;
            selHeight = (int)((float)selSize.height / (float)selWidth) * selWidth;
        }

        return new int[] {
                selWidth, selHeight
        };
*/
    }

    private int getDisplayRotation() {
        if (getContext() == null) {
            return NO_ID;
        }
        WindowManager manager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            //set the orientation.
            int rotation = manager.getDefaultDisplay().getRotation();
            return rotation;
        }
        return NO_ID;
    }

    private void adjustRotation() {
        //reconcile the current screen orientation with the camera view
        int rotation = getDisplayRotation();
        if (rotation != NO_ID) {
            //set the orientation.
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }
            int finalAngle;
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                finalAngle = (mCameraInfo.orientation + degrees) % 360;
                //invert the angle
                finalAngle = (360 - finalAngle) % 360;
            } else {
                finalAngle = (mCameraInfo.orientation - degrees + 360) % 360;
            }
            mCurrentCamera.setDisplayOrientation(finalAngle);
            mCurrentCamera.getParameters().setRotation(finalAngle);
        }
    }

    //NOTE:  These are some sane defaults that seem to work ok across different platforms
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setInternalParams() {
        //TODO:  Pretty limited so far.  maybe more parameters can be set here?
        if (mCurrentCamera == null) {
            return;
        }
        Camera.Parameters params = mCurrentCamera.getParameters();
        //let the driver do the focusing for us
        String focus = setFocusMode(params.getSupportedFocusModes());
        if (!TextUtils.isEmpty(focus)) {
            params.setFocusMode(focus);
        }
        if (mCurrentCamera.getParameters().isVideoStabilizationSupported()) {
            params.setVideoStabilization(true);
        }
        mCurrentCamera.setParameters(params);
        if (mEnableShutter && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mCurrentCamera.enableShutterSound(true);
        }
    }

    /**
     * <p>
     *      Toggles the camera position
     * </p>
     *
     * <p>
     *     For example, if the Current Camera in use is back, it will attempt to
     *     connect to the front camera, and vice versa
     * </p>
     * @return true if the view successfully swapped out the camera, false if no camera
     * was available or the parameter passed was invalid.
     */
    public boolean toggleCameraPosition() {
        return setCameraPosition(mCurrentPos == CameraPosition.Back ? CameraPosition.Front :
                CameraPosition.Back);
    }

    /**
     * @param facing can be either {@link android.hardware.Camera.CameraInfo#CAMERA_FACING_FRONT} or
     *               {@link android.hardware.Camera.CameraInfo#CAMERA_FACING_BACK}
     * @return true if the view successfully swapped out the camera, false if no front facing camera
     * was available or the parameter passed was invalid
     */
    public boolean setCameraPosition(CameraPosition facing) {
        //TODO:  This method is fairly heavyweight, might be good to move off the main thread
        int facingInt = facing.ordinal();
        if (facingInt != Camera.CameraInfo.CAMERA_FACING_FRONT &&
                facingInt != Camera.CameraInfo.CAMERA_FACING_BACK) {
            return false;
        }
        Camera.CameraInfo inspect = new Camera.CameraInfo();
        for (int idx = 0; idx < Camera.getNumberOfCameras(); idx++) {
            Camera.getCameraInfo(idx, inspect);
            if (inspect.facing == facingInt) {
                try {
                    if (mCurrentCameraId != ID_NONE) {
                        //no longer needs a preview texture listener
                        releaseCamera();
                    }
                    mCurrentCamera = Camera.open(idx);
                    mCurrentPos = facing;
                    mCurrentCameraId = idx;
                    mIsCameraOpen = true;
                    mCurrentCamera.setPreviewTexture(getSurfaceTexture());
                    mCurrentCamera.startPreview();
                    mCameraInfo = inspect;
                    if (mCurrentCamera.getParameters().isZoomSupported() && mZoomEnabled) {
                        mZoomListener.setMaxZoom(mCurrentCamera.getParameters().getMaxZoom());
                    }
                    adjustRotation();
                    setInternalParams();
                    mCurrentCamera.autoFocus(this);
                    return true;
                } catch (Exception ex) {
                    //presumably this one works
                    Log.e(TAG, String.format("Error initializing camera: %s", ex.getMessage()));
                    mIsCameraOpen = false;
                    return false;
                }
            }
        }
        return true;
    }

    public MediaRecorder startRecording() {

        if (mCurrentCamera != null) {
            MediaRecorder recorder = new MediaRecorder();
            recorder.setCamera(mCurrentCamera);
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            try {
                recorder.prepare();
                recorder.start();
                return recorder;
            } catch (IOException ioEx) {
                return null;
            }
        }
        return null;
    }

    /**
     * Invokes the {@link android.hardware.Camera#takePicture(android.hardware.Camera.ShutterCallback, android.hardware.Camera.PictureCallback, android.hardware.Camera.PictureCallback)}
     * @param callback
     */
    public void takePicture(final Camera.PictureCallback callback) {
        if (mIsCameraOpen) {
            mCurrentCamera.stopPreview();
            mCurrentCamera.takePicture(null,new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    //Curiously, at least sony crashes when this callback is null
                }
            }, callback);
        }
    }

    private class CameraZoomListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        public CameraZoomListener() {
            this.mMaxZoom = 1f;
            this.mCurrentZoom = 0;
            this.mTolerance = .009f;
            this.mPreviousFactor = 0f;
        }

        public void setMaxZoom(float maxZoom) {
            this.mMaxZoom = maxZoom;
        }

        private int mCurrentZoom;
        private float mMaxZoom;
        private float mPreviousFactor;
        private float mTolerance;
        //only used for cameras that don't have smooth scrolling
        private Camera.Parameters useParams;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            useParams = mCurrentCamera.getParameters();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!mCurrentCamera.getParameters().isZoomSupported()) {
                return false;
            }

            if (Math.abs(detector.getScaleFactor() - mPreviousFactor) < mTolerance) {
                return false;
            }

            boolean incr = detector.getScaleFactor() > 1;

            if (incr && mCurrentZoom < mMaxZoom) {
                mCurrentZoom++;
            } else if (!incr && mCurrentZoom > 0) {
                mCurrentZoom--;
            } else {
                return false;
            }

            if (mCurrentCamera.getParameters().isSmoothZoomSupported()) {
                mCurrentCamera.startSmoothZoom(mCurrentZoom);
            } else {
                if (!incr) {
                    mCurrentZoom = Math.max(mCurrentZoom-2, 0);
                } else {
                    mCurrentZoom = (int)Math.min(mCurrentZoom + 2, mMaxZoom);
                }
                useParams.setZoom(mCurrentZoom);
                mCurrentCamera.setParameters(useParams);
            }

            mPreviousFactor = detector.getScaleFactor();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }
}
