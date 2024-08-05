package cz.fromgithub.bezholdingu.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.fromgithub.bezholdingu.DownloadDataVersionAsync;
import cz.fromgithub.bezholdingu.R;

public class Notifications {

    public static Date todayDate;

    public static void checkForDataActualization(Context cont) {

        // fake datum pro ucely testovani
        //  todayDate = Date.from(new Date().toInstant().plus(52, ChronoUnit.DAYS));
        todayDate = new Date();

        int notifikaceSettings = SettingsHelper.getSettingInt(cont, SettingsHelper.Preference.DATA_NOTIFICATION);

        // notifikace nezobrazovat
        if (notifikaceSettings == 0)
            return;

        // nacist datum z datoveho souboru
        String verzeDatApp = getDateVersionApp(cont);
        if (verzeDatApp.isEmpty())
            return;

        long dataAge = daysFromDatafile(cont, verzeDatApp);
        if (notifikaceSettings == 1 && dataAge < 31) return;    // adta jeste nejsou starsi jak mesic..
        if (notifikaceSettings == 2 && dataAge < 92) return;    // adta jeste nejsou starsi jak 3 mesice..

        long lastNotificationDays = daysFromLastNotification(cont, SettingsHelper.Preference.DATA_LASTNOTIFICATION);
        if (lastNotificationDays < 7) return;       // od minule notifikace jeste neubehl tyden (zobrazujeme jenom jednou do tydne)

        // start zjisteni verze dat na serveru
        DownloadDataVersionAsync dataSync = new DownloadDataVersionAsync(cont.getString(R.string.eanverze_url), cont, (byte)2);
        dataSync.execute();
    }


    // udalost, volana z DownloadDataVersionAsync po nacteni verze ze serveru
    public static void onDownloadedVersion (String data, Object obj) {
        if (data.length()<1)
            return;
        try {
            JSONObject jObject = new JSONObject(data);
            String verzeDatServer = jObject.getString("datum");

            Context cont = (Context)obj;
            String verzeDatApp = getDateVersionApp(cont);

            if (newDataAvailable(verzeDatServer, verzeDatApp)) {
                String msg = String.format("Aplikace obsahuje vnitřní data o zboží. Tato data jsou z %s.\n\n", verzeDatApp);
                msg = msg.concat("Udělejte si prosím chvilku na jejich aktualizaci, aby aplikace měla opravdu ta nejnovější. ");
                msg = msg.concat(String.format("Ke stažení jsou připravena data z %s.\n\n", verzeDatServer));
                msg = msg.concat("Aktualizaci najdete v menu, v pravém horním rohu, pod položkou Aktualizace dat.");

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(cont);
                alertBuilder.setTitle("Aktualizace dat");
                alertBuilder.setMessage(msg);
                alertBuilder.setPositiveButton("Zavřít", null);
                alertBuilder.create().show();
            }
            // zapsat do registru datum posledni notifikace
            SettingsHelper.saveSetting(cont, SettingsHelper.Preference.DATA_LASTNOTIFICATION, todayDate);
        }
        catch (JSONException e) {
            Log.d("JSON parser", String.format("Chyba během parsování JSON.\n\n%s", e.getMessage()));
        }
    }



