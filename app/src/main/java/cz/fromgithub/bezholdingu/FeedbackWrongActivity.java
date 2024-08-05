package cz.fromgithub.bezholdingu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cz.fromgithub.bezholdingu.helpers.DatafilesHelper;
import cz.fromgithub.bezholdingu.helpers.Notifications;
import cz.fromgithub.bezholdingu.helpers.SettingsHelper;

public class FeedbackWrongActivity extends AppCompatActivity {

    private static TextView txtBrand;
    private String kod = "";
    private String vysledek;

    public static byte singleton = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_wrong);

        // nastaveni toolbaru
        ActionBar supportActionBar=getSupportActionBar();
        if (supportActionBar!=null) {
            supportActionBar.setTitle("Chyba v datech");
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }

        Notifications.checkForDataVersionFeedback(this);

        Intent myIntent = getIntent();
        kod = myIntent.getStringExtra("kod");
        vysledek = myIntent.getStringExtra("vysledek");

        TextView txtCode = (TextView)this.findViewById(R.id.txtCode);
        txtCode.setText(String.format("Kód: %s", kod));


        // obchodni retezec se zobrazi pouze pokud jde o vahovy kod
        TextView txtViewBrand=(TextView)findViewById(R.id.textViewBrand);
        txtBrand=(TextView)findViewById(R.id.txtBrand);
        if (kod.length()>2 && !kod.substring(0, 1).equals("2")) {
            LinearLayout linlay = (LinearLayout)findViewById(R.id.layoutLinear);
            linlay.removeView(txtViewBrand);
            linlay.removeView(txtBrand);
            txtBrand = null;
        }
        else {
            txtBrand.setText(SettingsHelper.getSettingString(this, SettingsHelper.Preference.FEEDBACK_BRAND));
        }

        // predvyplnit emailovou adresu
        TextView txtEmail=(TextView)findViewById(R.id.txtEmail);
        txtEmail.setText(SettingsHelper.getSettingString(this, SettingsHelper.Preference.FEEDBACK_EMAIL));
    }

    public void onClickBrand (View view) {
        // vytvorime si pole nazvu polozek seznamu
        final String [] poleNazvu = { "Albert", "Billa", "Globus", "Kaufland", "Lidl", "Makro", "Norma", "Penny", "Tesco" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Obchodní řetězec");
        builder.setItems(poleNazvu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int volba) {
            txtBrand.setText(poleNazvu[volba]);
            }
        });
        builder.create().show();
    }

    public void onClickSendSave (View view) {

        // ulozit do preferenci obchodni retezec a email
        if (txtBrand != null)
            SettingsHelper.saveSetting(this, SettingsHelper.Preference.FEEDBACK_BRAND, txtBrand.getText().toString());
        TextView txtEmail=(TextView)findViewById(R.id.txtEmail);
        String email = txtEmail.getText().toString();
        SettingsHelper.saveSetting(this, SettingsHelper.Preference.FEEDBACK_EMAIL, email);

        // test, jestli je vyplneny popis
        TextView txtDesc=(TextView)findViewById(R.id.txtDesc);
        String desc = txtDesc.getText().toString();
        if (desc.length()<2) {
            Toast.makeText(this, "Doplňte prosím alespoň stručný popis chyby, abychom věděli co máme opravit.", Toast.LENGTH_LONG).show();
            return;
        }

        // sestavit zpravu k odeslani/ulozeni
        String curDate = new SimpleDateFormat("d.M.yyyy HH:mm", Locale.GERMANY).format(Calendar.getInstance().getTime());
        String msg = String.format("Upozorneni na chybu v datech (%s):\n", curDate);
        msg += String.format("Verze dat: %s\n", DatafilesHelper.getDateVersionApp(this));
        if (txtBrand != null) msg += String.format("Řetězec: %s\n", txtBrand.getText().toString());
        msg += String.format("Kod: %s\n", kod);
        msg += String.format("Vysledek: %s\n", vysledek);
        msg += "Popis chyby: {0}\n";
        msg += String.format("Email pro odpoved: %s", email);

        if (view.getId()==R.id.btnSend) {
            msg=msg.replace("{0}", desc);

            // spustit progressbar
            ProgressBar prgBar=(ProgressBar)findViewById(R.id.prgFeedback);
            prgBar.bringToFront();
            prgBar.setVisibility(View.VISIBLE);

            // disablovat prvky formulare
            View txtView=(View)findViewById(R.id.txtBrand);
            if (txtView!=null)
                txtView.setEnabled(false);
            txtDesc.setEnabled(false);
            txtEmail.setEnabled(false);
            Button btn=(Button)findViewById(R.id.btnSave);
            btn.setEnabled(false);
            ((Button)view).setEnabled(false);

            // nacist identifikacni znaky uzivatele
            String ident = SettingsHelper.getSettingString(this, SettingsHelper.Preference.IDENTITY);

            // odeslat data
            FeedbackWrongAsync dataSync = null;
         //   Resources res = this.getResources();

            dataSync = new FeedbackWrongAsync(this, email, kod, msg, ident);
            dataSync.execute();
        }
        else if (view.getId()==R.id.btnSave) {

            ulozitData(kod, curDate, desc, msg, email);

            // info uzivateli, ze je ulozeno
            if (this != null) {
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
                alert.setTitle(R.string.app_name);
                alert.setMessage("Informace o chybě byla uložena. Odeslat ji můžete v menu, v pravém horním rohu aplikace, pod položkou K odeslání.");
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alert.create().show();
            }
        }
    }

    private void ulozitData(String code, String taken, String desc, String msg, String email)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("tosend", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // zjistit ID pro novy zaznam
        int iId = sharedPreferences.getInt("maxId", 0) + 1;
            String sID = String.valueOf(iId).trim();
        // zapsat data
        editor.putString("code" + sID, code);
        editor.putString("date" + sID, taken);
        editor.putString("desc" + sID, desc);
        editor.putString("msg" + sID, msg);
        editor.putString("email" + sID, email);

        // zapsat nove id
        editor.putInt("maxId", iId);
        editor.apply();
    }


    // metoda, volana po odeslani dat
    public static void onSentData(final Context cont, boolean success) {
        // vypnout progressbar
        ProgressBar prgBar=(ProgressBar)((Activity)cont).findViewById(R.id.prgFeedback);
        prgBar.setVisibility(View.GONE);

        // zobrazit info s vysledkem
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(cont);
        alert.setTitle(R.string.app_name);
        if (success) {
            alert.setMessage("Informace o chybě byla odeslána.\nDěkujeme za spolupráci!");
            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                ((Activity) cont).finish();
                }
            });
        }
        else {
            alert.setMessage("Informaci o chybě se nepodařilo odeslat.\nZkontrolujte prosím datové připojení, nebo zkuste odeslání později.");
            alert.setPositiveButton(R.string.ok,null);
            View view=(View)((Activity)cont).findViewById(R.id.txtBrand);
            if (view!=null)
                view.setEnabled(true);
            view=(View)((Activity)cont).findViewById(R.id.txtDesc);
            view.setEnabled(true);
            view=(View)((Activity)cont).findViewById(R.id.txtEmail);
            view.setEnabled(true);
            Button btn=(Button)((Activity)cont).findViewById(R.id.btnSave);
            btn.setEnabled(true);
            btn=(Button)((Activity)cont).findViewById(R.id.btnSend);
            btn.setEnabled(true);
        }

        try {
            alert.create().show();
        }
        catch (Exception ex) {
        }
    }

    // sipka zpet v toolbaru
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing())
            singleton=0;
    }
}