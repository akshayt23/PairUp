package com.rubberduck.pairup.fragments;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rubberduck.pairup.R;

public class WardrobeFragment extends Fragment {

    private OnWardrobeFragmentInteractionListener mListener;
    private ViewPager viewPager;
    private FragmentPagerAdapter wardrobePagerAdapter;

    public static WardrobeFragment newInstance() {
        WardrobeFragment fragment = new WardrobeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public WardrobeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_wardrobe, container, false);

        // The view pager for showing shirts and trousers
        viewPager = (ViewPager) view.findViewById(R.id.vp_wardrobe);
        wardrobePagerAdapter = new WardrobePagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(wardrobePagerAdapter);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnWardrobeFragmentInteractionListener) activity;
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

    public interface OnWardrobeFragmentInteractionListener {
        public void onWardrobeFragmentInteraction(Uri uri);
    }

    // Adapter for the ViewPager with two items (shirts/trousers)
    public static class WardrobePagerAdapter extends FragmentPagerAdapter {
        private static int NUM_TABS = 2;

        public WardrobePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return NUM_TABS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ShirtsFragment.newInstance();
                case 1:
                    return TrousersFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Shirts";
                case 1:
                    return "Trousers";
                default:
                    return "";
            }
        }

    }

}
