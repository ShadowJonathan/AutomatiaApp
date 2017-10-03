package com.github.shadowjonathan.automatiaapp.ffnet.select;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.ffnet.Category;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnCategoryTapListener}
 * interface.
 */
public class CategoryFragment extends Fragment {

    // TODO: Customize parameter argument names
    //private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    //private int mColumnCount = 1;

    private OnCategoryTapListener mListener;

    public CategoryFragment() {
    }

    public static CategoryFragment newInstance() {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        //args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            //mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ffnet_fragment_category_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new CategoryRecyclerAdapter(Category.getList(), mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCategoryTapListener) {
            mListener = (OnCategoryTapListener) context;
        } else {
            throw new RuntimeException(
                    context.toString()
                            + " must implement OnCategoryTapListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnCategoryTapListener {
        void onCTap(Category item);
    }

    public static class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder> {

        private final List<Category> mValues;
        private final OnCategoryTapListener mListener;

        public CategoryRecyclerAdapter(List<Category> items, OnCategoryTapListener listener) {
            mValues = items;
            mListener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ffnet_fragment_category, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mContentView.setText(mValues.get(position).getViewableName());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onCTap(holder.mItem);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mContentView;
            public Category mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mContentView = view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    /*
    public static class CategoryItem {

        /**
         * An array of sample (dummy) items.
         * /
        public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

        /**
         * A map of sample (dummy) items, by ID.
         * /
        public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

        private static final int COUNT = 25;

        static {
            // Add some sample items.
            for (int i = 1; i <= COUNT; i++) {
                addItem(createDummyItem(i));
            }
        }

        private static void addItem(DummyItem item) {
            ITEMS.add(item);
            ITEM_MAP.put(item.id, item);
        }

        private static DummyItem createDummyItem(int position) {
            return new DummyItem(String.valueOf(position), "Item " + position, makeDetails(position));
        }

        private static String makeDetails(int position) {
            StringBuilder builder = new StringBuilder();
            builder.append("Details about Item: ").append(position);
            for (int i = 0; i < position; i++) {
                builder.append("\nMore details information here.");
            }
            return builder.toString();
        }

        /**
         * A dummy item representing a piece of content.
         * /
        public static class DummyItem {
            public final String id;
            public final String content;
            public final String details;

            public DummyItem(String id, String content, String details) {
                this.id = id;
                this.content = content;
                this.details = details;
            }

            @Override
            public String toString() {
                return content;
            }
        }
    }*/
}
