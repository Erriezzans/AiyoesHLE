/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.touchhle.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * Runs in its own process (see AndroidManifest.xml). Kills the main app
 * process, waits for Android to notice it is gone, and then starts the app
 * again so the user lands back on the game list.
 */
public class RestartActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int pid = intent.getIntExtra("pid", -1);
        final Intent next = intent.getParcelableExtra("next");
        if (pid > 0) {
            android.os.Process.killProcess(pid);
        }
        // Starting the app right away can race with Android cleaning up the
        // dead process and end up doing nothing, so give it a moment.
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                if (next != null) {
                    next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    try {
                        startActivity(next);
                    } catch (Throwable t) {
                        android.util.Log.e("touchHLE", "Couldn't relaunch app", t);
                    }
                }
                finish();
                // Give the launch a moment to be handed over before exiting.
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        Runtime.getRuntime().exit(0);
                    }
                }, 500);
            }
        }, 700);
    }
}
