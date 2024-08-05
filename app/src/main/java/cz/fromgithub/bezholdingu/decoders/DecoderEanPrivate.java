package cz.fromgithub.bezholdingu;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.fromgithub.bezholdingu.helpers.DecoderHelper;
import cz.fromgithub.bezholdingu.helpers.FirmaData;

public class DecoderEanPrivate {

    private static List<FirmaData> privatniZnackaAlbert;
    private static List<FirmaData> privatniZnackaBilla;
    private static List<FirmaData> privatniZnackaGlobus;
    private static List<FirmaData> privatniZnackaKaufland;
    private static List<FirmaData> privatniZnackaLidl;
    private static List<FirmaData> privatniZnackaNorma;
    private static List<FirmaData> privatniZnackaPenny;
    private static List<FirmaData> privatniZnackaTesco;

    // vynuluje seznamy dat, znovu se nactou pri nasledujicim pouziti
    public static void init(Context cont, boolean resetData) {
        if (resetData) {
            privatniZnackaAlbert = null;
            privatniZnackaBilla = null;
            privatniZnackaGlobus = null;
            privatniZnackaKaufland = null;
            privatniZnackaLidl = null;
            privatniZnackaNorma = null;
            privatniZnackaPenny = null;
            privatniZnackaTesco = null;
        }

        // neinicializuje se, privatni znacky se plni az pri prvnim pouziti
    }

