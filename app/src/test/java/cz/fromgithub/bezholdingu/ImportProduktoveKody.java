package cz.fromgithub.bezholdingu;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImportProduktoveKody {

    @Test
    public void unitTest() {

        String htmlStranka = downloadHtmlPage(String.format("https://www.skrblik.cz/rodina/jidlo-a-vareni/jak-poznat-vyrobce-potravin-seznam-evidencnich-kodu-zpracovatele/"));

        // najit zacatek bloku <tbody> a vse predtim odriznout
        int start = htmlStranka.indexOf("<tbody>") + 7;
        htmlStranka = htmlStranka.substring(start);

        List<ProduktovyKod> lstCodes = new ArrayList<ProduktovyKod>();

        do {
            // najit zacatek bloku <tr> a vse predtim odriznout
            start = htmlStranka.indexOf("<tr>") + 4;
            htmlStranka = htmlStranka.substring(start);
            String kod = getKod(htmlStranka);

            // najit zacatek bloku </td> a vse predtim odriznout
            start = htmlStranka.indexOf("</td>") + 5;
            htmlStranka = htmlStranka.substring(start);

            String nazevFirmy = getNazevFirmy(htmlStranka);

            if (nazevFirmy.contains("odštěpný závod ")) nazevFirmy=nazevFirmy.replace("odštěpný závod ", "");
            if (nazevFirmy.contains("závod ")) nazevFirmy=nazevFirmy.replace("závod ", "");
            if (nazevFirmy.contains(",družstvo")) nazevFirmy=nazevFirmy.replace(",družstvo", "");
            if (nazevFirmy.contains(",výrobna")) nazevFirmy=nazevFirmy.replace(",výrobna", "");
            if (nazevFirmy.contains("provozovna ")) nazevFirmy=nazevFirmy.replace("provozovna ", "");
            if (nazevFirmy.contains(",provoz ")) nazevFirmy=nazevFirmy.replace(",provoz ", "");
            if (nazevFirmy.contains(", obchodní společnost")) nazevFirmy=nazevFirmy.replace(", obchodní společnost", "");
            if (nazevFirmy.contains(",obchodní společnost")) nazevFirmy=nazevFirmy.replace(",obchodní společnost", "");
            if (nazevFirmy.contains(",obchodní družstvo")) nazevFirmy=nazevFirmy.replace(",obchodní družstvo", "");
            if (nazevFirmy.contains(", akciová spolecnost")) nazevFirmy=nazevFirmy.replace(", akciová spolecnost", "");
            if (nazevFirmy.contains(",akciová společnost")) nazevFirmy=nazevFirmy.replace(",akciová společnost", "");
            if (nazevFirmy.contains(",společnost vlastníků")) nazevFirmy=nazevFirmy.replace(",společnost vlastníků", "");
            if (nazevFirmy.contains(",státní podnik")) nazevFirmy=nazevFirmy.replace(",státní podnik", "");
            if (nazevFirmy.contains(" a. s.")) nazevFirmy=nazevFirmy.replace(" a. s.", "");
            if (nazevFirmy.contains(" a.s.")) nazevFirmy=nazevFirmy.replace(" a.s.", "");
            if (nazevFirmy.contains(" ,a.s.")) nazevFirmy=nazevFirmy.replace(" ,a.s.", "");
            if (nazevFirmy.contains(",a.s.")) nazevFirmy=nazevFirmy.replace(",a.s.", "");
            if (nazevFirmy.contains(",a. s.")) nazevFirmy=nazevFirmy.replace(",a. s.", "");
            if (nazevFirmy.contains(",společnost s ručením omezeným")) nazevFirmy=nazevFirmy.replace(",společnost s ručením omezeným", "");
            if (nazevFirmy.contains(", společnost s ručením omezeným")) nazevFirmy=nazevFirmy.replace(", společnost s ručením omezeným", "");
            if (nazevFirmy.contains(" a spol. s r.o.")) nazevFirmy=nazevFirmy.replace(" a spol. s r.o.", "");
            if (nazevFirmy.contains(",spol. s r.o.")) nazevFirmy=nazevFirmy.replace(",spol. s r.o.", "");
            if (nazevFirmy.contains("S&nbsp;,spol. s&nbsp;r.o.")) nazevFirmy=nazevFirmy.replace("S&nbsp;,spol. s&nbsp;r.o.", "");
            if (nazevFirmy.contains(" a&nbsp;spol. s&nbsp;r.o.")) nazevFirmy=nazevFirmy.replace(" a&nbsp;spol. s&nbsp;r.o.", "");
            if (nazevFirmy.contains(" spol.. s&nbsp;r.o.")) nazevFirmy=nazevFirmy.replace(" spol.. s&nbsp;r.o.", "");
            if (nazevFirmy.contains(" SPOL. S&nbsp;R.O.")) nazevFirmy=nazevFirmy.replace(" SPOL. S&nbsp;R.O.", "");
            if (nazevFirmy.contains(",spol. s&nbsp;r.o.")) nazevFirmy=nazevFirmy.replace(",spol. s&nbsp;r.o.", "");
            if (nazevFirmy.contains(",spol. s&nbsp;r. o.")) nazevFirmy=nazevFirmy.replace(",spol. s&nbsp;r. o.", "");
            if (nazevFirmy.contains(",spol. s&nbsp;r.o.")) nazevFirmy=nazevFirmy.replace(",spol. s&nbsp;r.o.", "");
            if (nazevFirmy.contains(" spol. s&nbsp;r.o.")) nazevFirmy=nazevFirmy.replace(" spol. s&nbsp;r.o.", "");
            if (nazevFirmy.contains(" spol.s r.o.")) nazevFirmy=nazevFirmy.replace(" spol.s r.o.", "");
            if (nazevFirmy.contains(" spol.s.r.o")) nazevFirmy=nazevFirmy.replace(" spol.s.r.o", "");
            if (nazevFirmy.contains(",spol. s r.o.")) nazevFirmy=nazevFirmy.replace(",spol. s r.o.", "");
            if (nazevFirmy.contains(",spol.s.r.o.")) nazevFirmy=nazevFirmy.replace(",spol.s.r.o.", "");
            if (nazevFirmy.contains(" spol.s.r.o.")) nazevFirmy=nazevFirmy.replace(" spol.s.r.o.", "");
            if (nazevFirmy.contains(",spol.s r.o.")) nazevFirmy=nazevFirmy.replace(",spol.s r.o.", "");
            if (nazevFirmy.contains(" spol. s r. o.")) nazevFirmy=nazevFirmy.replace(" spol. s r. o.", "");
            if (nazevFirmy.contains(" s. r.o.")) nazevFirmy=nazevFirmy.replace(" s. r.o.", "");
            if (nazevFirmy.contains(" s. r. o.")) nazevFirmy=nazevFirmy.replace(" s. r. o.", "");
            if (nazevFirmy.contains(", s.r.o.")) nazevFirmy=nazevFirmy.replace(", s.r.o.", "");
            if (nazevFirmy.contains(",s. r. o.")) nazevFirmy=nazevFirmy.replace(",s. r. o.", "");
            if (nazevFirmy.contains(",s.r.o.")) nazevFirmy=nazevFirmy.replace(",s.r.o.", "");
            if (nazevFirmy.contains(" s.r.o.")) nazevFirmy=nazevFirmy.replace(" s.r.o.", "");
            if (nazevFirmy.contains(" s.r.o")) nazevFirmy=nazevFirmy.replace(" s.r.o", "");
            if (nazevFirmy.contains(" v.o.s.")) nazevFirmy=nazevFirmy.replace(" v.o.s.", "");
            if (nazevFirmy.contains(",z. s.")) nazevFirmy=nazevFirmy.replace(",z. s.", "");
            if (nazevFirmy.contains(",k.s.")) nazevFirmy=nazevFirmy.replace(",k.s.", "");
            if (nazevFirmy.contains(",o.s.")) nazevFirmy=nazevFirmy.replace(",o.s.", "");
            if (nazevFirmy.contains(",s.p.")) nazevFirmy=nazevFirmy.replace(",s.p.", "");
            if (nazevFirmy.contains("&#8211; ")) nazevFirmy=nazevFirmy.replace("&#8211; ", "-");
            if (nazevFirmy.contains("&nbsp;")) nazevFirmy=nazevFirmy.replace("&nbsp;", " ");
            if (nazevFirmy.contains("&amp;")) nazevFirmy=nazevFirmy.replace("&amp;", "&");
            if (nazevFirmy.contains("&#8217;")) nazevFirmy=nazevFirmy.replace("&#8217;", "'");
            if (nazevFirmy.contains("&#8220;")) nazevFirmy=nazevFirmy.replace("&#8220;", "'");
            if (nazevFirmy.contains("&#8221;")) nazevFirmy=nazevFirmy.replace("&#8221;", "'");
            if (nazevFirmy.contains(" spol. s r. o.")) nazevFirmy=nazevFirmy.replace(" spol. s r. o.", "'");
            if (nazevFirmy.contains(" a.s.")) nazevFirmy=nazevFirmy.replace(" a.s.", "'");
            if (nazevFirmy.contains(" s r.o.")) nazevFirmy=nazevFirmy.replace(" s r.o.", "'");
            if (nazevFirmy.contains(",spol.")) nazevFirmy=nazevFirmy.replace(",spol.", "'");
            if (nazevFirmy.contains(" s r.o.")) nazevFirmy=nazevFirmy.replace(" s r.o.", "'");
            if (nazevFirmy.contains(" spol. s r. o.")) nazevFirmy=nazevFirmy.replace(" spol. s r. o.", "'");
            if (nazevFirmy.contains(" -bourárna")) nazevFirmy=nazevFirmy.replace(" -bourárna", "'");
            if (nazevFirmy.contains(" -uzeniny")) nazevFirmy=nazevFirmy.replace(" -uzeniny", "'");
            if (nazevFirmy.contains(" -jatky")) nazevFirmy=nazevFirmy.replace(" -jatky", "'");
            // from(lstCodes).where("name", eq("Arthur")).first();

            boolean exist = false;
            for(int j=0;j<lstCodes.size();j++){
                if(lstCodes.get(j).kod.equals(kod)){
                    exist=true;
                    break;
                }
            }

            if (!exist) {
                lstCodes.add(new ProduktovyKod(kod, nazevFirmy));
            }

            // System.out.println(String.format("Kod: \"%s\", firma: \"%s\"", kod, nazevFirmy));

            start = htmlStranka.indexOf("<tr>");
        } while (start > 3);

        System.out.println("---START---");

        for (int i = 0; i < lstCodes.size(); ++i) {
            // System.out.println(String.format("if (num == %s) { return \"%s\"; }", lstCodes.get(i).kod.replace("CZ ","").replaceAll("^0*", ""), lstCodes.get(i).firma));
            System.out.println(String.format("znacky.add(new FirmaData(\"%s\", \"%s\"));", lstCodes.get(i).kod.replace("CZ ","").replaceAll("^0*", ""), lstCodes.get(i).firma));
            // znacky.add(new FirmaData("10", "Euroserum"));
            // System.out.println(String.format("Kod: \"%s\", firma: \"%s\"", lstCodes.get(i).kod, lstCodes.get(i).firma));
        }

        System.out.println("\n--- Data do kodu jsou vygenerovana ---");
        System.out.println("\n--- Vysledek nakopirovat do DecoderProducercode.java ---");

        System.out.println("\n--- Pozor, nakonec jeste pridat: ---");
        System.out.println("\n        znacky.add(new FirmaData(\"5107\", \"Pejskar, Praha\"));\n");
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
        }
        catch (MalformedURLException mue) {
        }
        catch (IOException ioe){
        }
        return stranka;
    }



    private String getKod (String htmlStranka){
        String nazev = "";
        int start = htmlStranka.indexOf("<td>") + 4;
        if (start > 3) {
            String rest = htmlStranka.substring(start, 200);
            int end = rest.indexOf("</td>");
            return rest.substring(0, end);
        }
        return "";
    }

    private String getNazevFirmy (String htmlStranka){
        String nazev = "";
        int start = htmlStranka.indexOf("<td>") + 4;
        if (start > 3) {
            String rest = htmlStranka.substring(start, 200);
            int end = rest.indexOf("</td>");
            return rest.substring(0, end);
        }
        return "";

    }

    class ProduktovyKod {
        String kod;
        String firma;

        public ProduktovyKod(String _kod, String _firma) {
            kod = _kod;
            firma = _firma;
        }
    }
}
