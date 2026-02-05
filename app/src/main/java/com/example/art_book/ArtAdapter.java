package com.example.art_book;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.art_book.databinding.RecyclerRowBinding;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {
    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ArtHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;
        public ArtHolder(RecyclerRowBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
