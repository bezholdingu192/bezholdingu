package cz.fromgithub.bezholdingu.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Switch;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import cz.fromgithub.bezholdingu.MainActivity;
import cz.fromgithub.bezholdingu.R;

public class AdsHelper {

    private enum AdvState { SHOWING, HIDDEN }
    private enum BroadcastState { ON, OFF }

    private static ConstraintLayout mAdlayout;  // constraint layout s reklamou a zaviracim ouskem
    private static AdView mAdView;  // View s reklamou na formulari
    private static AdvState advState = AdvState.HIDDEN;  // priznak, jestli se aktualne odchytavaji zmeny datoveho pripojeni
    private static CountDownTimer casovac = null;   // counter, ktery se pouzije pokud se maji reklamy automaticky zobrazovat/skryvat
    private static BroadcastState broadcastState = BroadcastState.OFF;  // priznak, jestli se aktualne odchytavaji zmeny datoveho pripojeni
    private static byte wifiStatus = -1;


    // aktivuje testovaci reklamy
    public static void zapnoutTestovaciReklamy() {
        /*
        List<String> testDeviceIds = Arrays.asList("", "");     // do uzozovek pripadne doplnit kody zarizeni, ktere maji zobrazovat testovaci reklamy (namisto ostrych)
        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
        */
    }

