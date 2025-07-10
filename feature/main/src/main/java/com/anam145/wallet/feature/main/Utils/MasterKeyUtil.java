package com.anam145.wallet.feature.main.Utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

class PasswordHashData {
    @SerializedName("hash")
    String hash;

    @SerializedName("salt")
    String salt;

    @SerializedName("iterations")
    int iterations;

    public PasswordHashData(String hash, String salt, int iterations) {
        this.hash = hash;
        this.salt = salt;
        this.iterations = iterations;
    }
}

public class MasterKeyUtil {
    static String fileName = "passwordHash.json";
    static String folderName = "MasterKeyDir";
    static int iterations = 100_000;  // 충분한 반복 수

    public static void genMasterKey(Context context, String _password) {
        try {
            // Salt 생성
            byte[] salt = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            // 해시 생성
            String hash = generatePBKDF2Hash(_password, salt, iterations);

            // 폴더 없으면 만들어야지 뭐..
            File dir = new File(context.getFilesDir(), folderName);
            if (!dir.exists()) dir.mkdirs();

            // 저장
            String encodedSalt = Base64.encodeToString(salt, Base64.NO_WRAP);
            PasswordHashData data = new PasswordHashData(hash, encodedSalt, iterations);
            Gson gson = new Gson();
            String json = gson.toJson(data);

            File file = new File(dir, fileName);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
                writer.flush();
                Log.d("MasterKeyUtil", "Master key generated successfully.");
                Log.d("MasterKeyUtil", "Hash: " + hash);
                Log.d("MasterKeyUtil", "Salt: " + encodedSalt);
                Log.d("MasterKeyUtil", "File path: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean vrfyPassword(Context context, String _input) {
        File dir = new File(context.getFilesDir(), folderName);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, fileName);
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            PasswordHashData storedData = gson.fromJson(reader, PasswordHashData.class);

            byte[] salt = Base64.decode(storedData.salt, Base64.NO_WRAP);
            String inputHash = generatePBKDF2Hash(_input, salt, storedData.iterations);

            return storedData.hash.equals(inputHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String generatePBKDF2Hash(String password, byte[] salt, int iterations) throws Exception {
        int keyLength = 256;  // 256-bit
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }
}
