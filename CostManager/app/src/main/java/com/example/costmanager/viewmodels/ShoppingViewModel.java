package com.example.costmanager.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.costmanager.models.Product;
import java.util.List;

public class ShoppingViewModel extends ViewModel {
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final MutableLiveData<Double> totalCost = new MutableLiveData<>(0.0);
    
    public LiveData<List<Product>> getProducts() {
        return products;
    }
    
    public LiveData<Double> getTotalCost() {
        return totalCost;
    }
    
    public void updateQuantity(Product product, int newQuantity) {
        // Update logic here
        product.setQuantity(newQuantity);
        // You might want to update Firebase here
    }
} 