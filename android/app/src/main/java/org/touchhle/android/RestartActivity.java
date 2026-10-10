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

import java.io.File;

/**
 * Runs in its own process (see AndroidManifest.xml, android:process=":restart")
 * so it survives the main process being killed. It waits for the old main
 * process to disappear, then launches the app again from scratch. Used for
 * "Back to menu" while a game is running.
 */
public class RestartActivity extends Activity {
    public static final String EXTRA_NEXT_INTENT = "next_intent";
    public static final String EXTRA_OLD_PID = "old_pid";

    private static final long POLL_MS = 100;
    private static final long TIMEOUT_MS = 5000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long waited = 0;
    private Intent next;
    private int oldPid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        next = getIntent().getParcelableExtra(EXTRA_NEXT_INTENT);
        oldPid = getIntent().getIntExtra(EXTRA_OLD_PID, -1);
        if (next == null) {
            finishAndExit();
            return;
        }
        poll();
    }

    private void poll() {
        boolean alive = oldPid > 0 && new File("/proc/" + oldPid).exists();
        if (alive && waited < TIMEOUT_MS) {
            waited += POLL_MS;
            handler.postDelayed(new Runnable() {
                public void run() {
                    poll();
                }
            }, POLL_MS);
            return;
        }
        if (alive) {
            // Old process is stuck; force it so the new one starts clean.
            android.os.Process.killProcess(oldPid);
        }
        startActivity(next);
        finishAndExit();
    }

    private void finishAndExit() {
        finish();
        // Don't leave this helper process lingering.
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
