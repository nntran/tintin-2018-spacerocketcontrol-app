package fr.sqli.tintinspacerocketcontrolapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import fr.sqli.tintinspacerocketcontrolapp.player.AddPlayerActivity;
import fr.sqli.tintinspacerocketcontrolapp.player.Player;
import fr.sqli.tintinspacerocketcontrolapp.player.ScanQRCodeActivity;
import fr.sqli.tintinspacerocketcontrolapp.service.SpaceRocketService;
import fr.sqli.tintinspacerocketcontrolapp.settings.SettingsActivity;
import retrofit2.HttpException;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_NEW_PLAYER = 0;
    private ImageView scanButton;
    private ImageView addPlayerManuallyButton;
    private ImageView playDemoButton;
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Player currentPLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        scanButton = findViewById(R.id.scan_button);
        addPlayerManuallyButton = findViewById(R.id.add_player_manually);
        playDemoButton = findViewById(R.id.play_demo);
        prepareView();
    }

    private void prepareView() {
        scanButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
            } else {
                startActivityForResult(new Intent(this, ScanQRCodeActivity.class), REQUEST_CODE_NEW_PLAYER);
            }
        });

        addPlayerManuallyButton.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddPlayerActivity.class), REQUEST_CODE_NEW_PLAYER);
        });

        playDemoButton.setOnClickListener(v -> {
            final Player player = new Player();
            player.setFirstName("Utilisateur démo");
            player.setLastName("Démo");
            player.setCompany("Démo");
            player.setEmail("Démo");
            player.setGenderMale(true);
            player.setTwitter("Démo");
            internalStartGame(player);
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
                internalStartGame(player);
            } else {
                Toast.makeText(this, "Prénom / Nom / Email obligatoires", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("CheckResult")
    private void internalStartGame(final Player player) {
        this.currentPLayer = player;
        // TODO
        SpaceRocketService.getInstance(this).start(player).subscribe(start -> {
            player.setId(start.gamerId);
            Toast.makeText(this, "Démarrage du jeu pour " + player + " !", Toast.LENGTH_SHORT).show();
        }, throwable -> {
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            if (throwable instanceof HttpException) {
                final HttpException httpException = (HttpException) throwable;
                alertDialog.setMessage(httpException.response().errorBody().string());
            } else {
                alertDialog.setMessage(throwable.getMessage());
            }

            alertDialog.show();
        });

    }
}
