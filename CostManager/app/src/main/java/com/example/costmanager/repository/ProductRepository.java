package com.example.costmanager.repository;

import com.example.costmanager.models.Product;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProductRepository {
    private final DatabaseReference cartRef;
    
    public ProductRepository() {
        cartRef = FirebaseDatabase.getInstance().getReference("cart");
    }
    
    public Task<Void> addProduct(Product product) {
        return cartRef.child(product.getId()).setValue(product);
    }
    
    public Task<DataSnapshot> getProducts() {
        return cartRef.get();
    }
} 