    // Zjisti stari dat v aplikaci a pripadne zobrazi upozorneni
    public static void checkForDataVersionFeedback(Context cont) {

        try {
            long lastNotificationDays = daysFromLastNotification(cont, SettingsHelper.Preference.FEEDBACK_LASTNOTIFICATION);
            if (lastNotificationDays < 1) return;       // od minule notifikace jeste neubehl den (zobrazujeme jenom jednou dene)

            String verzeDatApp = getDateVersionApp(cont);

            Date dataApp = new SimpleDateFormat("d.M.yyyy").parse(verzeDatApp);
            long dataAge = new Date().getTime() - dataApp.getTime();

            if (dataAge> (long)(1000 * 60 * 60 * 24) * 30) {
                String msg = String.format("Aplikace obsahuje data z %s.\n\n", verzeDatApp);
                msg = msg.concat("Před odesláním upozornění doporučujeme zkontrolovat, jestli není k dispozici novější verze dat, která by chybu mohla mít již vyřešenou.\n\n");
                msg = msg.concat("Aktualizaci najdete na hlavní straně aplikace v menu, pod položkou Aktualizace dat.");

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(cont);
                alertBuilder.setTitle("Aktualizace dat");
                alertBuilder.setMessage(msg);
                alertBuilder.setPositiveButton("Zavřít", null);
                alertBuilder.create().show();
            }
            // zapsat do registru datum posledni notifikace
            SettingsHelper.saveSetting(cont, SettingsHelper.Preference.FEEDBACK_LASTNOTIFICATION, todayDate);
        }
        catch (Exception e) {
                Log.d("Verze dat (days)", String.format("Chyba při zjistovani stari dat v aplikaci.\n\n%s", e.getMessage()));
        }
    }

    // srovnani verze dat na serveru s verzi dat v aplikaci
    private static boolean newDataAvailable(String verzeDatServer, String verzeDatApp) {
        try {
            Date dataServer = new SimpleDateFormat("d.M.yyyy").parse(verzeDatServer);
            Date dataApp= new SimpleDateFormat("d.M.yyyy").parse(verzeDatApp);
            long diffMs = dataServer.getTime() - dataApp.getTime();
            return diffMs > 1000 * 60 * 60 * 2; // pro jistotu dvouhodinova tolerance, ale nemela by byt potreba
        }
        catch (Exception e) {
            Log.d("Verze dat (days)", String.format("Chyba při srovnavani verze dat aplikace s datumemverzi dat na serveru.\n\n%s", e.getMessage()));
            return false;
        }
    }


    // pocet dni od posledni notifikace
    private static long daysFromLastNotification (Context cont, SettingsHelper.Preference pref) {

        String lastNotif = getLastNotification(cont, pref);
        if (lastNotif.isEmpty())
            return 1000;
        try {
            Date versionDate = new SimpleDateFormat("d.M.yyyy").parse(lastNotif);
            Date currentDate = todayDate;
            long diffMs = currentDate.getTime() - versionDate.getTime();
            return diffMs / (1000 * 60 * 60 * 24);
        }
        catch (Exception e) {
            Log.d("Verze dat (days)", String.format("Chyba během zjišťování data poslední notifikace.\n\n%s", e.getMessage()));
            return 1000;
        }
    }

    // Zjisti datum posledne zobrazene notifikace
    private static String getLastNotification (Context cont, SettingsHelper.Preference pref) {
        try {
            return SettingsHelper.getSettingString(cont, pref);
        }
        catch (Exception e) {
            Log.d("Preferences", String.format("Chyba během zjišťování data posledni notifikace.\n\n%s", e.getMessage()));
        }
        return "";
    }


    // pocet dni od datumu v datovem souboru
    private static long daysFromDatafile (Context cont, String verzeDat) {

        if (verzeDat.isEmpty())
            return 0;

        try {
            // zjistit, kolik dni jsou stara data
            Date versionDate = new SimpleDateFormat("d.M.yyyy").parse(verzeDat);
            Date currentDate = todayDate;
            long diffMs = currentDate.getTime() - versionDate.getTime();
            return diffMs / (1000 * 60 * 60 * 24);
        }
        catch (Exception e) {
            Log.d("Verze dat (days)", String.format("Chyba během ověřování verze dat.\n\n%s", e.getMessage()));
            return 0;
        }
    }

