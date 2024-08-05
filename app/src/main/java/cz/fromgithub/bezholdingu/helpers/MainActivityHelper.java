package cz.fromgithub.bezholdingu.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.google.android.material.tabs.TabLayout;

import cz.fromgithub.bezholdingu.MainActivity;
import cz.fromgithub.bezholdingu.R;

public class MainActivityHelper {

    private static String lastManualBarcode="";
    private static String lastManualProductCode="";
    private static String lastManualBrandName="";

    // v okne pro rucni zadavani kodu nastavi TabLayout
    public void startTabLayout(final MainActivity cont) {
        // nastaveni ikonek v okne rucniho zadavani
        TabLayout tabLayout = (TabLayout) cont.findViewById(R.id.tabLayout);
        ImageView imageView = new ImageView(cont);
        imageView.setImageResource(R.drawable.ico_barcode);
        tabLayout.getTabAt(0).setCustomView(imageView);
        imageView = new ImageView(cont);
        imageView.setImageResource(R.drawable.ico_productcode);
        imageView.setImageAlpha(100);
        tabLayout.getTabAt(1).setCustomView(imageView);
        imageView = new ImageView(cont);
        imageView.setImageResource(R.drawable.ico_name);
        imageView.setImageAlpha(100);
        tabLayout.getTabAt(2).setCustomView(imageView);

        EditText entry = (EditText)cont.findViewById(R.id.txtManualCode);
        entry.bringToFront();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                tabLayoutSwitchTab(cont, tab.parent, tab.getPosition());

                EditText entry = (EditText)cont.findViewById(R.id.txtManualCode);
                if (tab.getPosition() == 0)
                    entry.setText(lastManualBarcode);
                else if (tab.getPosition() == 1)
                    entry.setText(lastManualProductCode);
                else
                    entry.setText(lastManualBrandName);

                entry.setSelection(entry.getText().length());

                /*
                TextView info = (TextView)cont.findViewById(R.id.txtManualEntryInfo);
                TextView entryCz = (TextView)cont.findViewById(R.id.txtManualCodeCz);
                EditText entry = (EditText)cont.findViewById(R.id.txtManualCode);
                TextView entryEs = (TextView)cont.findViewById(R.id.txtManualCodeEs);
                if (tab.getPosition() == 0) {
                    info.setText("Zadejte 7-8 číslic pro krátký, nebo 12-13 číslic pro dlouhý kód:");
                    entry.setPadding( entryCz.getPaddingLeft(), entry.getPaddingTop(), entryEs.getPaddingRight(), entry.getPaddingBottom());
                    entry.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
                    entryCz.setVisibility(View.INVISIBLE);
                    entryEs.setVisibility(View.INVISIBLE);
                    entry.bringToFront();
                    lastManualProductCode = entry.getText().toString();
                    entry.setText(lastManualBarcode);
                    ((ImageView)tab.getCustomView()).setImageAlpha(255);
                    ((ImageView)tab.parent.getTabAt(1).getCustomView()).setImageAlpha(100);
                }
                else {
                    info.setText("Zadejte kód z oválu značky výrobce. Dohledat lze pouze české kódy (CZ).");
                    entry.setPadding( entry.getPaddingLeft() + entryCz.getWidth() + 2, entry.getPaddingTop(), entry.getPaddingRight() + entryEs.getWidth()+2, entry.getPaddingBottom());
                    entry.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                    entryCz.setVisibility(View.VISIBLE);
                    entryEs.setVisibility(View.VISIBLE);
                    lastManualBarcode = entry.getText().toString();
                    entry.setText(lastManualProductCode);
                    ((ImageView)tab.parent.getTabAt(0).getCustomView()).setImageAlpha(100);
                    ((ImageView)tab.getCustomView()).setImageAlpha(255);
                }
                entry.setSelection(entry.getText().length());
               */
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // zapamatovat hodnotu napsanou do textboxu
                EditText entry = (EditText)cont.findViewById(R.id.txtManualCode);
                if (tab.getPosition() == 0)
                    lastManualBarcode = entry.getText().toString();
                else if (tab.getPosition() == 1)
                    lastManualProductCode = entry.getText().toString();
                else
                    lastManualBrandName= entry.getText().toString();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        // tabLayout.selectTab(tabLayout.getTabAt(0));
    }