    // spusteni reklam (start app nebo restart pokud byly mezitim zastaveny)
    public static void startReklamy(final MainActivity cont, boolean withInit) {

        if(withInit) {
            // inicializace reklamy
            mAdlayout = (ConstraintLayout)cont.findViewById(R.id.layAd);
            mAdView = (AdView) cont.findViewById(R.id.adView);

            MobileAds.initialize(cont, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });

            // sem muzou prijit udalosti reklamniho prouzku (existuje jich asi sest)
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    Switch swManualEntry = (Switch) cont.findViewById(R.id.swManualEntry);
                    if (!swManualEntry.isChecked())
                        mAdlayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                }
            });
        }
        else {
            zapnoutReklamy(cont, (byte) -1);
        }
  }

    // zastaveni reklam (otevrena jina aktivita nebo je aplikace na pozadi)
    public static void stopReklamy(MainActivity cont) {
        stopBroadcast(cont);     // odregistrovat receiver (broadcast zastavit, ignorovat zmeny typu pripojeni)

        if (casovac != null) {
            casovac.cancel();
            casovac = null;
        }
        hideAd(cont);
    }

    // preruseni reklam (kliknuti na "zavrit reklamu")
    public static void tmpBezReklam(MainActivity cont) {
        SettingsHelper.saveSetting(cont, SettingsHelper.Preference.ADS_STEP, -2);  // pocet reklamnich cyklu bez zobrazeni reklamy
        hideAd (cont);
    }



    // pokud jsou reklamy v Nastaveni zapnute, spustit jejich zobrazovani (wifiState: 1 = zapnute WiFi, 0 = vypnute WiFi, -1 = neznamy stav WiFi)
    private static void zapnoutReklamy(final MainActivity cont, byte wifiState) {

        int settingsFreqData = SettingsHelper.getSettingInt(cont, SettingsHelper.Preference.ADS_FREQUENCY_DATA);
        int settingsFreqWiFi = SettingsHelper.getSettingInt(cont, SettingsHelper.Preference.ADS_FREQUENCY_WIFI);

        // AAA toto cele se muze v dalsi verzi odstranit, je to jenom kvuli kompatibilite s verzi 1.6.3
        if (settingsFreqData < 0) {
            settingsFreqData = 2;
            int compatibility = SettingsHelper.getSettingInt(cont, SettingsHelper.Preference.ADS_ALLOW);
            if (compatibility==2)
                settingsFreqData = 10;
            SettingsHelper.saveSetting(cont, SettingsHelper.Preference.ADS_FREQUENCY_DATA, settingsFreqData);
        }

        Switch swManualEntry = (Switch) cont.findViewById(R.id.swManualEntry);
        if ((wifiState == 0 && settingsFreqData == 10) || (wifiState == 1 && settingsFreqWiFi == 10))  {
            // zobrazovat ads vzdy, bez casovace
           if (casovac != null) {
                casovac.cancel();
                casovac = null;
            }
            showAd(cont);
        }
        else if((wifiState == 0 && settingsFreqData > 0 && settingsFreqData < 10) || (wifiState == 1 && settingsFreqWiFi > 0 && settingsFreqWiFi < 10)) {
            // zobrazovat ads s casovacem
            if (casovac == null) {
                casovac = new CountDownTimer(Long.MAX_VALUE, 90*1000) {
                    public void onTick(long millisUntilFinished) {
                        refreshAd(cont);
                    }
                    public void onFinish() { }
                };
                casovac.start();
            }
        }
        else if((wifiState == 0 && settingsFreqData == 0) || (wifiState == 1 && settingsFreqWiFi == 0) || swManualEntry.isChecked()) {
            // nezobrazovat ads vubec
            if (casovac != null) {
                casovac.cancel();
                casovac = null;
            }
            hideAd(cont);
        }

        // pokud se maji reklamy zobrazovat pri wifi/datech, tak spustit broadcast, jinak ho zastavit
        if (settingsFreqData > 0 || settingsFreqWiFi > 0)  // WiFi
            startBroadcast(cont);    // broadcast spustit
        else
            stopBroadcast(cont);     // broadcast zastavit
    }

    // poradi, ve kterem se zobrazuji a skryvaji reklamy
    private static final boolean[][] adPattern = {
            {false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, true},
            {false, false, false, false, true, false, false, false, false, true},
            {false, false, true, false, false, true, false, false, true, false},
            {true, false, false, true, false, false, true, false, false, true},
            {true, false, true, false, true, false, true, false, true, false},
            {false, true, true, false, true, true, false, true, true, false},
            {true, true, true, false, true, true, false, true, true, false},
            {true, true, true, true, false, true, true, true, true, false},
            {true, true, true, true, true, true, true, true, true, false},
            {true, true, true, true, true, true, true, true, true, true},
    };

    // volane casovacem
    private static void refreshAd (MainActivity cont) {

        int freq = SettingsHelper.getSettingInt(cont, wifiStatus == 1 ? SettingsHelper.Preference.ADS_FREQUENCY_WIFI : SettingsHelper.Preference.ADS_FREQUENCY_DATA);
        int adStep = SettingsHelper.getSettingInt(cont, SettingsHelper.Preference.ADS_STEP);

        // AAA toto se muze v dalsi verzi odstranit, je to jenom kvuli kompatibilite s verzi 1.6.3
        if (freq < 0)
            freq = 2;

        boolean co = adStep<0 ? false : adPattern [freq][adStep];
        if(co)
            showAd(cont);
        else
            hideAd(cont);

/*
        MediaPlayer prehravac;
        if(co)
            prehravac= MediaPlayer.create(cont, R.raw.beep_noholding);
        else
            prehravac= MediaPlayer.create(cont, R.raw.beep_holding);

        if (prehravac.isPlaying())
            prehravac.stop();
        prehravac.start();
*/

        adStep = adStep<9 ? ++adStep : 0;
        SettingsHelper.saveSetting(cont, SettingsHelper.Preference.ADS_STEP, adStep);
    }

    // zobrazit reklamu
    private static void showAd (MainActivity cont) {
        if (advState == AdvState.HIDDEN) {
            mAdView.setEnabled(true);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            advState = AdvState.SHOWING;
        }
    }

    // skryt reklamu
    private static void hideAd (MainActivity cont) {
      //  if (advState == AdvState.SHOWING) {
            mAdlayout.setVisibility(View.GONE);
            mAdView.setEnabled(false);
            advState = AdvState.HIDDEN;
     //   }
    }




    // zaregistrovat receiver pro zmenu typu pripojeni (pokud zatim zaregistrovany neni)
    private static void startBroadcast(MainActivity cont) {
        if (broadcastState == BroadcastState.OFF) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            cont.registerReceiver(broadcastReceiver, intentFilter);
            broadcastState = BroadcastState.ON;
        }
    }

    // odregistrovat receiver pro zmenu typu pripojeni (pokud je zaregistrovany)
    private static void stopBroadcast(MainActivity cont) {
        if (broadcastState == BroadcastState.ON) {
            cont.unregisterReceiver(broadcastReceiver);
            broadcastState = BroadcastState.OFF;
        }
    }

    // odchytavani zmeny typu datoveho pripojeni
    private static final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            wifiStatus = (networkInfo.getState() == NetworkInfo.State.CONNECTED) ? (byte)1 : (byte)0;

            zapnoutReklamy((MainActivity)context, wifiStatus);
        }
    };

    // Overi dostupnost datoveho pripojeni
    // POZOR toto bohuzel nefunguje dobre s broadcastem - ten triggeruje driv, nez tato metoda dokaze detekovat zmenu
    // (cili v debugu to vypada jako funkcni, v runtime ne). Takze to radej zjistuji uz v Broadcastu, zastaralym zpusobem.
    /*
    private static byte getConnectionType(Context context) {
        // Context context = getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return -3;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network ntwrk = cm.getActiveNetwork();
            if (ntwrk == null)
                return -5;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(ntwrk);
            if (capabilities != null) { // && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    return 0;
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) // || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE))
                    return 1;
                else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    return 3;
                else
                    return -4;
            }
            else
                return -6;
        }
        else {
            return cm.getActiveNetworkInfo() == null ? -1 : (byte)0;    // jako by to bylo pres data
        }
        // return -2;
    }*/
}
