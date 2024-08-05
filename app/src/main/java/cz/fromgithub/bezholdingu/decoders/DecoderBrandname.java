package cz.fromgithub.bezholdingu;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cz.fromgithub.bezholdingu.helpers.DecoderHelper;
import cz.fromgithub.bezholdingu.helpers.FirmaData;

public class DecoderBrandname {

    List<FirmaData> seznamZnacek;

    public ResultData getResultData(Context cont, String brandname) {

        // normalizace nazvu (odstraneni diakritiky, dvojtych mezser, atd.)
        brandname=DecoderHelper.normalizeString(brandname.replace("  ", " "));

        if (seznamZnacek == null)
            seznamZnacek = loadBrands(cont);

        // hledani v lokalnim seznamu znacek
        ResultData found = searchInBrands(cont, seznamZnacek, brandname);
        DecoderHelper.Kategorie kategorieFirmy = DecoderHelper.Kategorie.NEJASNE;
        if(found != null) {
            if (found.vysledek == DecoderHelper.Kategorie.HOLDING) {
                found.nazev = "AGROFERT - ".concat(found.nazev);
            }
            return found;
        }

        // hledani v seznamu firem EAN13, EAN8
        if (found == null) {
            DecoderEan decoder = new DecoderEan();
            found = decoder.getResultDataByName(cont, brandname, false);
            if(found != null)
                return found;
        }

        // hledani v produktovych kodech (znacka v ovalu)
        if (found == null) {
            DecoderProducercode decoder = new DecoderProducercode();
            found = decoder.getProducerNameByName(cont, brandname);
            if(found != null)
                return found;
        }

        // hledani v privatnich znackach
        if (found == null) {
            DecoderEan decoder = new DecoderEan();
            found = decoder.getResultDataByName(cont, brandname, true);
            if(found != null)
                return found;
        }

        return new ResultData(brandname, "výrobce se nepodařilo dohledat", "", DecoderHelper.Kategorie.NENALEZENO);
    }

    private ResultData searchInBrands(Context cont, List<FirmaData> seznamZnacek, String hledanaZnacka) {
        for (FirmaData znacka : seznamZnacek) {
            if (DecoderHelper.normalizeString(znacka.nazev).startsWith(hledanaZnacka))
                return new ResultData(hledanaZnacka, znacka.nazev, znacka.pozn, znacka.holding);
        }
        return null;
    }

    private List<FirmaData> loadBrands (Context cont) {

        // nacist data ze souboru do promenne tridy typu List<FirmaData>
        List<FirmaData> seznamZnacekSoubor = new ArrayList<FirmaData>();
        try {
            // firmy z holdingu
            JSONObject jObject = new JSONObject(getJsonFromFile(cont, cont.getString(R.string.seznamZnacek_soubor)));
            JSONArray firmy = jObject.getJSONArray("znacky");
            for(int i = 0; i < firmy.length(); i++) {
                JSONObject c = firmy.getJSONObject(i);
                if (c.isNull("znacka"))
                    continue;
                String nazev = c.getString("znacka");
                DecoderHelper.Kategorie holding = !c.isNull("noholding") && c.getInt("noholding") > 0 ? DecoderHelper.Kategorie.MIMOHOLDING : DecoderHelper.Kategorie.NEJASNE;
                holding = !c.isNull("holding") && c.getInt("holding") > 0 ? DecoderHelper.Kategorie.HOLDING : holding;
                String pozn = c.isNull("popis") ? null : c.getString("popis");
                seznamZnacekSoubor.add(new FirmaData(nazev, "", holding, null, pozn));
            }
        }
        catch (Exception ex) {
            Log.e("test", "Unable to parse JSON.", ex);
        }
        // znacka, vztahujici se k aplikaci
        seznamZnacekSoubor.add(new FirmaData("Bez Holdingu", "", DecoderHelper.Kategorie.MIMOHOLDING, null, "Aplikace, na kterou se právě díváte."));
        return seznamZnacekSoubor;
    }

    // nacte ze souboru JSON data
    private static String getJsonFromFile (Context cont, String fileName) {

        try {
            // otevreme soubor pro cteni
            FileInputStream inStream = cont.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inStream, "UTF_16");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.readLine();
        }
        catch (IOException e) {
            return null;
        }
    }
/*
    private List<FirmaData> getOwnBrands(Context cont) {
        List<FirmaData> ownData = new List<FirmaData>;
        ownData.add(new FirmaData("Bez Holdingu", "", DecoderHelper.Kategorie.MIMOHOLDING, null, "Aplikace, na kterou se právě díváte."));
        ownData.add(new FirmaData("Bez Holdingu", "", DecoderHelper.Kategorie.MIMOHOLDING, null, "Aplikace, na kterou se právě díváte."));
        return ownData;
    }
 */
}