    // nastavvi zvolenou zalozku na TabLayoutu
    private void tabLayoutSwitchTab (MainActivity cont, TabLayout tabLayout, int activeTab) {
        TextView info = (TextView)cont.findViewById(R.id.txtManualEntryInfo);
        TextView entryCz = (TextView)cont.findViewById(R.id.txtManualCodeCz);
        EditText entry = (EditText)cont.findViewById(R.id.txtManualCode);
        TextView entryEs = (TextView)cont.findViewById(R.id.txtManualCodeEs);
        if (activeTab == 0) {
            info.setText("Zadejte 7-8 číslic pro krátký, nebo 12-13 číslic pro dlouhý kód:");
            entry.setPadding( entryCz.getPaddingLeft(), entry.getPaddingTop(), entryEs.getPaddingRight(), entry.getPaddingBottom());
            entry.setInputType(InputType.TYPE_CLASS_NUMBER);
            entry.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
            entryCz.setVisibility(View.INVISIBLE);
            entryEs.setVisibility(View.INVISIBLE);
            entry.bringToFront();
            ((ImageView)tabLayout.getTabAt(0).getCustomView()).setImageAlpha(255);
            ((ImageView)tabLayout.getTabAt(1).getCustomView()).setImageAlpha(100);
            ((ImageView)tabLayout.getTabAt(2).getCustomView()).setImageAlpha(100);
        }
        else if (activeTab == 1) {
            info.setText("Zadejte kód z oválu značky výrobce. Dohledat lze pouze české kódy (CZ):");
            int entryCzWidth = entryCz.getWidth() > 0 ? entryCz.getWidth() : 52;
            entry.setPadding( entry.getPaddingLeft() + entryCzWidth + 5, entry.getPaddingTop(), entry.getPaddingRight() + entryEs.getWidth()+2, entry.getPaddingBottom());
            entry.setInputType(InputType.TYPE_CLASS_NUMBER);
            entry.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            entryCz.setVisibility(View.VISIBLE);
            entryEs.setVisibility(View.VISIBLE);
            ((ImageView)tabLayout.getTabAt(0).getCustomView()).setImageAlpha(100);
            ((ImageView)tabLayout.getTabAt(1).getCustomView()).setImageAlpha(255);
            ((ImageView)tabLayout.getTabAt(2).getCustomView()).setImageAlpha(100);
        }
        else {
            info.setText("Zadejte jméno výrobce nebo prodejní značky. Stačí několik prvních písmen, diakritiku není nutné používat:");
            entry.setPadding( entryCz.getPaddingLeft(), entry.getPaddingTop(), entryEs.getPaddingRight(), entry.getPaddingBottom());
            entry.setInputType(InputType.TYPE_CLASS_TEXT);
            entry.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
            entryCz.setVisibility(View.INVISIBLE);
            entryEs.setVisibility(View.INVISIBLE);
            ((ImageView)tabLayout.getTabAt(0).getCustomView()).setImageAlpha(100);
            ((ImageView)tabLayout.getTabAt(1).getCustomView()).setImageAlpha(100);
            ((ImageView)tabLayout.getTabAt(2).getCustomView()).setImageAlpha(255);
        }
    }

    // obnova dat ve formulari (treba pri otoceni displeje); ukladani dat je v udalosti onSaveInstanceState v MainActivity.java
    public void restoreFormData(Bundle savedInstanceState, MainActivity cont){
        if(savedInstanceState!=null)
        {
            TextView txtBarCode = (TextView)cont.findViewById(R.id.txtBarCode);
            txtBarCode.setText(savedInstanceState.getString("txtBarCode"));
            TextView txtProducerLabel = (TextView)cont.findViewById(R.id.txtProducerLabel);
            txtProducerLabel.setText(savedInstanceState.getString("txtProducerLabel"));
            TextView txtProducer = (TextView)cont.findViewById(R.id.txtProducer);
            txtProducer.setText(savedInstanceState.getString("txtProducer"));
            TextView txtCountry = (TextView)cont.findViewById(R.id.txtNote);
            txtCountry.setText(savedInstanceState.getString("txtCountry"));
            TextView txtSendInfo = (TextView)cont.findViewById(R.id.txtSendInfo);
            txtSendInfo.setText(savedInstanceState.getString("txtSendInfo"));

            ImageView img = (ImageView) cont.findViewById(R.id.imgStatus);
            int imgTag=savedInstanceState.getInt("imgStatus", 0);
            if(imgTag != 0) {
                img.setImageResource(imgTag);
                img.setTag(imgTag);
            }

            img = (ImageView) cont.findViewById(R.id.imgBarcode);
            if (savedInstanceState.getBoolean("imgBarcode"))
                img.setVisibility(View.GONE);

            // toto mame v promenne, neni nutne ukladat/nacitat pres restore
            EditText entry = (EditText)cont.findViewById(R.id.txtManualCode);
            entry.setText(lastManualBarcode);

            // prepnout TabLayout, pokud je potreba
            TabLayout tabLayout = (TabLayout) cont.findViewById(R.id.tabLayout);
            int selectedTab=savedInstanceState.getInt("selectedTab", 0);
            tabLayout.selectTab(tabLayout.getTabAt(selectedTab));
        }
    }

    // zneviditelni obrazek ilustracniho caroveho kodu
    public static void fadeImage(final ImageView imageView)
    {
        // pouze pokud je zobrazeny
        if (imageView.getAnimation() != null || imageView.getVisibility() == View.GONE)
            return;

        Animation fadeOut = new AlphaAnimation(1, 0f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1500);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                imageView.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        imageView.startAnimation(fadeOut);
    }


    // pipne/zavibruje
    public static void doHapticNotification(MainActivity cont, boolean positive) {
        if (SettingsHelper.getSettingBoolean(cont, SettingsHelper.Preference.BARCODE_BEEP)) {
            // zvuky pochazi z www.zapsplat.com
            MediaPlayer prehravac = MediaPlayer.create(cont, positive ? R.raw.beep_noholding : R.raw.beep_holding);
            prehravac.start();
        }
        if (SettingsHelper.getSettingBoolean(cont, SettingsHelper.Preference.BARCODE_VIBRATE)) {
            Vibrator v = (Vibrator) cont.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                if (positive)
                    v.vibrate(60);
                else
                {
                    long[] pattern = {0, 40, 40, 40};
                    v.vibrate(pattern, -1);
                }
            }
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    public void requestCameraPermission(MainActivity cont, final int cameraPerm) {
        Log.w("MainActivity", "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(cont, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(cont, permissions, cameraPerm);
            return;
        }

        final Activity thisActivity = cont;


        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(thisActivity, permissions, cameraPerm);
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.permission_camera_rationale)
                .setPositiveButton(R.string.ok, dialogListener)
                .show();
        /*
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_CAMERA_PERM);
            }
        };

        findViewById(R.id.topLayout).setOnClickListener(listener);
        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
         */
    }

}
