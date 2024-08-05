package cz.fromgithub.bezholdingu.helpers;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;

public class DecoderHelper {

    // nacte ze souboru JSON data
    public static String getJsonFromFile (Context cont, String fileName) {

        try {
            // otevreme soubor pro cteni
            FileInputStream inStream = cont.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inStream, "UTF_16");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // soubor postupne, po radcich, nacteme
            // String line;

            return bufferedReader.readLine();

            /*
            while ((line = bufferedReader.readLine()) != null) {
                // sb.append(line);
                lstStanice.add(line);
            }
            */
        }
        catch (IOException e) {
            // zobrazi text, v pripade chyby
            String textChyby = String.format("Nepodařilo se načíst soubor z interního úložiště&#8230;\\n%1$s", e.getMessage());
            Toast.makeText(cont, textChyby, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    // Compare left - srovna dva stringy, jestli se shoduji od zacatku zleva
    public static boolean compLeft(String s1, String s2) {
        if (s2.length() > s1.length()) return false;

        int pos = s2.length();
        while (pos-- > 0) {
            if (s2.charAt(pos) != s1.charAt(pos)) return false;
        }
        return true;
    }

    // odstrani z textu hacky, carky a z velkych udela mala pismena
    public static String normalizeString(String s) {
        if(s == null || s.trim().length() == 0)
            return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "").trim().toLowerCase();
    }

    public enum Kategorie {
        HOLDING,
        TOMAN,
        RUSKO,
        BELORUSKO,
        MIMOHOLDING,
        MIMOHOLDING_NENALEZENO,
        NEJASNE,
        NENALEZENO
    }

    public enum Retezec {
        NENIRETEZEC,
        RETEZEC,
        DODAVATELRETEZCE,
        VICEDODAVATELU,      // pod stejnym EAN dodava vice dodavatelu (napr EAN-8 zacinajici "20")
        KUSOVYKOD,
        VAHOVYKOD
    }



}


