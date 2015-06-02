package com.rubberduck.pairup.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.rubberduck.pairup.R;
import com.rubberduck.pairup.adapters.ShirtsRVAdapter;
import com.rubberduck.pairup.database.DBHelper;
import com.rubberduck.pairup.model.ApparelType;
import com.rubberduck.pairup.model.Pair;
import com.rubberduck.pairup.model.Shirt;
import com.rubberduck.pairup.model.Trouser;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// The main fragment which shows the pair for taday

public class MainFragment extends Fragment {

    public static final String TAG = "Akshay/MainFragment";

    private OnMainFragmentInteractionListener mListener;

    private FloatingActionsMenu floatingMenu;
    private FloatingActionButton fabAddShirt, fabAddTrouser;

    private Pair currentPair;
    private TextView tvNoPairs;
    private FrameLayout todayTextAndCard;
    private CardView cardView;
    private ImageView ivShirt, ivTrouser;
    private ShareDialog shareDialog;

    // Request codes for various add requests
    private static final int CAPTURE_SHIRT_REQ_CODE = 101;
    private static final int CAPTURE_TROUSER_REQ_CODE = 102;
    private static final int PICK_SHIRT_REQ_CODE = 103;
    private static final int PICK_TROUSER_REQ_CODE = 104;
    private File imageFile;

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        // Try and fetch the daily pair from the preferences file
        currentPair = getPairFromPrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Setup the floating action button
        setupFab(rootView);

        // Initialize the FB share dialog
        shareDialog = new ShareDialog(getActivity());

        tvNoPairs = (TextView) rootView.findViewById(R.id.tv_no_pairs);
        todayTextAndCard = (FrameLayout) rootView.findViewById(R.id.fl_text_card);

