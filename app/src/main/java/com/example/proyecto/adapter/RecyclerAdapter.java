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

    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_item_list, parent,false);
        RecyclerHolder recyclerHolder = new RecyclerHolder(view);

        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        CircularProgressDrawable progressDrawable;
        progressDrawable = new CircularProgressDrawable(holder.itemView.getContext());
        progressDrawable.setStrokeWidth(10f);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.setCenterRadius(30f);
        progressDrawable.start();

        Personaje personaje = listaPersonajes.get(position);
        holder.txtViewNombre.setText(personaje.getNombre());
        holder.txtViewActor.setText(personaje.getActor());
        //RESIDUOS
        //holder.itemView.setOnClickListener(onClickListener);
        //holder.itemView.setOnLongClickListener(onLongClickListener);
        Glide.with(holder.itemView.getContext())
                        .load(personaje.getImagenUri())
                        .placeholder(progressDrawable)
                        .error(R.mipmap.ic_launcher)
                        .into(holder.imgPersonaje);
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.onClickListener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        this.onLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return listaPersonajes.size();
    }

    /**
     * Clase que extiende de ViewHolder y asocia los elementos de la vista con el código
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

            imgPersonaje = (ImageView) itemView.findViewById(R.id.image_item);
            txtViewNombre = (TextView) itemView.findViewById(R.id.txt_nombre_item);
            txtViewActor = (TextView) itemView.findViewById(R.id.txt_actor_item);
            itemView.setTag(this);
            //RESIDUOS
            //itemView.setOnClickListener(onClickListener);
            //itemView.setOnLongClickListener(onLongClickListener);
        }
    }
}
