/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cnlaunch.physics.wifi.settings;

import com.cnlaunch.bluetooth.R;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;

class Summary {
    static String get(Context context, String ssid, DetailedState state, boolean isEphemeral) {
        if (state == DetailedState.CONNECTED && isEphemeral && ssid == null) {
            // Special case for connected + ephemeral networks.
            return context.getString(R.string.connected_via_wfa);
        }

        String[] formats = context.getResources().getStringArray((ssid == null)
                ? R.array.wifi_status : R.array.wifi_status_with_ssid);
        int index = state.ordinal();

        if (index >= formats.length || formats[index].length() == 0) {
            return "";
        }
        return String.format(formats[index], ssid);
    }

    static String get(Context context, DetailedState state, boolean isEphemeral) {
        return get(context, null, state, isEphemeral);
    }
}