    // Zjisti z datoveho souboru verzi dat (resp. datum, verzujeme datumem)
    private static String getDateVersionApp (Context cont) {
        String jsonFromFile = "";
        try {
            // otevreme soubor pro cteni
            FileInputStream inStream = cont.openFileInput(cont.getString(R.string.eanverze_soubor));
            InputStreamReader inputStreamReader = new InputStreamReader(inStream, "UTF_16");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            jsonFromFile = bufferedReader.readLine();

            JSONObject jObject = new JSONObject(jsonFromFile);



            // --- DEBUG ONLY --- POUZE KVULI LADENI ---
            // return "18.10.2020";
            return jObject.getString("datum");
        }
        catch (Exception e) {
            Log.d("JSON parser", String.format("Chyba během zjišťování verze dat v aplikaci.\n\n%s", e.getMessage()));
        }
        return "";
    }



/*
    // pocet dni od posledniho downloadu
    private static long daysFromLastDownload (Context cont) {

        // zjistit, jak je to dlouho od posledniho stazeni dat
        String verzeDat = getLastDataDownload(cont);
        if (verzeDat.isEmpty())
            return -1000;
        try {
            Date versionDate = new SimpleDateFormat("d.M.yyyy").parse(verzeDat);
            Date currentDate = todayDate;
            long diffMs = currentDate.getTime() - versionDate.getTime();
            return diffMs / (1000 * 60 * 60 * 24);
        }
        catch (Exception e) {
            Log.d("Verze dat (days)", String.format("Chyba během zjišťování verze dat.\n\n%s", e.getMessage()));
            return -1000;
        }
    }
*/

    /*
        // spocita, jestli je cas zobrazit upozorneni
        // upozornuje se prvni mesic 1x do tydne
        // dalsi mesice 1x za mesic
        private static boolean isMessageTime(Context cont, long daysExpired) {

            long daysFromLastNotifications = daysFromLastNotification(cont, SettingsHelper.Preference.DATA_LASTNOTIFICATION);
            if (daysExpired < 31 && daysFromLastNotifications>6) {
                return true;
            }
            else if (daysExpired > 30 && daysFromLastNotifications>30) {
                return true;
            }
            return false;
        }
    */

/*
    // Zjisti z datoveho souboru verzi dat (resp. datum, verzujeme datumem)
    private static String getLastDataDownload (Context cont) {
        try {
            return SettingsHelper.getSettingString(cont, SettingsHelper.Preference.DATA_LASTDOWNLOAD);
        }
        catch (Exception e) {
            Log.d("Preferences", String.format("Chyba během zjišťování data posledniho stazeni dat.\n\n%s", e.getMessage()));
        }
        return "";
    }
*/

    /*
    public static void checkForDataActualization(Context cont)   :

        try {
            long dataAge = daysFromLastDownload(cont);
            if (dataAge < 0)
                dataAge = daysFromDatafile(cont, verzeDatApp);

            if (notifikaceSettings == 1 && dataAge < 31) return;

            if (notifikaceSettings == 2 && dataAge < 92) return;

            long daysExpired = 0;
            if (notifikaceSettings == 1)
                daysExpired = dataAge - 31;
            else if (notifikaceSettings == 2)
                daysExpired = dataAge - 92;

            if (!isMessageTime(cont, daysExpired))
                return;

        }
        catch (Exception e) {
            Log.d("Verze dat", String.format("Chyba během ověřování verze dat.\n\n%s", e.getMessage()));
            return;
        }

        String msg = String.format("Aplikace obsahuje vnitřní data o zboží. Tato data jsou z %s.\n", verzeDatApp);
        msg = msg.concat("Udělejte si prosím chvilku na jejich aktualizaci, aby aplikace měla opravdu ta nejnovější.\n\n");
        msg = msg.concat("Aktualizaci najdete v menu, v pravém horním rohu, pod položkou Aktualizace dat.");

        // vytvorime instanci tridy AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setTitle("Aktualizace dat");
        builder.setMessage(msg);
        builder.setPositiveButton("Zavřít", null);
        builder.create().show();

        // zapsat do registru datum posledni notifikace
        SettingsHelper.setSettingStringDate(cont, SettingsHelper.Preference.DATA_LASTNOTIFICATION, todayDate);

 */

}
