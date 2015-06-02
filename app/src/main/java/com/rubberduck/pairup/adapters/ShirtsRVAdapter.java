package com.rubberduck.pairup.adapters;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.rubberduck.pairup.R;
import com.rubberduck.pairup.database.DBHelper;
import com.rubberduck.pairup.database.WardrobeContract;
import com.rubberduck.pairup.fragments.ShirtsFragment;
import com.rubberduck.pairup.model.Shirt;
import com.rubberduck.pairup.view.SquaredImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Adapter for the recycler view showing shirts
public class ShirtsRVAdapter extends RecyclerView.Adapter<ShirtsRVAdapter.ViewHolder> {

    public static String TAG = "Akshay/ShirtsRVAdapter";

    private Context context;

    /*
     * We're wrapping a cursor adapter inside the recycler view adapter as RV adapter does not
     * natively support cursors
     */
    private CursorAdapter cursorAdapter;
    private SquaredImageView ivSquare;
    private OnShirtDeleteListener listener;

    public ShirtsRVAdapter(ShirtsFragment parent, Cursor cursor) {
        Log.d(TAG, "ShirtsRVAdapter()");
        this.context = parent.getActivity();

        // Try and bind the interface callback
        try {
            listener = (OnShirtDeleteListener) parent;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnShirtDeleteListener");
        }

        // Initialize the cursor adapter
        cursorAdapter = new CursorAdapter(context, cursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.grid_image, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                String path = cursor.getString(cursor.getColumnIndex(WardrobeContract.ShirtEntry.IMG_PATH));

                ivSquare = (SquaredImageView) view;
                Picasso.with(context).load(new File(path)).fit().centerCrop().into(ivSquare);
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

        int id = cursor.getInt(cursor.getColumnIndex(WardrobeContract.ShirtEntry._ID));
        String path = cursor.getString(cursor.getColumnIndex(WardrobeContract.ShirtEntry.IMG_PATH));

        // Set the current shirt
        Shirt currentShirt = new Shirt(id, path);
        holder.currentShirt = currentShirt;

        // Pass the handling of view binding to the cursor adapter
        cursorAdapter.bindView(holder.ivShirt, context, cursor);
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
        Shirt currentShirt;
        ImageView ivShirt;

        ViewHolder(final View itemView) {
            super(itemView);
            ivShirt = (SquaredImageView) itemView.findViewById(R.id.iv_grid);

            // Long click to delete
            ivShirt.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.dialog_delete_sure)
                            .setCancelable(false)
                            .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Delete the image
                                    File file = new File(currentShirt.getImagePath());
                                    if (file.delete()) {
                                        DBHelper dbHelper = new DBHelper(context);
                                        dbHelper.deleteShirt(currentShirt);
                                        dbHelper.close();

                                        // Check if the sharedPrefs has the current shirt. If present, mark as unusable
                                        SharedPreferences prefs =
                                                PreferenceManager.getDefaultSharedPreferences(context);
                                        int trouserId = prefs.getInt(context.getString(
                                                R.string.prefs_shirt_id), -1);
                                        if (trouserId == currentShirt.getId()) {
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putInt(context.getString(R.string.prefs_shirt_id), -1);
                                            editor.commit();
                                        }

                                        if (listener != null)
                                            listener.onShirtDelete(currentShirt);

                                        Toast.makeText(context, "Removed from wardrobe.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Could not remove from wardrobe. Please try again",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    builder.create();
                    builder.show();

                    return true;
                }
            });
        }
    }

    // Interface which the ShirtsFragment will implement to handle delete event
    public interface OnShirtDeleteListener {
        public void onShirtDelete(Shirt shirt);
    }

}
