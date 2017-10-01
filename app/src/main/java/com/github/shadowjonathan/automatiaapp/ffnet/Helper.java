package com.github.shadowjonathan.automatiaapp.ffnet;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Helper {
    public static Date parseDate(String s) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX").parse(s);
        } catch (ParseException ignored) {

        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(s);
        } catch (ParseException ignored) {

        }

        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(s);
        } catch (ParseException ignored) {

        }

        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(s);
        } catch (ParseException ignored) {

        }

        return null;
    }

    public static ArrayList<JSONObject> makeObjects(JSONArray a) throws JSONException {
        ArrayList<JSONObject> list = new ArrayList<JSONObject>();
        for (int i = 0; i < a.length(); i++) {
            list.add(a.getJSONObject(i));
        }
        return list;
    }

    public static class JSONConstructor extends JSONObject {
        private static String TAG = "JSON_CONSTRUCT";

        JSONConstructor() {
            super();
        }

        public JSONConstructor i(String name, boolean value) {
            try {
                super.put(name, value);
            } catch (JSONException je) {
                Log.w(TAG, "i_bool: ERR", je);
            }
            return this;
        }

        public JSONConstructor i(String name, double value) {
            try {
                super.put(name, value);
            } catch (JSONException je) {
                Log.w(TAG, "i_double: ERR", je);
            }
            return this;
        }

        public JSONConstructor i(String name, int value) {
            try {
                super.put(name, value);
            } catch (JSONException je) {
                Log.w(TAG, "i_int: ERR", je);
            }
            return this;
        }

        public JSONConstructor i(String name, long value) {
            try {
                super.put(name, value);
            } catch (JSONException je) {
                Log.w(TAG, "i_long: ERR", je);
            }
            return this;
        }

        public JSONConstructor i(String name, String value) {
            try {
                super.put(name, value);
            } catch (JSONException je) {
                Log.w(TAG, "i_string: ERR", je);
            }
            return this;
        }
    }

    public static class TSUtils {
        private TSUtils() {
        }

        public static String getISO8601(Date date) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.format(date);
        }
    }
}
