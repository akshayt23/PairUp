package com.rubberduck.pairup.fragments;

import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.rubberduck.pairup.R;
import com.rubberduck.pairup.adapters.ShirtsRVAdapter;
import com.rubberduck.pairup.database.DBHelper;
import com.rubberduck.pairup.database.WardrobeContract;
import com.rubberduck.pairup.loader.MyCursorLoader;
import com.rubberduck.pairup.model.ApparelType;
import com.rubberduck.pairup.model.Shirt;

import java.util.ArrayList;
import java.util.List;

public class ShirtsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ShirtsRVAdapter.OnShirtDeleteListener {

    public static String TAG = "Akshay/ShirtsFragment";

    // Constant to define the laoder
    private static final int SHIRTS_LOADER = 0;

    private OnShirtsFragmentInteractionListener mListener;

    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private ShirtsRVAdapter rvAdapter;
    private TextView tvNoShirts;

    public static ShirtsFragment newInstance() {
        ShirtsFragment fragment = new ShirtsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ShirtsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        rvAdapter = new ShirtsRVAdapter(this, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shirts, container, false);

        // Initialize a loader to load the shirts off the UI thread
        getLoaderManager().initLoader(SHIRTS_LOADER, null, this);

        tvNoShirts = (TextView) view.findViewById(R.id.tv_no_shirts);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_shirts);
        recyclerView.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Set adapter for recycler view
        recyclerView.setAdapter(rvAdapter);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnShirtsFragmentInteractionListener) activity;
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

    public interface OnShirtsFragmentInteractionListener {
        public void onShirtsFragmentInteraction(Uri uri);
    }

    @Override
    public void onShirtDelete(Shirt shirt) {
        getLoaderManager().restartLoader(SHIRTS_LOADER, null, this);
    }

    // Create a new loader object
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == SHIRTS_LOADER)
            return new MyCursorLoader(getActivity(), ApparelType.SHIRT);
        else
            return null;
    }

    // Data loading is done, update the cursor
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            tvNoShirts.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoShirts.setVisibility(View.GONE);
            rvAdapter.swapCursor(data);
            rvAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvAdapter.swapCursor(null);
    }

}
