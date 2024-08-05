package cz.fromgithub.bezholdingu;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.fromgithub.bezholdingu.helpers.DecoderHelper;
import cz.fromgithub.bezholdingu.helpers.FirmaData;
public class DecoderEanWeight {

    private static List<FirmaData> seznamVahovychKodu;

    // vynuluje seznamy dat, znovu se nactou pri nasledujicim pouziti
    public static void init(Context cont, boolean resetData) {
        if (resetData)
            seznamVahovychKodu = null;

        // neinicializuje se, privatni znacky se plni az pri prvnim pouziti
    }

    // najde odpovidajici vahovy EAN13 a vrati data o firme
    public static FirmaData findByCode(Context cont, String barcode) {

        if (seznamVahovychKodu == null)
            seznamVahovychKodu = loadEans(cont);

        FirmaData nalezenaFirma = null;

        for (FirmaData firma : seznamVahovychKodu) {
            if (DecoderHelper.compLeft(barcode, firma.kod)) {

                if (nalezenaFirma == null) {
                    nalezenaFirma = new FirmaData(firma);
                    nalezenaFirma.pozn = getFormatedPozn(barcode, firma.pozn, firma.nazev);
                }
                else  {
                    nalezenaFirma.pozn = nalezenaFirma.pozn.concat("\n").concat(getFormatedPozn(barcode, firma.pozn, firma.nazev));
                    nalezenaFirma.holding = nalezenaFirma.holding.equals(firma.holding) ? firma.holding : DecoderHelper.Kategorie.NEJASNE;
                }
                nalezenaFirma.pozn = nalezenaFirma.pozn.concat(firma.holding == DecoderHelper.Kategorie.HOLDING ? " (AGROFERT)" : "");
                nalezenaFirma.retezec = DecoderHelper.Retezec.VAHOVYKOD;
            }
        }
        return nalezenaFirma;
    }

    // sestavi poznamku z nazvu firmy a vyrobku
    private static String getFormatedPozn (String kod, String vyrobek, String firma) {
        vyrobek = vyrobek.replace("{A}", "Albert: ");
        vyrobek = vyrobek.replace("{B}", "Billa: ");
        vyrobek = vyrobek.replace("{G}", "Globus: ");
        vyrobek = vyrobek.replace("{K}", "Kaufland: ");
        vyrobek = vyrobek.replace("{L}", "Lidl: ");
        vyrobek = vyrobek.replace("{M}", "Makro: ");
        vyrobek = vyrobek.replace("{N}", "Norma: ");
        vyrobek = vyrobek.replace("{P}", "Penny: ");
        vyrobek = vyrobek.replace("{T}", "Tesco: ");
        vyrobek = vyrobek.concat(getPriceFromBarcode(kod)).concat(", ").concat(firma);
        return vyrobek;
    }

    // ziskani hmotnosti z vahoveho caroveho kodu
    private static String getPriceFromBarcode (String kod) {
        if (kod.length()<12)
            return "";

        try {
            String sWeight = kod.substring(7, 12);

            Double iWeight = Double.parseDouble(sWeight);
            // prilis mala hmotnost je v gramech, jinak kg
            if (iWeight < 100) {
                return String.format(", %.0fg", iWeight);
            } else {
                return String.format(", %.3fkg", iWeight / 1000);
            }
        }
        catch (Exception ex) {
            return "";
        }
    }

    private static List<FirmaData> loadEans (Context cont) {

        // nacist data ze souboru do promenne tridy typu List<FirmaData>
        List<FirmaData> seznamFirem = new ArrayList<FirmaData>();
        try {
            JSONObject jObject = new JSONObject(DecoderHelper.getJsonFromFile(cont, cont.getString(R.string.vahoveKody_soubor)));
            JSONArray firmy = jObject.getJSONArray("firmy");
            for(int i = 0; i < firmy.length(); i++) {
                JSONObject c = firmy.getJSONObject(i);
                if (c.isNull("vyrobce") || c.isNull("kod"))
                    continue;
                String kod = c.getString("kod");
                String nazev = c.getString("vyrobce");
                DecoderHelper.Kategorie holding = !c.isNull("holding") && c.getInt("holding") > 0 ? DecoderHelper.Kategorie.HOLDING : DecoderHelper.Kategorie.MIMOHOLDING;
                String pozn = c.isNull("produkt") ? "" : c.getString("produkt");
                seznamFirem.add(new FirmaData(nazev, kod, holding, DecoderHelper.Retezec.NENIRETEZEC, pozn));
            }
        }
        catch (Exception ex) {
            Log.e("test", "Unable to parse JSON.", ex);
        }
        return seznamFirem;
    }
}

