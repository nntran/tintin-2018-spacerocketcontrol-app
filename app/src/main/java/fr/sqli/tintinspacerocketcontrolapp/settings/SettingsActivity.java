package fr.sqli.tintinspacerocketcontrolapp.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import fr.sqli.tintinspacerocketcontrolapp.R;
import fr.sqli.tintinspacerocketcontrolapp.service.SpaceRocketService;

public class SettingsActivity extends AppCompatActivity {

    public static final String SHARED_PREF_NAME = "tintinspacerocket";

    private SpaceRocketService spaceRocketService;

    private EditText serverUrlEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main);
    }

    @Override
    public void onStart() {
        super.onStart();

        prepareServerUrlFields();

        showPasswordDialog(false);

        // Bouton changement du mot de passe administrateur
        findViewById(R.id.change_password_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showPasswordDialog(true);
            }
        });
    }

    /**
     * Prépare les champs pour les saisie/test de l'url du serveur
     */
    @SuppressWarnings("checkResult")
    private void prepareServerUrlFields() {
        serverUrlEditText = findViewById(R.id.server_url_edit_text);

        spaceRocketService = SpaceRocketService.getInstance(this);

        // Bouton enregistrement nouvelle URL
        findViewById(R.id.modify_url_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spaceRocketService.setServerUrl(serverUrlEditText.getText().toString());
                Toast.makeText(SettingsActivity.this, "Modification URL OK", Toast.LENGTH_SHORT).show();
            }
        });

        // Bouton test URL
        findViewById(R.id.test_url_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spaceRocketService.health().subscribe(health -> {
                    if (Boolean.TRUE.equals(health)) {
                        Toast.makeText(SettingsActivity.this, "Test URL OK", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Test URL KO !", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    Toast.makeText(SettingsActivity.this, "Test URL KO ! (" + error.getMessage() + ")", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Affiche la pop up modale pour créer/saisir/modifier le mot de passe administrateur
     * @param isEditMode indique si la pop up doit s'afficher en mode édition du mot de passe
     */
    private void showPasswordDialog(final boolean isEditMode) {
        final String correctPassword = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString("admin_password", null);

        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.password_dialog, (ViewGroup) findViewById(R.id.root_password_modal));
        final EditText passwordEditText = view.findViewById(R.id.password_text);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        if (correctPassword == null || isEditMode) {
            builder.setTitle("Saisir le nouveau mot de passe");
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            builder.setTitle("Entrer le mot de passe");
        }

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                if (!isEditMode) {
                    SettingsActivity.this.finish();
                }
            }
        });

        // On déclare le callback à null ici pour éviter que la pop up ne se ferme site au tap du bouton OK
        builder.setPositiveButton(android.R.string.ok, null);


        builder.setCancelable(false);
        final AlertDialog alertDialog = builder.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (correctPassword == null || isEditMode) {
                    // Création d'un nouveau mot de passe
                    getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString("admin_password", passwordEditText.getText().toString())
                            .apply();
                    alertDialog.dismiss();

                    if (isEditMode) {
                        Toast.makeText(SettingsActivity.this, "Changement de mot de passe OK", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Création du mot de passe OK", Toast.LENGTH_SHORT).show();
                        serverUrlEditText.setText(spaceRocketService.getServerUrl());
                    }

                } else if (!correctPassword.equals(passwordEditText.getText().toString())) {
                    // Vérification du mot de passe KO
                    Toast.makeText(alertDialog.getContext(), "Mot de passe incorrect !", Toast.LENGTH_SHORT).show();
                } else {
                    // Vérification du mot de passe OK
                    alertDialog.dismiss();
                    serverUrlEditText.setText(spaceRocketService.getServerUrl());
                    Toast.makeText(SettingsActivity.this, "Mot de passe OK", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
