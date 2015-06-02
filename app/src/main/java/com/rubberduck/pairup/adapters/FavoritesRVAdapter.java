package com.rubberduck.pairup.adapters;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.rubberduck.pairup.R;
import com.rubberduck.pairup.database.DBHelper;
import com.rubberduck.pairup.database.WardrobeContract;
import com.rubberduck.pairup.fragments.FavoritesFragment;
import com.rubberduck.pairup.fragments.LoginFragment;
import com.rubberduck.pairup.fragments.MainFragment;
import com.rubberduck.pairup.fragments.ShirtsFragment;
import com.rubberduck.pairup.model.Pair;
import com.rubberduck.pairup.model.Shirt;
import com.rubberduck.pairup.model.Trouser;
import com.rubberduck.pairup.view.SquaredImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Adapter for the recycler view showing shirts
public class FavoritesRVAdapter extends RecyclerView.Adapter<FavoritesRVAdapter.ViewHolder> {

    public static String TAG = "Akshay/FavoritesRVAdapter";

    private Context context;

    /*
     * We're wrapping a cursor adapter inside the recycler view adapter as RV adapter does not
     * natively support cursors
     */
    private CursorAdapter cursorAdapter;
    private OnFavoriteDeleteListener listener;

    private ShareDialog shareDialog;

    public FavoritesRVAdapter(FavoritesFragment parent, Cursor cursor) {
        Log.d(TAG, "FavoritesRVAdapter()");
        context = parent.getActivity();

        // Try and bind the interface callback
        try {
            listener = (OnFavoriteDeleteListener) parent;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnShirtDeleteListener");
        }

        // Facebook share dialog
        shareDialog = new ShareDialog(parent.getActivity());

        // Initialize the cursor adapter
        cursorAdapter = new CursorAdapter(context, cursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.pair, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Fetch the image paths from the cursor
                String shirtPath = cursor.getString(3);
                String trouserPath = cursor.getString(4);

                ImageView ivShirt = (ImageView) view.findViewById(R.id.iv_shirt);
                ImageView ivTrouser = (ImageView) view.findViewById(R.id.iv_trouser);

                Picasso.with(context).load(new File(shirtPath)).fit().centerCrop().into(ivShirt);
                Picasso.with(context).load(new File(trouserPath)).fit().centerCrop().into(ivTrouser);
            }
        };
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int itemType) {
        View view = cursorAdapter.newView(context, cursorAdapter.getCursor(), parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Cursor cursor = cursorAdapter.getCursor();
        cursor.moveToNext();

        int shirtId = cursor.getInt(1);
        int trouserId = cursor.getInt(2);
        String shirtPath = cursor.getString(3);
        String trouserPath = cursor.getString(4);

        Shirt currentShirt = new Shirt(shirtId, shirtPath);
        Trouser currentTrouser = new Trouser(trouserId, trouserPath);
        holder.currentPair = new Pair(currentShirt, currentTrouser);

        // Pass the handling of view binding to the cursor adapter
        cursorAdapter.bindView(holder.cardView, context, cursor);
    }

    @Override
    public int getItemCount() {
        return cursorAdapter.getCount();
    }

    // Data loading done, swap the old cursor with the new cursor
    public void swapCursor(Cursor newCursor) {
        if (cursorAdapter != null)
            cursorAdapter.swapCursor(newCursor);
    }

    // Class that defines each child view of the recycler view
    class ViewHolder extends RecyclerView.ViewHolder {
        Pair currentPair;

        CardView cardView;
        ImageView ivShirt, ivTrouser;
        ImageButton ibRefresh, ibShare, ibFavorite;

        ViewHolder(final View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.cv_pair);

            ivShirt = (ImageView) itemView.findViewById(R.id.iv_shirt);
            ivTrouser = (ImageView) itemView.findViewById(R.id.iv_trouser);

            ibRefresh = (ImageButton) itemView.findViewById(R.id.ib_refresh);
            ibRefresh.setVisibility(View.GONE);

            ibShare = (ImageButton) itemView.findViewById(R.id.ib_share);
            ibShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LoginFragment.getFBProfile() != null) {
                        if (shareDialog.canShow(ShareLinkContent.class)) {
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

                            Bitmap bmShirt = ((BitmapDrawable) ivShirt.getDrawable()).getBitmap();
                            Bitmap bmTrouser = ((BitmapDrawable) ivTrouser.getDrawable()).getBitmap();

                            Bitmap bmStitched = MainFragment.stitchBitmaps(bmShirt, bmTrouser);
                            SharePhoto photo = new SharePhoto.Builder().setBitmap(bmStitched).build();
                            SharePhotoContent photoContent = new SharePhotoContent.Builder().addPhoto(photo).build();

                            shareDialog.show(photoContent, ShareDialog.Mode.AUTOMATIC);
                        }
                    } else {
                        Toast.makeText(context, "Could not share to facebook!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ibFavorite = (ImageButton) itemView.findViewById(R.id.ib_favorite);
            ibFavorite.setImageResource(R.drawable.ic_toggle_star_24dp);
            ibFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DBHelper dbHelper = new DBHelper(context);
                    dbHelper.deleteFavPair(currentPair);
                    dbHelper.close();
                    if (listener != null)
                        listener.onFavoriteDelete(currentPair);

                    Toast.makeText(context, "Removed from favorites!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Interface which the FavoritesFragment will implement to handle delete event
    public interface OnFavoriteDeleteListener {
        public void onFavoriteDelete(Pair pair);
    }

}
