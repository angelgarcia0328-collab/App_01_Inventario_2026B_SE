package com.example.inventario_app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {

    public interface OnMaterialClickListener {
        void onMaterialClick(Material material, int position);
    }

    private final List<Material> listaMateriales;
    private final OnMaterialClickListener listener;

    public MaterialAdapter(List<Material> listaMateriales, OnMaterialClickListener listener) {
        this.listaMateriales = listaMateriales;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Material m = listaMateriales.get(position);

        holder.tvNombre.setText(m.getNombre());
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", m.getPrecio()));
        holder.tvCantidad.setText(String.valueOf(m.getCantidad()));

        if (m.getCantidad() <= 0) {
            holder.cardMaterial.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            holder.cardMaterial.setStrokeColor(Color.parseColor("#D32F2F"));
            holder.tvCantidad.setTextColor(Color.parseColor("#D32F2F"));
            holder.tvEstado.setText("AGOTADO");
            holder.tvEstado.setTextColor(Color.parseColor("#D32F2F"));
        } else if (m.getCantidad() <= m.getStockMinimo()) {
            holder.cardMaterial.setCardBackgroundColor(Color.parseColor("#FFF8E1"));
            holder.cardMaterial.setStrokeColor(Color.parseColor("#FFB300"));
            holder.tvCantidad.setTextColor(Color.parseColor("#EF6C00"));
            holder.tvEstado.setText("STOCK BAJO");
            holder.tvEstado.setTextColor(Color.parseColor("#EF6C00"));
        } else {
            holder.cardMaterial.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.cardMaterial.setStrokeColor(Color.parseColor("#E0E0E0"));
            holder.tvCantidad.setTextColor(Color.parseColor("#37474F"));
            holder.tvEstado.setText("DISPONIBLE");
            holder.tvEstado.setTextColor(Color.parseColor("#2E7D32"));
        }

        holder.itemView.setOnClickListener(v -> listener.onMaterialClick(m, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return listaMateriales.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardMaterial;
        TextView tvNombre, tvPrecio, tvCantidad, tvEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMaterial = itemView.findViewById(R.id.cardMaterial);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}