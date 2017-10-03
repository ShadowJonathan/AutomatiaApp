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
import com.github.shadowjonathan.automatiaapp.global.Helper;

import java.util.List;

public class ArchiveFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_PARENT_CAT = "category";
    // TODO: Customize parameters
    //private int mColumnCount = 1;
    public Category parentCategory;
    private OnArchiveTapListener mListener;

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
            recyclerView.setAdapter(new CategoryRecyclerAdapter(parentCategory.getArchives(), mListener));
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

    public interface OnArchiveTapListener {
        void onATap(Category.ArchiveRef item);
    }

    public static class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder> {

        private final List<Category.ArchiveRef> mValues;
        private final OnArchiveTapListener mListener;

        public CategoryRecyclerAdapter(List<Category.ArchiveRef> items, OnArchiveTapListener listener) {
            mValues = items;
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
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onATap(holder.mItem);
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
            public final TextView mAmountView;
            public Category.ArchiveRef mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mContentView = view.findViewById(R.id.content);
                mAmountView = view.findViewById(R.id.len);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
