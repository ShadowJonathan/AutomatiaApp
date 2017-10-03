package com.github.shadowjonathan.automatiaapp.global;

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
import java.util.Map;
import java.util.NavigableMap;
import java.util.TimeZone;
import java.util.TreeMap;

public final class Helper {
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

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

    public static String formatNumber(long value) {
        if (value == Long.MIN_VALUE) return formatNumber(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatNumber(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static class JSONConstructor extends JSONObject {
        private static String TAG = "JSON_CONSTRUCT";

        public JSONConstructor() {
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
