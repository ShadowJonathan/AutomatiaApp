package com.github.shadowjonathan.automatiaapp.web;

import android.os.AsyncTask;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class WebReader extends AsyncTask<String, String, String> {

    private AsyncResponse delegate = null;

    public WebReader(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(params[0]);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        System.out.println(params[0]);
        try {
            result = client.execute(request, responseHandler);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;

    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

    // you may separate this or combined to caller class.
    public interface AsyncResponse {
        void processFinish(String output);
    }
}