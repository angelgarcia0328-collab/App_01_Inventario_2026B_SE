package com.example.inventario_app;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "inventario_prefs";
    private static final String KEY_MATERIALES_JSON = "materiales_json";
    private static final String KEY_DATOS_INICIALES_CARGADOS = "datos_iniciales_cargados";
    private static final String KEY_SIGUIENTE_ID = "siguiente_id";

    private final List<Material> todosLosMateriales = new ArrayList<>();
    private final List<Material> materialesMostrados = new ArrayList<>();
    private MaterialAdapter adapter;
    private int filtroActualId = R.id.chipTodos;
    private int siguienteId = 1;

    private ImageView ivPreviewActual;
    private String rutaFotoSeleccionadaActual;
    private ActivityResultLauncher<String> selectorImagenLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registrarSelectorDeImagen();

        RecyclerView rvMateriales = findViewById(R.id.rvMateriales);
        rvMateriales.setLayoutManager(new LinearLayoutManager(this));

        cargarInventarioGuardado();
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

    @Override
    protected void onPause() {
        super.onPause();
        guardarInventario();
    }

    private void registrarSelectorDeImagen() {
        selectorImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String rutaGuardada = copiarImagenAAlmacenamientoInterno(uri);
                        if (rutaGuardada != null) {
                            rutaFotoSeleccionadaActual = rutaGuardada;
                            if (ivPreviewActual != null) {
                                MaterialAdapter.mostrarFotoEnImageView(ivPreviewActual, rutaFotoSeleccionadaActual);
                            }
                        }
                    }
                }
        );
    }

    private String copiarImagenAAlmacenamientoInterno(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;

            Bitmap original = BitmapFactory.decodeStream(inputStream);
            if (original == null) return null;

            Bitmap optimizada = redimensionarSiNecesario(original, 1024);

            File carpetaFotos = new File(getFilesDir(), "fotos_materiales");
            if (!carpetaFotos.exists() && !carpetaFotos.mkdirs()) {
                return null;
            }

            File archivoDestino = new File(carpetaFotos, "mat_" + UUID.randomUUID() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(archivoDestino)) {
                optimizada.compress(Bitmap.CompressFormat.JPEG, 85, out);
                out.flush();
            }

            return archivoDestino.getAbsolutePath();
        } catch (Exception e) {
            Snackbar.make(findViewById(R.id.rvMateriales),
                    "No se pudo cargar la fotografía", Snackbar.LENGTH_SHORT).show();
            return null;
        }
    }

    private Bitmap redimensionarSiNecesario(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }
        float escala = Math.min((float) maxDimension / width, (float) maxDimension / height);
        int nuevoAncho = Math.round(width * escala);
        int nuevoAlto = Math.round(height * escala);
        return Bitmap.createScaledBitmap(bitmap, nuevoAncho, nuevoAlto, true);
    }

    private void eliminarArchivoFotoSiExiste(String rutaImagen) {
        if (rutaImagen != null && !rutaImagen.isEmpty()) {
            File archivo = new File(rutaImagen);
            if (archivo.exists()) {
                //noinspection ResultOfMethodCallIgnored
                archivo.delete();
            }
        }
    }

    private void cargarInventarioGuardado() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean yaCargoIniciales = prefs.getBoolean(KEY_DATOS_INICIALES_CARGADOS, false);
        siguienteId = prefs.getInt(KEY_SIGUIENTE_ID, 1);

        todosLosMateriales.clear();

        if (!yaCargoIniciales) {
            todosLosMateriales.add(new Material(siguienteId++, "Tornillos M6", 1.50, 120, 20, ""));
            todosLosMateriales.add(new Material(siguienteId++, "Varilla Corrugada 3/8", 145.00, 4, 5, ""));
            todosLosMateriales.add(new Material(siguienteId++, "Cemento Gris 50kg", 210.00, 0, 10, ""));
            prefs.edit().putBoolean(KEY_DATOS_INICIALES_CARGADOS, true).apply();
            guardarInventario();
            return;
        }

        String jsonStr = prefs.getString(KEY_MATERIALES_JSON, "[]");
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                todosLosMateriales.add(Material.fromJson(obj));
            }
        } catch (JSONException e) {
            todosLosMateriales.clear();
        }
    }

    private void guardarInventario() {
        JSONArray array = new JSONArray();
        for (Material m : todosLosMateriales) {
            try {
                array.put(m.toJson());
            } catch (JSONException ignored) {
            }
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_MATERIALES_JSON, array.toString())
                .putInt(KEY_SIGUIENTE_ID, siguienteId)
                .apply();
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
        ImageView ivPreview = vistaDialogo.findViewById(R.id.ivPreviewFoto);
        Button btnFoto = vistaDialogo.findViewById(R.id.btnSeleccionarFoto);
        EditText etNombre = vistaDialogo.findViewById(R.id.etNombre);
        EditText etPrecio = vistaDialogo.findViewById(R.id.etPrecio);
        EditText etCantidad = vistaDialogo.findViewById(R.id.etCantidad);

        rutaFotoSeleccionadaActual = "";
        ivPreviewActual = ivPreview;
        MaterialAdapter.mostrarFotoEnImageView(ivPreview, rutaFotoSeleccionadaActual);

        btnFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Material")
                .setView(vistaDialogo)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (TextUtils.isEmpty(nombre)) {
                        eliminarArchivoFotoSiExiste(rutaFotoSeleccionadaActual);
                        return;
                    }

                    double precio = parseDoubleSeguro(etPrecio.getText().toString());
                    int cantidad = parseIntSeguro(etCantidad.getText().toString());

                    Material nuevo = new Material(
                            siguienteId++,
                            nombre,
                            precio,
                            cantidad,
                            5,
                            rutaFotoSeleccionadaActual
                    );
                    todosLosMateriales.add(nuevo);
                    guardarInventario();
                    aplicarFiltro(filtroActualId);

                    if (cantidad <= 0) {
                        avisoMaterialAgotado(nombre);
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) ->
                        eliminarArchivoFotoSiExiste(rutaFotoSeleccionadaActual))
                .show();
    }

    private void mostrarDialogoEditarOEliminar(Material material) {
        View vistaDialogo = LayoutInflater.from(this).inflate(R.layout.dialog_material, null);
        ImageView ivPreview = vistaDialogo.findViewById(R.id.ivPreviewFoto);
        Button btnFoto = vistaDialogo.findViewById(R.id.btnSeleccionarFoto);
        EditText etNombre = vistaDialogo.findViewById(R.id.etNombre);
        EditText etPrecio = vistaDialogo.findViewById(R.id.etPrecio);
        EditText etCantidad = vistaDialogo.findViewById(R.id.etCantidad);

        String rutaOriginal = material.getRutaImagen() != null ? material.getRutaImagen() : "";
        rutaFotoSeleccionadaActual = rutaOriginal;
        ivPreviewActual = ivPreview;
        MaterialAdapter.mostrarFotoEnImageView(ivPreview, rutaFotoSeleccionadaActual);

        if (!rutaOriginal.isEmpty()) {
            btnFoto.setText("Cambiar fotografía");
        }

        btnFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));

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

                    if (!rutaOriginal.equals(rutaFotoSeleccionadaActual)) {
                        eliminarArchivoFotoSiExiste(rutaOriginal);
                    }

                    material.setNombre(nombre);
                    material.setPrecio(precio);
                    material.setCantidad(cantidad);
                    material.setRutaImagen(rutaFotoSeleccionadaActual);

                    guardarInventario();
                    aplicarFiltro(filtroActualId);

                    if (cantidad <= 0) {
                        avisoMaterialAgotado(nombre);
                    }
                })
                .setNeutralButton("Eliminar", (dialog, which) -> {
                    if (!rutaOriginal.equals(rutaFotoSeleccionadaActual)) {
                        eliminarArchivoFotoSiExiste(rutaFotoSeleccionadaActual);
                    }
                    eliminarArchivoFotoSiExiste(material.getRutaImagen());
                    todosLosMateriales.remove(material);
                    guardarInventario();
                    aplicarFiltro(filtroActualId);
                    Snackbar.make(findViewById(R.id.rvMateriales),
                            "Se eliminó: " + material.getNombre(), Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    if (!rutaOriginal.equals(rutaFotoSeleccionadaActual)) {
                        eliminarArchivoFotoSiExiste(rutaFotoSeleccionadaActual);
                    }
                })
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