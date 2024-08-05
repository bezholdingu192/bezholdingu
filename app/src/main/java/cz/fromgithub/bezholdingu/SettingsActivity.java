package cz.fromgithub.bezholdingu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import cz.fromgithub.bezholdingu.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // nastaveni toolbaru
        ActionBar supportActionBar=getSupportActionBar();
        if (supportActionBar!=null) {
            supportActionBar.setTitle("Nastavení");
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }

        initSeekBar ();

        // naplnit formular daty
        nacistData();
    }

    // sipka zpet v toolbaru
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void nacistData()
    {
        // otevreme soubor pro cteni preferenci
        SharedPreferences sharedPreferences = this.getSharedPreferences("preferences.xml", Context.MODE_PRIVATE);
        // nacteme ulozenou hodnotu
        boolean barcodeBeep = sharedPreferences.getBoolean("barcodeBeep", true);
        boolean barcodeVibrate = sharedPreferences.getBoolean("barcodeVibrate", false);
        boolean toman = sharedPreferences.getBoolean("toman", false);
        boolean rusko = sharedPreferences.getBoolean("rusko", false);
        boolean belorusko = sharedPreferences.getBoolean("belorusko", false);
        int displayOrientation = sharedPreferences.getInt("displayOrientation", 1);
        int dataNotification = sharedPreferences.getInt("dataNotification", 1);
        int adsAllow = sharedPreferences.getInt("adsAllow", 1);
        int adsFreq = sharedPreferences.getInt("adsFreq", 2);
        int adsFreqWiFi = sharedPreferences.getInt("adsFreqWiFi", 10);
        // String text2 = Integer.toString(sharedPreferences.getInt("text2", 0));

        // ulozime hodnoty do formulare
        ((Switch)findViewById(R.id.swPipnout)).setChecked(barcodeBeep);
        ((Switch)findViewById(R.id.swZavibrovat)).setChecked(barcodeVibrate);

        ((Switch)findViewById(R.id.swToman)).setChecked(toman);
        ((Switch)findViewById(R.id.swRusko)).setChecked(rusko);
        ((Switch)findViewById(R.id.swBelorusko)).setChecked(belorusko);

        if(displayOrientation == 0)
            ((RadioButton) findViewById(R.id.rb_otaceni_neblokovat)).setChecked(true);
        else if(displayOrientation == 1)
            ((RadioButton) findViewById(R.id.rb_otaceni_portrait)).setChecked(true);
        else if(displayOrientation == 2)
            ((RadioButton) findViewById(R.id.rb_otaceni_landscape)).setChecked(true);

        if(dataNotification == 0)
            ((RadioButton) findViewById(R.id.rb_aktualizace_vypnout)).setChecked(true);
        else if(dataNotification == 1)
            ((RadioButton) findViewById(R.id.rb_aktualizace_mesic)).setChecked(true);
        else if(dataNotification == 2)
            ((RadioButton) findViewById(R.id.rb_aktualizace_3mesice)).setChecked(true);

        ((SeekBar) findViewById(R.id.sbFreqData)).setProgress(adsFreq);
        ((SeekBar) findViewById(R.id.sbFreqWifi)).setProgress(adsFreqWiFi);
    }

    public void ulozitZmenu(View sender)
    {
        // otevreme soubor pro zapis preferenci
        SharedPreferences sharedPreferences = this.getSharedPreferences("preferences.xml", Context.MODE_PRIVATE);
        // vytvorime objekt editor preferenci
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // pipnuti/vibrace
        if (sender.getId()==R.id.swPipnout) {
            editor.putBoolean("barcodeBeep", ((Switch)sender).isChecked());
        }
        else if (sender.getId()==R.id.swZavibrovat) {
            editor.putBoolean("barcodeVibrate", ((Switch)sender).isChecked());
        }

        // Toman
        if (sender.getId()==R.id.swToman) {
            editor.putBoolean("toman", ((Switch)sender).isChecked());
        }

        // Rusko
        if (sender.getId()==R.id.swRusko) {
            boolean isChecked = ((Switch)sender).isChecked();
            editor.putBoolean("rusko", isChecked);
            if (isChecked)
                showMessage("Aplikace rozpozná a označí zboží s originálním ruským čárovým kódem, tj. dovezené z Ruska. Nepozná ruské výrobky balené v Česku a opatřené českým kódem.");
        }

        // Belorusko
        if (sender.getId()==R.id.swBelorusko) {
            boolean isChecked = ((Switch)sender).isChecked();
            editor.putBoolean("belorusko", isChecked);
            if (isChecked)
                showMessage("Aplikace rozpozná a označí zboží s originálním běloruským čárovým kódem, tj. dovezené z Běloruska. Nepozná běloruské výrobky balené v Česku a opatřené českým kódem.");
        }

        // otaceni displeje
        if (sender.getId()==R.id.rb_otaceni_neblokovat) {
            editor.putInt("displayOrientation", 0);
        }
        else if (sender.getId()==R.id.rb_otaceni_portrait) {
            editor.putInt("displayOrientation", 1);
        }
        else if (sender.getId()==R.id.rb_otaceni_landscape) {
            editor.putInt("displayOrientation", 2);
        }

        // notifikace na zastarala data
        if (sender.getId()==R.id.rb_aktualizace_vypnout) {
            editor.putInt("dataNotification", 0);
        }
        else if (sender.getId()==R.id.rb_aktualizace_mesic) {
            editor.putInt("dataNotification", 1);
        }
        else if (sender.getId()==R.id.rb_aktualizace_3mesice) {
            editor.putInt("dataNotification", 2);
        }

        // reklamy
        if (sender.getId()==R.id.sbFreqData) {
            int progressData = ((SeekBar)sender).getProgress();
            editor.putInt("adsFreq", progressData);
            if (progressData == 0)
                showMessage("Při mobilním datovém připojení se reklamy nebudou zobrazovat.\nPokud se je rozhodnete v budoucnu opět zapnout, podpoříte tím další rozvoj aplikace.");
            else
                showMessage("Děkujeme, že si necháváte zobrazovat reklamy. Podporujete tím další rozvoj aplikace.");
        }
        else if (sender.getId()==R.id.sbFreqWifi) {
            int progressWiFi = ((SeekBar)sender).getProgress();
            editor.putInt("adsFreqWiFi", progressWiFi);
            if (progressWiFi == 0)
                showMessage("Při WiFi připojení se reklamy nebudou zobrazovat.\nPokud se je rozhodnete v budoucnu opět zapnout, podpoříte tím další rozvoj aplikace.");
            else
                showMessage("Děkujeme, že si necháváte zobrazovat reklamy. Podporujete tím další rozvoj aplikace.");
        }

        // data ulozime
        editor.apply();
    }

    private void showMessage (String msgText) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msgText, Snackbar.LENGTH_LONG);
        TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.GRAY);
        textView.setMaxLines(5);
        snackbar.show();
    }
    private void initSeekBar () {
        final TextView txtSeekBarData = (TextView)this.findViewById(R.id.txtSeekBarData);
        SeekBar sb = (SeekBar) this.findViewById(R.id.sbFreqData);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ulozitZmenu(seekBar);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int pos, boolean user) {
                String infotext = "";
                if (pos == 0)
                    infotext = "Datové připojení: reklamy nezobrazovat";
                else if (pos == 1)
                    infotext = "Datové připojení: zobrazit každou 10. reklamu";
                else if (pos > 1 && pos < 10)
                    infotext = String.format("Datové připojení: zobrazit %d z 10 reklam", pos);
                else if (pos == 10)
                    infotext = "Datové připojení: reklamy zobrazovat vždy";
                txtSeekBarData.setText(infotext);
            }
        });

        final TextView txtSeekBarWiFi = (TextView)this.findViewById(R.id.txtSeekBarWiFi);
        sb = (SeekBar) this.findViewById(R.id.sbFreqWifi);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ulozitZmenu(seekBar);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int pos, boolean user) {
                String infotext = "";
                if (pos == 0)
                    infotext = "WiFi připojení: reklamy nezobrazovat";
                else if (pos == 1)
                    infotext = "WiFi připojení: zobrazit každou 10, reklamu";
                else if (pos > 1 && pos < 10)
                    infotext = String.format("WiFi připojení: zobrazit %d z 10 reklam", pos);
                else if (pos == 10)
                    infotext = "WiFi připojení: reklamy zobrazovat vždy";
                txtSeekBarWiFi.setText(infotext);
            }
        });

    }
}