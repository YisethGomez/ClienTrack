package co.edu.ean.clientrack.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import co.edu.ean.clientrack.R;
import co.edu.ean.clientrack.data.DatabaseHelper;
import co.edu.ean.clientrack.model.Caso;
import co.edu.ean.clientrack.util.Formatos;
import co.edu.ean.clientrack.util.SessionManager;
import co.edu.ean.clientrack.util.UiUtils;

// Detalle de un caso: editar, cerrar o eliminar
public class CasoDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_CASO_ID = "caso_id";

    private DatabaseHelper db;
    private SessionManager sesion;
    private long casoId;
    private Caso casoActual;

    private View raiz, barraAcciones, viewPrioridad, btnCerrarCaso, filaCorreo, filaTelefono;
    private TextView tvAsunto, tvEstado, tvPrioridad, tvVencido, tvDescripcion, tvFechaLimite,
            tvFechaCreacion, tvClienteIniciales, tvClienteNombre, tvClienteEmpresa, tvClienteCorreo,
            tvClienteTelefono;

    // al volver de editar se recarga en onResume
    private final ActivityResultLauncher<Intent> abrirEdicion = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            resultado -> {
                Intent datos = resultado.getData();
                if (datos != null && raiz != null) {
                    UiUtils.mostrarMensaje(raiz, datos.getStringExtra(UiUtils.EXTRA_MENSAJE), barraAcciones);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caso_detalle);

        db = DatabaseHelper.getInstance(this);
        sesion = new SessionManager(this);
        casoId = getIntent().getLongExtra(EXTRA_CASO_ID, -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.menu_detalle);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_editar) {
                Intent intent = new Intent(this, CasoFormActivity.class);
                intent.putExtra(CasoFormActivity.EXTRA_CASO_ID, casoId);
                abrirEdicion.launch(intent);
                return true;
            }
            return false;
        });

        raiz = findViewById(R.id.raiz);
        barraAcciones = findViewById(R.id.barraAcciones);
        tvAsunto = findViewById(R.id.tvAsunto);
        tvEstado = findViewById(R.id.tvEstado);
        viewPrioridad = findViewById(R.id.viewPrioridad);
        tvPrioridad = findViewById(R.id.tvPrioridad);
        tvVencido = findViewById(R.id.tvVencido);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvFechaLimite = findViewById(R.id.tvFechaLimite);
        tvFechaCreacion = findViewById(R.id.tvFechaCreacion);
        tvClienteIniciales = findViewById(R.id.tvClienteIniciales);
        tvClienteNombre = findViewById(R.id.tvClienteNombre);
        tvClienteEmpresa = findViewById(R.id.tvClienteEmpresa);
        tvClienteCorreo = findViewById(R.id.tvClienteCorreo);
        tvClienteTelefono = findViewById(R.id.tvClienteTelefono);
        filaCorreo = findViewById(R.id.filaCorreo);
        filaTelefono = findViewById(R.id.filaTelefono);
        btnCerrarCaso = findViewById(R.id.btnCerrarCaso);

        btnCerrarCaso.setOnClickListener(v -> cerrarCaso());
        findViewById(R.id.btnEliminar).setOnClickListener(v -> confirmarEliminar());
        filaCorreo.setOnClickListener(v -> abrir(Intent.ACTION_SENDTO,
                Uri.parse("mailto:" + casoActual.getClienteCorreo())));
        filaTelefono.setOnClickListener(v -> abrir(Intent.ACTION_DIAL,
                Uri.parse("tel:" + casoActual.getClienteTelefono())));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCaso(); // Se recarga al volver de "Editar" para mostrar los cambios
    }

    private void cargarCaso() {
        Caso caso = db.obtenerCaso(casoId, sesion.getUsuarioId());
        if (caso == null) {
            UiUtils.terminarConMensaje(this, R.string.msg_caso_no_existe);
            return;
        }
        casoActual = caso;

        UiUtils.pintarEstado(tvEstado, caso.getEstado());
        UiUtils.pintarPunto(viewPrioridad, UiUtils.colorPrioridad(this, caso.getPrioridad()));
        tvPrioridad.setText(getString(R.string.prioridad_larga, caso.getPrioridad()));

        boolean vencido = caso.isVencido();
        tvVencido.setVisibility(vencido ? View.VISIBLE : View.GONE);
        if (vencido) {
            UiUtils.pintarVencido(tvVencido);
        }

        tvAsunto.setText(caso.getAsunto());
        String descripcion = caso.getDescripcion();
        tvDescripcion.setText(Formatos.estaVacio(descripcion) ? getString(R.string.sin_descripcion) : descripcion);

        tvFechaLimite.setText(Formatos.fechaMedia(caso.getFechaLimite()));
        tvFechaLimite.setTextColor(ContextCompat.getColor(this, vencido ? R.color.error : R.color.ink));
        tvFechaCreacion.setText(Formatos.fechaMedia(caso.getFechaCreacion()));

        tvClienteIniciales.setText(Formatos.iniciales(caso.getClienteNombre()));
        tvClienteNombre.setText(caso.getClienteNombre());
        tvClienteEmpresa.setText(Formatos.estaVacio(caso.getClienteEmpresa())
                ? getString(R.string.sin_empresa) : caso.getClienteEmpresa());

        pintarContacto(filaCorreo, tvClienteCorreo, caso.getClienteCorreo());
        pintarContacto(filaTelefono, tvClienteTelefono, caso.getClienteTelefono());

        // Si ya está cerrado no tiene sentido mostrar el botón de cerrar
        btnCerrarCaso.setVisibility(Caso.ESTADO_CERRADO.equals(caso.getEstado()) ? View.GONE : View.VISIBLE);
    }

    // si hay dato se puede tocar (correo/llamada), si no se muestra en gris
    private void pintarContacto(View fila, TextView valor, String dato) {
        boolean hayDato = !Formatos.estaVacio(dato);
        valor.setText(UiUtils.valorOpcional(this, dato));
        valor.setTextColor(ContextCompat.getColor(this, hayDato ? R.color.primary : R.color.ink_secondary));
        fila.setClickable(hayDato);
        fila.setFocusable(hayDato);
    }

    private void abrir(String accion, Uri uri) {
        try {
            startActivity(new Intent(accion, uri));
        } catch (ActivityNotFoundException e) {
            UiUtils.mostrarMensaje(raiz, R.string.msg_sin_app, barraAcciones);
        }
    }

    private void cerrarCaso() {
        if (db.actualizarEstado(casoId, sesion.getUsuarioId(), Caso.ESTADO_CERRADO) > 0) {
            cargarCaso();
            UiUtils.mostrarMensaje(raiz, R.string.msg_caso_cerrado, barraAcciones);
        }
    }

    private void confirmarEliminar() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dlg_eliminar_caso_titulo)
                .setMessage(R.string.dlg_no_deshacer)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    if (db.eliminarCaso(casoId, sesion.getUsuarioId()) > 0) {
                        UiUtils.terminarConMensaje(this, R.string.msg_caso_eliminado);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
