package com.github.shadowjonathan.automatiaapp.ffnet.select;


import com.github.shadowjonathan.automatiaapp.ffnet.Registry;

import java.util.Comparator;

public class Sort {
    public static final class byUpdated implements Comparator<Registry.RegistryEntry> {
        @Override
        public int compare(Registry.RegistryEntry o2, Registry.RegistryEntry o1) {
            if ((o1.updated != null ? o1.updated : o1.published).getTime() > (o2.updated != null ? o2.updated : o2.published).getTime())
                return 1;
            else
                return -1;
        }
    }

    public static final class byPublished implements Comparator<Registry.RegistryEntry> {
        @Override
        public int compare(Registry.RegistryEntry o2, Registry.RegistryEntry o1) {
            if (o1.published.getTime() > o2.published.getTime())
                return 1;
            else
                return -1;
        }
    }

    public static final class byFavorites implements Comparator<Registry.RegistryEntry> {
        @Override
        public int compare(Registry.RegistryEntry o2, Registry.RegistryEntry o1) {
            return o1.favs - o2.favs;
        }
    }

    public static final class byFollows implements Comparator<Registry.RegistryEntry> {
        @Override
        public int compare(Registry.RegistryEntry o2, Registry.RegistryEntry o1) {
            return o1.follows - o2.follows;
        }
    }

    public static final class byReviews implements Comparator<Registry.RegistryEntry> {
        @Override
        public int compare(Registry.RegistryEntry o2, Registry.RegistryEntry o1) {
            return o1.reviews - o2.reviews;
        }
    }

    public static final class byWords implements Comparator<Registry.RegistryEntry> {
        @Override
        public int compare(Registry.RegistryEntry o2, Registry.RegistryEntry o1) {
            return o1.words - o2.words;
        }
    }
}
