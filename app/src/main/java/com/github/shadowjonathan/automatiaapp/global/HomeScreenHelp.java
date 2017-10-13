package com.github.shadowjonathan.automatiaapp.global;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeScreenHelp {
    public interface HasPalette {
        Palette getPalette();
    }

    public interface Categorisable {
        int getType(Palette fromPalette);
    }

    public static class Palette extends HashMap<Class, Integer> {
        public int getType(Class clazz) {
            for (Map.Entry<Class, Integer> entry : this.entrySet()) {
                if (clazz.equals(entry.getKey()))
                    return entry.getValue();
            }
            return -1;
        }
    }

    public static class GroupHolder extends ArrayList<Group> {
        public int getItemViewType(int position) {
            int start = 0;
            int end;
            for (Group g : this) {
                end = start + g.size();
                if (end > position && start <= position) {
                    return g.getItemViewType(position - start);
                } else {
                    start = end;
                }
            }
            return -1;
        }

        public Object getData(int position) {
            int start = 0;
            int end;
            for (Group g : this) {
                end = start + g.size();
                if (end > position && start <= position) {
                    return g.getData(position - start);
                } else {
                    start = end;
                }
            }
            return null;
        }

        public int amount() {
            int total = 0;
            for (Group g : this) total += g.size();
            return total;
        }

        public Group getGroupAt(int position) {
            int start = 0;
            int end;
            for (Group g : this) {
                end = start + g.size();
                if (end > position && start <= position) {
                    return g;
                } else {
                    start = end;
                }
            }
            return null;
        }
    }

    public static class Group extends ArrayList<CategorisedViewHolder> {
        protected Palette palette;
        protected SparseArray<Object> data = new SparseArray<>();

        public Group(Palette palette) {
            this.palette = palette;
        }

        public int getItemViewType(int position) {
            return this.get(position).getType(palette);
        }

        public Object getData(int position) {
            return data.get(position);
        }
    }

    public static abstract class CategorisedViewHolder extends RecyclerView.ViewHolder implements Categorisable {
        public CategorisedViewHolder(View view) {
            super(view);
        }
    }
}
