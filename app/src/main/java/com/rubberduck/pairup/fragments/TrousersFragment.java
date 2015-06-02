package com.rubberduck.pairup.fragments;

import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.rubberduck.pairup.R;
import com.rubberduck.pairup.adapters.ShirtsRVAdapter;
import com.rubberduck.pairup.adapters.TrousersRVAdapter;
import com.rubberduck.pairup.database.DBHelper;
import com.rubberduck.pairup.loader.MyCursorLoader;
import com.rubberduck.pairup.model.ApparelType;
import com.rubberduck.pairup.model.Shirt;
import com.rubberduck.pairup.model.Trouser;

import java.util.ArrayList;
import java.util.List;

public class TrousersFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TrousersRVAdapter.OnTrouserDeleteListener {

    public static String TAG = "Akshay/TrousersFragment";

    // Constant to define the laoder
    private static final int TROUSERS_LOADER = 0;

    private OnTrousersFragmentInteractionListener mListener;

    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private TrousersRVAdapter rvAdapter;
    private TextView tvNoTrousers;

    public static TrousersFragment newInstance() {
        TrousersFragment fragment = new TrousersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TrousersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        rvAdapter = new TrousersRVAdapter(this, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_trousers, container, false);

        // Initialize a laoder to load the trousers off the UI thread
        getLoaderManager().initLoader(TROUSERS_LOADER, null, this);

        tvNoTrousers = (TextView) view.findViewById(R.id.tv_no_trousers);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_trousers);
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
            mListener = (OnTrousersFragmentInteractionListener) activity;
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

    public interface OnTrousersFragmentInteractionListener {
        public void onTrousersFragmentInteraction(Uri uri);
    }

    @Override
    public void onTrouserDelete(Trouser trouser) {
        getLoaderManager().restartLoader(TROUSERS_LOADER, null, this);
    }

    // Create a new loader object
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == TROUSERS_LOADER)
            return new MyCursorLoader(getActivity(), ApparelType.TROUSER);
        else
            return null;
    }

    // Data loading is done, update the cursor
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            tvNoTrousers.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoTrousers.setVisibility(View.GONE);
            rvAdapter.swapCursor(data);
            rvAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        rvAdapter.swapCursor(null);
    }

}
