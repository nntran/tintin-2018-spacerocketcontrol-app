package fr.sqli.tintinspacerocketcontrolapp.player;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import fr.sqli.tintinspacerocketcontrolapp.R;

public class AddPlayerActivity extends AppCompatActivity {

    public static String PLAYER_INFORMATION = "PLAYER_INFORMATION";
    private Player playerInformation;
    private RadioButton male;
    private EditText twitter;
    private EditText company;
    private EditText email;
    private EditText lastName;
    private EditText firstName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player);

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        company = findViewById(R.id.company);
        twitter = findViewById(R.id.twitter);
        male = findViewById(R.id.male);
        RadioButton female = findViewById(R.id.female);

        if(getIntent().getExtras() != null){
            playerInformation = (Player) getIntent().getExtras().getSerializable(PLAYER_INFORMATION);
        }

        if (playerInformation == null){
            playerInformation = new Player();
        }
        firstName.setText(playerInformation.getFirstName());
        lastName.setText(playerInformation.getLastName());
        email.setText(playerInformation.getEmail());
        company.setText(playerInformation.getCompany());
        twitter.setText(playerInformation.getTwitter());
        if (playerInformation.isGenderMale()) {
            male.setChecked(true);
        } else {
            female.setChecked(true);
        }

        setupLaunchBtn();
    }

    private void setupLaunchBtn() {
        findViewById(R.id.launchBtn).setOnClickListener(v -> {
            if (firstName.getText().toString().isEmpty()
                    || lastName.getText().toString().isEmpty()
                    || email.getText().toString().isEmpty()) {
                Toast.makeText(this, "Prénom / Nom / Email obligatoires", Toast.LENGTH_SHORT).show();
            } else {
                playerInformation.setCompany(company.getText().toString());
                playerInformation.setEmail(email.getText().toString());
                playerInformation.setLastName(lastName.getText().toString());
                playerInformation.setFirstName(firstName.getText().toString());
                playerInformation.setTwitter(twitter.getText().toString());
                playerInformation.setGenderMale(male.isChecked());
                final Intent intent = new Intent();
                intent.putExtra(PLAYER_INFORMATION, playerInformation);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
