package com.example.proyecto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.example.proyecto.R;
import com.example.proyecto.model.Personaje;

import java.util.List;

/**
 * Clase adaptadora encargada de fungir como intermediario entre la vista y la actividad
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder> {
    //Atributos de la clase
    private List<Personaje> listaPersonajes;
    private AdapterView.OnClickListener onClickListener;
    private AdapterView.OnLongClickListener onLongClickListener;

    /**
     * Constructor por parámetros
     * @param listaPersonajes Lista de personajes cuya información se desea mostrar en un
     *                        RecyclerView
     */
    public RecyclerAdapter(List<Personaje> listaPersonajes){
        this.listaPersonajes = listaPersonajes;
    }

    /**
     * Método onCreate que se ejecuta al crear el ViewHolder. Infla el diseño de cada celda, la
     * rellena con el RecyclerHolder y les asigna los oyentes necesarios
     * @param parent Vista padre en la cual se mostrará el RecyclerHolder
     * @param viewType Tipo de vista
     * @return
     */
    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_item_list, parent,false);
        RecyclerHolder recyclerHolder = new RecyclerHolder(view);

        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    /**
     * Método onBind que se encarga rellenar los elementos de la celda creada previamente con la
     * información de la lista de elementos asignada en el constructor
     * @param holder RecyclerHolder que contiene los componentes que mostrarán la información
     * @param position Position del elemento en la lista de elementos
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        // Creacion de un elementos de la clase CircularProgressDrawable para comunicar una espera
        // al usuario
        CircularProgressDrawable progressDrawable;
        progressDrawable = new CircularProgressDrawable(holder.itemView.getContext());
        progressDrawable.setStrokeWidth(10f);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.setCenterRadius(30f);
        progressDrawable.start();

        // Rellenamos los componentes de la celda
        Personaje personaje = listaPersonajes.get(position);
        holder.txtViewNombre.setText(personaje.getNombre());
        holder.txtViewActor.setText(personaje.getActor());
        Glide.with(holder.itemView.getContext())
                        .load(personaje.getImagenUri())
                        .placeholder(progressDrawable)
                        .error(R.drawable.image_not_found)
                        .into(holder.imgPersonaje);
    }

    /**
     * Setter de onClickListener
     * @param listener Oyente encargado de captar el evento onClick
     */
    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    /**
     * Setter de onLongClickListener
     * @param listener Oyente encargado de captar el evento onLongClick
     */
    public void setOnLongClickListener(View.OnLongClickListener listener){
        this.onLongClickListener = listener;
    }

    /**
     * Método que devuelve el tamaño de la lista de personajes
     * @return Devuelve el número de elementos de la lista
     */
    @Override
    public int getItemCount() {
        return listaPersonajes.size();
    }

    /**
     * Clase de tipo RecyclerHolder que extiende de ViewHolder y asocia los elementos de la vista
     * con el código
     */
    public class RecyclerHolder extends ViewHolder {
        // Atributos de la clase
        ImageView imgPersonaje;
        TextView txtViewNombre;
        TextView txtViewActor;

        /**
         * Constructor por parámetros
         * @param itemView Vista del layout
         */
        public RecyclerHolder(@NonNull View itemView) {
            super(itemView);

            // Inicialización de los atributos
            imgPersonaje = (ImageView) itemView.findViewById(R.id.image_item);
            txtViewNombre = (TextView) itemView.findViewById(R.id.txt_nombre_item);
            txtViewActor = (TextView) itemView.findViewById(R.id.txt_actor_item);
            // Asignamos un tag para posteriormente poder identificar el itemView en la actividad para
            // la creacion de los oyentes
            itemView.setTag(this);
        }
    }
}
