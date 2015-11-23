/*
* Copyright (C) 2015 Ashley Davis (SgtCoDFish, https://github.com/SgtCoDFish)
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

package com.sgtcodfish.nfcplayground;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private NfcAdapter nfc = null;

    private TextView idNumberField = null;
    private TextView expiryDateField = null;

    private Button resetButton = null;

    private PendingIntent pendingIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfc = NfcAdapter.getDefaultAdapter(this);

        init();
        handleIntent(getIntent());
    }

    private void init() {
        if (nfc == null) {
            Log.e(TAG, "No NFC.");
            finish();
        }

        idNumberField = (TextView)findViewById(R.id.id_number_field);
        expiryDateField = (TextView)findViewById(R.id.expiry_date_field);

        resetButton = (Button)findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doResetButton();
            }
        });

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(nfc != null) {
            nfc.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        init();

        if(nfc != null) {
            nfc.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "GOT NEW INTENT: " + intent.getAction());
        setIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        final String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Log.d(TAG, "TECH_DISCOVERED");

            final Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            final IsoDep iso = IsoDep.get((Tag) tag);

            try {
                iso.connect();

                doGetApps(iso);
                doSelectPICC(iso);
                doGetFileIDs(iso);

                final String expiry = doGetFile(iso, (byte)0x02, (byte)0x0A);
                final String idNumber = doGetFile(iso, (byte)0x03, (byte)0x09);

                fillFields(idNumber, expiry);

                iso.close();
            } catch (IOException io) {
                Log.e(TAG, "Error communicating: " + io.toString());
            }
        }
    }

    // https://github.com/nfc-tools/libfreefare/blob/master/libfreefare/mifare_desfire.c
    private void doTransceive(IsoDep iso, byte[] command) throws IOException {
        final byte[] ret = iso.transceive(command);

        Log.d(TAG, "Sent: " + ByteArrayToHexString(command) + "\nRecv: " + ByteArrayToHexString(ret));
    }

    private String doTransceiveUTF8(IsoDep iso, byte[] command) throws IOException {
        final byte[] ret = iso.transceive(command);

        Log.d(TAG, "Sent: " + ByteArrayToHexString(command) + "\nRecv: " + ByteArrayToHexString(ret));

        final StringBuffer buffer = new StringBuffer();
        for(int i = 1; i < ret.length; i++) {
            buffer.append((char)ret[i]);
        }

        final String retStr = buffer.toString();

        Log.d(TAG, "UTF-8: " + retStr);
        return retStr;
    }

    private void doGetApps(IsoDep iso) throws IOException {
        final byte[] command = {(byte) 0x6a};
        doTransceive(iso, command);
    }

    private void doSelectPICC(IsoDep iso) throws  IOException {
        final byte[] command = { (byte)0x5a, (byte)0x30, (byte)0x85, (byte)0xF5};
        doTransceive(iso, command);
    }

    private void doGetFileIDs(IsoDep iso) throws  IOException {
        final byte[] command = { (byte)0x6f };
        doTransceive(iso, command);
    }

    private String doGetFile(IsoDep iso, byte fileID, byte len) throws  IOException {
        final byte[] command = {(byte) 0xbd, fileID, (byte) 0x00, (byte) 0x00, (byte) 0x00, len, (byte) 0x00, (byte) 0x00};
        return doTransceiveUTF8(iso, command);
    }

    private void fillFields(String idnum, String expiryDate) {
        Log.d(TAG, "Fill fields");
        idNumberField.setText(idnum);
        expiryDateField.setText(expiryDate);
        resetButton.setEnabled(true);
    }

    private void doResetButton() {
        Log.d(TAG, "Reset button");
        resetButton.setEnabled(false);
        idNumberField.setText("");
        expiryDateField.setText("");
    }

    /**
     * This method from the CardReader Android sample
     * Utility class to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     *
     * Copyright (C) 2013 The Android Open Source Project
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
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

