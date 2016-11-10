package com.example.chris.bbhomes;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainBBhomes extends AppCompatActivity {

   public Button mSubmit;
   public EditText mRadius;
   public EditText mZipCode;
    TextView mWelcomeText;
    ListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bbhomes);

        mRadius = (EditText) findViewById(R.id.radius);
        mZipCode = (EditText) findViewById(R.id.zipcode);
        mSubmit = (Button) findViewById(R.id.submit_button);
        mWelcomeText = (TextView)findViewById(R.id.welcome_text);
        mListView = (ListView)findViewById(R.id.list_view);
        
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            FetchItemsTask fetchItemsTask = new FetchItemsTask();
                fetchItemsTask.execute();
            }
        });

    }

    private class FetchItemsTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void...params){

            new StoreFetcher().fetchItems();
            return null;

        }
    }

    public class StoreFetcher {

        private  final String TAG = "StoreFetcher";





        public byte [] getUrlBytes (String urlSpec)
                throws IOException {
            URL url = new URL(urlSpec);
            HttpURLConnection connection = (HttpURLConnection)
                    url.openConnection();
            try{
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStream in = connection.getInputStream();
                if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                    throw new IOException(connection.getResponseMessage() + ": with"
                            + urlSpec);
                }

                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while((bytesRead = in.read(buffer)) > 0){
                    out.write(buffer,0,bytesRead);
                }
                out.close();
                return out.toByteArray();
            }finally {
                connection.disconnect();
            }
        }

        public String getUrlString (String urlSpec) throws IOException{
            return new String(getUrlBytes(urlSpec));
        }

        public List<String> fetchItems() {


            List store = null;
            try {
                String url = Uri.parse("https://api.bestbuy.com/v1/stores(area(55423,10))?format=json&show=storeId,storeType,name&pageSize=2&apiKey=KMyMOtBFDLsjYojaIGhZOGse")
                        .buildUpon()
                        .build().toString();
                String jsonString = getUrlString(url);
                Log.i(TAG, "Received Json: " + jsonString);
                JSONObject jsonBody = new JSONObject(jsonString);
                JSONArray results = jsonBody.getJSONArray("stores");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject jsonObject = results.getJSONObject(i);
                    Log.e(TAG, "json object" + results.getJSONObject(i));

                   store = new ArrayList();
                    store.add(results);
                }




            } catch (JSONException e) {
                Log.e(TAG, "Failed to fetch items", e);
            } catch (IOException e) {
                Log.e(TAG, "Failed to parse JSON", e);
            }
            return store;
        }



    }

}
