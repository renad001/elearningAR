/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/


package com.example.elearningar.Vuforia.SampleApplication;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import com.example.elearningar.R;
import com.vuforia.CameraDevice;
import com.vuforia.Device;
import com.vuforia.INIT_ERRORCODE;
import com.vuforia.INIT_FLAGS;
import com.vuforia.State;
import com.vuforia.Vuforia;
import com.vuforia.Vuforia.UpdateCallbackInterface;


public class SampleApplicationSession implements UpdateCallbackInterface
{
    
    private static final String LOGTAG = "SampleAppSession";
    
    // Reference to the current activity
    private Activity mActivity;
    private SampleApplicationControl mSessionControl;
    
    // Flags
    private boolean mStarted = false;
    private boolean mCameraRunning = false;
    
    // The async tasks to initialize the Vuforia SDK:
    private InitVuforiaTask mInitVuforiaTask;
    private LoadTrackerTask mLoadTrackerTask;
    
    // An object used for synchronizing Vuforia initialization, dataset loading
    // and the Android onDestroy() life cycle event. If the application is
    // destroyed while a data set is still being loaded, then we wait for the
    // loading operation to finish before shutting down Vuforia:
    private Object mShutdownLock = new Object();
    
    // Vuforia initialization flags:
    private int mVuforiaFlags = 0;
    
    // Holds the camera configuration to use upon resuming
    private int mCamera = CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT;
    