    // najde odpovidajici EAN a vrati data o firme
    public static FirmaData findByCode(Context cont, String barcode) {

        FirmaData privTmp = getPrivatniZnacka(cont, "Albert", barcode, "");
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Billa", barcode, ""); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Globus", barcode, ""); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Kaufland", barcode, ""); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Lidl", barcode, ""); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Norma", barcode, ""); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Penny", barcode, ""); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Tesco", barcode, ""); }

        return privTmp;
    }

    public static FirmaData findByName(Context cont, String name) {

        FirmaData privTmp = getPrivatniZnacka(cont, "Albert", "", name);
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Billa", "", name); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Globus", "", name); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Kaufland", "", name); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Lidl", "", name); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Norma", "", name); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Penny", "", name); }
        if (privTmp == null) { privTmp = getPrivatniZnacka(cont, "Tesco", "", name); }

        return privTmp;
    }


    private List<FirmaData> loadPrivateLabels (Context cont, String label) {

        // nacist data ze souboru do promenne tridy typu List<FirmaData>
        List<FirmaData> seznamFirem = new ArrayList<FirmaData>();
        try {
            // firmy z holdingu
            JSONObject jObject = new JSONObject(DecoderHelper.getJsonFromFile(cont, cont.getString(R.string.privatelabel_soubor).replace("{0}",label)));
            JSONArray firmy = jObject.getJSONArray("firmy");
            for(int i = 0; i < firmy.length(); i++) {
                JSONObject c = firmy.getJSONObject(i);
                if (c.isNull("vyrobce") || c.isNull("kod"))
                    continue;
                String kod = c.getString("kod");
                if (kod.charAt(0)=='2')  // pouze kody ktere nejsou 2xxxx..
                    continue;
                String nazev = c.getString("vyrobce");

                // DecoderHelper.Kategorie holding = !c.isNull("holding") && c.getInt("holding") > 0 ? DecoderHelper.Kategorie.HOLDING : DecoderHelper.Kategorie.MIMOHOLDING;
                DecoderHelper.Kategorie holding = DecoderHelper.Kategorie.MIMOHOLDING;
                if(!c.isNull("holding")){
                    if (c.getInt("holding") == 1)
                        holding = DecoderHelper.Kategorie.HOLDING;
                    else if (c.getInt("holding") == 2)
                        holding = DecoderHelper.Kategorie.TOMAN;
                }

                String pozn = c.isNull("produkt") ? null : c.getString("produkt");
                seznamFirem.add(new FirmaData(nazev, kod, holding, DecoderHelper.Retezec.NENIRETEZEC, pozn));
            }
        }
        catch (Exception ex) {
            Log.e("test", "Unable to parse JSON.", ex);
        }
        return seznamFirem;
    }

    private static FirmaData getPrivatniZnacka(Context cont, String znacka, String barcode, String companyName) {
        List<FirmaData> privatniZnacka = null;

        DecoderEanPrivate eanPriv = new DecoderEanPrivate();

        if (znacka.contains("Albert") ) {
            if (privatniZnackaAlbert == null)
                privatniZnackaAlbert = eanPriv.loadPrivateLabels(cont, "Albert");
            privatniZnacka = privatniZnackaAlbert;
        }
        else if (znacka.contains("Billa")) {
            if (privatniZnackaBilla == null)
                privatniZnackaBilla = eanPriv.loadPrivateLabels(cont, "Billa");
            privatniZnacka = privatniZnackaBilla;
        }
        else if (znacka.contains("Globus")) {
            if (privatniZnackaGlobus == null)
                privatniZnackaGlobus = eanPriv.loadPrivateLabels(cont, "Globus");
            privatniZnacka = privatniZnackaGlobus;
        }
        else if (znacka.contains("Kaufland")) {
            if (privatniZnackaKaufland == null)
                privatniZnackaKaufland = eanPriv.loadPrivateLabels(cont, "Kaufland");
            privatniZnacka = privatniZnackaKaufland;
        }
        else if (znacka.contains("Lidl")) {
            if (privatniZnackaLidl == null)
                privatniZnackaLidl = eanPriv.loadPrivateLabels(cont, "Lidl");
            privatniZnacka = privatniZnackaLidl;
        }
        else if (znacka.contains("Norma")) {
            if (privatniZnackaNorma == null)
                privatniZnackaNorma = eanPriv.loadPrivateLabels(cont, "Norma");
            privatniZnacka = privatniZnackaNorma;
        }
        else if (znacka.contains("Penny")) {
            if (privatniZnackaPenny == null)
                privatniZnackaPenny = eanPriv.loadPrivateLabels(cont, "Penny");
            privatniZnacka = privatniZnackaPenny;
        }
        else if (znacka.contains("Tesco")) {
            if (privatniZnackaTesco == null)
                privatniZnackaTesco = eanPriv.loadPrivateLabels(cont, "Tesco");
            privatniZnacka = privatniZnackaTesco;
        }
        else {
            return null;
        }

        if (!barcode.isEmpty()) {
            final String strViceDodavatelu = "Více možných dodavatelů";
            FirmaData nalezenaFirma = null;
            for (FirmaData firma : privatniZnacka) {
                if (DecoderHelper.compLeft(barcode, firma.kod)) {
                    if (nalezenaFirma == null) { // prvni nalezeny dodavatel
                        nalezenaFirma = new FirmaData(firma);
                    }
                    else if (!nalezenaFirma.nazev.equals(strViceDodavatelu)) {  // druhy davatel pod stejnym kodem
                        nalezenaFirma.pozn = nalezenaFirma.nazev.concat(nalezenaFirma.holding == DecoderHelper.Kategorie.HOLDING ? " (AGROFERT)" : "").concat(" - ").concat(nalezenaFirma.pozn).concat("\n");
                        nalezenaFirma.pozn = nalezenaFirma.pozn.concat(firma.nazev).concat(firma.holding == DecoderHelper.Kategorie.HOLDING ? " (AGROFERT)" : "").concat(" - ").concat(firma.pozn);
                        nalezenaFirma.nazev = strViceDodavatelu;
                        nalezenaFirma.holding = nalezenaFirma.holding.equals(firma.holding) ? firma.holding : DecoderHelper.Kategorie.NEJASNE;
                    } else {  // treti+ davatel pod stejnym kodem
                        nalezenaFirma.pozn = nalezenaFirma.pozn.concat("\n").concat(firma.nazev).concat(firma.holding == DecoderHelper.Kategorie.HOLDING ? " (AGROFERT)" : "").concat(" - ").concat(firma.pozn);
                        nalezenaFirma.holding = nalezenaFirma.holding.equals(firma.holding) ? firma.holding : DecoderHelper.Kategorie.NEJASNE;
                    }
                }
            }
            return nalezenaFirma;
        }
        else if (!companyName.isEmpty()) {
            for (FirmaData firma : privatniZnacka) {
                if (DecoderHelper.normalizeString(firma.nazev).startsWith(companyName))
                    return new FirmaData(firma);
            }
        }
        return null;
    }
}
