package cz.fromgithub.bezholdingu.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import cz.fromgithub.bezholdingu.R;

public class DatafilesHelper {

    // zkontroluje, jestli existuji datove soubory a pripadne je vytvori z defaultnich
    public static void initDataFiles (Context cont, boolean fastCheck){

        String resPrivateSoubor = cont.getString(R.string.privatelabel_soubor);
        String resPrivateDefault = cont.getString(R.string.privatelabel_default);

        if (fastCheck) {
            File file = new File(cont.getFilesDir().getAbsolutePath() + "/" + R.string.eanverze_soubor);
            if (file.exists())
                return;
        }

        checkDataFiles(cont, cont.getString(R.string.eanverze_soubor), cont.getString(R.string.eanverze_default));
        checkDataFiles(cont, cont.getString(R.string.ean13agro_soubor), cont.getString(R.string.ean13agro_default));
        checkDataFiles(cont, cont.getString(R.string.ean13neagro_soubor), cont.getString(R.string.ean13neagro_default));
        checkDataFiles(cont, cont.getString(R.string.ean8agro_soubor), cont.getString(R.string.ean8agro_default));
        checkDataFiles(cont, resPrivateSoubor.replace("{0}", "Albert"), resPrivateDefault.replace("{0}", "Albert"));
        checkDataFiles(cont, resPrivateSoubor.replace("{0}", "Billa"), resPrivateDefault.replace("{0}", "Billa"));
        checkDataFiles(cont, resPrivateSoubor.replace("{0}", "Globus"), resPrivateDefault.replace("{0}", "Globus"));
        checkDataFiles(cont, resPrivateSoubor.replace("{0}", "Kaufland"), resPrivateDefault.replace("{0}", "Kaufland"));
        checkDataFiles(cont, resPrivateSoubor.replace("{0}", "Lidl"), resPrivateDefault.replace("{0}", "Lidl"));
        checkDataFiles(cont, resPrivateSoubor.replace("{0}", "Norma"), resPrivateDefault.replace("{0}", "Norma"));
        checkDataFiles(cont, resPrivateSoubor.replace("{0}", "Penny"), resPrivateDefault.replace("{0}", "Penny"));
        checkDataFiles(cont, resPrivateSoubor.replace("{0}", "Tesco"), resPrivateDefault.replace("{0}", "Tesco"));
        checkDataFiles(cont, cont.getString(R.string.vahoveKody_soubor), cont.getString(R.string.vahoveKody_default));
        checkDataFiles(cont, cont.getString(R.string.seznamZnacek_soubor), cont.getString(R.string.seznamZnacek_default));
    }

    // prepise datove soubory defaultnima souborama
    public static boolean replaceDataFromDefault (Context cont) {
        String resPrivateDefault = cont.getString(R.string.privatelabel_default);
        String resPrivateSoubor = cont.getString(R.string.privatelabel_soubor);
        boolean result = copyFile (cont, cont.getString(R.string.eanverze_default), cont.getString(R.string.eanverze_soubor));
        result |= copyFile (cont, cont.getString(R.string.ean13agro_default), cont.getString(R.string.ean13agro_soubor));
        result |= copyFile (cont, cont.getString(R.string.ean13neagro_default), cont.getString(R.string.ean13neagro_soubor));
        result |= copyFile (cont, cont.getString(R.string.ean8agro_default), cont.getString(R.string.ean8agro_soubor));
        result |= copyFile (cont, resPrivateDefault.replace("{0}", "Albert"), resPrivateSoubor.replace("{0}", "Albert"));
        result |= copyFile (cont, resPrivateDefault.replace("{0}", "Billa"), resPrivateSoubor.replace("{0}", "Billa"));
        result |= copyFile (cont, resPrivateDefault.replace("{0}", "Globus"), resPrivateSoubor.replace("{0}", "Globus"));
        result |= copyFile (cont, resPrivateDefault.replace("{0}", "Kaufland"), resPrivateSoubor.replace("{0}", "Kaufland"));
        result |= copyFile (cont, resPrivateDefault.replace("{0}", "Lidl"), resPrivateSoubor.replace("{0}", "Lidl"));
        result |= copyFile (cont, resPrivateDefault.replace("{0}", "Norma"), resPrivateSoubor.replace("{0}", "Norma"));
        result |= copyFile (cont, resPrivateDefault.replace("{0}", "Penny"), resPrivateSoubor.replace("{0}", "Penny"));
        result |= copyFile (cont, resPrivateDefault.replace("{0}", "Tesco"), resPrivateSoubor.replace("{0}", "Tesco"));
        result |= copyFile (cont, cont.getString(R.string.vahoveKody_default), cont.getString(R.string.vahoveKody_soubor));
        result |= copyFile (cont, cont.getString(R.string.seznamZnacek_default), cont.getString(R.string.seznamZnacek_soubor));
        return result;
    }

