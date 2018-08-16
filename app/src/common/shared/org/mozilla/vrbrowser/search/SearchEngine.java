package org.mozilla.vrbrowser.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.SettingsStore;

public class SearchEngine implements GeolocationTask.GeolocationTaskDelegate {

    private static final String LOGTAG = "VRB";

    private static SearchEngine mSearchEngineInstance;

    private static class Engine {

        private int mUrlResource;
        private int mQueryResource;

        public String getSearchQuery(Context aContext, String aQuery) {
            return aContext.getString(mUrlResource) + "?" + aContext.getString(mQueryResource, aQuery);
        }

        public static Engine create(int urlResource, int queryResource) {
            Engine engine = new Engine();
            engine.mUrlResource = urlResource;
            engine.mQueryResource = queryResource;

            return engine;
        }

        @NonNull
        public static Engine getEngine(@NonNull GeolocationTask.GeolocationData data) {
            if (data.getCountryCode().equalsIgnoreCase("US"))
                return Engine.create(R.string.google, R.string.google_us_params);

            else if (data.getCountryCode().equalsIgnoreCase("CN"))
                return Engine.create(R.string.baidu_cn, R.string.baidu_params);

            else if (data.getCountryCode().equalsIgnoreCase("RU"))
                return Engine.create(R.string.yandex_ru, R.string.yandex_params);

            else if (data.getCountryCode().equalsIgnoreCase("BY"))
                return Engine.create(R.string.yandex_by, R.string.yandex_params);

            else if (data.getCountryCode().equalsIgnoreCase("TR"))
                return Engine.create(R.string.yandex_tr, R.string.yandex_params);

            else if (data.getCountryCode().equalsIgnoreCase("KZ"))
                return Engine.create(R.string.yandex_kz, R.string.yandex_params);

            else
                return Engine.create(R.string.google, R.string.google_params);
        }

    }

    public static synchronized @NonNull
    SearchEngine get(final @NonNull Context aContext) {
        if (mSearchEngineInstance == null) {
            mSearchEngineInstance = new SearchEngine(aContext);
        }

        return mSearchEngineInstance;
    }

    private Context mContext;
    private Engine mEngine;
    private boolean isUpdating;
    private GeolocationTask mTask;
    private String mEndpoint;

    private SearchEngine(@NonNull Context aContext) {
        mContext = aContext;
        isUpdating = false;
        mEndpoint = mContext.getString(R.string.geolocation_endpoint);

        String geolocationJson = SettingsStore.getInstance(mContext).getGeolocationData();
        GeolocationTask.GeolocationData data = GeolocationTask.GeolocationData.parse(geolocationJson);
        mEngine = Engine.getEngine(data);
    }

    public String getSearchURL(String aQuery) {
        return mEngine.getSearchQuery(mContext, aQuery);
    }

    public void update() {
        if (!isUpdating) {
            mTask = new GeolocationTask(mEndpoint, this);
            mTask.execute();

        } else {
            Log.i(LOGTAG, "Geolocation update cancelled, previous update already running");
        }
    }

    @Override
    public void onGeolocationRequestStarted() {
        isUpdating = true;
    }

    @Override
    public void onGeolocationRequestFinished(GeolocationTask.GeolocationData data) {
        isUpdating = false;

        SettingsStore.getInstance(mContext).setGeolocationData(data.toString());
        mEngine = Engine.getEngine(data);

        Log.d(LOGTAG, "Geolocation success: " + data.toString());
    }

    @Override
    public void onGeolocationRequestError(String error) {
        isUpdating = false;

        Log.w(LOGTAG, "Max retries count reached. Geolocation update cancelled");
    }
}

