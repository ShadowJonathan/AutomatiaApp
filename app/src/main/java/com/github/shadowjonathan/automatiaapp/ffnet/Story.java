package com.github.shadowjonathan.automatiaapp.ffnet;

import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

public class Story {
    private static Map<String, Story> Stories;
    public String ID;
    public Archive from;

    Story(String ID) {
        this.ID = ID;
    }

    Story(String ID, Archive a) {
        from = a;
        this.ID = ID;
    }

    public static Story getStory(URL url) {
        return getStory(Pattern.compile("^[/\\\\](\\d+)", Pattern.CASE_INSENSITIVE).matcher(url.getPath()).group(1));
    }

    public static Story getStory(String ID) {
        if (Stories.containsKey(ID))
            return Stories.get(ID);
        Story s = new Story(ID);
        Stories.put(ID, s);
        return s;
    }

    public class StoryNotFoundException extends Exception {
        private String ID;

        StoryNotFoundException(String ID) {
            this.ID = ID;
        }

        @Override
        public String toString() {
            //return super.toString();
            return "STORY \"" + this.ID + "\" DOES NOT EXIST";
        }
    }
}
