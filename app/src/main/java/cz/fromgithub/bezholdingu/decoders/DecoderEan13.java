package cz.fromgithub.bezholdingu;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.fromgithub.bezholdingu.helpers.DecoderHelper;
import cz.fromgithub.bezholdingu.helpers.FirmaData;

public class DecoderEan13 {

    private static List<FirmaData> seznamFirem13;

    // nacteni dat o firmach do seznamu
    // musi se zavolat pred prvnim hledanim vyrobce
    public static void init(Context cont, boolean resetData) {
        if (resetData)
            seznamFirem13 = null;

        if (seznamFirem13 == null) {
            DecoderEan13 de13 = new DecoderEan13();
            seznamFirem13 = de13.loadEans13(cont);
        }
    }

    // najde odpovidajici EAN13 a vrati data o firme
    public static FirmaData findByCode(Context cont, String barcode) {
        FirmaData nalezenaFirma = null;

        for (FirmaData firma : seznamFirem13) {
            if (DecoderHelper.compLeft(barcode, firma.kod)) {
                nalezenaFirma = new FirmaData(firma);
                ZemeData zemeData = DecoderCountry.getCompanyCountry(nalezenaFirma.kod);
                if (firma.retezec != DecoderHelper.Retezec.RETEZEC)
                    nalezenaFirma.nazev = nalezenaFirma.nazev.concat(", ").concat(zemeData.nazev);
                break;
            }
        }
        return nalezenaFirma;
    }

    // najde firmu podle nazvu a vrati jeji data
    public static FirmaData findByName(Context cont, String name) {

        for (FirmaData firma : seznamFirem13) {
            if (DecoderHelper.normalizeString(firma.nazev).startsWith(name)) {
                FirmaData found = new FirmaData(firma);
                if (firma.retezec != DecoderHelper.Retezec.RETEZEC) {
                    ZemeData zemeData = DecoderCountry.getCompanyCountry(firma.kod);
                    found.nazev = found.nazev.concat(", ").concat(zemeData.nazev);
                }
                return found;
            }
        }
        return null;
    }



    private List<FirmaData> loadEans13 (Context cont) {

        // nacist data ze souboru do promenne tridy typu List<FirmaData>
        List<FirmaData> seznamFirem = new ArrayList<FirmaData>();
        try {
            // firmy z holdingu
            JSONObject jObject = new JSONObject(DecoderHelper.getJsonFromFile(cont, cont.getString(R.string.ean13agro_soubor)));
            JSONArray firmy = jObject.getJSONArray("firmy");
            for(int i = 0; i < firmy.length(); i++) {
                JSONObject c = firmy.getJSONObject(i);
                if (c.isNull("nazev") || c.isNull("kod"))
                    continue;
                String nazev = c.getString("nazev");
                String kod = c.getString("kod");
                DecoderHelper.Kategorie holding = DecoderHelper.Kategorie.HOLDING;
                DecoderHelper.Retezec retezec = (!c.isNull("retezec") && c.getInt("retezec") > 0) ? DecoderHelper.Retezec.RETEZEC : DecoderHelper.Retezec.NENIRETEZEC;
                String pozn = c.isNull("pozn") ? null : c.getString("pozn");
                seznamFirem.add(new FirmaData(nazev, kod, holding, retezec, pozn));
            }

            // firmy mimo holding
            jObject = new JSONObject(DecoderHelper.getJsonFromFile(cont, cont.getString(R.string.ean13neagro_soubor)));
            firmy = jObject.getJSONArray("firmy");
            for(int i = 0; i < firmy.length(); i++) {
                JSONObject c = firmy.getJSONObject(i);
                if (c.isNull("nazev") || c.isNull("kod"))
                    continue;
                String nazev = c.getString("nazev");
                String kod = c.getString("kod");
                String struktura = c.isNull("struktura") ? "" : c.getString("struktura").toLowerCase();
                DecoderHelper.Kategorie holding = struktura.equals("toman") ? DecoderHelper.Kategorie.TOMAN:DecoderHelper.Kategorie.MIMOHOLDING;
                DecoderHelper.Retezec retezec = (!c.isNull("retezec") && c.getInt("retezec") > 0) ? DecoderHelper.Retezec.RETEZEC : DecoderHelper.Retezec.NENIRETEZEC;
                String pozn = c.isNull("pozn") ? null : c.getString("pozn");
                seznamFirem.add(new FirmaData(nazev, kod, holding, retezec, pozn));
            }
        }
        catch (Exception ex) {
            Log.e("test", "Unable to parse JSON.", ex);
        }
        return seznamFirem;
    }

}
