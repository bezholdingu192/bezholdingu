package cz.fromgithub.bezholdingu;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.fromgithub.bezholdingu.helpers.DecoderHelper;
import cz.fromgithub.bezholdingu.helpers.FirmaData;

public class DecoderEan8 {

    private static List<FirmaData> seznamFirem8;

    // nacteni dat o firmach do seznamu
    // musi se zavolat pred prvnim hledanim vyrobce
    public static void init(Context cont, boolean resetData) {
        if (resetData)
            seznamFirem8 = null;

        if (seznamFirem8 == null) {
            DecoderEan8 de8 = new DecoderEan8();
            seznamFirem8 = de8.loadEans8(cont);
        }
    }

    // najde odpovidajici EAN8 a vrati data o firme
    public static FirmaData findByCode(Context cont, String barcode) {
        FirmaData nalezenaFirma = null;

        for (FirmaData firma : seznamFirem8) {
            if (DecoderHelper.compLeft(barcode, firma.kod)) {
                nalezenaFirma = new FirmaData(firma);
                nalezenaFirma.nazev=nalezenaFirma.nazev.concat(", Česká republika");

                if (DecoderHelper.normalizeString(nalezenaFirma.nazev).startsWith("boneco")) {
                    nalezenaFirma.holding = DecoderHelper.Kategorie.TOMAN;
                }

                break;
            }
        }
        return nalezenaFirma;
    }

    // najde firmu podle nazvu a vrati jeji data
    public static FirmaData findByName(Context cont, String name) {
        for (FirmaData firma : seznamFirem8) {
            if (DecoderHelper.normalizeString(firma.nazev).startsWith(name)) {
                FirmaData found = new FirmaData(firma);
                found.nazev = found.nazev.concat(", Česká republika");

                if (DecoderHelper.normalizeString(found.nazev).startsWith("boneco")) {
                    found.holding = DecoderHelper.Kategorie.TOMAN;
                }

                return found;
            }
        }
        return null;
    }




    // nacist data a rozbalit je do promenne tridy typu List<FirmaData>
    private List<FirmaData> loadEans8 (Context cont) {

        List<FirmaData> seznamFirem = new ArrayList<FirmaData>();
        try {
            // firmy z holdingu
            JSONObject jObject = new JSONObject(DecoderHelper.getJsonFromFile(cont, cont.getString(R.string.ean8agro_soubor)));
            JSONArray firmy = jObject.getJSONArray("firmy");
            for(int i = 0; i < firmy.length(); i++) {
                JSONObject c = firmy.getJSONObject(i);
                if (c.isNull("nazev") || c.isNull("kod"))
                    continue;
                String nazev = c.getString("nazev");
                String kod = c.getString("kod");
                DecoderHelper.Kategorie holding = DecoderHelper.Kategorie.HOLDING;
                String pozn = c.isNull("pozn") ? null : c.getString("pozn");
                int pocet = c.isNull("pocet") ? 1 : c.getInt("pocet");

                // System.out.println(String.format("e8.add(new Ean8Code(%s, \"%s\", %d));", kod, nazev, pocet));

                int iKod = Integer.parseInt(kod);
                for(int j = 0; j < pocet; j++) {
                    String sKod=String.valueOf(iKod+j);
                    seznamFirem.add(new FirmaData(nazev, sKod, holding, DecoderHelper.Retezec.NENIRETEZEC, pozn));
                }
            }

            // firmy mimo holding
            List<Ean8Code> ean8Codes = getEan8Codes();
            for (Ean8Code ean8Code : ean8Codes) {
                int iKod = ean8Code.kod;
                for(int i = 0; i < ean8Code.pocet; i++) {
                    String sKod=String.valueOf(iKod+i);
                    seznamFirem.add(new FirmaData(ean8Code.nazev, sKod, DecoderHelper.Kategorie.MIMOHOLDING, DecoderHelper.Retezec.NENIRETEZEC, ""));
                }
            }
        }
        catch (Exception ex) {
            Log.e("test", "Unable to parse JSON.", ex);
        }
        return seznamFirem;
    }

