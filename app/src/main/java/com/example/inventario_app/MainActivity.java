package com.example.inventario_app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<Material> todosLosMateriales = new ArrayList<>();
    private final List<Material> materialesMostrados = new ArrayList<>();
    private MaterialAdapter adapter;
    private int filtroActualId = R.id.chipTodos;
    private int siguienteId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rvMateriales = findViewById(R.id.rvMateriales);
        rvMateriales.setLayoutManager(new LinearLayoutManager(this));

        cargarDatosIniciales();
        aplicarFiltro(R.id.chipTodos);

        adapter = new MaterialAdapter(materialesMostrados, (material, position) ->
                mostrarDialogoEditarOEliminar(material)
        );
        rvMateriales.setAdapter(adapter);

        ChipGroup chipGroup = findViewById(R.id.chipGroupFiltros);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                aplicarFiltro(checkedIds.get(0));
            }
        });

        FloatingActionButton fabAgregar = findViewById(R.id.fabAgregar);
        fabAgregar.setOnClickListener(v -> mostrarDialogoAgregar());
    }

    private void cargarDatosIniciales() {
        todosLosMateriales.add(new Material(siguienteId++, "Tornillos M6", 1.50, 120, 20));
        todosLosMateriales.add(new Material(siguienteId++, "Varilla Corrugada 3/8", 145.00, 4, 5));
        todosLosMateriales.add(new Material(siguienteId++, "Cemento Gris 50kg", 210.00, 0, 10));
    }

    private void aplicarFiltro(int chipId) {
        filtroActualId = chipId;
        materialesMostrados.clear();

        for (Material m : todosLosMateriales) {
            if (chipId == R.id.chipAgotados) {
                if (m.getCantidad() <= 0) materialesMostrados.add(m);
            } else if (chipId == R.id.chipBajoStock) {
                if (m.getCantidad() > 0 && m.getCantidad() <= m.getStockMinimo()) {
                    materialesMostrados.add(m);
                }
            } else {
                materialesMostrados.add(m);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void mostrarDialogoAgregar() {
        View vistaDialogo = LayoutInflater.from(this).inflate(R.layout.dialog_material, null);
        EditText etNombre = vistaDialogo.findViewById(R.id.etNombre);
        EditText etPrecio = vistaDialogo.findViewById(R.id.etPrecio);
        EditText etCantidad = vistaDialogo.findViewById(R.id.etCantidad);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Material")
                .setView(vistaDialogo)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (TextUtils.isEmpty(nombre)) return;

                    double precio = parseDoubleSeguro(etPrecio.getText().toString());
                    int cantidad = parseIntSeguro(etCantidad.getText().toString());

                    Material nuevo = new Material(siguienteId++, nombre, precio, cantidad, 5);
                    todosLosMateriales.add(nuevo);
                    aplicarFiltro(filtroActualId);

                    if (cantidad <= 0) {
                        avisoMaterialAgotado(nombre);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditarOEliminar(Material material) {
        View vistaDialogo = LayoutInflater.from(this).inflate(R.layout.dialog_material, null);
        EditText etNombre = vistaDialogo.findViewById(R.id.etNombre);
        EditText etPrecio = vistaDialogo.findViewById(R.id.etPrecio);
        EditText etCantidad = vistaDialogo.findViewById(R.id.etCantidad);

        etNombre.setText(material.getNombre());
        etPrecio.setText(String.valueOf(material.getPrecio()));
        etCantidad.setText(String.valueOf(material.getCantidad()));

        new AlertDialog.Builder(this)
                .setTitle("Modificar Material")
                .setView(vistaDialogo)
                .setPositiveButton("Guardar cambios", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (TextUtils.isEmpty(nombre)) return;

                    double precio = parseDoubleSeguro(etPrecio.getText().toString());
                    int cantidad = parseIntSeguro(etCantidad.getText().toString());

                    material.setNombre(nombre);
                    material.setPrecio(precio);
                    material.setCantidad(cantidad);

                    aplicarFiltro(filtroActualId);

                    if (cantidad <= 0) {
                        avisoMaterialAgotado(nombre);
                    }
                })
                .setNeutralButton("Eliminar", (dialog, which) -> {
                    todosLosMateriales.remove(material);
                    aplicarFiltro(filtroActualId);
                    Snackbar.make(findViewById(R.id.rvMateriales),
                            "Se eliminó: " + material.getNombre(), Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void avisoMaterialAgotado(String nombreMaterial) {
        Snackbar.make(findViewById(R.id.rvMateriales),
                "¡Atención! Se acabó el material: " + nombreMaterial,
                Snackbar.LENGTH_LONG).show();
    }

    private double parseDoubleSeguro(String texto) {
        try {
            return TextUtils.isEmpty(texto) ? 0.0 : Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseIntSeguro(String texto) {
        try {
            return TextUtils.isEmpty(texto) ? 0 : Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}