package cz.fromgithub.bezholdingu;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class FeedbackWrongAsync extends AsyncTask<Void, Void, Void> {

    private Context cont;
    private String email;
    private String code;
    private String msg;
    private String ident;

    private String err="";

    public FeedbackWrongAsync(Context cont, String email, String code, String msg, String ident) {
        this.cont = cont;
        this.email = email;
        this.code = code;
        this.msg = msg;
        this.ident = ident;
    }


    @Override
    protected Void doInBackground(Void... voids) {

        String data = "";
        try {
            data = "msg=" + URLEncoder.encode(this.msg, "UTF-8");
            data += "&code=" + URLEncoder.encode(this.code, "UTF-8");
            data += "&email=" + URLEncoder.encode(this.email, "UTF-8");
            data += "&ident=" + URLEncoder.encode(this.ident, "UTF-8");
            data += "&hash=" + URLEncoder.encode("22645983", "UTF-8");
        }
        catch (Exception ex) {
            return null;
        }

        // Send POST data request
        String nactenaData = tryToSend (cont.getResources().getString(R.string.feedback_wrong_url), data);
        if (nactenaData != null && nactenaData.length() > 1 && nactenaData.toLowerCase().startsWith("ok"))
            return null;

        return null;
    }


    private String tryToSend (String url, String data) {

        BufferedReader reader=null;
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setDoOutput(true);
            OutputStream stream = conn.getOutputStream();
            OutputStreamWriter wr = new OutputStreamWriter(stream);
            wr.write( data );
            wr.flush();

            // Get the server response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response in string
                sb.append(line + "\n");
            }
            err = "";
            return sb.toString();

        }
            catch (Exception ex) {
            if (ex.getClass().getName().equals("java.net.UnknownHostException"))
                err = "Nepodařilo se připojit k serveru.";
            else if (ex.getClass().getName().equals("java.io.FileNotFoundException"))
                err = "Na serveru se nepodařilo předat data přes soubor feedbackwrong.";
            else
                err = ex.getMessage();
        }
            finally {
            try {
                if (reader != null)
                    reader.close();
                //
            }
            catch (Exception ex) {
                err = ex.getMessage();
            }
        }
        return "";
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (cont.getClass().equals(FeedbackWrongActivity.class))
            FeedbackWrongActivity.onSentData(cont, err.length() < 1);
        else if (cont.getClass().equals(ToSendActivity.class))
            ToSendActivity.onSentData(cont, false, err);
   }
}
