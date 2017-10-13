package com.github.shadowjonathan.automatiaapp.ffnet.select;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener;
import com.github.shadowjonathan.automatiaapp.MainActivity;
import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.ffnet.Archive;
import com.github.shadowjonathan.automatiaapp.ffnet.Registry;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

// second class because then it's just so easier to make a search bar than replace all fragments
public class FFNetStorySelectActivity extends AppCompatActivity implements StoryFragment.OnStoryTapListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "STORY_SELECT";
    private static final int MAX_ITEMS_PER_REQUEST = 25;
    public Toolbar toolbar;
    public RecyclerView recyclerView;
    public ProgressBar progressBar;
    public TextView progressText;
    public SwipeRefreshLayout swipe;
    private Archive archive;
    private MenuItem filter;
    private MenuItem refresh;
    private MenuItem pin;
    private StoryFragment.StoryRecyclerAdapter adapter;
    private int page;
    private int count = MAX_ITEMS_PER_REQUEST;
    private LinearLayoutManager layoutManager;
    private List<Registry.RegistryEntry> items;
    private InfiniteScrollListener ISL;
    private int by_current = 0;
    private int status_current = 0; // 0: any, 1: in-progress, 2: complete


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ffnet_fragment_story_list);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        archive = Archive.getArchive(getIntent().getStringExtra("archive"));
        initViews();
        initRecyclerView();
        swipe.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {

                long startTime = System.nanoTime();
                items = archive.reg.getList();
                long endTime = System.nanoTime();
                long duration = (endTime - startTime);
                Log.d(TAG, "getList: " + duration / 1000000 + "ms");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (items.isEmpty()) {
                            swipe.setRefreshing(false);
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
                                            recyclerView.setAdapter(new StoryFragment.StoryRecyclerAdapter(getSubItems(items, MAX_ITEMS_PER_REQUEST), FFNetStorySelectActivity.this, FFNetStorySelectActivity.this));
                                            recyclerView.invalidate();
                                            sortList();
                                            endLoad();
                                        }
                                    });
                                }
                            });
                        } else {
                            onInitRefresh();
                            swipe.setRefreshing(false);
                        }
                    }
                });
            }
        }).start();

        getSupportActionBar().setTitle(archive.getViewableName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ffa, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        filter = menu.findItem(R.id.action_filter);
        refresh = menu.findItem(R.id.action_refresh);
        pin = menu.findItem(R.id.action_pin);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            // MENU
            case R.id.action_filter:
                openSort();
                return true;
            case R.id.action_refresh:
                if (archive.refresh())
                    Toast.makeText(this, "Queried refresh", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Refresh already queried", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_pin:
                if (archive.togglePin())
                    Toast.makeText(this, "Pinned archive", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Unpinned archive", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh: Called refresh");
        swipe.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Registry.RegistryList items = archive.reg.getList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FFNetStorySelectActivity.this.items = items;
                        sortList();
                        swipe.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    public void onInitRefresh() {
        swipe.setRefreshing(true);
        sortList();
        swipe.setRefreshing(false);
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressText = (TextView) findViewById(R.id.progress_bar_subtitle);

        swipe = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipe.setOnRefreshListener(this);
        swipe.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.setAdapter(new StoryFragment.StoryRecyclerAdapter(getSubItems(items, MAX_ITEMS_PER_REQUEST), this, this));
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
            @Override
            public void onScrolledToEnd(final int firstVisibleItemPosition) {
                Log.d(TAG, "onScrolledToEnd: " + firstVisibleItemPosition);
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

    private void sortList(String by, int status) {
        Collections.sort(this.items, getByType(by));
        Filter.sort(this.items, new Filter.byStatus(status));
        status_current = status;
        recyclerView.setAdapter(new StoryFragment.StoryRecyclerAdapter(getSubItems(this.items, MAX_ITEMS_PER_REQUEST), this, this));
    }

    private void sortList() {
        Collections.sort(this.items, getByType(by_current));
        Filter.sort(this.items, new Filter.byStatus(status_current));
        recyclerView.setAdapter(new StoryFragment.StoryRecyclerAdapter(getSubItems(this.items, MAX_ITEMS_PER_REQUEST), this, this));
    }

    private Comparator<Registry.RegistryEntry> getByType(String by) {
        switch (by) {
            case "Date (updated)":
                by_current = 0;
                return new Sort.byUpdated();
            case "Date (published)":
                by_current = 1;
                return new Sort.byPublished();
            case "Favorites":
                by_current = 2;
                return new Sort.byFavorites();
            case "Follows":
                by_current = 3;
                return new Sort.byFollows();
            case "Reviews":
                by_current = 4;
                return new Sort.byReviews();
            case "Words":
                by_current = 5;
                return new Sort.byWords();
        }
        return null;
    }

    private Comparator<Registry.RegistryEntry> getByType(int by) {
        switch (by) {
            case 0:
                return new Sort.byUpdated();
            case 1:
                return new Sort.byPublished();
            case 2:
                return new Sort.byFavorites();
            case 3:
                return new Sort.byFollows();
            case 4:
                return new Sort.byReviews();
            case 5:
                return new Sort.byWords();
        }
        return null;
    }

    private void openSort() {
        LayoutInflater li = LayoutInflater.from(this);

        View promptsView = li.inflate(R.layout.ffnet_fragment_story_sort_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptsView);

        // set dialog message

        alertDialogBuilder.setTitle("Sort stories");
        //alertDialogBuilder.setIcon(R.mipmap.ic_launcher);
        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        final Spinner spinner_by = (Spinner) promptsView
                .findViewById(R.id.spinner_by);
        final Spinner spinner_status = (Spinner) promptsView
                .findViewById(R.id.spinner_status);
        final Button button = (Button) promptsView
                .findViewById(R.id.button_done);

        ArrayAdapter<CharSequence> adapter_by = ArrayAdapter.createFromResource(this,
                R.array.story_sort_by, android.R.layout.simple_spinner_item);
        adapter_by.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_by.setAdapter(adapter_by);
        spinner_by.setSelection(by_current);

        ArrayAdapter<CharSequence> adapter_status = ArrayAdapter.createFromResource(this,
                R.array.story_filter_status, android.R.layout.simple_spinner_item);
        adapter_status.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_status.setAdapter(adapter_status);
        spinner_status.setSelection(status_current);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                swipe.setRefreshing(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        items = archive.reg.getList();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sortList(spinner_by.getSelectedItem().toString(), spinner_status.getSelectedItemPosition());
                                swipe.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });
    }
}
