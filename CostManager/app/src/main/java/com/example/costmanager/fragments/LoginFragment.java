package com.example.costmanager.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.costmanager.databinding.FragmentLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private FragmentLoginBinding binding;
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
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in AND has user data
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Verify user data exists before navigating
            usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && isAdded()) {  // Check if fragment is still attached
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_login_to_shopping);
                    } else {
                        // No user data found, sign out
                        mAuth.signOut();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                    Log.e(TAG, "Error checking user data", error.toException());
                    if (isAdded()) {  // Check if fragment is still attached
                        mAuth.signOut();
                    }
                }
            });
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Clear error when user starts typing
        binding.usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.usernameLayout.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.passwordLayout.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.loginButton.setOnClickListener(v -> attemptLogin());
        binding.registerButton.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_login_to_register));
    }

    private void attemptLogin() {
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();
        boolean hasError = false;

        if (username.isEmpty()) {
            binding.usernameLayout.setError("Username is required");
            hasError = true;
        }
        if (password.isEmpty()) {
            binding.passwordLayout.setError("Password is required");
            hasError = true;
        }

        if (hasError) return;

        binding.loginButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.errorText.setVisibility(View.GONE);

        // Create a database reference specifically for username lookup
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Query all users without ordering
        usersRef.get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !isAdded()) {
                        showError("Error checking username");
                        Log.d(TAG, "Database query failed");
                        return;
                    }

                    DataSnapshot dataSnapshot = task.getResult();
                    String userEmail = null;
                    
                    // Debug logging
                    Log.d(TAG, "Searching for username: " + username);
                    Log.d(TAG, "Total users in DB: " + dataSnapshot.getChildrenCount());
                    
                    // Iterate through all users to find matching username
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        try {
                            String dbUsername = userSnapshot.child("username").getValue(String.class);
                            Log.d(TAG, "Checking user: " + dbUsername + 
                                  " (ID: " + userSnapshot.getKey() + ")");
                            
                            if (dbUsername != null && dbUsername.equals(username)) {
                                userEmail = userSnapshot.child("email").getValue(String.class);
                                Log.d(TAG, "Match found! Email: " + userEmail);
                                break;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading user data: ", e);
                        }
                    }

                    if (userEmail == null) {
                        showError("Username not found");
                        Log.d(TAG, "No matching username found in database");
                        return;
                    }

                    // Now login with email
                    String finalUserEmail = userEmail;
                    mAuth.signInWithEmailAndPassword(userEmail, password)
                            .addOnCompleteListener(requireActivity(), authTask -> {
                                if (authTask.isSuccessful()) {
                                    Log.d(TAG, "Login successful for user: " + username);
                                    Navigation.findNavController(requireView())
                                            .navigate(R.id.action_login_to_shopping);
                                } else {
                                    Log.d(TAG, "Login failed for user: " + username);
                                    showError("Invalid password");
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Login error: ", e);
                    showError("Login failed: " + e.getMessage());
                });
    }

    private void showError(String message) {
        binding.loginButton.setEnabled(true);
        binding.progressBar.setVisibility(View.GONE);
        binding.errorText.setVisibility(View.VISIBLE);
        binding.errorText.setText(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 