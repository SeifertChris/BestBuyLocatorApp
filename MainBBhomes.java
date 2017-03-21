package com.example.chris.bbhomes;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import java.util.Timer;
import java.util.TimerTask;

public class MainBBhomes extends AppCompatActivity {

   public Button mSubmit;
   public EditText mRadius;
   public EditText mZipCode;
    TextView mWelcomeText;
    ListView mListView;
    String parsedString = "";


    int zip = 0;
    int radius = 0;
    float lat =0;
    float lon = 0;
    ArrayAdapter<String> adapter;
    ArrayList<String> store = new ArrayList<>();

    SharedPreferences mSharedPreferences;
    int listSize = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bbhomes);

        mRadius = (EditText) findViewById(R.id.radius);
        mZipCode = (EditText) findViewById(R.id.zipcode);
        mSubmit = (Button) findViewById(R.id.submit_button);
        mWelcomeText = (TextView)findViewById(R.id.welcome_text);
        mListView = (ListView)findViewById(R.id.list_view);

        mSharedPreferences = getSharedPreferences("FILE", MODE_PRIVATE);

        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,store);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putFloat("selectedlon",mSharedPreferences.getFloat("longitude"+position,0));
                editor.putFloat("selectedlat",mSharedPreferences.getFloat("latitude"+position,0));
                editor.putString("selectedname",mSharedPreferences.getString("name"+position,""));
                editor.apply();

                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(intent);
            }
        });

                mSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        zip = Integer.parseInt(mZipCode.getText().toString());
                        radius = Integer.parseInt(mRadius.getText().toString());

                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putInt("ZIP", zip);
                        editor.putInt("RADIUS", radius);
                        editor.apply();

                        store.clear();

                        FetchItemsTask fetchItemsTask = new FetchItemsTask();
                        fetchItemsTask.execute();


                        adapter.notifyDataSetChanged();

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                hideKeyboard();
                            }
                        }, 1000);


                    }
                });

        if (savedInstanceState != null) {
            store.addAll(savedInstanceState.getStringArrayList("LIST"));
            mRadius.setText(savedInstanceState.getString("RADIUS"));
            mZipCode.setText(savedInstanceState.getString("ZIP"));
            adapter.notifyDataSetChanged();
            hideKeyboard ();
        }

        checkSharedPreferences();

        hideKeyboard();
    }



    @Override
    protected void onPause(){
        final String TAG = "MainBBhomes";

        super.onPause();
        Log.d(TAG, "OnPause() called");


    }


    @Override
    protected void onStart(){
        final String TAG = "MainBBhomes";
        super.onStart();
        Log.d(TAG, "OnStart called:");
    }
    @Override
    protected void onDestroy(){
        final String TAG = "MainBBhomes";
        super.onDestroy();
        Log.d(TAG, "OnDestroy called");
    }
    @Override
    protected void onResume () {
        final String TAG = "MainBBhomes";

        super.onResume();
        Log.d(TAG, "OnResume Called");

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

        public void fetchItems() {


            try {
                String url = Uri.parse("https://api.bestbuy.com/v1/stores(area("+zip+","+radius+"))?format=json&show=storeId,storeType,name,lat,lng&pageSize=2&apiKey=KMyMOtBFDLsjYojaIGhZOGse")
                        .buildUpon()
                        .build().toString();
                String jsonString = getUrlString(url);
                Log.i(TAG, "Received Json: " + jsonString);
                JSONObject jsonBody = new JSONObject(jsonString);
                JSONArray results = jsonBody.getJSONArray("stores");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject jsonObject = results.getJSONObject(i);
                    Log.e(TAG, "Json object" + results.getJSONObject(i));


                    parsedString = jsonObject.getString("name")+ ":" + jsonObject.getString("storeId") + ":" + jsonObject.getString("storeType") + ": Lat: "
                    + jsonObject.getString("lat") + ": Long: " + jsonObject.getString("lng");
                    store.add(parsedString);
                    lat = Float.parseFloat(jsonObject.getString("lat"));
                    lon = Float.parseFloat(jsonObject.getString("lng"));

                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("" + i, parsedString);
                    editor.putFloat("latitude"+i,lat);
                    editor.putFloat("longitude"+i,lon);
                    editor.putString("name"+i,jsonObject.getString("name"));
                    editor.apply();
                }
                Log.i("ran", parsedString);

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt("SIZE", results.length());
                editor.apply();



            } catch (JSONException e) {
                Log.e(TAG, "Failed to fetch items", e);
            } catch (IOException e) {
                Log.e(TAG, "Failed to parse JSON", e);
            }


        }



    }

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState){

        savedInstanceState.putStringArrayList("LIST", store);
        savedInstanceState.putString("ZIP", mZipCode.getText().toString());
        savedInstanceState.putString("RADIUS", mRadius.getText().toString());

        super.onSaveInstanceState(savedInstanceState);

    }

    private void checkSharedPreferences(){
        if (mSharedPreferences.contains("SIZE"))
            listSize = mSharedPreferences.getInt("SIZE",0);
        if (listSize > 0){
            store.clear();
            for(int i = 0; i < listSize; i++){
                store.add(i,mSharedPreferences.getString(""+i,""));
            }

            adapter.notifyDataSetChanged();

            mZipCode.setText(String.valueOf(mSharedPreferences.getInt("ZIP",0)));
            mRadius.setText(String.valueOf(mSharedPreferences.getInt("RADIUS",0)));

        }

        hideKeyboard();
    }

    public void hideKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
