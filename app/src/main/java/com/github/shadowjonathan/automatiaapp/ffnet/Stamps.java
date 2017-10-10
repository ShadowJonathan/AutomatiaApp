package com.github.shadowjonathan.automatiaapp.ffnet;

import com.github.shadowjonathan.automatiaapp.global.Helper;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Stamps {
    private HashMap<String, ArchReg> map = new HashMap<String, ArchReg>();

    private ArchReg get(String key) {
        if (map.containsKey(key))
            return map.get(key);
        else {
            ArchReg ar = new ArchReg();
            map.put(key, ar);
            return ar;
        }
    }

    public void input(String ID, String regarding, Date date) {
        switch (regarding) {
            case "registry":
                get(ID).setRegistry(date);
                break;
            case "archive":
                get(ID).setArchive(date);
                break;
        }
    }

    public JSONObject getJSON() {
        Helper.JSONConstructor json = new Helper.JSONConstructor();
        for (Map.Entry<String, ArchReg> entry : map.entrySet()) {
            String key = entry.getKey();
            ArchReg value = entry.getValue();
            Helper.JSONConstructor obj = new Helper.JSONConstructor();
            obj.i("cat", Category.findCat(key));
            obj.i("meta", value.archive);
            obj.i("reg", value.registry);
            json.i(key, obj);
        }
        return json;
    }

    public static class ArchReg {
        protected Date registry;
        protected Date archive;

        ArchReg() {

        }

        public void setRegistry(Date registry) {
            this.registry = registry;
        }

        public void setArchive(Date archive) {
            this.archive = archive;
        }
    }
}
