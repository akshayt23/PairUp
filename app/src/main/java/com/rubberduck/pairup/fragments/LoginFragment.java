package com.rubberduck.pairup.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.rubberduck.pairup.R;

// Facebook login fragment

public class LoginFragment extends Fragment {

    public static String TAG = "Akshay/LoginFragment";

    private OnLoginFragmentInteractionListener mListener;
    private FrameLayout frameLayoutParent;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessToken accessToken;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoginFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onResume() {
        // If user is logged in and pass the result to the Activity
        if (isLoggedIn()) {
            onLoginEvent(true);
        }
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        frameLayoutParent = (FrameLayout) view.findViewById(R.id.fl_parent);
        frameLayoutParent.getBackground().setColorFilter(
                getResources().getColor(R.color.primary_light), PorterDuff.Mode.MULTIPLY);

        loginButton = (LoginButton) view.findViewById(R.id.login_button);

        // If using in a fragment
        loginButton.setFragment(this);
        customiseLoginButton();

        // Callback registration for login attempt, and pass the result to the Activity
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getActivity(), "Logged in successfully!", Toast.LENGTH_SHORT).show();
                onLoginEvent(true);
            }
            @Override
            public void onCancel() {
                Toast.makeText(getActivity(), "Login canceled.", Toast.LENGTH_SHORT).show();
                onLoginEvent(false);
            }
            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getActivity(), "Failed to log in. Please try again.", Toast.LENGTH_SHORT).show();
                onLoginEvent(false);
            }
        });

        return view;
    }

    // Pass the login result to the activity
    public void onLoginEvent(boolean loginSuccess) {
        if (mListener != null) {
            mListener.onLoginFragmentInteraction(loginSuccess);
        }
    }

    // Return the FB Profile logged in
    public static Profile getFBProfile() {
        return Profile.getCurrentProfile();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Interface to interact with the activity
    public interface OnLoginFragmentInteractionListener {
        public void onLoginFragmentInteraction(boolean onLoginSuccess);
    }

    // Handle result returned by the FB login activity by passing it to the callback manager
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // Customise the FB Login button looks
    public void customiseLoginButton() {
        float fbIconScale = 1.4F;
        Drawable drawable = getActivity().getResources().getDrawable(
                com.facebook.R.drawable.com_facebook_button_icon);
        drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*fbIconScale),
                (int)(drawable.getIntrinsicHeight()*fbIconScale));
        loginButton.setCompoundDrawables(drawable, null, null, null);
        loginButton.setCompoundDrawablePadding(getActivity().getResources().
                getDimensionPixelSize(R.dimen.fb_margin_override_textpadding));
        loginButton.setPadding(
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_lr),
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_top_bottom),
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_lr),
                getActivity().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_top_bottom));
    }

    // Check if user is logged in by validating the access token
    public boolean isLoggedIn() {
        accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()) {
            Log.d(TAG, "Logged In!");
            return true;
        }
        else {
            Log.d(TAG, "Logged Out!");
            return false;
        }
    }

}
