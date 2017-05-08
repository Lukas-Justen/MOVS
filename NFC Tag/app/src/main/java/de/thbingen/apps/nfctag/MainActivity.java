package de.thbingen.apps.nfctag;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private EditText editText_Message;
    private Button button_Write;
    private Button button_Read;
    private TextView textView_Message;

    private NfcAdapter nfcAdapter;

    private Action action = Action.Read;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported!!!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "NFC is supported!!!", Toast.LENGTH_SHORT).show();
        }

        editText_Message = (EditText) findViewById(R.id.editText_Message);
        button_Write = (Button) findViewById(R.id.button_Write);
        button_Read = (Button) findViewById(R.id.button_Read);
        textView_Message = (TextView) findViewById(R.id.textView_Message);

        button_Write.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                action = Action.Write;
            }
        });

        button_Read.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                action = Action.Read;
            }
        });
    }

    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    private void writeToNfc(Ndef ndef, String message) {
        if (ndef != null) {
            try {
                ndef.connect();
                NdefRecord mimeRecord = NdefRecord.createMime("text/plain", message.getBytes(Charset.forName("US-ASCII")));
                ndef.writeNdefMessage(new NdefMessage(mimeRecord));
                ndef.close();
                Toast.makeText(this, "Writing to NFC-Tag succeeded!!!", Toast.LENGTH_SHORT).show();
            } catch (IOException | FormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "Writing to NFC-Tag failed!!!", Toast.LENGTH_SHORT).show();
            }
        }
        action = Action.Read;
    }

    private void readFromNFC(Ndef ndef) {
        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            String message = new String(ndefMessage.getRecords()[0].getPayload());
            textView_Message.setText(message);
            ndef.close();
            Toast.makeText(this, "Reading from NFC-Tag succeeded!!!", Toast.LENGTH_SHORT).show();
        } catch (IOException | FormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Reading from NFC-Tag failed!!!", Toast.LENGTH_SHORT).show();
        }
        action = Action.Read;
    }

    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (tag != null) {
            Ndef ndef = Ndef.get(tag);
            if (action == Action.Write) {
                writeToNfc(ndef, editText_Message.getText().toString());
            } else {
                readFromNFC(ndef);
            }
        }
    }

}

enum Action {

    Write, Read;

}
