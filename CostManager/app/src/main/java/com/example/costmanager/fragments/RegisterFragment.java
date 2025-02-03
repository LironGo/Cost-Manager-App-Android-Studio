package com.example.costmanager.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.costmanager.R;
import com.example.costmanager.databinding.FragmentRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";
    private FragmentRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.registerButton.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        String email = binding.emailInput.getText().toString().trim();
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();
        boolean hasError = false;

        // Clear previous errors
        binding.emailLayout.setError(null);
        binding.usernameLayout.setError(null);
        binding.passwordLayout.setError(null);
        binding.phoneLayout.setError(null);
        binding.errorText.setVisibility(View.GONE);

        if (email.isEmpty()) {
            binding.emailLayout.setError("Email is required");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Invalid email format");
            hasError = true;
        }

        if (username.isEmpty()) {
            binding.usernameLayout.setError("Username is required");
            hasError = true;
        }

        if (password.isEmpty()) {
            binding.passwordLayout.setError("Password is required");
            hasError = true;
        } else if (password.length() < 6) {
            binding.passwordLayout.setError("Password must be at least 6 characters");
            hasError = true;
        }

        if (phone.isEmpty()) {
            binding.phoneLayout.setError("Phone number is required");
            hasError = true;
        } else if (!phone.matches("\\d{10}")) {
            binding.phoneLayout.setError("Invalid phone number format");
            hasError = true;
        }

        if (hasError) return;

        binding.registerButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        registerUser(email, username, password, phone);
    }

    private void registerUser(String email, String username, String password, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        UserData userData = new UserData(username, phone, email);
                        
                        usersRef.child(userId).setValue(userData)
                                .addOnSuccessListener(aVoid -> {
                                    binding.registerButton.setEnabled(true);
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Registration successful", 
                                                 Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                    Navigation.findNavController(requireView())
                                            .navigate(R.id.action_register_to_login);
                                })
                                .addOnFailureListener(e -> {
                                    binding.registerButton.setEnabled(true);
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.errorText.setVisibility(View.VISIBLE);
                                    binding.errorText.setText("Failed to save user data. Please try again.");
                                    Log.e(TAG, "Database error: ", e);
                                    // Delete the authentication user since we couldn't save the data
                                    mAuth.getCurrentUser().delete();
                                });
                    } else {
                        binding.registerButton.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);
                        binding.errorText.setVisibility(View.VISIBLE);
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Registration failed";
                        binding.errorText.setText(errorMessage);
                    }
                });
    }

    private static class UserData {
        public String username;
        public String phone;
        public String email;

        public UserData() {
            // Required empty constructor for Firebase
        }

        public UserData(String username, String phone, String email) {
            this.username = username;
            this.phone = phone;
            this.email = email;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 