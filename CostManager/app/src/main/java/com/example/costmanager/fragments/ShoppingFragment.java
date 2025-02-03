package com.example.costmanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.costmanager.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.costmanager.adapters.FruitAdapter;
import com.example.costmanager.databinding.FragmentShoppingBinding;
import com.example.costmanager.models.Product;
import com.example.costmanager.utils.FruitConstants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;

public class ShoppingFragment extends Fragment implements FruitAdapter.OnQuantityChangeListener {
    private FragmentShoppingBinding binding;
    private FruitAdapter adapter;
    private List<Product> cartItems;
    private FirebaseAuth mAuth;
    private DatabaseReference cartRef;
    private ValueEventListener cartListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShoppingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadCartItems();
        loadUsername();
       
        binding.addProductFab.setOnClickListener(v -> {
            if (isAdded()) {
                Navigation.findNavController(v).navigate(R.id.action_shopping_to_addProduct);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new FruitAdapter(this);
        binding.fruitsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.fruitsRecyclerView.setAdapter(adapter);
    }

    private void loadCartItems() {
        cartListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                
                cartItems = new ArrayList<>();
                double total = 0;
                
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null && product.getQuantity() > 0) {
                        cartItems.add(product);
                        total += product.getPrice() * product.getQuantity();
                    }
                }
                
                if (binding != null) {
                    adapter.setFruits(cartItems);
                    binding.totalCost.setText(String.format("Total: $%.2f", total));
                    
                    if (cartItems.isEmpty()) {
                        binding.emptyStateText.setVisibility(View.VISIBLE);
                        binding.fruitsRecyclerView.setVisibility(View.GONE);
                    } else {
                        binding.emptyStateText.setVisibility(View.GONE);
                        binding.fruitsRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                Log.e("ShoppingFragment", "Error loading cart", error.toException());
            }
        };
        
        cartRef.addValueEventListener(cartListener);
    }

    private void loadUsername() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded() && binding != null) {
                    String username = snapshot.child("username").getValue(String.class);
                    if (username != null) {
                        binding.usernameText.setText("Welcome, " + username);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ShoppingFragment", "Error loading username", error.toException());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cartRef != null && cartListener != null) {
            cartRef.removeEventListener(cartListener);
        }
        binding = null;
    }

    @Override
    public void onQuantityChanged(Product product, int newQuantity) {
        if (!isAdded()) return;
        
        if (newQuantity == 0) {
            cartRef.child(product.getId()).removeValue();
        } else {
            product.setQuantity(newQuantity);
            cartRef.child(product.getId()).setValue(product);
        }
    }
} 