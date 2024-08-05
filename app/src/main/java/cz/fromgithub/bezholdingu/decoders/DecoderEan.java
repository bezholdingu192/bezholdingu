package cz.fromgithub.bezholdingu;

import android.content.Context;

import cz.fromgithub.bezholdingu.helpers.DecoderHelper;
import cz.fromgithub.bezholdingu.helpers.FirmaData;
import cz.fromgithub.bezholdingu.helpers.SettingsHelper;

public class DecoderEan {

    // nacteni/reset dat o firmach do drseznamu
    // musi se zavolat pred prvnim hledanim vyrobce
    public void init(Context cont, boolean resetData) {
        DecoderEan8.init(cont, resetData);
        DecoderEan13.init(cont, resetData);
        DecoderEanPrivate.init(cont, resetData);
        DecoderEanPrivate20.init(cont, resetData);
        DecoderEanWeight.init(cont, resetData);
    }

    // podle caroveho kodu najde a vrati dostupne informace o vyrobci
    public ResultData getResultData(Context cont, String barcode) {
        FirmaData firmaData = getCompanyData(cont, barcode);
        ZemeData zemeData = DecoderCountry.getCompanyCountry(barcode);

        ResultData rslt = new ResultData();
        rslt.kod=barcode;

        // neznama firma
        if (firmaData == null) {
            // CZ
            if (zemeData.kategorie == DecoderCountry.Kategorie.CZ) {
                rslt.nazev = "nezjištěná firma z České republiky";
                rslt.dodatek = "Kód výrobce není v seznamu firem holdingu. Pro jistotu se můžete pokusit najít na obalu jméno výrobce nebo produktovou značku (číslo v malém oválu) a zadat ji do ručního vyhledávání.\n\nAplikace umí ověřit pouze maso, uzeniny, sýry, vejce, mlékárenské a pekárenské výrobky.";
                rslt.vysledek = DecoderHelper.Kategorie.NENALEZENO;
            }
            else if (zemeData.kategorie == DecoderCountry.Kategorie.ZAHRANICNI) {
                rslt.nazev = "nezjištěná firma ".concat(zemeData.nazevSklonovany);
                rslt.dodatek = "";
                rslt.vysledek = DecoderHelper.Kategorie.MIMOHOLDING_NENALEZENO;
            }
            else if (zemeData.kategorie == DecoderCountry.Kategorie.RUSKO) {
                rslt.nazev = "nezjištěná firma ".concat(zemeData.nazevSklonovany);
                rslt.dodatek = "";
                if (SettingsHelper.getSettingBoolean(cont, SettingsHelper.Preference.RUSKO))
                    rslt.vysledek = DecoderHelper.Kategorie.RUSKO;
                else
                    rslt.vysledek = DecoderHelper.Kategorie.MIMOHOLDING_NENALEZENO;
            }
            else if (zemeData.kategorie == DecoderCountry.Kategorie.BELORUSKO) {
                rslt.nazev = "nezjištěná firma ".concat(zemeData.nazevSklonovany);
                rslt.dodatek = "";
                if (SettingsHelper.getSettingBoolean(cont, SettingsHelper.Preference.BELORUSKO))
                    rslt.vysledek = DecoderHelper.Kategorie.BELORUSKO;
                else
                    rslt.vysledek = DecoderHelper.Kategorie.MIMOHOLDING_NENALEZENO;
            }
            else if (zemeData.kategorie == DecoderCountry.Kategorie.PRIVATNI) {
                rslt.nazev = "privátní značka obchodního řetězce, neznámý dodavatel";
                rslt.dodatek = "Pod privátní značkou dodávají řetězcům zboží různí výrobci. Pokuste se najít na obalu jméno výrobce nebo produktovou značku (číslo v malém oválu) a zadat ji do ručního vyhledávání.";
                rslt.vysledek = DecoderHelper.Kategorie.NENALEZENO;
            }
            else if (zemeData.kategorie == DecoderCountry.Kategorie.VAHOVE) {
                rslt.nazev = "váhový/kusový kód, neznámý dodavatel";
                rslt.dodatek = "Váhové/kusové kódy si tiskne každá prodejna sama, podle hmotnosti nebo variabilní ceny zboží. Pokuste se najít na obalu jméno výrobce nebo produktovou značku (číslo v malém oválu) a zadat ji do ručního vyhledávání.";
                rslt.vysledek = DecoderHelper.Kategorie.NENALEZENO;
            }
            else {
                rslt.nazev = "podle kódu nelze identifikovat";
                rslt.dodatek = "Tento typ kódu neumožňuje identifikaci výrobce ani země.";
                rslt.vysledek = DecoderHelper.Kategorie.NENALEZENO;
            }
        }
        // znama firma
        else {

            // pokud nerozlisujeme Tomana, zmenit na MIMOHOLDING
            if (firmaData.holding.equals(DecoderHelper.Kategorie.TOMAN)
                    && !SettingsHelper.getSettingBoolean(cont, SettingsHelper.Preference.TOMAN)) {
                firmaData.holding=DecoderHelper.Kategorie.MIMOHOLDING;
            }

            // retezec
            if (firmaData.retezec == DecoderHelper.Retezec.RETEZEC) {
                rslt.nazev = "Obchodní řetězec ".concat(firmaData.nazev);
                rslt.dodatek = "Řetězce využívají různých dodavatelů. Pokuste se najít na obalu jméno výrobce nebo produktovou značku (číslo v malém oválu) a zadat ji do ručního vyhledávání.";
                rslt.vysledek = DecoderHelper.Kategorie.NEJASNE;
            }
            // dodavatel retezce (privatni znacky) HOLDING
            else if (firmaData.retezec == DecoderHelper.Retezec.DODAVATELRETEZCE && firmaData.holding == DecoderHelper.Kategorie.HOLDING) {
                rslt.nazev = "AGROFERT - ".concat(firmaData.nazev);
                rslt.dodatek = firmaData.pozn;
                rslt.vysledek = firmaData.holding;
            }
            // dodavatel retezce (privatni znacky) NEHOLDING
            else if (firmaData.retezec == DecoderHelper.Retezec.DODAVATELRETEZCE && firmaData.holding == DecoderHelper.Kategorie.MIMOHOLDING) {
                rslt.nazev = firmaData.nazev;
                rslt.dodatek = firmaData.pozn;
                rslt.vysledek = firmaData.holding;
            }
            // se stejným kódem dodává více dodavatelů (napr. EAN-8, zacinajici "20")
            else if (firmaData.retezec == DecoderHelper.Retezec.VICEDODAVATELU) {
                rslt.nazev = "Privátní značka řetězce";
                rslt.dodatek = firmaData.pozn.concat("\n\nPozor, pod stejným kódem mohou mít ostatní řetězce jiné dodavatele.");
                rslt.vysledek = firmaData.holding;
            }
            // se stejným kódem dodává více dodavatelů (napr. EAN-13, zacinajici "21")
            else if (firmaData.retezec == DecoderHelper.Retezec.KUSOVYKOD) {
                rslt.nazev = "Kusový kód";
                rslt.dodatek = firmaData.pozn.concat("\n\nPozor, pod stejným kódem mohou mít ostatní řetězce jiné dodavatele.");
                rslt.vysledek = firmaData.holding;
            }
            // se stejným kódem dodává více dodavatelů (napr. EAN-13, zacinajici "28" nebo "29")
            else if (firmaData.retezec == DecoderHelper.Retezec.VAHOVYKOD) {
                rslt.nazev = "Váhový kód";
                rslt.dodatek = firmaData.pozn.concat("\n\nPozor, pod stejným kódem mohou mít ostatní řetězce jiné dodavatele.");
                rslt.vysledek = firmaData.holding;
            }
            // HOLDING
            else if (firmaData.holding == DecoderHelper.Kategorie.HOLDING) {
                rslt.nazev = "AGROFERT - ".concat(firmaData.nazev);
                rslt.dodatek = firmaData.pozn;
                rslt.vysledek = firmaData.holding;
            }
            // TOMAN
            else if (firmaData.holding == DecoderHelper.Kategorie.TOMAN) {
                rslt.nazev = "Politik TOMAN - ".concat(firmaData.nazev);
                rslt.dodatek = firmaData.pozn;
                rslt.vysledek = firmaData.holding;
            }
            // NEHOLDING
            else {
                rslt.nazev = firmaData.nazev; //.concat(", ").concat(zemeData.nazev);
                rslt.dodatek = firmaData.pozn;
                rslt.vysledek = firmaData.holding;
            }
        }

        return rslt;
    }

