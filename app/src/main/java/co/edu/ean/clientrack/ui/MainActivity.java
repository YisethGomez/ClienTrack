package co.edu.ean.clientrack.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import co.edu.ean.clientrack.R;
import co.edu.ean.clientrack.adapter.CasoAdapter;
import co.edu.ean.clientrack.data.DatabaseHelper;
import co.edu.ean.clientrack.model.Caso;
import co.edu.ean.clientrack.util.SessionManager;
import co.edu.ean.clientrack.util.UiUtils;

// Pantalla principal: resumen, búsqueda, filtros y lista de casos
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager sesion;
    private CasoAdapter adapter;

    private View raiz, layoutVacio;
    private TextView tvAbiertos, tvEnProceso, tvCerrados, tvVencidos, tvVacioTitulo, tvVacioTexto;
    private TextInputEditText etBuscar;
    private ChipGroup chipGroupEstado;
    private ExtendedFloatingActionButton fabNuevoCaso;

    // al volver del formulario o del detalle se muestra el mensaje que dejaron
    private final ActivityResultLauncher<Intent> abrirPantalla = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            resultado -> {
                Intent datos = resultado.getData();
                if (datos != null && raiz != null) {
                    UiUtils.mostrarMensaje(raiz, datos.getStringExtra(UiUtils.EXTRA_MENSAJE), fabNuevoCaso);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sesion = new SessionManager(this);
        if (!sesion.haySesion()) {
            irAlLogin();
            return;
        }

        setContentView(R.layout.activity_main);
        db = DatabaseHelper.getInstance(this);

        raiz = findViewById(R.id.raiz);
        TextView tvSaludo = findViewById(R.id.tvSaludo);
        tvSaludo.setText(getString(R.string.saludo, sesion.getPrimerNombre()));

        tvAbiertos = findViewById(R.id.tvAbiertos);
        tvEnProceso = findViewById(R.id.tvEnProceso);
        tvCerrados = findViewById(R.id.tvCerrados);
        tvVencidos = findViewById(R.id.tvVencidos);
        layoutVacio = findViewById(R.id.layoutVacio);
        tvVacioTitulo = layoutVacio.findViewById(R.id.tvVacioTitulo);
        tvVacioTexto = layoutVacio.findViewById(R.id.tvVacioTexto);
        etBuscar = findViewById(R.id.etBuscar);
        chipGroupEstado = findViewById(R.id.chipGroupEstado);
        fabNuevoCaso = findViewById(R.id.fabNuevoCaso);

        // Lista de casos: al tocar uno se abre su detalle
        RecyclerView rvCasos = findViewById(R.id.rvCasos);
        rvCasos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CasoAdapter(caso -> {
            Intent intent = new Intent(this, CasoDetalleActivity.class);
            intent.putExtra(CasoDetalleActivity.EXTRA_CASO_ID, caso.getId());
            abrirPantalla.launch(intent);
        });
        rvCasos.setAdapter(adapter);

        // el botón se encoge al hacer scroll hacia abajo
        rvCasos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 6 && fabNuevoCaso.isExtended()) {
                    fabNuevoCaso.shrink();
                } else if (dy < -6 && !fabNuevoCaso.isExtended()) {
                    fabNuevoCaso.extend();
                }
            }
        });

        // busca mientras se escribe
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarCasos();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        // Filtro por estado
        chipGroupEstado.setOnCheckedStateChangeListener((group, checkedIds) -> cargarCasos());

        fabNuevoCaso.setOnClickListener(v -> nuevoCaso());
        findViewById(R.id.btnClientes).setOnClickListener(v ->
                startActivity(new Intent(this, ClientesActivity.class)));
        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> confirmarCerrarSesion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null) {
            cargarResumen();
            cargarCasos();
        }
    }

    private void cargarResumen() {
        int[] resumen = db.obtenerResumen(sesion.getUsuarioId());
        tvAbiertos.setText(String.valueOf(resumen[0]));
        tvEnProceso.setText(String.valueOf(resumen[1]));
        tvCerrados.setText(String.valueOf(resumen[2]));
        tvVencidos.setText(String.valueOf(resumen[3]));
    }

    private void cargarCasos() {
        String texto = etBuscar.getText() == null ? "" : etBuscar.getText().toString();
        String estado = estadoSeleccionado();

        List<Caso> casos = db.buscarCasos(sesion.getUsuarioId(), texto, estado);
        adapter.submitList(casos);

        boolean hayFiltros = !texto.trim().isEmpty() || estado != null;
        tvVacioTitulo.setText(hayFiltros ? R.string.empty_search_title : R.string.empty_cases_title);
        tvVacioTexto.setText(hayFiltros ? R.string.empty_search_text : R.string.empty_cases_text);
        layoutVacio.setVisibility(casos.isEmpty() ? View.VISIBLE : View.GONE);

        // Si la lista queda vacía no hay scroll para volver a expandir el botón
        if (casos.isEmpty() && !fabNuevoCaso.isExtended()) {
            fabNuevoCaso.extend();
        }
    }

    // null = "Todos"
    private String estadoSeleccionado() {
        int id = chipGroupEstado.getCheckedChipId();
        if (id == R.id.chipAbiertos) return Caso.ESTADO_ABIERTO;
        if (id == R.id.chipEnProceso) return Caso.ESTADO_EN_PROCESO;
        if (id == R.id.chipCerrados) return Caso.ESTADO_CERRADO;
        return null;
    }

    private void nuevoCaso() {
        // No se puede crear un caso sin un cliente al que asociarlo
        if (db.obtenerClientes(sesion.getUsuarioId()).isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dlg_primer_cliente_titulo)
                    .setMessage(R.string.dlg_primer_cliente_texto)
                    .setPositiveButton(R.string.dlg_agregar_cliente, (d, w) ->
                            startActivity(new Intent(this, ClientesActivity.class)))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }
        abrirPantalla.launch(new Intent(this, CasoFormActivity.class));
    }

    private void confirmarCerrarSesion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_logout)
                .setMessage(R.string.dlg_logout_texto)
                .setPositiveButton(R.string.menu_logout, (d, w) -> {
                    sesion.cerrarSesion();
                    irAlLogin();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void irAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
