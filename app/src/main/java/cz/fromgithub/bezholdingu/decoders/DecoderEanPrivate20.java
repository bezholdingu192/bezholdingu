package cz.fromgithub.bezholdingu;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import cz.fromgithub.bezholdingu.helpers.DecoderHelper;
import cz.fromgithub.bezholdingu.helpers.FirmaData;

public class DecoderEanPrivate20 {

    private static List<FirmaData> privatniZnacka20;

    // vynuluje seznamy dat, znovu se nactou pri nasledujicim pouziti
    public static void init(Context cont, boolean resetData) {
        if (resetData)
            privatniZnacka20 = null;

        // neinicializuje se, privatni znacky se plni az pri prvnim pouziti
    }

    // najde odpovidajici EAN a vrati data o firme
    public static FirmaData findByCode(Context cont, String barcode) {
        return getPrivatniZnacka20(cont, barcode, "");
    }

    // najde odpovidajici EAN a vrati data o firme
    public static FirmaData findByName(Context cont, String name) {
        return getPrivatniZnacka20(cont,"", name);
    }



    private static FirmaData getPrivatniZnacka20(Context cont, String barcode, String companyName) {

        if (privatniZnacka20 == null)
        {
            privatniZnacka20 = loadPrivateLabels20(cont, "Albert");
            privatniZnacka20.addAll(loadPrivateLabels20(cont, "Globus"));
            privatniZnacka20.addAll(loadPrivateLabels20(cont, "Lidl"));
            privatniZnacka20.addAll(loadPrivateLabels20(cont, "Norma"));
            // privatniZnacka20.addAll(eanPriv.loadPrivateLabels20(cont, "Penny"));
        }

        FirmaData nalezenaFirma = null;

        if (!barcode.isEmpty()) {
            for (FirmaData firma : privatniZnacka20) {
                if (DecoderHelper.compLeft(barcode, firma.kod)) {

                    if (nalezenaFirma == null) {
                        nalezenaFirma = new FirmaData(firma);
                        nalezenaFirma.pozn = firma.pozn.concat(", ").concat(nalezenaFirma.nazev);
                    }
                    else  {
                        nalezenaFirma.pozn = nalezenaFirma.pozn.concat("\n").concat(firma.pozn).concat(", ").concat(nalezenaFirma.nazev);
                        nalezenaFirma.holding = nalezenaFirma.holding.equals(firma.holding) ? firma.holding : DecoderHelper.Kategorie.NEJASNE;
                    }
                    nalezenaFirma.pozn = nalezenaFirma.pozn.concat(firma.holding == DecoderHelper.Kategorie.HOLDING ? " (AGROFERT)" : "");
                    nalezenaFirma.pozn = nalezenaFirma.pozn.concat(firma.holding == DecoderHelper.Kategorie.TOMAN ? " (Politik TOMAN)" : "");
                    nalezenaFirma.retezec = DecoderHelper.Retezec.VICEDODAVATELU;
                }
            }
        }
        else if (!companyName.isEmpty()) {
            for (FirmaData firma : privatniZnacka20) {
                if (DecoderHelper.normalizeString(firma.nazev).startsWith(companyName))
                    return new FirmaData(firma);
            }
        }
        return nalezenaFirma;
    }

    // type20 - pouze typy kodu zacinajici 20 az 29
    private static List<FirmaData> loadPrivateLabels20 (Context cont, String label) {

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
                if (kod.charAt(0)!='2')  // pouze kody 2xxxx..
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


                String pozn = label.concat(": ").concat(c.isNull("produkt") ? "" : c.getString("produkt"));
                seznamFirem.add(new FirmaData(nazev, kod, holding, DecoderHelper.Retezec.NENIRETEZEC, pozn));
            }
        }
        catch (Exception ex) {
            Log.e("test", "Unable to parse JSON.", ex);
        }
        return seznamFirem;
    }
}

