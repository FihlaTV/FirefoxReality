package org.mozilla.vrbrowser.search;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class GeolocationTask extends AsyncTask<Void, Void, String> {

    private static final String LOGTAG = "VRB";

    private static final int MAX_RETRIES = 2;

    public static class GeolocationData {

        private String mCountryCode;
        private String mCountryName;

        private GeolocationData(String aCountryCode, String aCountryName) {
            mCountryCode = aCountryCode;
            mCountryName = aCountryName;
        }

        @NonNull
        public static GeolocationData create(@NonNull String aCountryCode, @NonNull String aCountryName) {
            return new GeolocationData(aCountryCode, aCountryName);
        }

        public static GeolocationData parse(String aGeolocationJson) {
            GeolocationData data = new GeolocationData(null, null);

            JSONObject json;
            try {
                json = new JSONObject(aGeolocationJson);
                data.mCountryCode = (String) json.get("country_code");
                data.mCountryName = (String) json.get("country_name");

            } catch (JSONException e) {
                Log.e(LOGTAG, "Error parsing geolocation data: " + e.getLocalizedMessage());

                data.mCountryCode = "";
                data.mCountryName = "";
            }

            return data;
        }

        public String getCountryCode() {
            return mCountryCode;
        }

        public String getCountrName() {
            return mCountryName;
        }

        public String toString() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("country_code", mCountryCode);
                jsonObject.put("country_name", mCountryName);

            } catch (JSONException e) {
                Log.e(LOGTAG, "Error: " + e.getLocalizedMessage());
            }

            return jsonObject.toString();
        }
    }


    public interface GeolocationTaskDelegate {
        void onGeolocationRequestStarted();
        void onGeolocationRequestFinished(GeolocationData response);
        void onGeolocationRequestError(String error);
    }

    private GeolocationTaskDelegate mDelegate;
    private int mRetries;
    private int mRetryCount;
    private String mEndpoint;

    public GeolocationTask(@NonNull String endpoint, GeolocationTaskDelegate aDelegate) {
        this(endpoint, aDelegate, MAX_RETRIES);
    }

    public GeolocationTask(@NonNull String endpoint, GeolocationTaskDelegate aDelegate, int retries) {
        mEndpoint = endpoint;
        mDelegate = aDelegate;
        mRetries = retries;
        mRetryCount = 0;
    }

    @Override
    protected String doInBackground(Void... params) {
        if (mDelegate != null)
            mDelegate.onGeolocationRequestStarted();

        String result = null;
        while(mRetryCount++ < mRetries && result == null) {
            result = executeGeoLocationRequest();

            if (result == null) {
                if (mRetryCount <= mRetries) {
                    Log.e(LOGTAG, "Geolocation request error, retrying...");

                } else {
                    Log.e(LOGTAG, "Max geolocation request retry count reached. Cancelling");
                }
            }
        }

        return result;
    }

    @Nullable
    private String executeGeoLocationRequest() {
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;

        String result;

        try {
            URL url = new URL(mEndpoint);

            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setRequestProperty("User-Agent", "");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }

            // If response is not 200, treat it as an error
            if (urlConnection.getResponseCode() != 200)
                return null;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            result = buffer.toString();
            return result;

        } catch (IOException e) {
            Log.e(LOGTAG, "Error: " + e.getLocalizedMessage());
            return null;

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOGTAG, "Error closing stream: " + e.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);

        if (response != null) {
            GeolocationData data = GeolocationData.parse(response);
            if (mDelegate != null)
                mDelegate.onGeolocationRequestFinished(data);

        } else {
            if (mDelegate != null)
                mDelegate.onGeolocationRequestError("Geolocation response error");
        }
    }
}
