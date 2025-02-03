package com.example.costmanager.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.costmanager.R;
import com.example.costmanager.databinding.FragmentAddProductBinding;
import com.example.costmanager.models.Product;
import com.example.costmanager.utils.FruitConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddProductFragment extends Fragment {
    private FragmentAddProductBinding binding;
    private DatabaseReference cartRef;
    private FirebaseAuth mAuth;
    private int selectedFruitPosition = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFruitSpinner();
        setupQuantityListener();
        setupAddToCartButton();
    }

    private void setupFruitSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            FruitConstants.FRUITS
        );
        binding.fruitSpinner.setAdapter(adapter);
        binding.fruitSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedFruitPosition = position;
            updatePrice();
        });
    }

    private void setupQuantityListener() {
        binding.quantityInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePrice();
            }
        });
    }

    private void updatePrice() {
        if (selectedFruitPosition != -1 && !binding.quantityInput.getText().toString().isEmpty()) {
            int quantity = Integer.parseInt(binding.quantityInput.getText().toString());
            double price = FruitConstants.PRICES.get(selectedFruitPosition);
            double total = quantity * price;
            binding.priceText.setText(String.format("Total Price: $%.2f", total));
        } else {
            binding.priceText.setText("");
        }
    }

    private void setupAddToCartButton() {
        binding.addToCartButton.setOnClickListener(v -> {
            if (selectedFruitPosition == -1) {
                Toast.makeText(getContext(), "Please select a fruit", Toast.LENGTH_SHORT).show();
                return;
            }

            String quantityStr = binding.quantityInput.getText().toString();
            if (quantityStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    Toast.makeText(getContext(), "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.addToCartButton.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);

            Product product = new Product(
                String.valueOf(selectedFruitPosition),
                FruitConstants.FRUITS.get(selectedFruitPosition),
                FruitConstants.PRICES.get(selectedFruitPosition),
                quantity
            );

            cartRef.child(product.getId()).setValue(product)
                    .addOnSuccessListener(aVoid -> {
                        if (!isAdded()) return;
                        
                        binding.addToCartButton.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);
                        
                        try {
                            Toast.makeText(requireContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                            requireActivity().runOnUiThread(() -> {
                                try {
                                    if (isAdded() && binding != null) {
                                        Navigation.findNavController(binding.getRoot())
                                                .navigate(R.id.action_addProduct_to_shopping);
                                    }
                                } catch (Exception e) {
                                    Log.e("AddProduct", "Navigation error", e);
                                }
                            });
                        } catch (Exception e) {
                            Log.e("AddProduct", "Error showing toast or navigating", e);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        binding.addToCartButton.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Failed to add: " + e.getMessage(),
                                     Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 