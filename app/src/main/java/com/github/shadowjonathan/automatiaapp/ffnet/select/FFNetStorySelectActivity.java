package com.github.shadowjonathan.automatiaapp.ffnet.select;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener;
import com.github.shadowjonathan.automatiaapp.MainActivity;
import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.ffnet.Archive;
import com.github.shadowjonathan.automatiaapp.ffnet.Registry;

import java.util.LinkedList;
import java.util.List;

// second class because then it's just so easier to make a search bar than replace all fragments
public class FFNetStorySelectActivity extends AppCompatActivity implements StoryFragment.OnStoryTapListener {
    private Archive archive;
    private static final String TAG = "STORY_SELECT";
    private MenuItem filter;
/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ffnet_select_archive);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Archive a = Archive.getArchive(getIntent().getStringExtra("archive"));
        sFrag = StoryFragment.newInstance(a);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.root_layout, sFrag, "Story")
                .commit();

        getSupportActionBar().setTitle(a.getViewableName());
    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ffa, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        filter = menu.findItem(R.id.action_filter);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        super.onBackPressed();
    }

    @Override
    public void onSTap(Registry.RegistryEntry item) {
        Toast.makeText(this, "Selected " + item.title, Toast.LENGTH_SHORT).show();
    }

    private static final int MAX_ITEMS_PER_REQUEST = 25;

    public Toolbar toolbar;
    public RecyclerView recyclerView;
    public ProgressBar progressBar;
    public TextView progressText;
    private int page;
    private int count = MAX_ITEMS_PER_REQUEST;

    private LinearLayoutManager layoutManager;
    private List<Registry.RegistryEntry> items;
    private InfiniteScrollListener ISL;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ffnet_fragment_story_list);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        archive = Archive.getArchive(getIntent().getStringExtra("archive"));

        items = archive.reg.getList();
        initViews();
        initRecyclerView();

        if (items.isEmpty()) {
            startLoad();
            archive.getRegistry(new Archive.RegistryUpdateCallback() {
                @Override
                public void onUpdate(final String text) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressText.setVisibility(View.VISIBLE);
                            progressText.setText(text);
                        }
                    });
                }

                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            items = archive.reg.getList();
                            recyclerView.setAdapter(new StoryFragment.StoryRecyclerAdapter(items, FFNetStorySelectActivity.this, FFNetStorySelectActivity.this));
                            recyclerView.invalidate();
                            endLoad();
                        }
                    });
                }
            });
        }

        getSupportActionBar().setTitle(archive.getViewableName());
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressText = (TextView) findViewById(R.id.progress_bar_subtitle);
    }

    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new StoryFragment.StoryRecyclerAdapter(getSubItems(items,MAX_ITEMS_PER_REQUEST), this, this));
        ISL = createInfiniteScrollListener();
        recyclerView.addOnScrollListener(ISL);
    }

    private List<Registry.RegistryEntry> getSubItems(List<Registry.RegistryEntry> list, int end) {
        if (list.size() < end) {
            return list;
        } else {
            return list.subList(0, end);
        }
    }

    @NonNull
    private InfiniteScrollListener createInfiniteScrollListener() {
        return new InfiniteScrollListener(MAX_ITEMS_PER_REQUEST, layoutManager) {
            @Override public void onScrolledToEnd(final int firstVisibleItemPosition) {
                startLoad();
                int start = ++page * MAX_ITEMS_PER_REQUEST;
                final boolean allItemsLoaded = start >= items.size();
                if (allItemsLoaded) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    int end = start + MAX_ITEMS_PER_REQUEST;
                    final List<Registry.RegistryEntry> itemsLocal = getItemsToBeLoaded(start, end);
                    count = end;
                    refreshView(recyclerView, new StoryFragment.StoryRecyclerAdapter(itemsLocal, FFNetStorySelectActivity.this, FFNetStorySelectActivity.this), firstVisibleItemPosition);
                }
                endLoad();
            }
        };
    }

    @NonNull
    private List<Registry.RegistryEntry> getItemsToBeLoaded(int start, int end) {
        List<Registry.RegistryEntry> newItems = items.subList(start, end);
        final List<Registry.RegistryEntry> oldItems = ((StoryFragment.StoryRecyclerAdapter) recyclerView.getAdapter()).getItems();
        final List<Registry.RegistryEntry> itemsLocal = new LinkedList<>();
        itemsLocal.addAll(oldItems);
        itemsLocal.addAll(newItems);
        return itemsLocal;
    }

    private void startLoad() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void endLoad() {
        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
    }
}
