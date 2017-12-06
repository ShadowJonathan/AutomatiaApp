package com.github.shadowjonathan.automatiaapp.ffnet.select;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.ffnet.Category;
import com.github.shadowjonathan.automatiaapp.global.Helper;
import com.github.shadowjonathan.automatiaapp.global.HomeScreenHelp;

import java.util.ArrayList;
import java.util.List;

public class ArchiveFragment extends Fragment {

    private static final String ARG_PARENT_CAT = "category";
    private static String TAG = "AR_frag";
    public Category parentCategory;
    private OnArchiveTapListener mListener;
    private ArchiveRecyclerAdapter CRA;

    public ArchiveFragment() {
    }

    public static ArchiveFragment newInstance(Category cat) {
        ArchiveFragment fragment = new ArchiveFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARENT_CAT, cat.name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            parentCategory = Category.getCategory(getArguments().getString(ARG_PARENT_CAT));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ffnet_fragment_archive_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            CRA = new ArchiveRecyclerAdapter(parentCategory.getArchives(), mListener);
            recyclerView.setAdapter(CRA);
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnArchiveTapListener) {
            mListener = (OnArchiveTapListener) context;
        } else {
            throw new RuntimeException(
                    context.toString()
                            + " must implement OnArchiveTapListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void filter(String s) {
        CRA.filter(s);
    }

    public interface OnArchiveTapListener {
        void onATap(Category.ArchiveRef item);
    }

    public static class ArchiveRecyclerAdapter extends RecyclerView.Adapter<ArchiveRecyclerAdapter.ViewHolder> {

        private final List<Category.ArchiveRef> allValues;
        private final OnArchiveTapListener mListener;
        private List<Category.ArchiveRef> mValues;

        public ArchiveRecyclerAdapter(List<Category.ArchiveRef> items, OnArchiveTapListener listener) {
            mValues = items;
            allValues = new ArrayList<>(items);
            mListener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ffnet_fragment_archive, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            Category.ArchiveRef ar = mValues.get(position);
            holder.mContentView.setText(ar.name);
            holder.mAmountView.setText(Helper.formatNumber(ar.len));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onATap(holder.mItem);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public void filter(String s) {
            s = s.toLowerCase();
            mValues.clear();
            for (Category.ArchiveRef a : allValues) {
                if (a.name.toLowerCase().contains(s)) {
                    mValues.add(a);
                }
            }
            notifyDataSetChanged();

            Log.d(TAG, "filtering '" + s + String.format("'... (%d/%d)", mValues.size(), allValues.size()));
        }

        public static class ViewHolder extends HomeScreenHelp.CategorisedViewHolder {
            public final View mView;
            public final TextView mContentView;
            public final TextView mAmountView;
            public Category.ArchiveRef mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mContentView = (TextView) view.findViewById(R.id.content);
                mAmountView = (TextView) view.findViewById(R.id.len);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }

            @Override
            public int getType(HomeScreenHelp.Palette fromPalette) {
                return fromPalette.getType(ViewHolder.class);
            }

            public static class Ref {
                public int amount;
                public String text;

                public Ref(int amount, String text) {
                    this.amount = amount;
                    this.text = text;
                }
            }
        }
    }
}
