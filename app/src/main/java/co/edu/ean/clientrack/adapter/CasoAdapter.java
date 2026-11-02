package co.edu.ean.clientrack.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import co.edu.ean.clientrack.R;
import co.edu.ean.clientrack.model.Caso;
import co.edu.ean.clientrack.util.Formatos;
import co.edu.ean.clientrack.util.UiUtils;

// Adaptador de la lista de casos (ListAdapter + DiffUtil)
public class CasoAdapter extends ListAdapter<Caso, CasoAdapter.CasoViewHolder> {

    /** Acción que se ejecuta al tocar un caso. */
    public interface OnCasoClickListener {
        void onCasoClick(Caso caso);
    }

    private static final DiffUtil.ItemCallback<Caso> DIFERENCIAS = new DiffUtil.ItemCallback<Caso>() {
        @Override
        public boolean areItemsTheSame(@NonNull Caso viejo, @NonNull Caso nuevo) {
            return viejo.getId() == nuevo.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Caso viejo, @NonNull Caso nuevo) {
            return Objects.equals(viejo.getAsunto(), nuevo.getAsunto())
                    && Objects.equals(viejo.getEstado(), nuevo.getEstado())
                    && Objects.equals(viejo.getPrioridad(), nuevo.getPrioridad())
                    && Objects.equals(viejo.getFechaLimite(), nuevo.getFechaLimite())
                    && Objects.equals(viejo.getClienteNombre(), nuevo.getClienteNombre())
                    && Objects.equals(viejo.getClienteEmpresa(), nuevo.getClienteEmpresa())
                    && viejo.isVencido() == nuevo.isVencido();
        }
    };

    private final OnCasoClickListener listener;

    public CasoAdapter(OnCasoClickListener listener) {
        super(DIFERENCIAS);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CasoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_caso, parent, false);
        return new CasoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull CasoViewHolder holder, int position) {
        Caso caso = getItem(position);
        Context context = holder.itemView.getContext();

        holder.tvAsunto.setText(caso.getAsunto());

        String empresa = caso.getClienteEmpresa();
        holder.tvCliente.setText(Formatos.estaVacio(empresa)
                ? caso.getClienteNombre()
                : context.getString(R.string.cliente_y_empresa, caso.getClienteNombre(), empresa));

        UiUtils.pintarEstado(holder.tvEstado, caso.getEstado());
        UiUtils.pintarPunto(holder.viewPrioridad, UiUtils.colorPrioridad(context, caso.getPrioridad()));
        holder.tvPrioridad.setText(caso.getPrioridad());
        // Para lectores de pantalla: "Prioridad Alta" en lugar de solo "Alta"
        holder.tvPrioridad.setContentDescription(context.getString(R.string.prioridad_larga, caso.getPrioridad()));
        holder.tvFecha.setText(Formatos.fechaCorta(caso.getFechaLimite()));

        boolean vencido = caso.isVencido();
        holder.tvVencido.setVisibility(vencido ? View.VISIBLE : View.GONE);
        if (vencido) {
            UiUtils.pintarVencido(holder.tvVencido);
        }

        holder.itemView.setOnClickListener(v -> listener.onCasoClick(caso));
    }

    static class CasoViewHolder extends RecyclerView.ViewHolder {
        final View viewPrioridad;
        final TextView tvAsunto, tvCliente, tvEstado, tvPrioridad, tvVencido, tvFecha;

        CasoViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPrioridad = itemView.findViewById(R.id.viewPrioridad);
            tvAsunto = itemView.findViewById(R.id.tvAsunto);
            tvCliente = itemView.findViewById(R.id.tvCliente);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvPrioridad = itemView.findViewById(R.id.tvPrioridad);
            tvVencido = itemView.findViewById(R.id.tvVencido);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}