    public SampleApplicationSession(SampleApplicationControl sessionControl)
    {
        mSessionControl = sessionControl;
    }
    
    
    // Initializes Vuforia and sets up preferences.
    public void initAR(Activity activity, int screenOrientation)
    {
        SampleApplicationException vuforiaException = null;
        mActivity = activity;
        
        if ((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            && (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
        
        // Use an OrientationChangeListener here to capture all orientation changes.  Android
        // will not send an Activity.onConfigurationChanged() callback on a 180 degree rotation,
        // ie: Left Landscape to Right Landscape.  Vuforia needs to react to this change and the
        // SampleApplicationSession needs to update the Projection Matrix.
        OrientationEventListener orientationEventListener = new OrientationEventListener(mActivity) {
            @Override
            public void onOrientationChanged(int i) {
                int activityRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
                if(mLastRotation != activityRotation)
                {
                    mLastRotation = activityRotation;
                }
            }

            int mLastRotation = -1;
        };
        
        if(orientationEventListener.canDetectOrientation())
            orientationEventListener.enable();

        // Apply screen orientation
        mActivity.setRequestedOrientation(screenOrientation);
        
        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright:
        mActivity.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mVuforiaFlags = INIT_FLAGS.GL_20;
        
        // Initialize Vuforia SDK asynchronously to avoid blocking the
        // main (UI) thread.
        //
        // NOTE: This task instance must be created and invoked on the
        // UI thread and it can be executed only once!
        if (mInitVuforiaTask != null)
        {
            String logMessage = "Cannot initialize SDK twice";
            vuforiaException = new SampleApplicationException(
                SampleApplicationException.VUFORIA_ALREADY_INITIALIZATED,
                logMessage);
            Log.e(LOGTAG, logMessage);
        }
        
        if (vuforiaException == null)
        {
            try
            {
                mInitVuforiaTask = new InitVuforiaTask();
                mInitVuforiaTask.execute();
            } catch (Exception e)
            {
                String logMessage = "Initializing Vuforia SDK failed";
                vuforiaException = new SampleApplicationException(
                    SampleApplicationException.INITIALIZATION_FAILURE,
                    logMessage);
                Log.e(LOGTAG, logMessage);
            }
        }
        
        if (vuforiaException != null)
            mSessionControl.onInitARDone(vuforiaException);
    }
    
    
    // Starts Vuforia, initialize and starts the camera and start the trackers
    public void startAR(int camera) throws SampleApplicationException
    {
        String error;
        if(mCameraRunning)
        {
        	error = "Camera already running, unable to open again";
        	Log.e(LOGTAG, error);
            throw new SampleApplicationException(
                SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }
        
        mCamera = camera;
        if (!CameraDevice.getInstance().init(camera))
        {
            error = "Unable to open camera device: " + camera;
            Log.e(LOGTAG, error);
            throw new SampleApplicationException(
                SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }
               
        if (!CameraDevice.getInstance().selectVideoMode(
            CameraDevice.MODE.MODE_DEFAULT))
        {
            error = "Unable to set video mode";
            Log.e(LOGTAG, error);
            throw new SampleApplicationException(
                SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }
        
        if (!CameraDevice.getInstance().start())
        {
            error = "Unable to start camera device: " + camera;
            Log.e(LOGTAG, error);
            throw new SampleApplicationException(
                SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error);
        }
        
        mSessionControl.doStartTrackers();
        
        mCameraRunning = true;
        
        if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO))
        {
            if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
        }
    }
    
    
    // Stops any ongoing initialization, stops Vuforia
    public void stopAR() throws SampleApplicationException
    {
        // Cancel potentially running tasks
        if (mInitVuforiaTask != null
            && mInitVuforiaTask.getStatus() != InitVuforiaTask.Status.FINISHED)
        {
            mInitVuforiaTask.cancel(true);
            mInitVuforiaTask = null;
        }
        
        if (mLoadTrackerTask != null
            && mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
        {
            mLoadTrackerTask.cancel(true);
            mLoadTrackerTask = null;
        }
        
        mInitVuforiaTask = null;
        mLoadTrackerTask = null;
        
        mStarted = false;
        
        stopCamera();
        
        // Ensure that all asynchronous operations to initialize Vuforia
        // and loading the tracker datasets do not overlap:
        synchronized (mShutdownLock)
        {
            
            boolean unloadTrackersResult;
            boolean deinitTrackersResult;
            
            // Destroy the tracking data set:
            unloadTrackersResult = mSessionControl.doUnloadTrackersData();
            
            // Deinitialize the trackers:
            deinitTrackersResult = mSessionControl.doDeinitTrackers();
            
            // Deinitialize Vuforia SDK:
            Vuforia.deinit();
            
            if (!unloadTrackersResult)
                throw new SampleApplicationException(
                    SampleApplicationException.UNLOADING_TRACKERS_FAILURE,
                    "Failed to unload trackers\' data");
            
            if (!deinitTrackersResult)
                throw new SampleApplicationException(
                    SampleApplicationException.TRACKERS_DEINITIALIZATION_FAILURE,
                    "Failed to deinitialize trackers");
            
        }
    }
    
    
    // Resumes Vuforia, restarts the trackers and the camera
    public void resumeAR() throws SampleApplicationException
    {
        // Vuforia-specific resume operation
        Vuforia.onResume();
        
        if (mStarted)
        {
            startAR(mCamera);
        }
    }
    
    
    // Pauses Vuforia and stops the camera
    public void pauseAR() throws SampleApplicationException
    {
        if (mStarted)
        {
            stopCamera();
        }
        
        Vuforia.onPause();
    }
    
    
    // Callback called every cycle
    @Override
    public void Vuforia_onUpdate(State s)
    {
        mSessionControl.onVuforiaUpdate(s);
    }
    
    
    // Manages the configuration changes
    public void onConfigurationChanged()
    {
        Device.getInstance().setConfigurationChanged();
    }
    
    
    // Methods to be called to handle lifecycle
    public void onResume()
    {
        Vuforia.onResume();
    }
    
    
    public void onPause()
    {
        Vuforia.onPause();
    }
    
    
    public void onSurfaceChanged(int width, int height)
    {
        Vuforia.onSurfaceChanged(width, height);
    }
    
    
    public void onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated();
    }
    
    // An async task to initialize Vuforia asynchronously.
    private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean>
    {
        // Initialize with invalid value:
        private int mProgressValue = -1;
        
        
        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap with initialization:
            synchronized (mShutdownLock)
            {
                Vuforia.setInitParameters(mActivity, mVuforiaFlags, "AYHhAbP/////AAABmfNihlUKlUQDsBfLTljf9V0/c7IIwFnC15fS+8unzYX9T9ju3Uz7TfQ7nsZgW/nTzzf+wdGs/H6Q/xKFn2rVnrQ+ZjJMgDyLE0dhX5qldxecHA+Q7D7jJO4EyKrw9w7sP+vmEe7wDML1p1QoXfarztPdjKTRj5ZoF+hD3bMfIYTZ/2mdQHKaAXpaw3JkLVNT7mYjzbXO0ZJGbGwOYhxc4M017/4xayxtMptp7Y4MBEkcoT/Kyt2Q7Bfq16qSp7WCvNmXCs16+R7pcUcLY/7pOKe0dETtFkZv71qCFxZOr3iTcal8x/peyEJpxOZxuEaFLinUsPA6fepDC70JlHitdufhy186FYRqRh15YyoQk9wC");
                
                do
                {
                    // Vuforia.init() blocks until an initialization step is
                    // complete, then it proceeds to the next step and reports
                    // progress in percents (0 ... 100%).
                    // If Vuforia.init() returns -1, it indicates an error.
                    // Initialization is done when progress has reached 100%.
                    mProgressValue = Vuforia.init();
                    
                    // Publish the progress value:
                    publishProgress(mProgressValue);
                    
                    // We check whether the task has been canceled in the
                    // meantime (by calling AsyncTask.cancel(true)).
                    // and bail out if it has, thus stopping this thread.
                    // This is necessary as the AsyncTask will run to completion
                    // regardless of the status of the component that
                    // started is.
                } while (!isCancelled() && mProgressValue >= 0
                    && mProgressValue < 100);

                return (mProgressValue > 0);
            }
        }
        
        
        protected void onProgressUpdate(Integer... values)
        {
            // Do something with the progress value "values[0]", e.g. update
            // splash screen, progress bar, etc.
        }
        
        
        protected void onPostExecute(Boolean result)
        {
            // Done initializing Vuforia, proceed to next application
            // initialization status:
            
            SampleApplicationException vuforiaException = null;
            
            if (result)
            {
                Log.d(LOGTAG, "InitVuforiaTask.onPostExecute: Vuforia "
                    + "initialization successful");
                
                boolean initTrackersResult;
                initTrackersResult = mSessionControl.doInitTrackers();
                
                if (initTrackersResult)
                {
                    try
                    {
                        mLoadTrackerTask = new LoadTrackerTask();
                        mLoadTrackerTask.execute();
                    } catch (Exception e)
                    {
                        String logMessage = "Loading tracking data set failed";
                        vuforiaException = new SampleApplicationException(
                            SampleApplicationException.LOADING_TRACKERS_FAILURE,
                            logMessage);
                        Log.e(LOGTAG, logMessage);
                        mSessionControl.onInitARDone(vuforiaException);
                    }
                    
                } else
                {
                    vuforiaException = new SampleApplicationException(
                        SampleApplicationException.TRACKERS_INITIALIZATION_FAILURE,
                        "Failed to initialize trackers");
                    mSessionControl.onInitARDone(vuforiaException);
                }
            } else
            {
                String logMessage;
                
                // NOTE: Check if initialization failed because the device is
                // not supported. At this point the user should be informed
                // with a message.
                logMessage = getInitializationErrorString(mProgressValue);
                
                // Log error:
                Log.e(LOGTAG, "InitVuforiaTask.onPostExecute: " + logMessage
                    + " Exiting.");
                
                // Send Vuforia Exception to the application and call initDone
                // to stop initialization process
                vuforiaException = new SampleApplicationException(
                    SampleApplicationException.INITIALIZATION_FAILURE,
                    logMessage);
                mSessionControl.onInitARDone(vuforiaException);
            }
        }
    }
    
    // An async task to load the tracker data asynchronously.
    private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>
    {
        protected Boolean doInBackground(Void... params)
        {
            // Prevent the onDestroy() method to overlap:
            synchronized (mShutdownLock)
            {
                // Load the tracker data set:
                return mSessionControl.doLoadTrackersData();
            }
        }
        
        
        protected void onPostExecute(Boolean result)
        {
            
            SampleApplicationException vuforiaException = null;
            
            Log.d(LOGTAG, "LoadTrackerTask.onPostExecute: execution "
                + (result ? "successful" : "failed"));
            
            if (!result)
            {
                String logMessage = "Failed to load tracker data.";
                // Error loading dataset
                Log.e(LOGTAG, logMessage);
                vuforiaException = new SampleApplicationException(
                    SampleApplicationException.LOADING_TRACKERS_FAILURE,
                    logMessage);
            } else
            {
                // Hint to the virtual machine that it would be a good time to
                // run the garbage collector:
                //
                // NOTE: This is only a hint. There is no guarantee that the
                // garbage collector will actually be run.
                System.gc();
                
                Vuforia.registerCallback(SampleApplicationSession.this);
                
                mStarted = true;
            }
            
            // Done loading the tracker, update application status, send the
            // exception to check errors
            mSessionControl.onInitARDone(vuforiaException);
        }
    }
    
    
    // Returns the error message for each error code
    private String getInitializationErrorString(int code)
    {
        if (code == INIT_ERRORCODE.INIT_DEVICE_NOT_SUPPORTED)
            return mActivity.getString(R.string.INIT_ERROR_DEVICE_NOT_SUPPORTED);
        if (code == INIT_ERRORCODE.INIT_NO_CAMERA_ACCESS)
            return mActivity.getString(R.string.INIT_ERROR_NO_CAMERA_ACCESS);
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_MISSING_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_MISSING_KEY);
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_INVALID_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_INVALID_KEY);
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT);
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT);
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_CANCELED_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_CANCELED_KEY);
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH);
        else
        {
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_UNKNOWN_ERROR);
        }
    }
    
    
    public void stopCamera()
    {
        if (mCameraRunning)
        {
            mSessionControl.doStopTrackers();
            mCameraRunning = false;
            CameraDevice.getInstance().stop();
            CameraDevice.getInstance().deinit();
        }
    }
    
    
    // Returns true if Vuforia is initialized, the trackers started and the
    // tracker data loaded
    private boolean isARRunning()
    {
        return mStarted;
    }
    
}
