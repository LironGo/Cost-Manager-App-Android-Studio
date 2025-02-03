package com.example.costmanager.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.costmanager.databinding.ItemFruitBinding;
import com.example.costmanager.models.Product;

import java.util.ArrayList;
import java.util.List;

public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.FruitViewHolder> {
    private List<Product> fruits = new ArrayList<>();
    private final OnQuantityChangeListener listener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged(Product product, int newQuantity);
    }

    public FruitAdapter(OnQuantityChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FruitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFruitBinding binding = ItemFruitBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new FruitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FruitViewHolder holder, int position) {
        holder.bind(fruits.get(position));
    }

    @Override
    public int getItemCount() {
        return fruits.size();
    }

    public void setFruits(List<Product> fruits) {
        this.fruits = fruits;
        notifyDataSetChanged();
    }

    class FruitViewHolder extends RecyclerView.ViewHolder {
        private final ItemFruitBinding binding;

        FruitViewHolder(ItemFruitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product fruit) {
            binding.fruitName.setText(fruit.getName());
            binding.fruitPrice.setText(String.format("$%.2f", fruit.getPrice()));
            binding.quantity.setText(String.format("Quantity: %d", fruit.getQuantity()));

            binding.decreaseButton.setOnClickListener(v -> {
                int newQuantity = Math.max(0, fruit.getQuantity() - 1);
                listener.onQuantityChanged(fruit, newQuantity);
            });

            binding.increaseButton.setOnClickListener(v -> {
                int newQuantity = fruit.getQuantity() + 1;
                listener.onQuantityChanged(fruit, newQuantity);
            });
        }
    }
} 