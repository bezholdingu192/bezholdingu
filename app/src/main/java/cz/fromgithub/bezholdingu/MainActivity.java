package cz.fromgithub.bezholdingu;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;

import cz.fromgithub.bezholdingu.helpers.AdsHelper;
import cz.fromgithub.bezholdingu.helpers.DatafilesHelper;
import cz.fromgithub.bezholdingu.helpers.DecoderHelper;
import cz.fromgithub.bezholdingu.helpers.MainActivityHelper;
import cz.fromgithub.bezholdingu.helpers.Notifications;
import cz.fromgithub.bezholdingu.helpers.SettingsHelper;
import cz.fromgithub.bezholdingu.scanner.BarcodeGraphic;
import cz.fromgithub.bezholdingu.scanner.BarcodeGraphicTracker;
import cz.fromgithub.bezholdingu.scanner.BarcodeTrackerFactory;
import cz.fromgithub.bezholdingu.scanner.CameraSource;
import cz.fromgithub.bezholdingu.scanner.CameraSourcePreview;
import cz.fromgithub.bezholdingu.scanner.GraphicOverlay;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;

// Hlavni trida aplikace. Zde vsechno zacina...
public class MainActivity extends AppCompatActivity implements BarcodeGraphicTracker.BarcodeUpdateListener {

    private static final int SETTINGS_CODE = 1;
    private static final int DOWNLOAD_CODE = 2;
    private static final int TOSEND_CODE = 3;

    private ResultData codeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MainActivityHelper mainActivityHelper = new MainActivityHelper();

        // testovaci reklamy na Sony a Samsung telefon
        // AdsHelper.zapnoutTestovaciReklamy();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // povolit/zakazat otaceni displeje
        if (SettingsHelper.getSettingInt(this, SettingsHelper.Preference.DISPLAY_ORIENTATION) == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else if (SettingsHelper.getSettingInt(this, SettingsHelper.Preference.DISPLAY_ORIENTATION) == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // kontrola datovych souboru a pripadne vytvoreni z default
        DatafilesHelper.initDataFiles(this, true);

        // --- inicializace seznamu firem ---
        DecoderEan decoderEan = new DecoderEan();
        decoderEan.init(this, false);

        // --- inicializace kamery + osvetleni ---
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<BarcodeGraphic>) findViewById(R.id.graphicOverlay);

        // read parameters from the intent used to launch the activity.
        // boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(true, false);
        } else {
            mainActivityHelper.requestCameraPermission(this, RC_HANDLE_CAMERA_PERM);
        }

        // --- inicializace gest na displeji ---
        // gestureDetector = new GestureDetector(this, new MainActivity.CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new MainActivity.ScaleListener());

        // ------- rucni zadavani  -------
        Switch swManualEntry = (Switch)this.findViewById(R.id.swManualEntry);
        final ConstraintLayout layManual = (ConstraintLayout) findViewById(R.id.layManual);
        final ImageView imgStatus = (ImageView) findViewById(R.id.imgStatus);
        final TextView txtBarCode = (TextView)this.findViewById(R.id.txtBarCode);
        imgStatus.bringToFront();

        mainActivityHelper.startTabLayout(this);

