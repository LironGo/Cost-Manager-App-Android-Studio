# Shopping List Manager App

A simple Android application that helps users manage their shopping list. Users can create accounts, log in, and maintain their personal shopping cart with various products.

## Features

- **User Authentication**
  - Register new account with email, username, phone number
  - Login with username and password
  - Secure Firebase authentication
  - Persistent login state

- **Shopping List Management**
  - Add products to cart with quantities
  - View all items in shopping cart
  - Update quantities directly from the list
  - Remove items from cart
  - Real-time total price calculation
  - Welcome message with username

- **Product Management**
  - Select products from predefined list
  - Specify quantity for each product
  - See price calculation before adding to cart
  - Easy-to-use dropdown product selection

![צילום מסך 2025-02-23 025554](https://github.com/user-attachments/assets/82ce36d6-1d23-4865-a32b-a6738917e8fb)
![צילום מסך 2025-02-23 025618](https://github.com/user-attachments/assets/90bf463c-1540-4336-a350-f8b89b32f6b6)
![צילום מסך 2025-02-23 025733](https://github.com/user-attachments/assets/bf5ac0d3-0d52-4ae5-9c22-a9d9dc883a74)
![צילום מסך 2025-02-23 025853](https://github.com/user-attachments/assets/0fec7185-2ed8-437c-83f9-486962af6e9c)
![צילום מסך 2025-02-23 025912](https://github.com/user-attachments/assets/bc5cfdea-6edc-4f92-aa0c-8db38e9b561d)
![צילום מסך 2025-02-23 025904](https://github.com/user-attachments/assets/f51a9bd3-c109-4b88-9b57-8e71f4028718)

# In order to run the app

# Adding google-services.json from Firebase to Android Studio

This tutorial explains how to integrate Firebase into an Android project while requiring each user to add their own `google-services.json` file manually.

## 1. Generate google-services.json from Firebase

Each user must generate their own `google-services.json` from Firebase:

1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Select your Firebase project (or create a new one).
3. Click on the gear icon ⚙️ > **Project settings**.
4. In the **General** tab, scroll down to **Your apps** and select **Android**.
5. Register your app by providing the package name (ensure it matches your app's package name).
6. Download the `google-services.json` file.

## 2. Add google-services.json to Android Studio

Each user must place their `google-services.json` file manually:

1. Open Android Studio.
2. Copy `google-services.json`.
3. Paste it inside the `app/` directory (not the root directory of the project).

## 3. Ignore google-services.json in Version Control

Since each user needs their own Firebase configuration, `google-services.json` should not be committed to version control.

1. Open your project's `.gitignore` file.
2. Add the following line if not already present:
   ```gitignore
   android/app/google-services.json
   ```

This ensures that `google-services.json` is ignored when pushing code to a Git repository.

## 4. Modify build.gradle Files

Ensure that Firebase is correctly set up in your project by updating the necessary Gradle files.

### **Project-Level build.gradle (Root-level)**

Modify the `build.gradle` file in the **root project** directory:

```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.3.10' // Use the latest version
    }
}
```

### **App-Level build.gradle (Inside `app/` Directory)**

Modify the `build.gradle` file in the `app` directory:

```gradle
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services' // Apply Google Services plugin
}
```

## 5. Sync Gradle & Run the App

1. Click **Sync Now** in Android Studio to ensure Firebase dependencies are loaded correctly.
2. Run the app and verify that Firebase is working properly.

