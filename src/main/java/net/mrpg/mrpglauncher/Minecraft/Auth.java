package net.mrpg.mrpglauncher.Minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class Auth {

    public static Auth instance;
    private static final Path SESSION_PATH = Path.of(System.getProperty("user.home"), ".mrpg-launcher", "session.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private StepFullJavaSession.FullJavaSession currentSession;

    public Auth() {
        instance = this;
        loadSession();
    }

    public boolean auth() {
        return auth(null);
    }

    public boolean auth(Consumer<StepMsaDeviceCode.MsaDeviceCode> deviceCodeCallback) {
        try {
            HttpClient httpClient = MinecraftAuth.createHttpClient();
            StepFullJavaSession.FullJavaSession javaSession = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCode -> {
                // Method to generate a verification URL and a code for the user to enter on that page
                System.out.println("Device code received: " + msaDeviceCode.getUserCode());
                System.out.println("Verification URL: " + msaDeviceCode.getDirectVerificationUri());

                // Call custom callback if provided
                if (deviceCodeCallback != null) {
                    deviceCodeCallback.accept(msaDeviceCode);
                }
            }));

            System.out.println("Authentication successful!");
            System.out.println("Username: " + javaSession.getMcProfile().getName());

            currentSession = javaSession;
            // Save session for future use
            saveSession();

            return true;
        } catch (Exception e) {
            System.err.println("Authentication failed with error: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        try {
            currentSession = null;
            Files.deleteIfExists(SESSION_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        if (currentSession != null && currentSession.getMcProfile() != null) {
            return currentSession.getMcProfile().getName();
        }
        return null;
    }

    public boolean isLoggedIn() {
        return currentSession != null && currentSession.getMcProfile() != null;
    }

    private void loadSession() {
        try {
            if (Files.exists(SESSION_PATH)) {
                String json = Files.readString(SESSION_PATH, StandardCharsets.UTF_8);
                com.google.gson.JsonObject jsonObject = GSON.fromJson(json, com.google.gson.JsonObject.class);
                currentSession = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.fromJson(jsonObject);

                if (currentSession != null) {
                    System.out.println("Loaded saved session for: " + getUsername());

                    // Try to refresh the session if needed
                    if (shouldRefreshSession()) {
                        refreshSession();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load session: " + e.getMessage());
            e.printStackTrace();
            // If loading fails, clear the corrupted session file
            try {
                Files.deleteIfExists(SESSION_PATH);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void saveSession() {
        try {
            Files.createDirectories(SESSION_PATH.getParent());
            com.google.gson.JsonObject jsonObject = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.toJson(currentSession);
            String json = GSON.toJson(jsonObject);
            Files.writeString(SESSION_PATH, json, StandardCharsets.UTF_8);
            System.out.println("Session saved successfully");
        } catch (Exception e) {
            System.err.println("Failed to save session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean shouldRefreshSession() {
        if (currentSession == null) {
            return false;
        }
        return currentSession.isExpiredOrOutdated();
    }

    private void refreshSession() {
        try {
            HttpClient httpClient = MinecraftAuth.createHttpClient();
            // Use the refresh token to get a new session
            currentSession = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.refresh(httpClient, currentSession);
            saveSession();
            System.out.println("Session refreshed successfully");
        } catch (Exception e) {
            System.err.println("Failed to refresh session: " + e.getMessage());
            e.printStackTrace();
            // If refresh fails, clear the session
            currentSession = null;
            try {
                Files.deleteIfExists(SESSION_PATH);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
