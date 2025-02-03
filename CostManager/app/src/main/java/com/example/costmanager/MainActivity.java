package com.example.costmanager;

import android.os.Bundle;
import android.util.Log;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.example.costmanager.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the toolbar
        setSupportActionBar(binding.toolbar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup Navigation
        NavHostFragment navHostFragment = 
            (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            // Configure the ActionBar
            appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.loginFragment  // Only loginFragment is top-level
            ).build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // Check if user is not logged in, navigate to login
            if (mAuth.getCurrentUser() == null && 
                navController.getCurrentDestination().getId() != R.id.loginFragment) {
                navController.navigate(R.id.loginFragment);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onBackPressed() {
        if (navController == null) {
            super.onBackPressed();
            return;
        }
        
        try {
            int currentDestination = navController.getCurrentDestination() != null ? 
                navController.getCurrentDestination().getId() : -1;

            if (currentDestination == R.id.shoppingFragment) {
                // Show a confirmation dialog
                new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to return to login?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            FirebaseAuth.getInstance().signOut();
                            // Use navigate instead of popBackStack
                            navController.navigate(R.id.action_shopping_to_login);
                        } catch (Exception e) {
                            Log.e("MainActivity", "Navigation error", e);
                            // If navigation fails, try to finish the activity
                            try {
                                finish();
                            } catch (Exception e2) {
                                Log.e("MainActivity", "Failed to finish activity", e2);
                            }
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
            } else if (currentDestination == R.id.loginFragment) {
                // If on login page, exit app
                finish();
            } else {
                // For other fragments, use the standard back navigation
                super.onBackPressed();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error handling back press", e);
            // If all else fails, try to finish the activity
            try {
                finish();
            } catch (Exception e2) {
                Log.e("MainActivity", "Failed to finish activity", e2);
            }
        }
    }
}