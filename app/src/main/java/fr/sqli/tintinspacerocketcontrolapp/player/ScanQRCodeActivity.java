package fr.sqli.tintinspacerocketcontrolapp.player;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import fr.sqli.tintinspacerocketcontrolapp.R;
import it.auron.library.mecard.MeCard;
import it.auron.library.mecard.MeCardParser;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ScanQRCodeActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;

    public static final int REQUEST_CODE_NEW_PLAYER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZBarScannerView(this);
        setContentView(R.layout.activity_scan_qrcode);
        LinearLayout view = findViewById(R.id.camera);
        view.addView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Player player = new Player();
        try {
            String rawResultText = rawResult.getContents();
            if(rawResultText != null){
                if (rawResultText.startsWith("BEGIN:MECARD")){
                    MeCard meCard = MeCardParser.parse(rawResultText);
                    player = PlayerFactory.getPlayer(meCard);
                } else if (rawResultText.startsWith("BEGIN:VCARD")){
                    VCard vCard = Ezvcard.parse(rawResultText).first();
                    player = PlayerFactory.getPlayer(vCard);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de la récupération du qrcode", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(this, AddPlayerActivity.class);
        intent.putExtra(AddPlayerActivity.PLAYER_INFORMATION, player);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEW_PLAYER && resultCode == RESULT_OK) {
            // Les infos du joueurs sont dans les extras de l'intent
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
