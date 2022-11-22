package com.example.proyecto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.example.proyecto.R;
import com.example.proyecto.model.Personaje;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Clase adaptadora encargada de fungir como intermediario entre la vista y la actividad
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder> {
    //Atributos de la clase
    private List<Personaje> listaPersonajes;

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

        return recyclerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        Personaje personaje = listaPersonajes.get(position);
        holder.txtViewNombre.setText(personaje.getNombre());
        holder.txtViewActor.setText(personaje.getActor());
        holder.imgPersonaje.setImageURI(personaje.getImagen());
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
        CircularProgressDrawable progressDrawable;


        /**
         * Constructor por parámetros
         * @param itemView Vista del layout
         */
        public RecyclerHolder(@NonNull View itemView) {
            super(itemView);
            progressDrawable = new CircularProgressDrawable(itemView.getContext());
            progressDrawable.setStrokeWidth(10f);
            progressDrawable.setStyle(CircularProgressDrawable.LARGE);
            progressDrawable.setCenterRadius(30f);
            progressDrawable.start();

            //TODO --> IMPLEMENTAR GLIDE
            /*Glide.with(MainActivity.this)
                    .load("https://as1.ftcdn.net/v2/jpg/01/20/68/68/1000_F_120686889_nDaqiMH8I5AmT5B0hpuJ14ZasdrrgRAK.jpg")
                    .placeholder(progressDrawable)
                    .error(R.mipmap.ic_launcher)
                    .into(imageView);*/

            imgPersonaje = (ImageView) itemView.findViewById(R.id.image_item);
            txtViewNombre = (TextView) itemView.findViewById(R.id.txt_nombre_item);
            txtViewActor = (TextView) itemView.findViewById(R.id.txt_actor_item);
        }
    }
}
