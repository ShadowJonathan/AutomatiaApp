package com.github.shadowjonathan.automatiaapp.ffnet.select;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.ffnet.Category;

// second class because then it's just so easier to make a search bar than replace all fragments
public class FFNetArchiveSelectActivity extends AppCompatActivity implements ArchiveFragment.OnArchiveTapListener {

    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private EditText searchEdit;
    private ArchiveFragment aFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ffnet_select_archive);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Category cat = Category.getCategory(getIntent().getStringExtra("cat"));
        aFrag = ArchiveFragment.newInstance(cat);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.root_layout, aFrag, "Archives")
                .commit();

        getSupportActionBar().setTitle(cat.getViewableName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fsa, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_search:
                handleMenuSearch();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isSearchOpened) {
            handleMenuSearch();
            return;
        }
        super.onBackPressed();
    }

    protected void handleMenuSearch() {
        final ActionBar action = getSupportActionBar(); //get the actionbar

        if (isSearchOpened) { //test if the search is open

            action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
            action.setDisplayShowTitleEnabled(true); //show the title in the action bar

            //hides the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);

            //add the search icon in the action bar
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_search));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            hideKeyboard();
            doSearch("");
            isSearchOpened = false;
        } else { //open the search entry

            action.setDisplayShowCustomEnabled(true); //enable it to display a
            // custom view in the action bar.
            action.setCustomView(R.layout.search_bar);//add the custom view
            action.setDisplayShowTitleEnabled(false); //hide the title
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            searchEdit = (EditText) action.getCustomView().findViewById(R.id.editSearch); //the text edito

            //this is a listener to do a search when the user clicks on search button
            searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        doSearch();
                        hideKeyboard();
                        return true;
                    }
                    return false;
                }
            });

            searchEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    doSearch();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            searchEdit.requestFocus();

            //open the keyboard focused in the edtSearch
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEdit, InputMethodManager.SHOW_IMPLICIT);

            //add the close icon
            mSearchAction.setIcon(R.drawable.ic_close);
            isSearchOpened = true;
        }
    }

    private void hideKeyboard() {
        View view = FFNetArchiveSelectActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void doSearch(String s) {
        Log.d("A_SEARCH", "Looking for '" + s + "'...");
        aFrag.filter(s);
    }

    private void doSearch() {
        doSearch(searchEdit.getText().toString());
    }

    @Override
    public void onATap(Category.ArchiveRef item) {
        //Toast.makeText(this, "Selected " + item.name, Toast.LENGTH_SHORT).show();
        Intent ffnetI = new Intent(this, FFNetStorySelectActivity.class);
        ffnetI.putExtra("archive", item.getArchive().makeID());
        startActivity(ffnetI);
    }
}
