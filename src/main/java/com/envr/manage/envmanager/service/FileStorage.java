package com.envr.manage.envmanager.service;

import com.envr.manage.envmanager.models.EnvironmentVariables;
import com.intellij.openapi.diagnostic.Logger;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * save and retrieve from local file on disk
 */
public class FileStorage implements EnvironmentVariableStorage {
    private static final String AES = "AES";
    private final char[] pin;
    private final String environmentFileLocation;
    private final Logger log = Logger.getInstance(FileStorage.class.getName());

    public FileStorage(String envFileLocation,char[] pin) {
        this.environmentFileLocation = envFileLocation;
        this.pin = pin;
    }

    public void saveEnvironments(HashSet<EnvironmentVariables> myEnvironments) throws IOException {
        try {
            byte[] salt = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);
            SealedObject x = encryptObject(myEnvironments, salt);
            Map<String, Object> data = new HashMap<>();
            data.put("DATA", x);
            data.put("SALT", salt);

            ObjectOutputStream oos;
            try (FileOutputStream fos = new FileOutputStream(environmentFileLocation)) {
                oos = new ObjectOutputStream(fos);
                oos.writeObject(data);
                oos.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw e;
        }
    }

    @Override
    public void backupEnvironments() throws IOException {
        try{
            Path source = Path.of(environmentFileLocation);
            if(Files.exists(source)){
                String targetPAth = environmentFileLocation + Instant.now().getEpochSecond();
                Files.copy(source,Path.of(targetPAth));
            }

        } catch(Exception ex){
            log.error(ex.getMessage(),ex);
            throw ex;
        }
    }

    private static SecretKey generateAESKeyFromPIN(char[] pin, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 10000; // Number of iterations
        int keyLength = 128;    // Key length in bits (128, 192, or 256)

        KeySpec keySpec = new javax.crypto.spec.PBEKeySpec(pin, salt, iterations, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();

        return new SecretKeySpec(keyBytes, AES);
    }

    public SealedObject encryptObject(Serializable object, byte[] salt) {
        try {
            SecretKey secretKey = generateAESKeyFromPIN(pin, salt);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return new SealedObject(object, cipher);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        return null;
    }

    public Serializable decryptObject(SealedObject sealedObject, byte[] salt) {
        try {
            SecretKey secretKey = generateAESKeyFromPIN(pin, salt);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return (Serializable) sealedObject.getObject(cipher);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        return null;
    }

    public HashSet<EnvironmentVariables> getEnvironments() throws IOException, ClassNotFoundException {
        try {
            BufferedInputStream bufferedInputStream;
            try (FileInputStream fileInputStream = new FileInputStream(environmentFileLocation)) {
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                ObjectInputStream inputStream = new ObjectInputStream(bufferedInputStream);
                Map<String, Object> data = (Map<String, Object>) inputStream.readObject();
                SealedObject obj = (SealedObject) data.get("DATA");
                byte[] salt = (byte[]) data.get("SALT");
                return (HashSet<EnvironmentVariables>) decryptObject(obj, salt);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw e;
        }
    }

}
