package co.edu.ean.clientrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import co.edu.ean.clientrack.R;
import co.edu.ean.clientrack.util.SessionManager;

// Pantalla de bienvenida. Si ya hay sesión, el botón lleva directo a los casos
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        SessionManager sesion = new SessionManager(this);
        MaterialButton btnComenzar = findViewById(R.id.btnComenzar);
        TextView tvIrLogin = findViewById(R.id.tvIrLogin);

        if (sesion.haySesion()) {
            btnComenzar.setText(R.string.welcome_continue);
            tvIrLogin.setVisibility(View.GONE);
            btnComenzar.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        } else {
            // La bienvenida queda debajo: con "atrás" el usuario puede volver a ella
            btnComenzar.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
            tvIrLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }
    }
}
