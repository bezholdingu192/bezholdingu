package cz.fromgithub.bezholdingu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cz.fromgithub.bezholdingu.helpers.SettingsHelper;

public class ToSendActivity extends AppCompatActivity {

    private static List<TosendData> recs = new ArrayList<TosendData>();
    private static ToSendAdapter adapter;
    private static ProgressBar progBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tosend);

        // nastaveni toolbaru
        ActionBar supportActionBar=getSupportActionBar();
        if (supportActionBar!=null) {
            supportActionBar.setTitle("K odeslání");
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }

        // naplnit formular daty
        nacistData();

        showProgressBar(this,false);
        showInfoText(this,recs.size()<1);
    }

    // sipka zpet v toolbaru
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void nacistData() {
        // otevreme soubor pro cteni preferenci
        SharedPreferences sharedPreferences = this.getSharedPreferences("tosend", Context.MODE_PRIVATE);
        // nacteme pocet ulozenych zaznamu
        int maxId = sharedPreferences.getInt("maxId", 0);

        // nacist vsechny zaznamy do seznamu
        recs.clear();
        for (int i = 0; i <= maxId; ++i) {
            String si = String.valueOf(i).trim();
            String code = sharedPreferences.getString("code" + si, "");
            String date = sharedPreferences.getString("date" + si, "");
            String desc = sharedPreferences.getString("desc" + si, "");
            String msg = sharedPreferences.getString("msg" + si, "");
            String email = sharedPreferences.getString("email" + si, "");
            if (!code.isEmpty()) {
                recs.add(new TosendData(si, code, date, desc, msg, email));
            }
        }

        // propojeni adapteru s kontejnerem RecyclerView
        adapter = new ToSendAdapter(recs, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recSeznam);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }


    public void onClickSend (View view) {
        if (isDatovePripojeniOk()<0) {
            Toast.makeText(getBaseContext(), "Není dostupné datové připojení.\nZapněte prosím WiFi nebo mobilní data.", Toast.LENGTH_LONG).show();
            return;
        }
        else if (recs.size() < 1) {
            Toast.makeText(getBaseContext(), "Nejsou k dispozici žádné záznamy, které by bylo možné odeslat.", Toast.LENGTH_LONG).show();
            return;
        }

        // disablovat tlacitko
        ((Button) view).setEnabled(false);

        // iniciace progressbaru
        progBar = (ProgressBar) findViewById(R.id.progressBar2);
        progBar.setMax(recs.size());
        progBar.setProgress(0);
        showProgressBar(this,true);


        // spustit odesilani
        onSentData(this, true, "");
    }

    // udalost, volana z FeedBackWrongAsync po nacteni dat ze serveru
    public static void onSentData (Context cont, boolean init, String err) {

        if (!init && err.length() < 1) {
            // smazat odeslany zaznam
            preferencesRemoveItem((ToSendActivity) cont, recs.get(0).id);
            recs.remove(0);
            adapter.notifyDataSetChanged();

            // posunout progressbar
            progBar.setProgress(progBar.getProgress()+1);
        }


        if (recs.size() < 1 || err.length() > 0) {
            String msg;
            if (err.length() > 0) {
                if (err.length() > 200) err = err.substring(0, 200).concat("....");
                msg = String.format("Během odesílání dat došlo k chybě.\n%s\n\nPokud se chyby budou opakovat, aktualizujte prosím aplikaci z Obchodu Google Play.", err);
            }
            else {
                msg = "Data byla odeslána.\nDěkujeme za spolupráci.";
            }

            // enablovat tlacitko
            Button btnUpload = (Button)((ToSendActivity)cont).findViewById(R.id.btnUpload);
            btnUpload.setEnabled(true);

            // nastavit progressbar
            // progBar.setProgress(progBar.getMax());
            showProgressBar((ToSendActivity) cont,false);
            showInfoText((ToSendActivity) cont,recs.size()<1);

            // zobrazit info s vysledkem
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(cont);
            alert.setTitle(R.string.app_name);
            alert.setMessage(msg);
            alert.setPositiveButton(R.string.ok, null);
            try {
                alert.create().show();
            }
            catch (Exception ex) {
            }

            return;
        }

        // odeslat dalsi data
        if (recs.size() > 0) {
            // nacist identifikacni znaky uzivatele
            String ident = SettingsHelper.getSettingString(cont, SettingsHelper.Preference.IDENTITY);

            FeedbackWrongAsync dataSync = new FeedbackWrongAsync(cont, recs.get(0).email, recs.get(0).code, recs.get(0).msg.replace("{0}", recs.get(0).desc), ident);
            dataSync.execute();
        }
    }

    public static void onClickRecyclerItem(final int id, final ToSendActivity cont) {
        LayoutInflater inflater = cont.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.activity_tosend_edit, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        final TextView txtKod = (TextView) alertLayout.findViewById(R.id.txtDesc);
        txtKod.setText(recs.get(id).desc);

        builder.setTitle("K odeslání");
        builder.setMessage("Kód: " + recs.get(id).code);
        builder.setPositiveButton("Uložit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String newDesc = txtKod.getText().toString();
                preferencesUpdateDesc(cont, recs.get(id).id, newDesc);
                recs.get(id).desc = newDesc;
                adapter.notifyDataSetChanged();
                return;
            }
        });
        builder.setNegativeButton("Zrušit", null);
        builder.setNeutralButton("Smazat záznam", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // potvrzeni, ze opravdu smazat
                AlertDialog.Builder builder2 = new AlertDialog.Builder(cont);
                builder2.setTitle("Smazat záznam");
                builder2.setMessage("Opravdu smazat záznam? Tuto operaci nejde vrátit.");
                builder2.setPositiveButton("Smazat", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        preferencesRemoveItem(cont, recs.get(id).id);
                        recs.remove(id);
                        adapter.notifyDataSetChanged();
                        showInfoText(cont,recs.size()<1);
                        return;

                    }
                });
                builder2.setNegativeButton("Zrušit", null);
                builder2.create().show();
                return;
            }
        });
        builder.setView(alertLayout);
        builder.create().show();
    }

    private static void preferencesUpdateDesc(ToSendActivity cont, String sID, String desc)
    {
        SharedPreferences sharedPreferences = cont.getSharedPreferences("tosend", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("desc" + sID, desc);
        editor.apply();
    }

    private static void preferencesRemoveItem(ToSendActivity cont, String sID)
    {
        SharedPreferences sharedPreferences = cont.getSharedPreferences("tosend", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("code" + sID);
        editor.remove("date" + sID);
        editor.remove("desc" + sID);
        editor.remove("msg" + sID);
        editor.remove("email" + sID);
        editor.apply();
    }

    private static void preferencesResetId(ToSendActivity cont)
    {
        SharedPreferences sharedPreferences = cont.getSharedPreferences("tosend", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("maxId", 0);
        editor.apply();
    }

    // Overi dostupnost datoveho pripojeni
    private byte isDatovePripojeniOk() {
        Context context = getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return -1;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    return 0;
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    return 1;
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) )
                    return 2;
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    return 3;
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) )
                    return 4;
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE))
                    return 5;
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) )
                    return 6;
            }
        }
        else {
            return cm.getActiveNetworkInfo() == null ? -1 : (byte)10;
        }
        return -1;
    }

    // zobrazi/skryje vrstvu s progressbarem
    private static void showProgressBar (ToSendActivity cont, boolean show) {
        ConstraintLayout progLayer = (ConstraintLayout) cont.findViewById(R.id.layProgressBar);
        if(show) {
            progLayer.bringToFront();
            progLayer.setVisibility(View.VISIBLE);
        }
        else {
            RecyclerView recsList = (RecyclerView) cont.findViewById(R.id.recSeznam);
            recsList.bringToFront();
            progLayer.setVisibility(View.GONE);
        }
    }

    // zobrazi/skryje infotext
    private static void showInfoText (ToSendActivity cont, boolean show) {
        TextView txtInfo = (TextView) cont.findViewById(R.id.txtInfo);
        txtInfo.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

class TosendData {
    String id;
    String code;
    String date;
    String desc;
    String msg;
    String email;

    public TosendData(String _id, String _code, String _date, String _desc, String _msg, String _email) {
        id=_id;
        code=_code;
        date=_date;
        desc=_desc;
        msg=_msg;
        email=_email;
    }
}