package com.theriddlebrothers.disruptor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Created with IntelliJ IDEA.
 * User: Joshua Riddle
 * Date: 10/5/12
 * Time: 6:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exitMenuItem:
                finish();
                System.exit(0);
                return true;
            case R.id.aboutMenuItem:
                AlertDialog dialog = createAboutDialog();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private AlertDialog createAboutDialog() {
        // Use the Builder class for convenient dialog construction
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = inflater.inflate(R.layout.dialog_about, null);
        builder.setView(dialogView)
                .setNegativeButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }
                });

        Button visitWebsite = (Button)dialogView.findViewById(R.id.visitWebsite);
        visitWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse(getString(R.string.url)) );
                startActivity( browse );
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