    // smaze datove soubory
    public static boolean removeDatafiles (Context cont) {
        String resPrivateSoubor = cont.getString(R.string.privatelabel_soubor);
        boolean result = removeFile(cont, cont.getString(R.string.eanverze_soubor));
        result |= removeFile(cont, cont.getString(R.string.ean13agro_soubor));
        result |= removeFile(cont, cont.getString(R.string.ean13neagro_soubor));
        result |= removeFile(cont, cont.getString(R.string.ean8agro_soubor));
        result |= removeFile(cont, resPrivateSoubor.replace("{0}", "Albert"));
        result |= removeFile(cont, resPrivateSoubor.replace("{0}", "Billa"));
        result |= removeFile(cont, resPrivateSoubor.replace("{0}", "Globus"));
        result |= removeFile(cont, resPrivateSoubor.replace("{0}", "Kaufland"));
        result |= removeFile(cont, resPrivateSoubor.replace("{0}", "Lidl"));
        result |= removeFile(cont, resPrivateSoubor.replace("{0}", "Norma"));
        result |= removeFile(cont, resPrivateSoubor.replace("{0}", "Penny"));
        result |= removeFile(cont, resPrivateSoubor.replace("{0}", "Tesco"));
        result |= removeFile(cont, cont.getString(R.string.vahoveKody_soubor));
        result |= removeFile(cont, cont.getString(R.string.seznamZnacek_soubor));
        return result;
    }

    // otestuje jestli jsou v pameti soubory s daty a pripadne je vytvori
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static void checkDataFiles (Context cont, String filename, String defaultData) {
        // test jestli existuje soubor
        File file = new File(cont.getFilesDir().getAbsolutePath() + "/" + filename);
        if (!file.exists()) {
            // neexistuje, vytvorime z default souboru..
            try {
                InputStream in = cont.getAssets().open(defaultData);
                try {
                    FileOutputStream out = cont.openFileOutput(filename, Context.MODE_PRIVATE);
                    try {
                        String nactenaData ="";
                        BufferedReader buff = new BufferedReader(new InputStreamReader(in));
                        String radka = "";
                        do {
                            nactenaData = nactenaData.concat(radka);
                            radka = buff.readLine();
                        } while (radka != null);
                        out.write(nactenaData.getBytes("UTF_16"));
                    } finally {
                        out.close();
                    }
                } finally {
                    in.close();
                }
            } catch (Exception e) {
                Toast.makeText(cont, String.format("Nedaří se nakopírovat data do interního úložiště.\n%s", e.getMessage()), Toast.LENGTH_LONG).show();
            }
        }
    }

    // zkopiruje soubor (resp jeho obsah)
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private static boolean copyFile (Context cont, String fileFrom, String fileTo) {
        try {
            InputStream in = cont.getAssets().open(fileFrom);
            try {
                FileOutputStream out = cont.openFileOutput(fileTo, Context.MODE_PRIVATE);
                try {
                    String nactenaData ="";
                    BufferedReader buff = new BufferedReader(new InputStreamReader(in));
                    String radka = "";
                    do {
                        nactenaData=nactenaData.concat(radka);
                        radka = buff.readLine();
                    } while (radka != null);

                    out.write(nactenaData.getBytes("UTF_16"));
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (Exception e) {
            Toast.makeText(cont, String.format("Nedaří se nakopírovat data do interního úložiště.\n\n%s", e.getMessage()), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // smaze datovy soubor
    private static boolean removeFile(Context cont, String filename) {
        try {
            File file = new File(cont.getFilesDir().getAbsolutePath() + "/" + filename);
            return file.delete();
        } catch (Exception e) {
            return false;
        }
    }

    // Zjisti z datoveho souboru verzi dat
    public static String getDateVersionApp (Context cont) {
        String jsonFromFile = "";
        try {
            // otevreme soubor pro cteni
            FileInputStream inStream = cont.openFileInput(cont.getString(R.string.eanverze_soubor));
            InputStreamReader inputStreamReader = new InputStreamReader(inStream, "UTF_16");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            jsonFromFile = bufferedReader.readLine();

            JSONObject jObject = new JSONObject(jsonFromFile);
            return jObject.getString("datum");
        }
        catch (Exception e) {
            Log.d("JSON parser", String.format("Chyba během zjišťování verze dat v souborech.\n\n%s", e.getMessage()));
            return "";
        }
    }



}
