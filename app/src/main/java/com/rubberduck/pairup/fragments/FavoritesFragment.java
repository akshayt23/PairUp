package com.rubberduck.pairup.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rubberduck.pairup.R;
import com.rubberduck.pairup.adapters.FavoritesRVAdapter;
import com.rubberduck.pairup.adapters.ShirtsRVAdapter;
import com.rubberduck.pairup.loader.MyCursorLoader;
import com.rubberduck.pairup.model.ApparelType;
import com.rubberduck.pairup.model.Pair;

// Show the pairs saved by the user for later use

public class FavoritesFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        FavoritesRVAdapter.OnFavoriteDeleteListener {

    public static String TAG = "Akshay/FavoritesFragment";
    private static final int FAVORITES_LOADER = 0;

    private OnFavoritesFragmentInteractionListener mListener;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager ;
    private FavoritesRVAdapter rvAdapter;
    private TextView tvNoFavorites;


    public static FavoritesFragment newInstance() {
        FavoritesFragment fragment = new FavoritesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        rvAdapter = new FavoritesRVAdapter(this, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_favorites, container, false);

        // Initialize a loader to load the shirts off the UI thread
        getLoaderManager().initLoader(FAVORITES_LOADER, null, this);

        tvNoFavorites = (TextView) view.findViewById(R.id.tv_no_favorites);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_favorites);
        recyclerView.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        // Set adapter for recycler view
        recyclerView.setAdapter(rvAdapter);

        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFavoritesFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFavoritesFragmentInteractionListener {
        public void onFavoritesFragmentInteraction(Uri uri);
    }

    // A pair has been deleted, restart the loader
    @Override
    public void onFavoriteDelete(Pair pair) {
        getLoaderManager().restartLoader(FAVORITES_LOADER, null, this);
    }

    // Create the loader which will load favorite pairs
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");
        if (id == FAVORITES_LOADER)
            return new MyCursorLoader(getActivity(), ApparelType.PAIR);
        else
            return null;

    }

    // Loading is done, update the UI
    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        if (data.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            tvNoFavorites.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoFavorites.setVisibility(View.GONE);
            rvAdapter.swapCursor(data);
            rvAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        rvAdapter.swapCursor(null);
    }
}
