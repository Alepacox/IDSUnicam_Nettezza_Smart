package unicam.nettezzasmart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SyncToServerTask {
    public static final String URL = "https://plagiando.com/";

    public static class GetServerTask extends AsyncTask<Void, Void, String> {
        private final int id;
        private final boolean reqOrRep;

        public GetServerTask(int id, boolean reqOrRep) {
            //true request, false report
            this.reqOrRep=reqOrRep;
            this.id = id;
        }

        protected String doInBackground(Void... urls) {
            String api;
            if(reqOrRep) api= "api/request/"; else api="api/reporting/";
            try {
                URL url = new URL(URL + api + String.valueOf(id));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    if (urlConnection.getResponseCode()==404) {
                        return "delete";
                    } else if (urlConnection.getResponseCode()==200){
                    InputStreamReader streamReader = new
                            InputStreamReader(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                    } else return null;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                return null;
            }
        }

    }

    public static class DeleteServerTask extends AsyncTask<Void, Void, String>{
        private int id;
        private final boolean reqOrRep;

        public DeleteServerTask(int id, boolean reqOrRep) {
            //true request, false report
            this.reqOrRep=reqOrRep;
            this.id = id;
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection urlConnection;
            String result;
            String api;
            if(reqOrRep) api= "api/request/"; else api="api/reporting/";

            try {
                //Connect
                urlConnection = (HttpURLConnection) ((new URL(URL + api + String.valueOf(id)).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("DELETE");
                urlConnection.connect();
                //Read
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                result = sb.toString();
                JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
                if (urlConnection.getResponseCode()==200) {
                    return "deleted";
                } else return null;
            } catch (UnsupportedEncodingException e) {
                return null;
            } catch (IOException e) {
                return null;
            } catch (JSONException e) {
                return null;
            }
        }
    }

    public static class PostRequestServerTask extends AsyncTask<Void, Void, String> {
        private String choosenData;
        private String choosenPlace;

        public PostRequestServerTask(String choosenData, String choosenPlace) throws IOException {
            this.choosenData = choosenData;
            this.choosenPlace = choosenPlace;
        }

        protected String doInBackground(Void... urls) {
            HttpURLConnection urlConnection;
            JSONObject json = new JSONObject();
            try {
                json.put("address", choosenPlace);
                json.put("phone", MyData.number);
                json.put("pref", choosenData);
                json.put("name", MyData.name);
                json.put("last_name", MyData.surname);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            String string = json.toString();
            String result;
            try {
                //Connect
                urlConnection = (HttpURLConnection) ((new URL(URL + "api/request/").openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();
                //Write
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(string);
                writer.close();
                outputStream.close();
                if (urlConnection.getResponseCode()==201) {
                //Read
                   BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                   String line = null;
                   StringBuilder sb = new StringBuilder();
                   while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                   }
                   bufferedReader.close();
                   result = sb.toString();
                   JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
                   JSONObject data = object.getJSONObject("data");
                   String id = data.getString("id");
                   return id;
                } else return null;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class PostReportServerTask extends AsyncTask<Void, Void, String>{
        private String trashType;
        private String comment;
        private double lat;
        private double lon;
        private Bitmap photo;

        public PostReportServerTask(String trashType, String comment, double lat, double lon, Bitmap photo) throws IOException {
            this.trashType = trashType;
            this.comment = comment;
            this.lat=lat;
            this.lon=lon;
            this.photo=photo;
        }

        protected String doInBackground(Void... urls) {
            HttpURLConnection urlConnection;
            JSONObject json = new JSONObject();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            try {
                json.put("name",MyData.name);
                json.put("last_name",MyData.surname);
                json.put("phone", MyData.number);
                json.put("lat", lat);
                json.put("long", lon);
                json.put("description", comment);
                json.put("category", trashType);
                json.put("photo", encodedImage);

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            String string = json.toString();
            String result;
            try {
                //Connect
                urlConnection = (HttpURLConnection) ((new URL(URL + "api/reporting/").openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();
                //Write
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(string);
                writer.close();
                outputStream.close();
                //Read
                if (urlConnection.getResponseCode()==201) {
                   BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                   String line = null;
                   StringBuilder sb = new StringBuilder();
                   while ((line = bufferedReader.readLine()) != null) {
                       sb.append(line);
                   }
                   bufferedReader.close();
                   result = sb.toString();
                   JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
                   JSONObject data = object.getJSONObject("data");
                   String id = data.getString("id");
                   return id;
                } else return null;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;

            }

        }
    }
    public static boolean checkConnection(Activity myActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) myActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }
        else return false;
    }
    }