        final MainActivity cont = this;
        swManualEntry.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ConstraintLayout layManual = (ConstraintLayout) findViewById(R.id.layManual);
                ImageView imgStatus = (ImageView) findViewById(R.id.imgStatus);
                TextView txtBarCode = (TextView)cont.findViewById(R.id.txtBarCode);
                try {
                    if (isChecked && mPreview != null) {
                        layManual.bringToFront();
                        mPreview.stop();
                        AdsHelper.stopReklamy(cont);
                    }
                    else if (!isChecked && mPreview != null) {
                        startCameraSource();
                        mPreview.bringToFront();
                        txtBarCode.bringToFront();
                        AdsHelper.startReklamy(cont, false);
                    }
                    imgStatus.bringToFront();
                } catch (Exception e) {
                    Toast.makeText(cont, "Chyba: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                MainActivityHelper.fadeImage((ImageView)findViewById(R.id.imgBarcode));
            }
        });

        // obnoveni dat ve formulari, napr.  po otoceni
        mainActivityHelper.restoreFormData(savedInstanceState, this);

        // inicializace reklam
        AdsHelper.startReklamy(this, true);

        // ------- inicializace blesku -------
        Switch swUseFlash = (Switch)this.findViewById(R.id.swUseFlash);
        swUseFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {
                if (mCameraSource.getFlashMode() == null)
                {
                    Toast.makeText(getApplicationContext(), "Osvětlení nelze zapnout. Pravděpodobně není v zařízení nainstalované.", Toast.LENGTH_LONG).show();
                    buttonView.setChecked(false);
                }
                else {
                    startCameraSource();
                }
            } catch (Exception e) {
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "Chyba: osvětlení nelze zapnout. Pravděpodobně není v zařízení nainstalované.", Toast.LENGTH_LONG).show();
                    buttonView.setChecked(false);
                }
            }
            MainActivityHelper.fadeImage((ImageView)findViewById(R.id.imgBarcode));
            }
        });

        Notifications.checkForDataActualization(this);

        /*
        // zobrazit zpravu pri prvnim spusteni
        if (SettingsHelper.getSettingBoolean(this, SettingsHelper.Preference.FIRST_RUN)) {
            // vytvorime instanci tridy AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bez Holdingu");
            builder.setMessage("Děkujeme za instalaci aplikace Bez Holdingu.\nAplikace ke své činnosti nepotřebuje stálé připojení k internetu, ale čas od času se hodí nahrát do ní nejnovější data.\nUdělejte si prosím ještě chvilku na jejich aktualizaci, aby aplikace měla opravdu ta nejnovější. Aktualizaci najdete v menu, v pravém horním rohu, pod položkou Aktualizace dat.");
            builder.setPositiveButton("Zavřít", null);
            builder.create().show();
        }
        */
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_nastaveni) {
            Intent itnt = new Intent(this, SettingsActivity.class);
            this.startActivityForResult(itnt, SETTINGS_CODE);
            return true;
        }
        else if (item.getItemId() == R.id.menu_aktualizace) {
            Intent itnt = new Intent(this, DownloadDataActivity.class);
            this.startActivityForResult(itnt, DOWNLOAD_CODE);
            return true;
        }
        else if (item.getItemId() == R.id.menu_kodeslani) {
            Intent itnt = new Intent(this, ToSendActivity.class);
            this.startActivityForResult(itnt, DOWNLOAD_CODE);
            return true;
        }
        else if (item.getItemId() == R.id.menu_oaplikaci) {
            about();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_CODE) {
            AdsHelper.startReklamy(this);
        }
    }*/

    // zachrana hodnot pri otoceni displeje
    @Override
    protected void onSaveInstanceState (@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        TextView txtBarCode = (TextView)this.findViewById(R.id.txtBarCode);
        outState.putString("txtBarCode", txtBarCode.getText().toString());
        TextView txtProducerLabel = (TextView)this.findViewById(R.id.txtProducerLabel);
        outState.putString("txtProducerLabel", txtProducerLabel.getText().toString());
        TextView txtProducer = (TextView)this.findViewById(R.id.txtProducer);
        outState.putString("txtProducer", txtProducer.getText().toString());
        TextView txtCountry = (TextView)this.findViewById(R.id.txtNote);
        outState.putString("txtCountry", txtCountry.getText().toString());
        TextView txtSendInfo = (TextView)this.findViewById(R.id.txtSendInfo);
        outState.putString("txtSendInfo", txtSendInfo.getText().toString());

        ImageView img = (ImageView) this.findViewById(R.id.imgStatus);
        Object imgTag = img.getTag();
        if (imgTag!=null)
            outState.putInt("imgStatus", (int)img.getTag());

        img = (ImageView) this.findViewById(R.id.imgBarcode);
        outState.putBoolean("imgBarcode", img.getAnimation() != null || img.getVisibility() == View.GONE);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        outState.putInt("selectedTab", tabLayout.getSelectedTabPosition());
    }


    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // constants used to pass extra data in the intent
    public static final String UseFlash = "UseFlash";
    public static final String BarcodeObject = "Barcode";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    // helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    // private GestureDetector gestureDetector;



    @Override
    public boolean onTouchEvent(MotionEvent e) {
       boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = false; // gestureDetector.onTouchEvent(e);  // toto zpusobovalo pad aplikace pri skenovani kodu a zaroven sahnuti na displej

        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    @SuppressWarnings("SameParameterValue")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, this);
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w("MainActivity", "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w("MainActivity", getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f);

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
        }

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // povolit/zakazat otaceni displeje, pokud se zmenilo nastaveni
        int displayOrientation = SettingsHelper.getSettingInt(this, SettingsHelper.Preference.DISPLAY_ORIENTATION);
        int displayCurrentOrientation = getRequestedOrientation();
        if (displayCurrentOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED && displayOrientation == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        else if (displayCurrentOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && displayOrientation == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else if (displayCurrentOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && displayOrientation == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // spustit kameru
        startCameraSource();

        // odchytavani zmeny typu pripojeni k netu (kvuli reklamam)
        AdsHelper.startReklamy(this, false);

        // TextView logView = (TextView)findViewById(R.id.txtLog);
        // logView.setText(AdsHelper.logText);

        // aby logovani na displej fungovalo, pridat do formulare TextView (zobrazeni logu
        // je pak po otevreni a zavreni Nastaveni, enbojineho formulare):
        /*
    <TextView
        android:id="@+id/txtLog"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginTop="32dp"
        android:alpha="0.9"
        android:background="#333333"
        android:singleLine="false"
        android:text="TextView"
        android:visibility="invisible"
        app:layout_constraintBottom_toTopOf="@+id/adView"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/layManual" />
                */
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }

        AdsHelper.stopReklamy(this);
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d("MainActivity", "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(true, useFlash);
            return;
        }

        Log.e("MainActivity", "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        // zjistit, jestli lze kameru spustit
        Switch swManualEntry = (Switch)this.findViewById(R.id.swManualEntry);
        if(swManualEntry.isChecked()) {
            return;
        }
        // neni potreba vypnout blesk?
        Switch swUseFlash = (Switch)this.findViewById(R.id.swUseFlash);
        if (!swUseFlash.isChecked() && mCameraSource !=null && mCameraSource.getFlashMode() != null) {
            mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }


        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);

                // neni potreba zaponout blesk?
                if (swUseFlash.isChecked() && mCameraSource.getFlashMode()!=null) {
                    mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            } catch (IOException e) {
                Log.e("MainActivity", "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * onTap returns the tapped barcode result to the calling Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        // Find tap point in preview frame coordinates.
        int[] location = new int[2];
        mGraphicOverlay.getLocationOnScreen(location);
        float x = (rawX - location[0]) / mGraphicOverlay.getWidthScaleFactor();
        float y = (rawY - location[1]) / mGraphicOverlay.getHeightScaleFactor();

        // Find the barcode whose center is closest to the tapped point.
        Barcode best = null;
        float bestDistance = Float.MAX_VALUE;
        for (BarcodeGraphic graphic : mGraphicOverlay.getGraphics()) {
            Barcode barcode = graphic.getBarcode();
            if (barcode.getBoundingBox().contains((int) x, (int) y)) {
                // Exact hit, no need to keep looking.
                best = barcode;
                break;
            }
            float dx = x - barcode.getBoundingBox().centerX();
            float dy = y - barcode.getBoundingBox().centerY();
            float distance = (dx * dx) + (dy * dy);  // actually squared distance
            if (distance < bestDistance) {
                best = barcode;
                bestDistance = distance;
            }
        }

        if (best != null) {
            Intent data = new Intent();
            data.putExtra(BarcodeObject, best);
            setResult(CommonStatusCodes.SUCCESS, data);
            finish();
            return true;
        }
        return false;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

    public void about () {
        // vytvorime instanci tridy AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // pripravime si formular
        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.activity_main_about, null);

        TextView txtNewName = alertLayout.findViewById(R.id.txtVersion);
        Resources res = getBaseContext().getResources();
        try {
            txtNewName.setText(String.format("Verze: %s",  getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        }
        catch (Exception e){
            String err = e.getMessage();
        }

        builder.setTitle(res.getString(R.string.main_about_title));
        builder.setPositiveButton("OK", null);
        builder.setView(alertLayout);
        builder.create().show();
    }

    // spusti na telefonu GitHub
    public void goToGitHub(View view) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bezholdingu192/bezholdingu"));
            startActivity(intent);
        } catch (Exception e) {
        }
    }

    // spusti na telefonu FaceBook
    public void goToFaceBook(View view) {
        try {
            // FB aplikace
            getPackageManager().getPackageInfo("com.facebook.katana", 0);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://groups/3382356618517340"));
            startActivity(intent);
        } catch (Exception e) {
            // FB browser
            Intent intent =  new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/groups/3382356618517340"));
            startActivity(intent);
        }
    }

        // udalost spustena tlacitkem po rucnim zadani caroveho kodu
    public void onBarcodeTyped(View view) {

        // schovat virtualni klavesnici
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        
        // najit kod
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        EditText txtManualCode = (EditText)findViewById(R.id.txtManualCode);

        if (tabLayout.getSelectedTabPosition()==0) {
            if (txtManualCode.getText().toString().trim().length()<2) {
                Toast.makeText(getApplicationContext(), "Zadaný kód je příliš krátký.", Toast.LENGTH_LONG).show();
                return;
            }
            Barcode barcode = new Barcode();
            if (txtManualCode.getText().length() > 8) barcode.format = Barcode.EAN_13;
            else barcode.format = Barcode.EAN_8;
            barcode.displayValue = txtManualCode.getText().toString();
            barcode.rawValue = txtManualCode.getText().toString();
            onBarcodeDetected(barcode);
        }
        else if (tabLayout.getSelectedTabPosition()==1){
            if (txtManualCode.getText().toString().trim().length()<1) {
                Toast.makeText(getApplicationContext(), "Zadaná značka výrobce je příliš krátká.", Toast.LENGTH_LONG).show();
                return;
            }

            // vypsat kod na displej
            TextView txtBarCode = (TextView)this.findViewById(R.id.txtBarCode);
            setText(txtBarCode, String.format("Kód: CZ %s ES", txtManualCode.getText().toString()));

            TextView txtProducerLabel = (TextView) this.findViewById(R.id.txtProducerLabel);
            setText(txtProducerLabel, "Výrobce");

            DecoderProducercode decoder = new DecoderProducercode();
            codeData = decoder.getResultData(this, txtManualCode.getText().toString());
            codeData.kod=String.format("CZ %s ES", codeData.kod);   // doplnit pred a priponu kodu
            showResult(codeData);
        }
        else {
            if (txtManualCode.getText().toString().trim().length()<2) {
                Toast.makeText(getApplicationContext(), "Zadaný název výrobce nebo značky je příliš krátký.", Toast.LENGTH_LONG).show();
                return;
            }

            // vypsat kod na displej
            TextView txtBarCode = (TextView)this.findViewById(R.id.txtBarCode);
            setText(txtBarCode, String.format("Kód: %s", txtManualCode.getText().toString()));

            TextView txtProducerLabel = (TextView) this.findViewById(R.id.txtProducerLabel);
            setText(txtProducerLabel, "Výrobce/značka");

            DecoderBrandname decoder = new DecoderBrandname();
            codeData = decoder.getResultData(this, txtManualCode.getText().toString());
            showResult(codeData);
        }
    }

    // kliknuti na text "odeslat upozorneni"
    public void onSendInfo(View view) {
        // muze byt spustene soucasne jenom jednou (ochrana proti vicenasobnemu kliknuti)
        if (FeedbackWrongActivity.singleton>0)
            return;
        FeedbackWrongActivity.singleton = 1;

        if (((TextView)view).getText().length()<1)
            return;

        TextView txtProducer = (TextView) this.findViewById(R.id.txtProducer);
        TextView txtNote = (TextView) this.findViewById(R.id.txtNote);
        Intent intent = new Intent(this, FeedbackWrongActivity.class);
        intent.putExtra("kod", codeData == null ? "" : codeData.kod);
        intent.putExtra("vysledek", txtProducer.getText() + " >>> " + txtNote.getText());
        startActivity(intent);
    }

    // udalost volana pri rozpoznani barcode
    @Override
    public void onBarcodeDetected(Barcode barcode) {

        // zneviditelni obrazek ilustracniho caroveho kodu, pokud je viditelny
        MainActivityHelper.fadeImage((ImageView)findViewById(R.id.imgBarcode));

        // cteme pouze kody EAN-8 a EAN-13
        if(barcode.format != Barcode.EAN_8 && barcode.format != Barcode.EAN_13)
            return;

        // vypsat kod na displej
        TextView txtBarCode = (TextView)this.findViewById(R.id.txtBarCode);
        setText(txtBarCode, String.format("Kód: %s", barcode.displayValue));

        TextView txtProducerLabel = (TextView) this.findViewById(R.id.txtProducerLabel);
        setText(txtProducerLabel, "Výrobce");

        DecoderEan decoder = new DecoderEan();
        codeData = decoder.getResultData(this, barcode.displayValue);

        // upozornit pipnutim nebo zavibrovanim (pouze pokud neni kod zadan rucne)
        if(barcode.isRecognized) {
            MainActivityHelper.doHapticNotification(this,codeData.vysledek != DecoderHelper.Kategorie.HOLDING && codeData.vysledek != DecoderHelper.Kategorie.TOMAN && codeData.vysledek != DecoderHelper.Kategorie.RUSKO && codeData.vysledek != DecoderHelper.Kategorie.BELORUSKO);
        }

        showResult(codeData);
    }

    private void showResult(cz.fromgithub.bezholdingu.ResultData data) {
        TextView txtProducer = (TextView) this.findViewById(R.id.txtProducer);
        setText(txtProducer, data.nazev);

        TextView txtNote = (TextView) this.findViewById(R.id.txtNote);
        setText(txtNote, data.dodatek);

        TextView txtSendInfo = (TextView) this.findViewById(R.id.txtSendInfo);
        if(data.vysledek.equals(DecoderHelper.Kategorie.NENALEZENO) || data.vysledek.equals(DecoderHelper.Kategorie.MIMOHOLDING_NENALEZENO))
            setText(txtSendInfo, "");
        else
            setText(txtSendInfo, "Upozornit na chybu");

        // zobrazi ikonu holding/neholding/nejiste
        ImageView img = (ImageView) findViewById(R.id.imgStatus);
        int ico = R.drawable.nejiste;
        if (data.vysledek == DecoderHelper.Kategorie.HOLDING || data.vysledek == DecoderHelper.Kategorie.TOMAN || data.vysledek == DecoderHelper.Kategorie.RUSKO || data.vysledek == DecoderHelper.Kategorie.BELORUSKO)
            ico = R.drawable.holding;
        else if (data.vysledek == DecoderHelper.Kategorie.MIMOHOLDING || data.vysledek == DecoderHelper.Kategorie.MIMOHOLDING_NENALEZENO)
            ico = R.drawable.neholding;

        setImg(img, ico);
    }

    // zapis do UI ve spravnem vlakne (na starsich telefonech primy zapis zlobil)
    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }
    private void setImg(final ImageView img,final int value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                img.setImageResource(value);
                img.setTag(value);
                img.bringToFront();
            }
        });
    }

    // kliknuti na zavreni reklamy
    public void onAdClose(View view) {
        AdsHelper.tmpBezReklam(this);
    }

    public void swManualEntryOnClicek(View view) {
        Switch sw = (Switch)view;
        ConstraintLayout layManual = (ConstraintLayout) findViewById(R.id.layManual);
        ImageView imgStatus = (ImageView) findViewById(R.id.imgStatus);
        TextView txtBarCode = (TextView)this.findViewById(R.id.txtBarCode);
        try {
            if (sw.isChecked() && mPreview != null) {
                layManual.bringToFront();
                mPreview.stop();
                AdsHelper.stopReklamy(this);
            }
            else if (!sw.isChecked() && mPreview != null) {
                startCameraSource();
                mPreview.bringToFront();
                txtBarCode.bringToFront();
                AdsHelper.startReklamy(this, false);
            }
            imgStatus.bringToFront();
        } catch (Exception e) {
            Toast.makeText(this, "Chyba: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        MainActivityHelper.fadeImage((ImageView)findViewById(R.id.imgBarcode));
    }

}
