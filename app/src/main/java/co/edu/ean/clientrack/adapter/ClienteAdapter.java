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
import co.edu.ean.clientrack.model.Cliente;
import co.edu.ean.clientrack.util.Formatos;

// Adaptador de la lista de clientes
public class ClienteAdapter extends ListAdapter<Cliente, ClienteAdapter.ClienteViewHolder> {

    public interface OnClienteListener {
        void onClienteClick(Cliente cliente);
    }

    private static final DiffUtil.ItemCallback<Cliente> DIFERENCIAS = new DiffUtil.ItemCallback<Cliente>() {
        @Override
        public boolean areItemsTheSame(@NonNull Cliente viejo, @NonNull Cliente nuevo) {
            return viejo.getId() == nuevo.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Cliente viejo, @NonNull Cliente nuevo) {
            return Objects.equals(viejo.getNombre(), nuevo.getNombre())
                    && Objects.equals(viejo.getEmpresa(), nuevo.getEmpresa())
                    && viejo.getTotalCasos() == nuevo.getTotalCasos();
        }
    };

    private final OnClienteListener listener;

    public ClienteAdapter(OnClienteListener listener) {
        super(DIFERENCIAS);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new ClienteViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        Cliente cliente = getItem(position);
        Context context = holder.itemView.getContext();

        holder.tvIniciales.setText(Formatos.iniciales(cliente.getNombre()));
        holder.tvNombre.setText(cliente.getNombre());
        holder.tvEmpresa.setText(Formatos.estaVacio(cliente.getEmpresa())
                ? context.getString(R.string.sin_empresa)
                : cliente.getEmpresa());

        int total = cliente.getTotalCasos();
        holder.tvTotalCasos.setText(total == 0
                ? context.getString(R.string.sin_casos)
                : context.getResources().getQuantityString(R.plurals.total_casos, total, total));

        holder.itemView.setOnClickListener(v -> listener.onClienteClick(cliente));
    }

    static class ClienteViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIniciales, tvNombre, tvEmpresa, tvTotalCasos;

        ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIniciales = itemView.findViewById(R.id.tvIniciales);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEmpresa = itemView.findViewById(R.id.tvEmpresa);
            tvTotalCasos = itemView.findViewById(R.id.tvTotalCasos);
        }
    }
}
