package fr.sqli.tintinspacerocketcontrolapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import fr.sqli.tintinspacerocketcontrolapp.player.AddPlayerActivity;
import fr.sqli.tintinspacerocketcontrolapp.player.Player;
import fr.sqli.tintinspacerocketcontrolapp.player.ScanQRCodeActivity;
import fr.sqli.tintinspacerocketcontrolapp.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_NEW_PLAYER = 0;
    private ImageView scanButton;
    private ImageView addPlayerManuallyButton;
    private static final int ZXING_CAMERA_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        scanButton = findViewById(R.id.scan_button);
        addPlayerManuallyButton = findViewById(R.id.add_player_manually);
        prepareView();
    }

    private void prepareView() {
        scanButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
                startActivityForResult(new Intent(this, ScanQRCodeActivity.class), REQUEST_CODE_NEW_PLAYER);
            } else {
                startActivityForResult(new Intent(this, ScanQRCodeActivity.class), REQUEST_CODE_NEW_PLAYER);
            }
        });
        addPlayerManuallyButton.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddPlayerActivity.class), REQUEST_CODE_NEW_PLAYER);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEW_PLAYER && resultCode == RESULT_OK) {
            final Player player = (Player) data.getExtras().getSerializable(AddPlayerActivity.PLAYER_INFORMATION);
            if (player.isNotEmpty()) {
                Toast.makeText(this, "Démarrage du jeu pour " + player.getFirstName() + " !", Toast.LENGTH_SHORT).show();
                // TODO
            } else {
                Toast.makeText(this, "Prénom / Nom / Email obligatoires", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
