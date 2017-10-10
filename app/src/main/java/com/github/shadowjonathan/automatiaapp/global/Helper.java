package com.github.shadowjonathan.automatiaapp.global;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    private static final String TAG = "Helper";

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

        Log.w(TAG, "parseDate: date will not parse: " + s);

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

    public static void writeMessageQueue(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("messageStack.json", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            Log.e("Exception", "File write failed: ", e);
        } catch (IOException e) {
            Log.e("Exception", "File write failed: ", e);
        }
    }

    public static String readMessageQueue(Context context) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("messageStack.json");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
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

        public JSONConstructor i(String name, Object value) {
            try {
                super.put(name, value);
            } catch (JSONException je) {
                Log.w(TAG, "i_bool: ERR", je);
            }
            return this;
        }

        public JSONConstructor i(String name, Date value) {
            return i(name, Helper.TSUtils.getISO8601(value));
        }
    }

    public static class TSUtils {
        public final static long ONE_SECOND = 1000;
        public final static long SECONDS = 60;
        public final static long ONE_MINUTE = ONE_SECOND * 60;
        public final static long MINUTES = 60;
        public final static long ONE_HOUR = ONE_MINUTE * 60;
        public final static long HOURS = 24;
        public final static long ONE_DAY = ONE_HOUR * 24;

        private TSUtils() {
        }

        public static String getISO8601(Date date) {
            if (date == null)
                return null;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.format(date);
        }

        /**
         * converts time (in milliseconds) to human-readable format
         * "<w> days, <x> hours, <y> minutes and (z) seconds"
         */
        public static String millisToLongDHMS(long duration) {
            StringBuilder res = new StringBuilder();
            long temp = 0;
            if (duration >= ONE_SECOND) {
                temp = duration / ONE_DAY;
                if (temp > 0) {
                    duration -= temp * ONE_DAY;
                    res.append(temp).append(" day").append(temp > 1 ? "s" : "")
                            .append(duration >= ONE_MINUTE ? ", " : "");
                }

                temp = duration / ONE_HOUR;
                if (temp > 0) {
                    duration -= temp * ONE_HOUR;
                    res.append(temp).append(" hour").append(temp > 1 ? "s" : "")
                            .append(duration >= ONE_MINUTE ? ", " : "");
                }

                temp = duration / ONE_MINUTE;
                if (temp > 0) {
                    duration -= temp * ONE_MINUTE;
                    res.append(temp).append(" minute").append(temp > 1 ? "s" : "");
                }

                if (!res.toString().equals("") && duration >= ONE_SECOND) {
                    res.append(" and ");
                }

                temp = duration / ONE_SECOND;
                if (temp > 0) {
                    res.append(temp).append(" second").append(temp > 1 ? "s" : "");
                }
                return res.toString();
            } else {
                return "0 seconds";
            }
        }

        public static String shortLongSince(long duration) {
            if (duration >= ONE_MINUTE) {
                if (duration > ONE_HOUR) {
                    return ((int) (duration / ONE_HOUR)) + "h ago";
                } else {
                    return ((int) (duration / ONE_MINUTE)) + "m ago";
                }
            } else {
                return "Just now";
            }
        }

        /**
         * converts time (in milliseconds) to human-readable format
         * "<dd:>hh:mm:ss"
         */
        public static String millisToShortDHMS(long duration) {
            String res;
            duration /= ONE_SECOND;
            int seconds = (int) (duration % SECONDS);
            duration /= SECONDS;
            int minutes = (int) (duration % MINUTES);
            duration /= MINUTES;
            int hours = (int) (duration % HOURS);
            int days = (int) (duration / HOURS);
            if (days == 0) {
                res = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } else {
                res = String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
            }
            return res;
        }

        public static String makeDate(Date date) {
            long since = new Date().getTime() - date.getTime();
            if (since < ONE_DAY) {
                return shortLongSince(since);
            } else {
                if (new Date().getYear() == date.getYear()) {
                    return new SimpleDateFormat("MMM d", Locale.ENGLISH).format(date);
                } else {
                    return new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).format(date);
                }
            }
        }
    }

    public static class Notification {
        private Notification() {
        }

        public static android.app.Notification.Builder base(Context context) {
            return new android.app.Notification.Builder(context)
                    .setColor(Color.GREEN)
                    ;
        }

        public static android.app.Notification.Builder perm(Context context) {
            return base(context)
                    .setPriority(android.app.Notification.PRIORITY_MIN)
                    .setCategory(android.app.Notification.CATEGORY_SERVICE)
                    .setOngoing(true)
                    ;
        }
    }
}
