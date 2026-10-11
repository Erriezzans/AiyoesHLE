/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.touchhle.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Runs in its own process (see AndroidManifest.xml). Kills the main app
 * process and starts the app again, so the user lands back on the game list.
 */
public class RestartActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int pid = intent.getIntExtra("pid", -1);
        Intent next = intent.getParcelableExtra("next");
        if (pid > 0) {
            android.os.Process.killProcess(pid);
        }
        if (next != null) {
            next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(next);
        }
        finish();
        Runtime.getRuntime().exit(0);
    }
}
