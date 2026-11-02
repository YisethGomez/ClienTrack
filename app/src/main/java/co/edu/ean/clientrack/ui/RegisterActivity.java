package co.edu.ean.clientrack.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import co.edu.ean.clientrack.R;
import co.edu.ean.clientrack.data.DatabaseHelper;
import co.edu.ean.clientrack.util.UiUtils;
import co.edu.ean.clientrack.util.Validador;

// Pantalla de registro
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilNombre, tilCorreo, tilPassword, tilConfirmar;
    private TextInputEditText etNombre, etCorreo, etPassword, etConfirmar;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilNombre = findViewById(R.id.tilNombre);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        etConfirmar = findViewById(R.id.etConfirmar);

        findViewById(R.id.btnRegistrar).setOnClickListener(v -> registrar());
        findViewById(R.id.tvIrLogin).setOnClickListener(v -> {
            // desde la bienvenida no hay login debajo, toca abrirlo
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
    }

    private void registrar() {
        tilNombre.setError(null);
        tilCorreo.setError(null);
        tilPassword.setError(null);
        tilConfirmar.setError(null);

        String nombre = texto(etNombre);
        String correo = texto(etCorreo).toLowerCase(Locale.ROOT);
        String password = texto(etPassword);
        String confirmar = texto(etConfirmar);
        boolean valido = true;

        if (Validador.estaVacio(nombre)) {
            tilNombre.setError(getString(R.string.err_nombre_vacio));
            valido = false;
        } else if (!Validador.esNombreValido(nombre)) {
            tilNombre.setError(getString(R.string.err_nombre_formato));
            valido = false;
        }

        if (Validador.estaVacio(correo)) {
            tilCorreo.setError(getString(R.string.err_email_vacio));
            valido = false;
        } else if (!Validador.esCorreoValido(correo)) {
            tilCorreo.setError(getString(R.string.err_email_formato));
            valido = false;
        } else if (db.existeCorreo(correo)) {
            tilCorreo.setError(getString(R.string.err_email_existe));
            valido = false;
        }

        if (!Validador.esPasswordValida(password)) {
            tilPassword.setError(getString(R.string.err_password_min, Validador.MIN_PASSWORD));
            valido = false;
        }

        if (!confirmar.equals(password)) {
            tilConfirmar.setError(getString(R.string.err_password_no_coincide));
            valido = false;
        }

        if (!valido) {
            return;
        }

        long id = db.registrarUsuario(nombre, correo, Validador.hashPassword(password));
        if (id == -1) {
            UiUtils.mostrarMensaje(findViewById(R.id.raiz), R.string.msg_cuenta_error, null);
            return;
        }

        // volver al login con el correo ya escrito
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_CORREO, correo);
        intent.putExtra(UiUtils.EXTRA_MENSAJE, getString(R.string.msg_cuenta_creada));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
