package com.example.campusgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campusgo.R;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import java.util.ArrayList;
import java.util.List;

public class SugerenciasAdapter extends RecyclerView.Adapter<SugerenciasAdapter.SugerenciaViewHolder> {

    // --- Interfaz para manejar los clics ---
    public interface OnSugerenciaClickListener {
        void onSugerenciaClick(AutocompletePrediction sugerencia);
    }

    private List<AutocompletePrediction> sugerencias;
    private final OnSugerenciaClickListener listener;

    public SugerenciasAdapter(OnSugerenciaClickListener listener) {
        this.sugerencias = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Actualiza la lista de sugerencias y notifica al RecyclerView para que se redibuje.
     */
    public void actualizarSugerencias(List<AutocompletePrediction> nuevasSugerencias) {
        this.sugerencias.clear();
        this.sugerencias.addAll(nuevasSugerencias);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SugerenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sugerencia_lugar, parent, false);
        return new SugerenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SugerenciaViewHolder holder, int position) {
        AutocompletePrediction sugerencia = sugerencias.get(position);
        holder.bind(sugerencia, listener);
    }

    @Override
    public int getItemCount() {
        return sugerencias.size();
    }


    // --- El ViewHolder que contiene las vistas de cada item ---
    static class SugerenciaViewHolder extends RecyclerView.ViewHolder {
        TextView primaryText;
        TextView secondaryText;

        public SugerenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            primaryText = itemView.findViewById(R.id.text_primary);
            secondaryText = itemView.findViewById(R.id.text_secondary);
        }

        public void bind(final AutocompletePrediction sugerencia, final OnSugerenciaClickListener listener) {
            // La API de Places devuelve el texto formateado
            primaryText.setText(sugerencia.getPrimaryText(null));
            secondaryText.setText(sugerencia.getSecondaryText(null));

            // Asignamos el listener al item completo
            itemView.setOnClickListener(v -> listener.onSugerenciaClick(sugerencia));
        }
    }
}