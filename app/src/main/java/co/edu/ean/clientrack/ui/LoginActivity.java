package co.edu.ean.clientrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import co.edu.ean.clientrack.R;
import co.edu.ean.clientrack.data.DatabaseHelper;
import co.edu.ean.clientrack.model.Usuario;
import co.edu.ean.clientrack.util.SessionManager;
import co.edu.ean.clientrack.util.UiUtils;
import co.edu.ean.clientrack.util.Validador;

// Pantalla de inicio de sesión
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_CORREO = "correo";

    private TextInputLayout tilCorreo, tilPassword;
    private TextInputEditText etCorreo, etPassword;
    private DatabaseHelper db;
    private SessionManager sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = DatabaseHelper.getInstance(this);
        sesion = new SessionManager(this);

        tilCorreo = findViewById(R.id.tilCorreo);
        tilPassword = findViewById(R.id.tilPassword);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);

        // si viene del registro, dejar el correo escrito
        if (savedInstanceState == null) {
            String correoRegistrado = getIntent().getStringExtra(EXTRA_CORREO);
            if (correoRegistrado != null) {
                etCorreo.setText(correoRegistrado);
                etPassword.requestFocus();
            }
            UiUtils.mostrarMensaje(findViewById(R.id.raiz),
                    getIntent().getStringExtra(UiUtils.EXTRA_MENSAJE), null);
        }

        findViewById(R.id.btnIngresar).setOnClickListener(v -> iniciarSesion());
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                iniciarSesion();
                return true;
            }
            return false;
        });
        findViewById(R.id.tvIrRegistro).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void iniciarSesion() {
        tilCorreo.setError(null);
        tilPassword.setError(null);

        String correo = texto(etCorreo).toLowerCase(Locale.ROOT);
        String password = texto(etPassword);
        boolean valido = true;

        if (Validador.estaVacio(correo)) {
            tilCorreo.setError(getString(R.string.err_email_vacio));
            valido = false;
        } else if (!Validador.esCorreoValido(correo)) {
            tilCorreo.setError(getString(R.string.err_email_formato));
            valido = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.err_password_vacio));
            valido = false;
        }
        if (!valido) {
            return;
        }

        Usuario usuario = db.login(correo, Validador.hashPassword(password));
        if (usuario == null) {
            tilPassword.setError(getString(R.string.err_credenciales));
            return;
        }

        sesion.iniciarSesion(usuario.getId(), usuario.getNombre());

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