        // Update UI based on whether we have a pair to display or not
        if (currentPair != null) {
            tvNoPairs.setVisibility(View.GONE);
            todayTextAndCard.setVisibility(View.VISIBLE);

            cardView = (CardView) todayTextAndCard.findViewById(R.id.pair);
            setupCard(cardView);

        } else {
            tvNoPairs.setVisibility(View.VISIBLE);
            todayTextAndCard.setVisibility(View.GONE);
        }

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMainFragmentInteractionListener) activity;
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

    public interface OnMainFragmentInteractionListener {
        public void onMainFragmentInteraction(String nameEntered);
    }

    // Customise the FAB button and define on click listeners
    public void setupFab(View rootView) {
        floatingMenu = (FloatingActionsMenu) rootView.findViewById(R.id.fab_menu);
        fabAddShirt = (FloatingActionButton) rootView.findViewById(R.id.fab_shirt);
        fabAddTrouser = (FloatingActionButton) rootView.findViewById(R.id.fab_trouser);

        // Add shirt event, show dialog for camera/gallery
        fabAddShirt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingMenu.collapse();

                final String[] options = new String[]{getString(R.string.dialog_camera), getString(R.string.dialog_gallery)};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialog_add_from)
                        .setItems(options, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    clickPicture(ApparelType.SHIRT);
                                } else {
                                    //Toast.makeText(getActivity(), "Gallery!", Toast.LENGTH_SHORT).show();
                                    pickFromGallery(ApparelType.SHIRT);
                                }
                            }
                        });
                builder.create();
                builder.show();

            }
        });

        // Add trouser event, show dialog for camera/gallery
        fabAddTrouser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingMenu.collapse();

                final String[] options = new String[]{getString(R.string.dialog_camera), getString(R.string.dialog_gallery)};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialog_add_from)
                        .setItems(options, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    clickPicture(ApparelType.TROUSER);
                                } else {
                                    pickFromGallery(ApparelType.TROUSER);
                                }
                            }
                        });
                builder.create();
                builder.show();

            }
        });

        floatingMenu.setVisibility(View.VISIBLE);
    }

    // Handle the card showing the pair for today and refresh/share/save actions
    public void setupCard(CardView card) {
        if (card == null) {
            return;
        }

        final DBHelper dbHelper = new DBHelper(getActivity().getApplicationContext());

        ivShirt = (ImageView) card.findViewById(R.id.iv_shirt);
        ivTrouser = (ImageView) card.findViewById(R.id.iv_trouser);

        // Load images into the views
        Picasso.with(getActivity()).load(new File(currentPair.getShirt().getImagePath()))
                .fit().centerCrop().into(ivShirt);
        Picasso.with(getActivity()).load(new File(currentPair.getTrouser().getImagePath()))
                .fit().centerCrop().into(ivTrouser);

        ImageButton ibRefresh = (ImageButton) card.findViewById(R.id.ib_refresh);
        ImageButton ibShare = (ImageButton) card.findViewById(R.id.ib_share);
        final ImageButton ibFavorite = (ImageButton) card.findViewById(R.id.ib_favorite);

        // Update the favorite button by checking if the current pair being shown is a favorite or not
        updateFavButton(ibFavorite, dbHelper.isPairAFav(currentPair));

        // Fetch a random pair and display
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPair = dbHelper.getRandomPair();

                if(currentPair != null) {
                    Picasso.with(getActivity()).load(
                            new File(currentPair.getShirt().getImagePath())).fit().into(ivShirt);
                    Picasso.with(getActivity()).load(
                            new File(currentPair.getTrouser().getImagePath())).fit().into(ivTrouser);
                }

                // Add pair to SharedPrefs
                addPairToPrefs(currentPair);

                // Check if pair is a favorite and mark the favorite button accordingly
                updateFavButton(ibFavorite, dbHelper.isPairAFav(currentPair));
            }
        });

        // Share the current pair being shown
        ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginFragment.getFBProfile() != null) {
                    if (shareDialog.canShow(ShareLinkContent.class)) {
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

                        Bitmap bmShirt = ((BitmapDrawable) ivShirt.getDrawable()).getBitmap();
                        Bitmap bmTrouser = ((BitmapDrawable) ivTrouser.getDrawable()).getBitmap();

                        // Stitch the bitmaps side-by-side for sharing on FB
                        Bitmap bmStitched = stitchBitmaps(bmShirt, bmTrouser);
                        SharePhoto photo = new SharePhoto.Builder().setBitmap(bmStitched).build();
                        SharePhotoContent photoContent = new SharePhotoContent.Builder().addPhoto(photo).build();

                        shareDialog.show(photoContent, ShareDialog.Mode.AUTOMATIC);
                    }
                } else {
                    Toast.makeText(getActivity(), "Could not share to facebook!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Save/Delete the pair for later
        ibFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dbHelper.isPairAFav(currentPair)) {
                    if (dbHelper.addFavPair(currentPair) != -1) {
                        ibFavorite.setImageResource(R.drawable.ic_toggle_star_24dp);
                        Toast.makeText(getActivity(), "Added to favorites!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Could not add to favorites!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (dbHelper.deleteFavPair(currentPair) != -1) {
                        ibFavorite.setImageResource(R.drawable.ic_toggle_star_outline_24dp);
                        Toast.makeText(getActivity(), "Removed from favorites!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Could not remove from favorites!", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        dbHelper.close();
    }

    // Try and fetch a pair from the preferences file, if not present, generate a random pair and return
    public Pair getPairFromPrefs() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int shirtId = prefs.getInt(getString(R.string.prefs_shirt_id), -1);
        int trouserId = prefs.getInt(getString(R.string.prefs_trouser_id), -1);
        String shirtPath = prefs.getString(getString(R.string.prefs_shirt_path), null);
        String trouserPath = prefs.getString(getString(R.string.prefs_trouser_path), null);

        Pair pair;
        if (shirtId != -1 && trouserId != -1 && shirtPath != null && trouserPath != null) {
            Log.d(TAG, "Pair found in prefs : " + shirtId + "," + trouserId);
            pair = new Pair(new Shirt(shirtId, shirtPath), new Trouser(trouserId, trouserPath));
        } else {
            Log.d(TAG, "Pair not present in prefs : " + shirtId + "," + trouserId);

            DBHelper dbHelper = new DBHelper(getActivity().getApplicationContext());
            pair = dbHelper.getRandomPair();
            dbHelper.close();

            // Add pair to SharedPrefs, pair could be null if database is empty
            if (pair != null)
                addPairToPrefs(pair);
        }

        return pair;
    }

    // Add the current pair to preferences file
    public void addPairToPrefs(Pair pair) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(getString(R.string.prefs_shirt_id), pair.getShirt().getId());
        editor.putInt(getString(R.string.prefs_trouser_id), pair.getTrouser().getId());
        editor.putString(getString(R.string.prefs_shirt_path), pair.getShirt().getImagePath());
        editor.putString(getString(R.string.prefs_trouser_path), pair.getTrouser().getImagePath());

        editor.commit();
    }

    // Helper function to start a capture image intent
    public void clickPicture(ApparelType apparelType) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a file to save the image and add it to the intent
        imageFile = getOutputMediaFile(getActivity(), apparelType);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));

        // Start the image capture intent, based on type
        if (apparelType == ApparelType.SHIRT)
            startActivityForResult(intent, CAPTURE_SHIRT_REQ_CODE);
        else
            startActivityForResult(intent, CAPTURE_TROUSER_REQ_CODE);
    }

    // Helper function to pick an image from the gallery
    public void pickFromGallery(ApparelType apparelType) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Create a file to save the image picked from gallery
        imageFile = getOutputMediaFile(getActivity(), apparelType);

        // Start the pick image intent, based on type
        if (apparelType == ApparelType.SHIRT)
            startActivityForResult(intent, PICK_SHIRT_REQ_CODE);
        else
            startActivityForResult(intent, PICK_TROUSER_REQ_CODE);
    }

    // Handle results returned from various requests
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode = " + requestCode + ", resultCode = " + resultCode
                + ", data = " + data);

        // If request code is for capturing shirt/trouser image
        if (requestCode == CAPTURE_SHIRT_REQ_CODE || requestCode == CAPTURE_TROUSER_REQ_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(getActivity(), "Added to your wardrobe.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Image saved to : " + imageFile.getAbsolutePath());

                DBHelper dbHelper = new DBHelper(getActivity().getApplicationContext());
                long result = 0;
                // Add shirt/trouser to database
                if (requestCode == CAPTURE_SHIRT_REQ_CODE) {
                    result = dbHelper.addShirt(new Shirt(1, imageFile.getAbsolutePath()));
                } else {
                    result = dbHelper.addTrouser(new Trouser(1, imageFile.getAbsolutePath()));
                }

                // If adding to database failed, alert user and delete the image
                if (result > 0)
                    Log.d(TAG, "Added to database : " + imageFile.getAbsolutePath());
                else {
                    Log.d(TAG, "Could not add to database.");
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Could not add to your wardrobe. Please try again.", Toast.LENGTH_SHORT).show();
                    deleteFile(imageFile.getAbsolutePath());
                }

                dbHelper.close();

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
                Toast.makeText(getActivity(), "Image capture failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }

        // If request code is for picking image from gallery
        else if (requestCode == PICK_SHIRT_REQ_CODE || requestCode == PICK_TROUSER_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getActivity(), "Added to your wardrobe.", Toast.LENGTH_SHORT).show();

                Log.d(TAG, "Copying from " + data.getData() + " to " + imageFile.getAbsolutePath());
                boolean success;
                try {
                    imageFile.createNewFile();
                    success = copyFromUriToFile(getActivity().getContentResolver().openInputStream(data.getData()),
                            new FileOutputStream(imageFile));

                    if(!success) {
                        Toast.makeText(getActivity(), "Could not add to your wardrobe. Please try again.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DBHelper dbHelper = new DBHelper(getActivity().getApplicationContext());
                    long result = 0;
                    // Add shirt/trouser to database
                    if (requestCode == PICK_SHIRT_REQ_CODE) {
                        result = dbHelper.addShirt(new Shirt(1, imageFile.getAbsolutePath()));
                    } else {
                        result = dbHelper.addTrouser(new Trouser(1, imageFile.getAbsolutePath()));
                    }

                    // If adding to database fails, alert user and delete the file
                    if (result > 0)
                        Log.d(TAG, "Added to database : " + imageFile.getAbsolutePath());
                    else {
                        Log.d(TAG, "Could not add to database.");
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Could not add to your wardrobe. Please try again.", Toast.LENGTH_SHORT).show();
                        deleteFile(imageFile.getAbsolutePath());
                    }

                    dbHelper.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
                Toast.makeText(getActivity(), "Could not add to your wardrobe. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }

        // Refresh the fragment
        MainFragment mainFragment = MainFragment.newInstance();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, mainFragment)
                .commit();
    }

    // Update the favorite button depending on whether the pair is a favorite or not
    public void updateFavButton(ImageButton ibFavorite, boolean isFav) {
        if (isFav)
            ibFavorite.setImageResource(R.drawable.ic_toggle_star_24dp);
        else
            ibFavorite.setImageResource(R.drawable.ic_toggle_star_outline_24dp);

    }

    // Create a File for saving an image
    private static File getOutputMediaFile(Context context, ApparelType apparelType) {
        File mediaStorageDir = null;

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        mediaStorageDir = new File(context.getExternalFilesDir(null), "PairUp/Images/");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File clickedImage;
        if (apparelType == ApparelType.SHIRT)
            clickedImage = new File(mediaStorageDir.getPath() + File.separator +
                    "SHIRT_" + timeStamp + ".jpg");
        else
            clickedImage = new File(mediaStorageDir.getPath() + File.separator +
                    "TROUSER" + timeStamp + ".jpg");

        return clickedImage;
    }

    // Helper function to delete a file
    public void deleteFile(String path) {
        File file = new File(path);
        file.delete();
    }

    // Make a copy of the image pointed to by the URI into a file
    public boolean copyFromUriToFile(InputStream inputStream, FileOutputStream fos) throws IOException {
        if (inputStream == null || fos == null)
            return false;

        int read = 0;
        byte[] buffer = new byte[1024];

        while ((read = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }

        fos.close();
        inputStream.close();

        return true;
    }

    // Helper function to stitch two bitmaps side-by-side
    public static Bitmap stitchBitmaps(Bitmap bm1, Bitmap bm2) {
        Bitmap stitched = null;

        int width, height = 0;

        if(bm1.getWidth() > bm2.getWidth()) {
            width = bm1.getWidth() + bm2.getWidth();
            height = bm1.getHeight();
        } else {
            width = bm2.getWidth() + bm2.getWidth();
            height = bm1.getHeight();
        }

        stitched = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(stitched);

        comboImage.drawBitmap(bm1, 0f, 0f, null);
        comboImage.drawBitmap(bm2, bm1.getWidth(), 0f, null);

        return stitched;
    }
}