    private List<Ean8Code> getEan8Codes () {
        List<Ean8Code> e8 = new ArrayList<Ean8Code>();


        // Kody EAN8 se do aplikace nestahuji, generuji se pomoci UnitTestu a vkladaji sem do kodu.
        // Stahuji se pouze kody Ean8 holdingu, podle toho se pozna, ktere do holdingu patri a ktere ne.

e8.add(new Ean8Code(8590001, "UNILEVER ČR", 2));
e8.add(new Ean8Code(8590010, "SPAK Foods", 61));
e8.add(new Ean8Code(8590071, "Orkla Food CZ/SK", 10));
e8.add(new Ean8Code(8590330, "SPAK Foods", 50));
e8.add(new Ean8Code(8590380, "DELIMAX", 50));
e8.add(new Ean8Code(8590434, "UNILEVER ČR", 20));
e8.add(new Ean8Code(8590454, "DELIMAX", 100));
e8.add(new Ean8Code(8590554, "UNILEVER ČR", 5));
e8.add(new Ean8Code(8590567, "A.W.", 10));
e8.add(new Ean8Code(8590587, "NESTLÉ ČESKO", 3));
e8.add(new Ean8Code(8590590, "FRUTANA", 2));
e8.add(new Ean8Code(8590592, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8590593, "Orkla Food CZ/SK", 1));
e8.add(new Ean8Code(8590594, "Kofola CS", 1));
e8.add(new Ean8Code(8590596, "POLABSKÉ MLÉKÁRNY", 2));
e8.add(new Ean8Code(8590598, "ZÁRUBA FOOD", 2));
e8.add(new Ean8Code(8590600, "Choceňská mlékárna", 10));
e8.add(new Ean8Code(8590610, "MEDOKOMERC", 5));
e8.add(new Ean8Code(8590625, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8590626, "Mondelez Europe", 2));
e8.add(new Ean8Code(8590628, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8590629, "Opavia - LU", 2));
e8.add(new Ean8Code(8590631, "Mondelez Europe ", 1));
e8.add(new Ean8Code(8590632, "Opavia - LU", 1));
e8.add(new Ean8Code(8590633, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8590634, "Opavia - LU", 1));
e8.add(new Ean8Code(8590645, "IRBIS", 20));
e8.add(new Ean8Code(8590665, "Opavia - LU", 1));
e8.add(new Ean8Code(8590666, "NESTLÉ ČESKO", 6));
e8.add(new Ean8Code(8590672, "Opavia - LU", 1));
e8.add(new Ean8Code(8590673, "NESTLÉ ČESKO", 7));
e8.add(new Ean8Code(8590680, "Opavia - LU", 3));
e8.add(new Ean8Code(8590683, "NESTLÉ ČESKO", 2));
e8.add(new Ean8Code(8590787, "Procter & Gamble CZ", 20));
e8.add(new Ean8Code(8590802, "JAPEK", 10));
e8.add(new Ean8Code(8590833, "UNILEVER ČR", 1));
e8.add(new Ean8Code(8590854, "MAKO BUTTERFLY", 10));
e8.add(new Ean8Code(8590864, "HELLADA", 5));
e8.add(new Ean8Code(8590873, "Pardubický pivovar", 30));
e8.add(new Ean8Code(8590903, "Topproduct Praha", 7));
e8.add(new Ean8Code(8590910, "KAND", 2));
e8.add(new Ean8Code(8590912, "Opavia - LU", 1));
e8.add(new Ean8Code(8590913, "NESTLÉ ČESKO", 2));
e8.add(new Ean8Code(8590915, "Opavia - LU", 6));
e8.add(new Ean8Code(8590921, "NESTLÉ ČESKO", 19));
e8.add(new Ean8Code(8590940, "Mondelez Europe", 2));
e8.add(new Ean8Code(8590952, "JAPEK", 10));
e8.add(new Ean8Code(8590962, "HELLADA", 5));
e8.add(new Ean8Code(8590977, "Sarantis CZ", 1));
e8.add(new Ean8Code(8590978, "ASTRID COSMETICS", 1));
e8.add(new Ean8Code(8590979, "UNILEVER ČR", 1));
e8.add(new Ean8Code(8590980, "POLABSKÉ MLÉKÁRNY", 2));
e8.add(new Ean8Code(8591000, "BONECO", 2));
e8.add(new Ean8Code(8591010, "TANY", 10));
e8.add(new Ean8Code(8591040, "POLABSKÉ MLÉKÁRNY", 2));
e8.add(new Ean8Code(8591054, "ZÁRUBA FOOD", 4));
e8.add(new Ean8Code(8591058, "Mondelez CZ", 2));
e8.add(new Ean8Code(8591060, "Pivovar Uherský Brod", 1));
e8.add(new Ean8Code(8591061, "POLABSKÉ MLÉKÁRNY", 7));
e8.add(new Ean8Code(8591077, "LIMACO", 8));
e8.add(new Ean8Code(8591085, "Natural Bars", 1));
e8.add(new Ean8Code(8591086, "KAND", 3));
e8.add(new Ean8Code(8591089, "AutoMax Group", 1));
e8.add(new Ean8Code(8591090, "VAVŘINEC TURČÍN", 6));
e8.add(new Ean8Code(8591108, "ALPA", 22));
e8.add(new Ean8Code(8591136, "ING. PAVEL KYTLICA", 3));
e8.add(new Ean8Code(8591139, "LIMACO", 6));
e8.add(new Ean8Code(8591145, "MADETA", 2));
e8.add(new Ean8Code(8591149, "LIMA plus", 1));
e8.add(new Ean8Code(8591150, "Mondelez Europe", 1));
e8.add(new Ean8Code(8591151, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8591152, "Opavia - LU", 2));
e8.add(new Ean8Code(8591154, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8591155, "Opavia - LU", 2));
e8.add(new Ean8Code(8591157, "NESTLÉ ČESKO", 5));
e8.add(new Ean8Code(8591162, "Opavia - LU", 3));
e8.add(new Ean8Code(8591165, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8591166, "Opavia - LU", 6));
e8.add(new Ean8Code(8591172, "NESTLÉ ČESKO", 2));
e8.add(new Ean8Code(8591174, "Opavia - LU", 1));
e8.add(new Ean8Code(8591175, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8591176, "Opavia - LU", 2));
e8.add(new Ean8Code(8591178, "NESTLÉ ČESKO", 2));
e8.add(new Ean8Code(8591180, "VAVŘINEC TURČÍN", 4));
e8.add(new Ean8Code(8591187, "HEMANN", 12));
e8.add(new Ean8Code(8591204, "UNILEVER ČR", 3));
e8.add(new Ean8Code(8591218, "Mlékárna Kunín", 40));
e8.add(new Ean8Code(8591258, "EUROICE", 2));
e8.add(new Ean8Code(8591260, "Poděbradka", 10));
e8.add(new Ean8Code(8591270, "JUWITAL", 2));
e8.add(new Ean8Code(8591272, "Crocodille ČR", 3));
e8.add(new Ean8Code(8591275, "ING. MILOŠ VESELÝ CSC. VINOPA", 8));
e8.add(new Ean8Code(8591283, "RINA EUROPE", 3));
e8.add(new Ean8Code(8591286, "NEKTON - VRŇATA", 10));
e8.add(new Ean8Code(8591350, "BIOPHARM VÚ BIOFARMACIE A VETER. LÉČIV", 1));
e8.add(new Ean8Code(8591351, "TOMIL", 9));
e8.add(new Ean8Code(8591360, "ČEŠKA L.- MORAVIAPRODUKT", 12));
e8.add(new Ean8Code(8591403, "STAROBRNO", 1));
e8.add(new Ean8Code(8591404, "Heineken CZ", 1));
e8.add(new Ean8Code(8591405, "STAROBRNO", 10));
e8.add(new Ean8Code(8591421, "VOLFOVA mladoboleslavská sodovkárna", 15));
e8.add(new Ean8Code(8591439, "AutoMax Group", 1));
e8.add(new Ean8Code(8591440, "TOMIL", 10));
e8.add(new Ean8Code(8591473, "HORÁKOVA benátecká sodovkárna", 20));
e8.add(new Ean8Code(8591495, "GlaxoSmithKline", 10));
e8.add(new Ean8Code(8591505, "Crocodille ČR", 1));
e8.add(new Ean8Code(8591508, "JAPEK", 20));
e8.add(new Ean8Code(8591528, "UNILEVER ČR", 4));
e8.add(new Ean8Code(8591538, "Hamé", 15));
e8.add(new Ean8Code(8591563, "Hamé", 8));
e8.add(new Ean8Code(8591586, "Bidfood Opava", 2));
e8.add(new Ean8Code(8591588, "Crocodille ČR", 1));
e8.add(new Ean8Code(8591593, "Sarantis CZ", 6));
e8.add(new Ean8Code(8591599, "F & N DODAVATELÉ", 1));
e8.add(new Ean8Code(8591600, "JT International", 30));
e8.add(new Ean8Code(8591662, "Jan Becher - Karlovarská Becherovka", 2));
e8.add(new Ean8Code(8591664, "Hamé", 4));
e8.add(new Ean8Code(8591668, "HLUBNA CHEMICKÉ VÝROBNÍ DRUŽSTVO V BRNĚ", 10));
e8.add(new Ean8Code(8591686, "Pivovar Uherský Brod", 4));
e8.add(new Ean8Code(8591690, "JT International", 10));
e8.add(new Ean8Code(8591705, "NESTLÉ ČESKO", 6));
e8.add(new Ean8Code(8591711, "Opavia - LU", 1));
e8.add(new Ean8Code(8591712, "NESTLÉ ČESKO", 2));
e8.add(new Ean8Code(8591714, "Opavia - LU", 5));
e8.add(new Ean8Code(8591719, "Mondelez Europe", 1));
e8.add(new Ean8Code(8591720, "Opavia - LU", 7));
e8.add(new Ean8Code(8591727, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8591728, "Opavia - LU", 1));
e8.add(new Ean8Code(8591729, "NESTLÉ ČESKO", 6));
e8.add(new Ean8Code(8591742, "JAPEK", 20));
e8.add(new Ean8Code(8591762, "Jan Becher - Karlovarská Becherovka", 10));
e8.add(new Ean8Code(8591772, "LIMACO", 12));
e8.add(new Ean8Code(8591784, "FONTEA", 10));
e8.add(new Ean8Code(8591804, "Crocodille ČR", 2));
e8.add(new Ean8Code(8591806, "AVEFLOR", 3));
e8.add(new Ean8Code(8591818, "PIVOVAR JIHLAVA", 17));
e8.add(new Ean8Code(8591836, "UNILEVER ČR", 1));
e8.add(new Ean8Code(8591838, "RYOR", 21));
e8.add(new Ean8Code(8591860, "Palírna U Zeleného stromu", 2));
e8.add(new Ean8Code(8591882, "RYOR", 2));
e8.add(new Ean8Code(8591884, "Lahůdky Radošovice", 22));
e8.add(new Ean8Code(8591910, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8591911, "NESTLÉ ČESKO", 30));
e8.add(new Ean8Code(8591941, "STAROBRNO", 10));
e8.add(new Ean8Code(8591951, "Krahulík-MASOZÁVOD Krahulčí", 10));
e8.add(new Ean8Code(8591986, "AVEFLOR", 1));
e8.add(new Ean8Code(8591987, "UNILEVER ČR", 1));
e8.add(new Ean8Code(8591991, "Sarantis CZ", 1));
e8.add(new Ean8Code(8591992, "CHOPA", 10));
e8.add(new Ean8Code(8592002, "JUWITAL", 3));
e8.add(new Ean8Code(8592027, "VÍNO MORAVA", 20));
e8.add(new Ean8Code(8592047, "Hamé", 2));
e8.add(new Ean8Code(8592049, "Crocodille ČR", 1));
e8.add(new Ean8Code(8592050, "EFco", 13));
e8.add(new Ean8Code(8592063, "FONTEA", 6));
e8.add(new Ean8Code(8592070, "NESTLÉ ČESKO", 23));
e8.add(new Ean8Code(8592093, "Opavia - LU", 3));
e8.add(new Ean8Code(8592096, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8592097, "Opavia - LU", 3));
e8.add(new Ean8Code(8592100, "NESTLÉ ČESKO", 2));
e8.add(new Ean8Code(8592102, "Opavia - LU", 3));
e8.add(new Ean8Code(8592105, "NESTLÉ ČESKO", 10));
e8.add(new Ean8Code(8592115, "Opavia - LU", 2));
e8.add(new Ean8Code(8592117, "NESTLÉ ČESKO", 9));
e8.add(new Ean8Code(8592126, "Opavia - LU", 4));
e8.add(new Ean8Code(8592160, "Crocodille ČR", 1));
e8.add(new Ean8Code(8592161, "STAROBRNO", 10));
e8.add(new Ean8Code(8592181, "PRO-REFORM", 2));
e8.add(new Ean8Code(8592183, "UNILEVER ČR", 1));
e8.add(new Ean8Code(8592204, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8592205, "Šťastný a spol.", 9));
e8.add(new Ean8Code(8592214, "ALPA", 20));
e8.add(new Ean8Code(8592234, "Hamé", 1));
e8.add(new Ean8Code(8592241, "GlaxoSmithKline", 13));
e8.add(new Ean8Code(8592253, "Orkla Food CZ/SK", 2));
e8.add(new Ean8Code(8592255, "Sarantis CZ", 2));
e8.add(new Ean8Code(8592261, "Světlana Stromská", 13));
e8.add(new Ean8Code(8592298, "Hamé", 1));
e8.add(new Ean8Code(8592299, "UNILEVER ČR", 2));
e8.add(new Ean8Code(8592301, "Sarantis CZ", 1));
e8.add(new Ean8Code(8592322, "CHOPA", 8));
e8.add(new Ean8Code(8592330, "Hamé", 2));
e8.add(new Ean8Code(8592332, "FONTEA", 3));
e8.add(new Ean8Code(8592335, "F & N DODAVATELÉ", 1));
e8.add(new Ean8Code(8592336, "SPAK Foods", 10));
e8.add(new Ean8Code(8592346, "Světlana Stromská", 10));
e8.add(new Ean8Code(8592357, "LIMACO", 6));
e8.add(new Ean8Code(8592363, "TEREZIA COMPANY", 2));
e8.add(new Ean8Code(8592365, "CHOPA", 10));
e8.add(new Ean8Code(8592375, "Hamé", 1));
e8.add(new Ean8Code(8592376, "Crocodille ČR", 2));
e8.add(new Ean8Code(8592384, "Hamé", 1));
e8.add(new Ean8Code(8592386, "Zenit", 4));
e8.add(new Ean8Code(8592390, "Kalabria", 1));
e8.add(new Ean8Code(8592391, "Kalabria", 1));
e8.add(new Ean8Code(8592392, "Kalabria", 1));
e8.add(new Ean8Code(8592393, "Kalabria", 1));
e8.add(new Ean8Code(8592394, "Kalabria", 1));
e8.add(new Ean8Code(8592395, "Kalabria", 1));
e8.add(new Ean8Code(8592396, "Kalabria", 1));
e8.add(new Ean8Code(8592397, "Kalabria", 1));
e8.add(new Ean8Code(8592398, "Kalabria", 1));
e8.add(new Ean8Code(8592399, "Kalabria", 1));
e8.add(new Ean8Code(8592421, "Hamé", 1));
e8.add(new Ean8Code(8592437, "VÍNO MORAVA", 10));
e8.add(new Ean8Code(8592447, "POLABSKÉ MLÉKÁRNY", 4));
e8.add(new Ean8Code(8592451, "FONTEA", 3));
e8.add(new Ean8Code(8592454, "PROXIM", 1));
e8.add(new Ean8Code(8592455, "Hamé", 1));
e8.add(new Ean8Code(8592461, "Reckitt Benckiser (CZ)", 2));
e8.add(new Ean8Code(8592463, "Crocodille ČR", 2));
e8.add(new Ean8Code(8592477, "MASO UZENINY POLIČKA", 2));
e8.add(new Ean8Code(8592480, "NESTLÉ ČESKO", 3));
e8.add(new Ean8Code(8592483, "Opavia - LU", 3));
e8.add(new Ean8Code(8592486, "NESTLÉ ČESKO", 3));
e8.add(new Ean8Code(8592489, "Opavia - LU", 4));
e8.add(new Ean8Code(8592493, "NESTLÉ ČESKO", 2));
e8.add(new Ean8Code(8592495, "Opavia - LU", 4));
e8.add(new Ean8Code(8592499, "NESTLÉ ČESKO", 4));
e8.add(new Ean8Code(8592503, "Opavia - LU", 1));
e8.add(new Ean8Code(8592504, "Mondelez Europe", 2));
e8.add(new Ean8Code(8592506, "NESTLÉ ČESKO", 1));
e8.add(new Ean8Code(8592507, "Mondelez Europe", 1));
e8.add(new Ean8Code(8592508, "NESTLÉ ČESKO", 18));
e8.add(new Ean8Code(8592526, "Mondelez Europe", 1));
e8.add(new Ean8Code(8592527, "NESTLÉ ČESKO", 12));
e8.add(new Ean8Code(8592539, "Mondelez Europe", 1));
e8.add(new Ean8Code(8592540, "MASO UZENINY POLIČKA", 8));
e8.add(new Ean8Code(8592548, "POLABSKÉ MLÉKÁRNY", 7));
e8.add(new Ean8Code(8592568, "UNILEVER ČR", 4));
e8.add(new Ean8Code(8592583, "LUDVÍK SVÍTEK", 6));
e8.add(new Ean8Code(8592590, "TEREZIA COMPANY", 1));
e8.add(new Ean8Code(8592591, "TEREZIA COMPANY", 9));
e8.add(new Ean8Code(8592603, "UNILEVER ČR", 3));
e8.add(new Ean8Code(8592606, "Poděbradka", 10));
e8.add(new Ean8Code(8592616, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8592617, "NORVITA", 1));
e8.add(new Ean8Code(8592618, "Kostelecké uzeniny", 1));
e8.add(new Ean8Code(8592629, "Hamé", 3));
e8.add(new Ean8Code(8592632, "MASO UZENINY POLIČKA", 10));
e8.add(new Ean8Code(8592642, "ING. KAREL VOITL", 1));
e8.add(new Ean8Code(8592646, "Hamé", 10));
e8.add(new Ean8Code(8592658, "RYOR", 1));
e8.add(new Ean8Code(8592659, "NORVITA", 2));
e8.add(new Ean8Code(8592661, "Lahůdky Radošovice", 4));
e8.add(new Ean8Code(8592666, "ALFA farm", 3));
e8.add(new Ean8Code(8592699, "DRYLL TRADE", 1));
e8.add(new Ean8Code(8592700, "DRYLL TRADE", 3));
e8.add(new Ean8Code(8592703, "SPAK Foods", 10));
e8.add(new Ean8Code(8592723, "Sarantis CZ", 1));
e8.add(new Ean8Code(8592724, "UNILEVER ČR", 1));
e8.add(new Ean8Code(8592745, "DRYLL TRADE", 1));
e8.add(new Ean8Code(8592747, "Hamé", 1));
e8.add(new Ean8Code(8592749, "TOMIL", 6));
e8.add(new Ean8Code(8592755, "Lahůdky Radošovice", 8));
e8.add(new Ean8Code(8592763, "DOMITA", 3));
e8.add(new Ean8Code(8592771, "NORVITA", 3));
e8.add(new Ean8Code(8592789, "STAROBRNO", 6));
e8.add(new Ean8Code(8592795, "Heineken CZ", 1));
e8.add(new Ean8Code(8592796, "STAROBRNO", 3));
e8.add(new Ean8Code(8592799, "VOLFOVA mladoboleslavská sodovkárna", 4));
e8.add(new Ean8Code(8592805, "RYOR", 2));
e8.add(new Ean8Code(8592807, "HomeBrands", 1));
e8.add(new Ean8Code(8592809, "Uni Roll Czech", 2));
e8.add(new Ean8Code(8592811, "VIF", 1));
e8.add(new Ean8Code(8592812, "BIOPHARM VÚ BIOFARMACIE A VETER. LÉČIV", 2));
e8.add(new Ean8Code(8592815, "CHOPA", 10));
e8.add(new Ean8Code(8592825, "RYOR", 1));
e8.add(new Ean8Code(8592826, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8592827, "ČEŠKA L.- MORAVIAPRODUKT", 1));
e8.add(new Ean8Code(8592828, "HÜGLI FOOD", 1));
e8.add(new Ean8Code(8592829, "DRYLL TRADE", 2));
e8.add(new Ean8Code(8592831, "CHOPA", 10));
e8.add(new Ean8Code(8592844, "Hamé", 3));
e8.add(new Ean8Code(8592850, "DOMITA", 2));
e8.add(new Ean8Code(8592852, "JIŘÍ ŠAŠINKA", 3));
e8.add(new Ean8Code(8592868, "Hamé", 3));
e8.add(new Ean8Code(8592872, "ARIA-CARDS", 10));
e8.add(new Ean8Code(8592887, "NEKTON - VRŇATA", 10));
e8.add(new Ean8Code(8592900, "NORVITA", 3));
e8.add(new Ean8Code(8592903, "Orkla Food CZ/SK", 1));
e8.add(new Ean8Code(8592904, "Sarantis CZ", 2));
e8.add(new Ean8Code(8592906, "Crocodille ČR", 2));
e8.add(new Ean8Code(8592908, "ARIA-CARDS", 10));
e8.add(new Ean8Code(8592918, "ČEŠKA L.- MORAVIAPRODUKT", 3));
e8.add(new Ean8Code(8592921, "Lahůdky Radošovice", 10));
e8.add(new Ean8Code(8592937, "Poděbradka", 2));
e8.add(new Ean8Code(8592942, "DOMITA", 6));
e8.add(new Ean8Code(8592952, "ROMAN VESELÝ - LIGRUS", 10));
e8.add(new Ean8Code(8592962, "ALPA", 20));
e8.add(new Ean8Code(8592990, "Crocodille ČR", 5));
e8.add(new Ean8Code(8593010, "Mikov", 1));
e8.add(new Ean8Code(8593011, "TOMIL", 32));
e8.add(new Ean8Code(8593043, "POLABSKÉ MLÉKÁRNY", 4));
e8.add(new Ean8Code(8593047, "Mlékárna Kunín", 20));
e8.add(new Ean8Code(8593067, "TOMIL", 10));
e8.add(new Ean8Code(8593083, "MADETA", 11));
e8.add(new Ean8Code(8593094, "F & N DODAVATELÉ", 1));
e8.add(new Ean8Code(8593097, "STAROBRNO", 10));
e8.add(new Ean8Code(8593118, "Pivovar Uherský Brod", 1));
e8.add(new Ean8Code(8593119, "F & N DODAVATELÉ", 1));
e8.add(new Ean8Code(8593122, "NEKTON - VRŇATA", 20));
e8.add(new Ean8Code(8593144, "F & N DODAVATELÉ", 1));
e8.add(new Ean8Code(8593145, "ARIA-CARDS", 10));
e8.add(new Ean8Code(8593155, "Orkla Food CZ/SK", 12));
e8.add(new Ean8Code(8593167, "FONTEA", 6));
e8.add(new Ean8Code(8593174, "TABAKUS GROUP", 1));
e8.add(new Ean8Code(8593177, "VIF", 3));
e8.add(new Ean8Code(8593180, "LIMACO", 6));
e8.add(new Ean8Code(8593188, "EKOMILK", 6));
e8.add(new Ean8Code(8593196, "AMUNAK", 10));
e8.add(new Ean8Code(8593206, "POLABSKÉ MLÉKÁRNY", 3));
e8.add(new Ean8Code(8593212, "BIOPHARM VÚ BIOFARMACIE A VETER. LÉČIV", 2));
e8.add(new Ean8Code(8593219, "Crocodille ČR", 2));
e8.add(new Ean8Code(8593223, "Jan Knopp", 4));
e8.add(new Ean8Code(8593227, "SPAK Foods", 1));
e8.add(new Ean8Code(8593228, "SPAK Foods", 10));
e8.add(new Ean8Code(8593240, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8593253, "NESTLÉ ČESKO", 3));
e8.add(new Ean8Code(8593276, "EKOMILK", 1));
e8.add(new Ean8Code(8593283, "Hamé", 2));
e8.add(new Ean8Code(8593285, "MADETA", 6));
e8.add(new Ean8Code(8593291, "Emco", 6));
e8.add(new Ean8Code(8593297, "ČOKO KLASIK družstvo", 2));
e8.add(new Ean8Code(8593299, "Emco", 2));
e8.add(new Ean8Code(8593326, "Lahůdky Radošovice", 5));
e8.add(new Ean8Code(8593331, "NESTLÉ ČESKO", 10));
e8.add(new Ean8Code(8593345, "JIŘÍ ŠAŠINKA", 5));
e8.add(new Ean8Code(8593365, "NESTLÉ ČESKO", 10));
e8.add(new Ean8Code(8593392, "STAROBRNO", 4));
e8.add(new Ean8Code(8593396, "Heineken CZ", 1));
e8.add(new Ean8Code(8593397, "STAROBRNO", 5));
e8.add(new Ean8Code(8593402, "POLABSKÉ MLÉKÁRNY", 3));
e8.add(new Ean8Code(8593412, "Lahůdky Radošovice", 3));
e8.add(new Ean8Code(8593415, "ČEŠKA L.- MORAVIAPRODUKT", 2));
e8.add(new Ean8Code(8593419, "SUNFOOD", 10));
e8.add(new Ean8Code(8593429, "HEMANN", 5));
e8.add(new Ean8Code(8593434, "Emco", 7));
e8.add(new Ean8Code(8593451, "Emco", 2));
e8.add(new Ean8Code(8593457, "FONTEA", 10));
e8.add(new Ean8Code(8593467, "Kofola CS", 3));
e8.add(new Ean8Code(8593488, "Savencia Fromage & Dairy CZ", 1));
e8.add(new Ean8Code(8593495, "YOPLAIT CZECH", 2));
e8.add(new Ean8Code(8593497, "Emco", 1));
e8.add(new Ean8Code(8593501, "PARAMO", 1));
e8.add(new Ean8Code(8593504, "TOMIL", 3));
e8.add(new Ean8Code(8593507, "Emco", 1));
e8.add(new Ean8Code(8593508, "YOPLAIT CZECH", 4));
e8.add(new Ean8Code(8593512, "The Candy Plus Sweet Factory", 10));
e8.add(new Ean8Code(8593524, "YOPLAIT CZECH", 4));
e8.add(new Ean8Code(8593528, "Bidfood CZ", 6));
e8.add(new Ean8Code(8593534, "YOPLAIT CZECH", 2));
e8.add(new Ean8Code(8593536, "Savencia Fromage & Dairy CZ", 1));
e8.add(new Ean8Code(8593540, "CONTRAX", 10));
e8.add(new Ean8Code(8593550, "TABAKUS GROUP", 1));
e8.add(new Ean8Code(8593552, "Bidfood CZ", 2));
e8.add(new Ean8Code(8593557, "BIOPHARM VÚ BIOFARMACIE A VETER. LÉČIV", 1));
e8.add(new Ean8Code(8593558, "Mlékárna Klatovy", 2));
e8.add(new Ean8Code(8593560, "Fruko-Schulz", 1));
e8.add(new Ean8Code(8593561, "The Candy Plus Sweet Factory", 10));
e8.add(new Ean8Code(8593571, "Bidfood CZ", 5));
e8.add(new Ean8Code(8593578, "YOPLAIT CZECH", 2));
e8.add(new Ean8Code(8593589, "Emco", 1));
e8.add(new Ean8Code(8593590, "BIOPHARM VÚ BIOFARMACIE A VETER. LÉČIV", 2));
e8.add(new Ean8Code(8593594, "PAVEL ČERVENÝ", 3));
e8.add(new Ean8Code(8593597, "ZÁRUBA FOOD", 6));
e8.add(new Ean8Code(8593603, "Savencia Fromage & Dairy CZ", 2));
e8.add(new Ean8Code(8593611, "POLABSKÉ MLÉKÁRNY", 2));
e8.add(new Ean8Code(8593614, "TABAKUS GROUP", 3));
e8.add(new Ean8Code(8593622, "ING. KAREL VOITL", 1));
e8.add(new Ean8Code(8593629, "ING. KAREL VOITL", 2));
e8.add(new Ean8Code(8593631, "ČEŠKA L.- MORAVIAPRODUKT", 2));
e8.add(new Ean8Code(8593633, "Sarantis CZ", 1));
e8.add(new Ean8Code(8593634, "Crocodille ČR", 5));
e8.add(new Ean8Code(8593639, "TABAKUS GROUP", 4));
e8.add(new Ean8Code(8593643, "CHOCOLAND", 3));
e8.add(new Ean8Code(8593649, "Orkla Food CZ/SK", 1));
e8.add(new Ean8Code(8593650, "F & N DODAVATELÉ", 1));
e8.add(new Ean8Code(8593651, "Palírna U Zeleného stromu", 5));
e8.add(new Ean8Code(8593656, "F & N DODAVATELÉ", 1));
e8.add(new Ean8Code(8593657, "Savencia Fromage & Dairy CZ", 3));
e8.add(new Ean8Code(8593660, "FORTIS - DB", 1));
e8.add(new Ean8Code(8593661, "BIOPHARM VÚ BIOFARMACIE A VETER. LÉČIV", 4));
e8.add(new Ean8Code(8593666, "Palírna U Zeleného stromu", 1));
e8.add(new Ean8Code(8593668, "ZÁRUBA FOOD", 6));
e8.add(new Ean8Code(8593674, "PARAMO", 2));
e8.add(new Ean8Code(8593676, "F & N DODAVATELÉ", 1));
e8.add(new Ean8Code(8593677, "Orkla Food CZ/SK", 1));
e8.add(new Ean8Code(8593686, "ING. KAREL VOITL", 16));
e8.add(new Ean8Code(8593702, "Savencia Fromage & Dairy CZ", 1));
e8.add(new Ean8Code(8593704, "AMUNAK", 10));
e8.add(new Ean8Code(8593714, "Palírna U Zeleného stromu", 4));
e8.add(new Ean8Code(8593719, "Savencia Fromage & Dairy CZ", 1));
e8.add(new Ean8Code(8593720, "Orkla Food CZ/SK", 12));
e8.add(new Ean8Code(8593735, "Fruko-Schulz", 1));
e8.add(new Ean8Code(8593736, "ING. KAREL VOITL", 1));
e8.add(new Ean8Code(8593737, "Zenit", 4));
e8.add(new Ean8Code(8593741, "TEREZIA COMPANY", 2));
e8.add(new Ean8Code(8593745, "Bidfood CZ", 1));
e8.add(new Ean8Code(8593751, "Pěkný - Unimex", 7));
e8.add(new Ean8Code(8593758, "RYOR", 1));
e8.add(new Ean8Code(8593759, "Hamé", 1));
e8.add(new Ean8Code(8593760, "Bidfood CZ", 8));
e8.add(new Ean8Code(8593768, "STOCK Plzeň - Božkov", 1));
e8.add(new Ean8Code(8593783, "PAPEI", 1));
e8.add(new Ean8Code(8593785, "Bidfood CZ", 1));
e8.add(new Ean8Code(8593786, "CARLA", 1));
e8.add(new Ean8Code(8593787, "Orkla Food CZ/SK", 4));
e8.add(new Ean8Code(8593801, "Pěkný - Unimex", 4));
e8.add(new Ean8Code(8593805, "TOMIL", 3));
e8.add(new Ean8Code(8593810, "ZÁRUBA FOOD", 5));
e8.add(new Ean8Code(8593815, "Sarantis CZ", 1));
e8.add(new Ean8Code(8593816, "CHOCOLAND", 1));
e8.add(new Ean8Code(8593817, "Pěkný - Unimex", 2));
e8.add(new Ean8Code(8593819, "CHOCOLAND", 1));
e8.add(new Ean8Code(8593820, "Sarantis CZ", 1));
e8.add(new Ean8Code(8593821, "Meggle", 1));
e8.add(new Ean8Code(8593822, "Hamé", 2));
e8.add(new Ean8Code(8593824, "Orkla Food CZ/SK", 3));
e8.add(new Ean8Code(8593827, "ING. KAREL VOITL", 1));
e8.add(new Ean8Code(8593830, "CHOCOLAND", 3));
e8.add(new Ean8Code(8593835, "CHOCOLAND", 2));
e8.add(new Ean8Code(8593838, "TOMIL", 1));
e8.add(new Ean8Code(8593839, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8593840, "Bidfood CZ", 3));
e8.add(new Ean8Code(8593843, "TEREZIA COMPANY", 2));
e8.add(new Ean8Code(8593845, "CHOCOLAND", 2));
e8.add(new Ean8Code(8593914, "CHOCOLAND", 1));
e8.add(new Ean8Code(8593917, "ING. KAREL VOITL", 1));
e8.add(new Ean8Code(8593918, "DANONE", 3));
e8.add(new Ean8Code(8593921, "CARLA", 1));
e8.add(new Ean8Code(8593926, "Bidfood CZ", 4));
e8.add(new Ean8Code(8593931, "CORMEN", 4));
e8.add(new Ean8Code(8593935, "Zenit", 4));
e8.add(new Ean8Code(8593939, "Povltavské mlékárny", 2));
e8.add(new Ean8Code(8593941, "Zenit", 4));
e8.add(new Ean8Code(8593945, "Pěkný - Unimex", 1));
e8.add(new Ean8Code(8593946, "SPAK Foods", 10));
e8.add(new Ean8Code(8593956, "CORMEN", 1));
e8.add(new Ean8Code(8593963, "BIOCEN Laboratories", 1));
e8.add(new Ean8Code(8593976, "Orkla Food CZ/SK", 3));
e8.add(new Ean8Code(8593989, "ITALAT CZ", 19));
e8.add(new Ean8Code(8594008, "ZÁRUBA FOOD", 2));
e8.add(new Ean8Code(8594010, "Bidfood Czech Republic", 1));
e8.add(new Ean8Code(8594014, "Palírna U Zeleného stromu", 1));
e8.add(new Ean8Code(8594015, "YOPLAIT CZECH", 3));
e8.add(new Ean8Code(8594018, "Orkla Food CZ/SK", 2));
e8.add(new Ean8Code(8594025, "YOPLAIT CZECH", 2));
e8.add(new Ean8Code(8594027, "KAISER SOSE", 1));
e8.add(new Ean8Code(8594028, "ITALAT CZ", 2));
e8.add(new Ean8Code(8594030, "CARLA", 1));
e8.add(new Ean8Code(8594031, "Bidfood Czech Republic", 2));
e8.add(new Ean8Code(8594033, "Světlana Stromská", 10));
e8.add(new Ean8Code(8594051, "Savencia Fromage & Dairy CZ", 1));
e8.add(new Ean8Code(8594063, "Bidfood CZ", 1));
e8.add(new Ean8Code(8594064, "ITALAT CZ", 14));
e8.add(new Ean8Code(8594078, "YOPLAIT CZECH", 14));
e8.add(new Ean8Code(8594097, "RAPETO", 1));
e8.add(new Ean8Code(8594114, "TAAR", 1));
e8.add(new Ean8Code(8594115, "ITALAT CZ", 4));
e8.add(new Ean8Code(8594119, "Orkla Food CZ/SK", 2));
e8.add(new Ean8Code(8594131, "Palírna U Zeleného stromu", 1));
e8.add(new Ean8Code(8594135, "BIOCEN Laboratories", 4));
e8.add(new Ean8Code(8594139, "Savencia Fromage & Dairy CZ", 2));
e8.add(new Ean8Code(8594141, "Bidfood Czech Republic", 1));
e8.add(new Ean8Code(8594142, "DANONE", 7));
e8.add(new Ean8Code(8594249, "TAAR", 2));
e8.add(new Ean8Code(8594251, "Orkla Food CZ/SK", 4));
e8.add(new Ean8Code(8594255, "Sarantis CZ", 2));
e8.add(new Ean8Code(8594257, "ITALAT CZ", 1));
e8.add(new Ean8Code(8594259, "Palírna U Zeleného stromu", 1));
e8.add(new Ean8Code(8594275, "FLEET", 1));
e8.add(new Ean8Code(8594276, "Deva Nutrition", 13));
e8.add(new Ean8Code(8594289, "ITALAT CZ", 4));
e8.add(new Ean8Code(8594297, "FAVEA", 1));
e8.add(new Ean8Code(8594299, "TAAR", 1));
e8.add(new Ean8Code(8594376, "ITALAT CZ", 1));
e8.add(new Ean8Code(8594385, "Bidfood Czech Republic", 1));
e8.add(new Ean8Code(8594488, "OTC přípravky", 1));
e8.add(new Ean8Code(8594499, "Palírna U Zeleného stromu", 2));
e8.add(new Ean8Code(8594505, "Pěkný - Unimex", 2));
e8.add(new Ean8Code(8594507, "Orkla Food CZ/SK", 2));
e8.add(new Ean8Code(8594509, "Pěkný - Unimex", 3));
e8.add(new Ean8Code(8594520, "HEINZ FOOD", 3));
e8.add(new Ean8Code(8594523, "Sarantis CZ", 1));
e8.add(new Ean8Code(8594524, "JIŘÍ ŠAŠINKA", 13));
e8.add(new Ean8Code(8594536, "Pěkný - Unimex", 1));
e8.add(new Ean8Code(8594541, "CHOCOLAND", 1));
e8.add(new Ean8Code(8594559, "DANONE", 3));
e8.add(new Ean8Code(8594577, "Sarantis CZ", 1));
e8.add(new Ean8Code(8594578, "MADE GROUP", 2));
e8.add(new Ean8Code(8594580, "HEINZ FOOD", 7));
e8.add(new Ean8Code(8594587, "Plzeňský Prazdroj", 2));
e8.add(new Ean8Code(8594713, "MADE GROUP", 1));
e8.add(new Ean8Code(8594714, "Pěkný - Unimex", 3));
e8.add(new Ean8Code(8594719, "HEINZ FOOD", 4));
e8.add(new Ean8Code(8594723, "POLABSKÉ MLÉKÁRNY", 2));
e8.add(new Ean8Code(8594733, "Savencia Fromage & Dairy CZ", 3));
e8.add(new Ean8Code(8594744, "Sarantis CZ", 2));
e8.add(new Ean8Code(8594756, "HEMANN", 5));
e8.add(new Ean8Code(8594761, "Sarantis CZ", 1));
e8.add(new Ean8Code(8594774, "HEINZ FOOD", 20));
e8.add(new Ean8Code(8594840, "BIOCEN Laboratories", 1));
e8.add(new Ean8Code(8594853, "BIOCEN Laboratories", 1));
e8.add(new Ean8Code(8594886, "VIOLET KANGAROO", 1));
e8.add(new Ean8Code(8594901, "Panther 4ever Limited", 2));
e8.add(new Ean8Code(8594903, "Sarantis CZ", 4));
e8.add(new Ean8Code(8594940, "DIMART", 2));
e8.add(new Ean8Code(8594942, "F & N DODAVATELÉ", 1));
e8.add(new Ean8Code(8595011, "PAPÍRNA MOUDRÝ", 2));
e8.add(new Ean8Code(8595013, "VIOLET KANGAROO", 3));
e8.add(new Ean8Code(8595016, "AVICENNA Company", 2));
e8.add(new Ean8Code(8595050, "PRIMEROS", 2));
e8.add(new Ean8Code(8595072, "YOPLAIT CZECH", 2));
e8.add(new Ean8Code(8595075, "BAPA", 8));
e8.add(new Ean8Code(8595089, "Savencia Fromage & Dairy CZ", 1));
e8.add(new Ean8Code(8595122, "Albert ČR", 5));
e8.add(new Ean8Code(8595129, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8595163, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8595164, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8595168, "Pěkný - Unimex", 3));
e8.add(new Ean8Code(8595183, "POLABSKÉ MLÉKÁRNY", 2));
e8.add(new Ean8Code(8595200, "CHOCOLAND", 3));
e8.add(new Ean8Code(8595203, "VIOLET KANGAROO", 1));
e8.add(new Ean8Code(8595204, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8595210, "Pivovar Hubertus", 2));
e8.add(new Ean8Code(8595212, "VELTA PLUS EU", 1));
e8.add(new Ean8Code(8595213, "VIOLET KANGAROO", 1));
e8.add(new Ean8Code(8595214, "Fruko-Schulz", 1));
e8.add(new Ean8Code(8595215, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8595216, "Savencia Fromage & Dairy CZ", 3));
e8.add(new Ean8Code(8595219, "POLABSKÉ MLÉKÁRNY", 4));
e8.add(new Ean8Code(8595227, "Savencia Fromage & Dairy CZ", 1));
e8.add(new Ean8Code(8595228, "Mlékárna Otinoves", 1));
e8.add(new Ean8Code(8595230, "Savencia Fromage & Dairy CZ", 2));
e8.add(new Ean8Code(8595232, "Mlékárna Otinoves", 1));
e8.add(new Ean8Code(8595233, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8595235, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8595248, "Mattoni 1873", 2));
e8.add(new Ean8Code(8595250, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8595271, "HEINZ FOOD", 3));
e8.add(new Ean8Code(8595274, "Pivovar Hubertus", 2));
e8.add(new Ean8Code(8595276, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8595279, "GlaxoSmithKline", 1));
e8.add(new Ean8Code(8595288, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8595348, "VIOLET KANGAROO", 1));
e8.add(new Ean8Code(8595349, "ITALAT CZ", 4));
e8.add(new Ean8Code(8595400, "Fruko-Schulz", 1));
e8.add(new Ean8Code(8595407, "Savencia Fromage & Dairy CZ", 1));
e8.add(new Ean8Code(8595408, "F & N DODAVATELÉ", 2));
e8.add(new Ean8Code(8595410, "Pěkný - Unimex", 6));
e8.add(new Ean8Code(8595416, "Kofola CS", 5));
e8.add(new Ean8Code(8595425, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8595426, "Kofola CS", 2));
e8.add(new Ean8Code(8595444, "Plzeňský Prazdroj", 3));
e8.add(new Ean8Code(8595454, "HEINZ FOOD", 3));
e8.add(new Ean8Code(8595459, "POLABSKÉ MLÉKÁRNY", 3));
e8.add(new Ean8Code(8595472, "www.smart-filter.com", 4));
e8.add(new Ean8Code(8595476, "VIOLET KANGAROO", 1));
e8.add(new Ean8Code(8595477, "KAUMY", 1));
e8.add(new Ean8Code(8595498, "CHOCOLAND", 3));
e8.add(new Ean8Code(8595501, "Kofola CS", 1));
e8.add(new Ean8Code(8595507, "ORIGINAL PRAGER TALER", 1));
e8.add(new Ean8Code(8595508, "Mattoni 1873", 2));
e8.add(new Ean8Code(8595510, "KAUMY", 2));
e8.add(new Ean8Code(8595515, "HEINZ FOOD", 2));
e8.add(new Ean8Code(8595533, "CHOCOLAND", 7));
e8.add(new Ean8Code(8595577, "HEINZ FOOD", 2));
e8.add(new Ean8Code(8595579, "POLABSKÉ MLÉKÁRNY", 4));
e8.add(new Ean8Code(8595585, "MASPEX Czech", 16));
e8.add(new Ean8Code(8595603, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8595604, "Pivovar Hubertus", 1));
e8.add(new Ean8Code(8595605, "MASPEX Czech", 6));
e8.add(new Ean8Code(8595620, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8595634, "Kofola CS", 2));
e8.add(new Ean8Code(8595636, "Orkla Food CZ/SK", 1));
e8.add(new Ean8Code(8595647, "BIOCEN Laboratories", 5));
e8.add(new Ean8Code(8595653, "Fruko-Schulz", 1));
e8.add(new Ean8Code(8595654, "MARKOL FOOD", 16));
e8.add(new Ean8Code(8595677, "MARKOL FOOD", 8));
e8.add(new Ean8Code(8595685, "Savencia Fromage & Dairy CZ", 2));
e8.add(new Ean8Code(8595687, "POLABSKÉ MLÉKÁRNY", 4));
e8.add(new Ean8Code(8595692, "NESTLÉ ČESKO", 50));
e8.add(new Ean8Code(8595746, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8595773, "FAKEER", 2));
e8.add(new Ean8Code(8595775, "Fruko-Schulz", 3));
e8.add(new Ean8Code(8595779, "Energieser", 2));
e8.add(new Ean8Code(8595797, "Kofola CS", 2));
e8.add(new Ean8Code(8595799, "KAUMY", 1));
e8.add(new Ean8Code(8595803, "FAKEER", 2));
e8.add(new Ean8Code(8595806, "AQM", 1));
e8.add(new Ean8Code(8595808, "Kofola CS", 2));
e8.add(new Ean8Code(8595810, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8595811, "Mattoni 1873", 1));
e8.add(new Ean8Code(8595812, "KAND", 3));
e8.add(new Ean8Code(8595842, "CHOCOLAND", 12));
e8.add(new Ean8Code(8595854, "MARKOL FOOD", 2));
e8.add(new Ean8Code(8595859, "Mondelez Europe", 5));
e8.add(new Ean8Code(8595864, "Orkla Food CZ/SK", 1));
e8.add(new Ean8Code(8595869, "FAKEER", 2));
e8.add(new Ean8Code(8595872, "POLABSKÉ MLÉKÁRNY", 5));
e8.add(new Ean8Code(8595877, "Pěkný - Unimex", 1));
e8.add(new Ean8Code(8595878, "Sarantis CZ", 2));
e8.add(new Ean8Code(8595880, "K2pharm", 3));
e8.add(new Ean8Code(8595883, "POLABSKÉ MLÉKÁRNY", 1));
e8.add(new Ean8Code(8595884, "CHOCOLAND", 3));
e8.add(new Ean8Code(8595887, "NATURA MEDIC APO", 5));
e8.add(new Ean8Code(8595948, "Fruko-Schulz", 1));
e8.add(new Ean8Code(8595949, "Sarantis Czech Republic", 1));
e8.add(new Ean8Code(8595950, "Energieser", 2));
e8.add(new Ean8Code(8595956, "MARKOL FOOD", 2));
e8.add(new Ean8Code(8595958, "Plzeňský Prazdroj", 4));
e8.add(new Ean8Code(8595977, "CHOCOLAND", 3));
e8.add(new Ean8Code(8595980, "POLABSKÉ MLÉKÁRNY", 3));
e8.add(new Ean8Code(8595983, "KULISH RADOMÍR", 14));
e8.add(new Ean8Code(8595997, "Mattoni 1873", 1));
e8.add(new Ean8Code(8596009, "NATURA MEDIC APO", 4));
e8.add(new Ean8Code(8596060, "Global Dental Trading", 1));
e8.add(new Ean8Code(8596061, "CHOCOLAND", 1));
e8.add(new Ean8Code(8596285, "POLABSKÉ MLÉKÁRNY", 2));
e8.add(new Ean8Code(8596307, "CHOCOLAND", 1));
e8.add(new Ean8Code(8596313, "POLABSKÉ MLÉKÁRNY", 3));
e8.add(new Ean8Code(8596316, "mcePharma", 4));
e8.add(new Ean8Code(8596351, "LOPLI", 1));
e8.add(new Ean8Code(8596381, "CHOCOLAND", 2));
e8.add(new Ean8Code(8596389, "Mast-Jaegermeister CZ", 1));
e8.add(new Ean8Code(8596390, "ITALAT CZ", 1));
e8.add(new Ean8Code(8596650, "ING. KAREL VOITL", 4));
e8.add(new Ean8Code(8596692, "Plzeňský Prazdroj", 3));
e8.add(new Ean8Code(8596863, "mcePharma", 1));
e8.add(new Ean8Code(8596886, "Kofola", 2));
e8.add(new Ean8Code(8596888, "mcePharma", 1));
e8.add(new Ean8Code(8596889, "EstheCeuti", 2));
e8.add(new Ean8Code(8596899, "POLABSKÉ MLÉKÁRNY", 3));
e8.add(new Ean8Code(8597130, "POLABSKÉ MLÉKÁRNY", 2));
e8.add(new Ean8Code(8597134, "Kofola", 1));
e8.add(new Ean8Code(8597159, "Pivovary Staropramen", 1));
e8.add(new Ean8Code(8597182, "CHOCOLAND", 5));
e8.add(new Ean8Code(8597197, "Plzeňský Prazdroj", 1));
e8.add(new Ean8Code(8597202, "mcePharma", 3));
e8.add(new Ean8Code(8597213, "Mandario Company", 1));
e8.add(new Ean8Code(8597214, "CHOCOLAND", 6));
e8.add(new Ean8Code(8597220, "LEROS", 1));
e8.add(new Ean8Code(8597222, "Farma Nahošovice", 3));
e8.add(new Ean8Code(8597267, "INCAN nutrition", 1));
e8.add(new Ean8Code(8597268, "Pivovary Staropramen", 2));

        return e8;
    }

    private class Ean8Code {
        int kod;
        String nazev;
        int pocet;

        public Ean8Code() {
        }
        public Ean8Code(int _kod, String _nazev, int _pocet) {
            kod = _kod;
            nazev = _nazev;
            pocet = _pocet;
        }
    }
}
