package cz.fromgithub.bezholdingu.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import cz.fromgithub.bezholdingu.R;

public class SettingsHelper {


    public enum Preference {
        FIRST_RUN,
        BARCODE_BEEP,
        BARCODE_VIBRATE,
        TOMAN,
        RUSKO,
        BELORUSKO,
        DISPLAY_ORIENTATION,
        DATA_NOTIFICATION,
        DATA_LASTNOTIFICATION,
        DATA_LASTDOWNLOAD,
        ADS_ALLOW,  // ADS_ALLOW se v pristi verzi muze odstranit
        ADS_FREQUENCY_DATA,
        ADS_FREQUENCY_WIFI,
        ADS_STEP,
        FEEDBACK_BRAND,
        FEEDBACK_EMAIL,
        FEEDBACK_LASTNOTIFICATION,
        IDENTITY
    }

    public static boolean getSettingBoolean (Context cont, Preference pref) {
        // otevreme soubor pro cteni preferenci
        SharedPreferences sharedPreferences = cont.getSharedPreferences("preferences.xml", Context.MODE_PRIVATE);

        // prvni spusteni po instalaci
        if (pref == Preference.FIRST_RUN) {
            // rovnou do preferenci zapiseme false - true to ma byt jen jednou jedinkrat
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstRun", false);
            editor.apply();
            return sharedPreferences.getBoolean("firstRun", false);
        }

        // nacteme ulozenou hodnotu
        if (pref == Preference.BARCODE_BEEP)
            return sharedPreferences.getBoolean("barcodeBeep", true);
        else if (pref == Preference.BARCODE_VIBRATE)
            return sharedPreferences.getBoolean("barcodeVibrate", false);
        else if (pref == Preference.TOMAN)
            return sharedPreferences.getBoolean("toman", false);
        else if (pref == Preference.RUSKO)
            return sharedPreferences.getBoolean("rusko", false);
        else if (pref == Preference.BELORUSKO)
            return sharedPreferences.getBoolean("belorusko", false);
        else
            return false;
        //throw new Exception("Chyba - pokus o čtení neexistujícího nastavení.");

    }

    public static int getSettingInt (Context cont, Preference pref) {
        // otevreme soubor pro cteni preferenci
        SharedPreferences sharedPreferences = cont.getSharedPreferences("preferences.xml", Context.MODE_PRIVATE);

        // nacteme ulozenou hodnotu
        if (pref == Preference.DISPLAY_ORIENTATION)
            return sharedPreferences.getInt("displayOrientation", 1);
        else if (pref == Preference.ADS_ALLOW)
            return sharedPreferences.getInt("adsAllow", -1);
        else if (pref == Preference.ADS_FREQUENCY_DATA)
            return sharedPreferences.getInt("adsFreq", -2); // AAA default zmenit na 2, toto je jenom kvuli kompatibilite s verzi 1.3.6
        else if (pref == Preference.ADS_FREQUENCY_WIFI)
            return sharedPreferences.getInt("adsFreqWiFi", 10);
        else if (pref == Preference.DATA_NOTIFICATION)
            return sharedPreferences.getInt("dataNotification", 1);
        else if (pref == Preference.ADS_STEP)
            return sharedPreferences.getInt("adStep", -1);
        else
            return 0;
    }

    public static String getSettingString (Context cont, Preference pref) {
        // otevreme soubor pro cteni preferenci
        SharedPreferences sharedPreferences = cont.getSharedPreferences("preferences.xml", Context.MODE_PRIVATE);

        // vygenerovany identifikator uzivatele
        if (pref == Preference.IDENTITY) {
            String ident = sharedPreferences.getString("identity", "");

            if (ident.length() < 1) {
                // pokud identita nebyla vygenerovana, vygenerovat a zapsat
                Random r = new Random();
                String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
                for (int i = 0; i < 8; ++i) {
                    int j = r.nextInt(62);
                    ident = ident.concat(CHARS.substring(j, j + 1));
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("identity", ident);
                editor.apply();
            }
            return ident;
        }

        // nacteme ulozenou hodnotu
        if (pref == Preference.DATA_LASTDOWNLOAD)
            return sharedPreferences.getString("lastDataDownload", "");
        else if (pref == Preference.DATA_LASTNOTIFICATION)
            return sharedPreferences.getString("lastDataNotification", "");
        else if (pref == Preference.FEEDBACK_BRAND)
            return sharedPreferences.getString("feedbackBrand", "");
        else if (pref == Preference.FEEDBACK_EMAIL)
            return sharedPreferences.getString("feedbackEmail", "");
        else if (pref == Preference.FEEDBACK_LASTNOTIFICATION)
            return sharedPreferences.getString("lastFeedbackNotification", "");
        else
            return "";
    }

    public static void saveSetting (@NotNull Context cont, Preference pref, Object value) {
        // otevreme soubor pro cteni preferenci
        SharedPreferences sharedPreferences = cont.getSharedPreferences("preferences.xml", Context.MODE_PRIVATE);

        // vytvorime objekt editor preferenci
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (pref == Preference.FEEDBACK_BRAND)
            editor.putString("feedbackBrand", (String)value);
        else if (pref == Preference.FEEDBACK_EMAIL)
            editor.putString("feedbackEmail", (String)value);

        if (pref == Preference.ADS_FREQUENCY_DATA)
            editor.putInt("adsFreq", (int)value);
        else if (pref == Preference.ADS_FREQUENCY_WIFI)
            editor.putInt("adsFreqWiFi", (int)value);
        else if (pref == Preference.ADS_STEP)
            editor.putInt("adStep", (int)value);

        if (pref == Preference.DATA_LASTDOWNLOAD)
            editor.putString("lastDataDownload", new SimpleDateFormat("d.M.yyyy").format((Date)value));
        else if (pref == Preference.DATA_LASTNOTIFICATION)
            editor.putString("lastDataNotification", new SimpleDateFormat("d.M.yyyy").format((Date)value));
        else if (pref == Preference.FEEDBACK_LASTNOTIFICATION)
            editor.putString("lastFeedbackNotification", new SimpleDateFormat("d.M.yyyy").format((Date)value));

        // data ulozime
        editor.apply();

    }
}
