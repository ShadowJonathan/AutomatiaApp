package com.github.shadowjonathan.automatiaapp.ffnet.select;

import com.github.shadowjonathan.automatiaapp.ffnet.Registry;

import java.util.ArrayList;
import java.util.List;

public abstract class Filter {
    public static void sort(List<Registry.RegistryEntry> list, FilterAble filter) {
        List<Registry.RegistryEntry> oldlist = new ArrayList<Registry.RegistryEntry>(list);
        list.clear();
        for (Registry.RegistryEntry e : oldlist) {
            if (filter.match(e)) {
                list.add(e);
            }
        }
    }

    public interface FilterAble {
        boolean match(Registry.RegistryEntry entry);
    }

    public static class byStatus implements FilterAble {
        private int status;

        byStatus(int status) {
            this.status = status;
        }

        @Override
        public boolean match(Registry.RegistryEntry entry) {
            switch (status) {
                case 0:
                default:
                    return true;
                case 1:
                    return !entry.completed;
                case 2:
                    return entry.completed;
            }
        }
    }
}