    // podle casti nazvu najde a vrati dostupne informace o vyrobci
    public ResultData getResultDataByName(Context cont, String name, boolean privatniZnacky) {
        FirmaData firmaData = getCompanyDataByName(cont, name, privatniZnacky);

        if (firmaData == null)
            return null;

        // pokud nerozlisujeme Tomana, zmenit na MIMOHOLDING
        if (firmaData.holding.equals(DecoderHelper.Kategorie.TOMAN)
                && !SettingsHelper.getSettingBoolean(cont, SettingsHelper.Preference.TOMAN)) {
            firmaData.holding=DecoderHelper.Kategorie.MIMOHOLDING;
        }

        ResultData rslt = new ResultData();
        rslt.kod = name;

        // retezec
        if (firmaData.retezec == DecoderHelper.Retezec.RETEZEC) {
            rslt.nazev = "Obchodní řetězec ".concat(firmaData.nazev);
            rslt.dodatek = "Řetězce využívají různých dodavatelů, jak spadajících pod holding, tak samostatných.";
            rslt.vysledek = DecoderHelper.Kategorie.NEJASNE;
        }
        // HOLDING
        else if (firmaData.holding == DecoderHelper.Kategorie.HOLDING) {
            rslt.nazev = "AGROFERT - ".concat(firmaData.nazev);
            rslt.dodatek = firmaData.pozn;
            rslt.vysledek = firmaData.holding;
        }
        // TOMAN
        else if (firmaData.holding == DecoderHelper.Kategorie.TOMAN) {
            rslt.nazev = "Politik TOMAN - ".concat(firmaData.nazev);
            rslt.dodatek = firmaData.pozn;
            rslt.vysledek = firmaData.holding;
        }
        // NEHOLDING
        else if (firmaData.holding == DecoderHelper.Kategorie.MIMOHOLDING) {
            rslt.nazev = firmaData.nazev;
            rslt.vysledek = firmaData.holding;
        }
        return rslt;
    }

