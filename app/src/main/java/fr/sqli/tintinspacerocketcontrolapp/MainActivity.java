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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;

import fr.sqli.tintinspacerocketcontrolapp.player.AddPlayerActivity;
import fr.sqli.tintinspacerocketcontrolapp.player.Player;
import fr.sqli.tintinspacerocketcontrolapp.player.ScanQRCodeActivity;
import fr.sqli.tintinspacerocketcontrolapp.pojos.CurrentTry;
import fr.sqli.tintinspacerocketcontrolapp.simon.ex.GameFinishedException;
import fr.sqli.tintinspacerocketcontrolapp.simon.pojos.Colors;
import fr.sqli.tintinspacerocketcontrolapp.simon.SimonService;
import fr.sqli.tintinspacerocketcontrolapp.settings.SettingsActivity;
import retrofit2.HttpException;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_NEW_PLAYER = 0;
    public static final String TAG = MainActivity.class.getName();
    private ImageView scanButton;
    private ImageView addPlayerManuallyButton;
    private ImageView playDemoButton;
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Player currentPlayer;
    private CurrentTry currentTry = null;
    private Date startTryDate = null;

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
            player.setContact(true);
            player.setTwitter("Démo");
            internalStartGame(player);
        });

        prepareSimonButtons();
    }


    private void prepareSimonButtons() {
        findViewById(R.id.blue_button).setOnClickListener(v -> {
            internalSimonGameButtonOnClick(Colors.BLUE);
        });
        findViewById(R.id.red_button).setOnClickListener(v -> {
            internalSimonGameButtonOnClick(Colors.RED);
        });
        findViewById(R.id.yellow_button).setOnClickListener(v -> {
            internalSimonGameButtonOnClick(Colors.YELLOW);
        });
        findViewById(R.id.green_button).setOnClickListener(v -> {
            internalSimonGameButtonOnClick(Colors.GREEN);
        });
    }

    private void internalSimonGameButtonOnClick(final Colors color) {
        if (currentTry != null) {
            if (currentTry.isPlayerTrying) {
                // Essai en cours
                if (currentTry.startDate == null) {
                    Log.d(TAG, "Nouvelle tentative");
                    // 1ère couleur de la séquence
                    currentTry.startDate = new Date();
                } else {
                    Log.d(TAG, "Tentative en cours");
                }

                currentTry.tryingSequence.add(color);

                if (currentTry.tryingSequence.size() == currentTry.correctSequence.size()) {
                    Log.d(TAG, "Tentative terminée");
                    // Fin de la séquence
                    currentTry.isPlayerTrying = false;
                    currentTry.time = new Date().getTime() - currentTry.startDate.getTime();
                    // Tentative
                    internalTrySequence();
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private void internalTrySequence() {
        Log.d(TAG, "Tentative de séquence");
        SimonService.getInstance(this)
                .trySequence(currentPlayer, currentTry.tryingSequence, currentTry.time).subscribe(tryResult -> {
            Log.d(TAG, "Résultat tentative : " + tryResult.result);
            if (tryResult.result == false) {
                // Essai KO : nouvel essai
                currentTry.remainingAttemps = tryResult.remainingAttempts;
                currentTry.isPlayerTrying = true;
                currentTry.tryingSequence.clear();
                currentTry.startDate = null;
                Toast.makeText(this, "Essai KO : " + currentTry.remainingAttemps  + " tentative(s) restante(s)", Toast.LENGTH_SHORT).show();
            } else {
                // Essai OK : séquence suivante
                internalPlayGame(currentPlayer);
            }
        }, throwable -> {
            internalManageServiceHttpException(throwable);
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
            currentPlayer = (Player) data.getExtras().getSerializable(AddPlayerActivity.PLAYER_INFORMATION);
            if (currentPlayer.isNotEmpty()) {
                internalStartGame(currentPlayer);
            } else {
                Toast.makeText(this, "Prénom / Nom / Email obligatoires", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("CheckResult")
    private void internalStartGame(final Player player) {
        SimonService.getInstance(this).start(player).subscribe(start -> {
            this.currentPlayer = player;
            player.setId(start.gamerId);
            // Toast.makeText(this, "Démarrage du jeu pour " + player.getFirstName() + " !", Toast.LENGTH_SHORT).show();
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Salut " + player.getFirstName() + ", bienvenu pour le challenge DevFest SQLI 2018 !\n\nAppuie sur OK pour démarrer la partie :)")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.cancel();
                        dialog.dismiss();

                        this.internalPlayGame(player);

                    }).create();
            alertDialog.show();

        }, throwable -> {
            internalManageServiceHttpException(throwable);
        });

    }

    @SuppressLint("CheckResult")
    private void internalPlayGame(final Player player) {
        SimonService.getInstance(this).play(player).subscribe(playResult -> {
            Log.d(TAG, "Nouvelle séquence affichée, au tour du joueur");

            findViewById(R.id.scan_button).setEnabled(false);
            findViewById(R.id.add_player_manually).setEnabled(false);
            findViewById(R.id.play_demo).setEnabled(false);

            // TODO afficher bouton stop

            this.currentTry = new CurrentTry();
            currentTry.correctSequence = playResult.correctSequence;
            currentTry.remainingAttemps = playResult.remainingAttempts;
            currentTry.isPlayerTrying = true;

        }, throwable -> {
            internalManageServiceHttpException(throwable);
        });
    }

    @SuppressLint("CheckResult")
    private void internalManageServiceHttpException(Throwable throwable) throws IOException {
        // TODO gérer problèmes de connexion (débloquer la partie)


        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.cancel();
                    dialog.dismiss();
                }).create();

        if (throwable instanceof GameFinishedException) {
            // TODO Récupération du score
            findViewById(R.id.scan_button).setEnabled(true);
            findViewById(R.id.add_player_manually).setEnabled(true);
            findViewById(R.id.play_demo).setEnabled(true);
            SimonService.getInstance(this).getScore(currentPlayer).subscribe(score -> {
                final String message = "Partie terminée ! \n\n Ton score est de " + score.score + " (temps total " + score.time +")";
                alertDialog.setMessage(message);
                alertDialog.show();
            }, throwable1 -> {
                internalManageServiceHttpException(throwable1);
            });

            currentTry = null;
            currentPlayer = null;

        } else if (throwable instanceof HttpException) {
            final HttpException httpException = (HttpException) throwable;
            final String message = "Exception HTTP \n" + httpException.getMessage() + "\n" + httpException.response().errorBody().string();
            alertDialog.setMessage(message);
            alertDialog.show();
        } else {
            final String message = throwable.getMessage();
            alertDialog.setMessage(message);
            alertDialog.show();
        }
    }
}
