package cz.fromgithub.bezholdingu;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ImportEan8 {

    @Test
    public void unitTest() {
        /*
        POZOR, data stahovana ze stranek gs1cz.org neobsahuji cestinu.
        Je dobre zkontrolovat jestli to stale plati a pokud ano, tak pouze doplnit do dat nove zmeny z vygenerovaneho seznamu.

        Seznam se generuje za obdobi od data - datum vhodne upravit, aby odpovidalo posledni aktualizaci, viz o par radek niz.

        Stahovani je mozne edlat po blocich, viz hlavni cyklus, tam nastavit od do (max inteval 0-10000).
        Pozor stahovani trva tak dve hodiny (lze zrychlit nebo zpomalit pomoci promenne pauzaMeziStahovanim, ktera dela delay mezi stahovanim stranek.

        Kdyby bylio poteba vygenerovat vystup z JSON souboru, tak se to nejsnaz udela vlozenim logovani do nacitani ze souboru v kodu mobilni aplikace.
        Nektere firmy jsou ze seznamu vyhozene. Takove ty, kterych je moc a nesouvisi s produkci holdingu (Philip Morris, Dermacol, atd)
        Sem napsat datum posledni aktualizace: 14.1.2021
        */
        Date datumMinuleAktualizace = (new GregorianCalendar(2021, 1, 13)).getTime();
        int pauzaMeziStahovanim = 400;  // pauza primerene delky (v ms)




        List<Tmp> e8raw = new ArrayList<Tmp>();

        System.out.println("---START---");

        for (int strankaCislo = 0; strankaCislo < 10005; strankaCislo++) {
            String htmlStranka = downloadHtmlPage(String.format("https://www.gs1cz.org/_/v1/Form/Gepir/1/859%04d0", strankaCislo));
            if (htmlStranka.equals("err")) {
                // stahovani stranky spadlo, zkusime znovu
                strankaCislo--;
                System.out.println("\n ### chyba pri stahovani stranky, zkusim znovu ###");
                delay(200);
            }
            if (isZaznamOk(htmlStranka)) {

                String nazevFirmy = getNazevFirmy(htmlStranka);
                Date datumZmeny = getDatumZmeny(htmlStranka);

                    if (nazevFirmy.length() > 0) {
                        nazevFirmy = NormalizeNazev(nazevFirmy);
                        String eanString = String.format("859%04d", strankaCislo);
                        int eanInt = Integer.parseInt(eanString);
                        if (datumZmeny.after(datumMinuleAktualizace))
                        {
                            e8raw.add(new Tmp(eanInt, nazevFirmy, 1));
                            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                            System.out.println(String.format("Stazeno a zapamatovano: %d %s (%s)", eanInt, nazevFirmy, dateFormat.format(datumZmeny)));
                        }
                        else
                        {
                            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                            System.out.println(String.format("Stazeno, vynechano: %d %s (%s)", eanInt, nazevFirmy, dateFormat.format(datumZmeny)));
                        }
                        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    }
            } else {
                System.out.println(String.format("Stranka %d je prazdna.", strankaCislo));
            }
            delay(pauzaMeziStahovanim);  // delay primerene delky
        }

        System.out.println("\n--- Data do kodu jsou vygenerovana ---");

        // shluknuti posloupnosti kodu
        List<Tmp> e8 = new ArrayList<Tmp>();

        Tmp last = new Tmp(0, "", 0);
        for (int i = 0; i < e8raw.size(); i++) {
            Tmp tmp = e8raw.get(i);
            if (tmp.kod == last.kod + 1 && tmp.nazev.equals(last.nazev))
                // last.pocet++;
                (e8.get(e8.size() - 1)).pocet++;
            else e8.add(tmp);

            last = tmp;
        }

        // vypsat vysledek
        System.out.println("\n--- ODTUD to patri do tridy ImportEan8.java ---");
        for (int i = 0; i < e8.size(); i++) {
            Tmp tmp = e8.get(i);
            System.out.println(String.format("e8.add(new Ean8Code(%d, \"%s\", %d));", tmp.kod, tmp.nazev, tmp.pocet));
            // e8.add(new Ean8Code(%d, \"%s\", %d));
            /*
            System.out.println(String.format("    {"));
            System.out.println(String.format("      \"nazev\": \"%s\",", nazevFirmy));
            System.out.println(String.format("      \"kod\": \"859%04d\"", strankaCislo));
            System.out.println(String.format("    },"));
             */
        }

        System.out.println("\n--- Hotovo ---");
    }


    private String downloadHtmlPage(String url) {
        String stranka = "";
        try {
            URL u = new URL(url);
            InputStream is = u.openStream();         // throws an IOException

            BufferedReader dis = new BufferedReader(new InputStreamReader(is));

            String s;
            while ((s = dis.readLine()) != null) {
                stranka = stranka + s;
            }
            is.close();
        } catch (MalformedURLException mue) {
            return "err";
        } catch (IOException ioe) {
            return "err";
        }
        return stranka;
    }

    private boolean isZaznamOk(String htmlStranka) {
        int start = htmlStranka.indexOf("error_code") + 14;
        if (start > 15) {
            String rest = htmlStranka.substring(start);
            int end = rest.indexOf("\"");
            return rest.substring(0, end).equals("0");
        }
        return false;
    }

    private String getNazevFirmy(String htmlStranka) {
        String nazev = "";
        int start = htmlStranka.indexOf("name") + 8;
        if (start > 10) {
            String rest = htmlStranka.substring(start);
            int end = rest.indexOf("\"");
            return rest.substring(0, end);
        }
        return "";
    }

    private Date getDatumZmeny(String htmlStranka) {
        String nazev = "";
        int start = htmlStranka.indexOf("change_date") + 15;
        if (start > 10) {
            String rest = htmlStranka.substring(start);
            int end = rest.indexOf("\"");
            String strDatumZmeny = rest.substring(0, end);

            try {
                SimpleDateFormat formatter1=new SimpleDateFormat("dd.MM.yyyy");
                return formatter1.parse(strDatumZmeny);
            }
            catch (Exception ex) {
                System.out.println(String.format("Chyba pri parsovani datumu: %s", ex));
            }
        }
        return null;
    }

    private String NormalizeNazev(String nazev) {
        nazev = nazev.trim();
        if (nazev.substring(nazev.length() - 1).equals(","))
            nazev = nazev.substring(0, nazev.length() - 1);
        nazev = nazev.replace("Cesko a Slovensko", "CZ/SK");
        nazev = nazev.replace("Czech Republic", "CZ");
        nazev = nazev.replace("Ceska republika", "ČR");
        nazev = nazev.replace(" (Czech Republic)", "");

        return nazev;
    }

/*
    preskakovat:
    GS1
    Philip Morris
    British American Tobacco
    Dermacol
    KOH-I-NOOR
    ZENTIVA
    Cosmonde
    Roll4You
    Gabriella Salvete
    TURISTICKÉ ZNÁMKY
    SALVUS PRAHA Profi
    Sellier & Bellot
    NATURLAB.ORGANIC
    Elektroodbyt Praha
*/

    private void delay(int delayTime) {
        try
        {
            Thread.sleep(delayTime);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    class Tmp {
        int kod;
        String nazev;
        int pocet;

        public Tmp() {
        }
        public Tmp(int _kod, String _nazev, int _pocet) {
            kod = _kod;
            nazev = _nazev;
            pocet = _pocet;
        }
    }
}



