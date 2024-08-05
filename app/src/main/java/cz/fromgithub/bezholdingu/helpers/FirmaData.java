package cz.fromgithub.bezholdingu.helpers;

public class FirmaData{
    public String nazev;
    public String kod;
    public DecoderHelper.Kategorie holding;
    public DecoderHelper.Retezec retezec;
    public String pozn;

    public FirmaData (FirmaData data) {
        nazev=data.nazev;
        kod=data.kod;
        holding=data.holding;
        retezec=data.retezec;
        pozn=data.pozn;
    }

    public FirmaData (String _kod, String _nazev) {
        nazev=_nazev;
        kod=_kod;
    }

    public FirmaData (String _nazev, String _kod, DecoderHelper.Kategorie _holding, DecoderHelper.Retezec _retezec, String _pozn) {
        nazev=_nazev;
        kod=_kod;
        holding=_holding;
        retezec=_retezec;
        pozn=_pozn;
    }
}