    // najde odpovidajici EAN a vrati data o firme
    private FirmaData getCompanyData(Context cont, String barcode) {
        // https://www.gs1cz.org/nabizime/nastroje/gepir/vyhledavani-podle-gln
        // https://www.gs1cz.org/nabizime/nastroje/gepir/vyhledavani-podle-nazvu-spolecnosti

        FirmaData nalezenaFirma = null;

        if (barcode.length()>1 && (barcode.substring(0,2).equals("20") || barcode.substring(0,2).equals("25"))) {
            return DecoderEanPrivate20.findByCode(cont, barcode);
        } else if (barcode.length()>1 && (barcode.substring(0,2).equals("21") || barcode.substring(0,2).equals("27") || barcode.substring(0,2).equals("28") || barcode.substring(0,2).equals("29"))) {
            return DecoderEanWeight.findByCode(cont, barcode);
        } else if (barcode.length() > 8) {
            nalezenaFirma = DecoderEan13.findByCode(cont, barcode);
        } else {
            nalezenaFirma = DecoderEan8.findByCode(cont, barcode);
        }

        if (nalezenaFirma != null && nalezenaFirma.retezec == DecoderHelper.Retezec.RETEZEC) {
            FirmaData priv = DecoderEanPrivate.findByCode(cont, barcode);
            if (priv != null)
                nalezenaFirma = priv;
        }
        return nalezenaFirma;
    }

    // najde firmu podle nazvu a vrati jeji data
    private FirmaData getCompanyDataByName(Context cont, String name, boolean privatniZnacky) {

        if (! privatniZnacky) {

            FirmaData ean13 = DecoderEan13.findByName(cont, name);
            if (ean13 != null)
                return ean13;

            FirmaData ean8 = DecoderEan8.findByName(cont, name);
            if (ean8 != null)
                return ean8;

            return null;
        }
        else {
            // privatni znacky
            FirmaData privTmp = DecoderEanPrivate.findByName(cont, name);
            if (privTmp != null)
                return privTmp;

            privTmp = DecoderEanPrivate20.findByName(cont, name);
            return privTmp;
        }
    }
}


class ResultData {
    String kod;
    String nazev;
    String dodatek;
    DecoderHelper.Kategorie vysledek;

    public ResultData() {
    }
    public ResultData(String _kod, String _nazev, String _dodatek, DecoderHelper.Kategorie _vysledek) {
        kod = _kod;
        nazev = _nazev;
        dodatek = _dodatek;
        vysledek = _vysledek;
    }
}