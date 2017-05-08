package de.thbingen.apps.nfcapp;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {

    TextView textViewInfo;
    NfcAdapter nfcAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewInfo = (TextView) findViewById(R.id.textView_info);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported!!!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "NFC is supported!!!", Toast.LENGTH_SHORT).show();
            nfcAdapter.setNdefPushMessageCallback(this, this);
        }
    }

    protected void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = ("Beam me up, Android!\n\nBeam Time: " + System.currentTimeMillis());
        NdefMessage msg = new NdefMessage(new NdefRecord[]{createMime("application/vnd.com.example.android.beam", text.getBytes())});
        return msg;
    }

    void processIntent(Intent intent) {
        textViewInfo = (TextView) findViewById(R.id.textView_info);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        textViewInfo.setText(new String(msg.getRecords()[0].getPayload()));
    }

}
