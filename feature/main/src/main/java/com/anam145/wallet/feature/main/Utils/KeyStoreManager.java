package com.anam145.wallet.feature.main.Utils;

import org.web3j.crypto.CipherException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.crypto.generators.SCrypt;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.Arrays;
import java.math.BigInteger;
import java.io.File;
import java.util.UUID;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.anam145.wallet.feature.miniapp.IMainBridgeService;
import com.anam145.wallet.feature.miniapp.common.bridge.service.MainBridgeService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KeyStoreManager {
    private static final String HEX_PREFIX = "0x";
    private static final char[] HEX_CHAR_MAP = "0123456789abcdef".toCharArray();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static IMainBridgeService mainBridgeService;
    private static boolean isBound = false;

    static{
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    static byte[] generateRandomBytes(int size) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    public static byte[] sha3(byte[] input, int offset, int length){
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(input, offset, length);
        return kecc.digest();
    }
    public static byte[] sha3(byte[] input) {
        return sha3(input, 0, input.length);
    }

    private static byte[] generateMac(byte[] derivedKey, byte[] cipherText) {
        byte[] result = new byte[16 + cipherText.length];

        System.arraycopy(derivedKey, 16, result, 0, 16);
        System.arraycopy(cipherText, 0, result, 16, cipherText.length);

        return sha3(result);
    }

    public static String toHexString(byte[] input, int offset, int length, boolean withPrefix) {
        final String output = new String(toHexCharArray(input, offset, length));
        return withPrefix ? new StringBuilder(HEX_PREFIX).append(output).toString() : output;
    }
    private static char[] toHexCharArray(byte[] input, int offset, int length) {
        final char[] output = new char[length << 1];
        for (int i = offset, j = 0; i < length + offset; i++, j++) {
            final int v = input[i] & 0xFF;
            output[j++] = HEX_CHAR_MAP[v >>> 4];
            output[j] = HEX_CHAR_MAP[v & 0x0F];
        }
        return output;
    }
    public static boolean SisEmpty(String s) {
        return s == null || s.isEmpty();
    }
    public static boolean containsHexPrefix(String input) {
        return !SisEmpty(input)
                && input.length() > 1
                && input.charAt(0) == '0'
                && input.charAt(1) == 'x';
    }
    public static String cleanHexPrefix(String input) {
        if (containsHexPrefix(input)) {
            return input.substring(2);
        } else {
            return input;
        }
    }
    public static byte[] hexStringToByteArray(String input) {
        String cleanInput = cleanHexPrefix(input);

        int len = cleanInput.length();

        if (len == 0) {
            return new byte[] {};
        }

        byte[] data;
        int startIdx;
        if (len % 2 != 0) {
            data = new byte[(len / 2) + 1];
            data[0] = (byte) Character.digit(cleanInput.charAt(0), 16);
            startIdx = 1;
        } else {
            data = new byte[len / 2];
            startIdx = 0;
        }

        for (int i = startIdx; i < len; i += 2) {
            data[(i + 1) / 2] =
                    (byte)
                            ((Character.digit(cleanInput.charAt(i), 16) << 4)
                                    + Character.digit(cleanInput.charAt(i + 1), 16));
        }
        return data;
    }
    public static byte[] toBytesPadded(BigInteger value, int length) {
        byte[] result = new byte[length];
        byte[] bytes = value.toByteArray();

        int bytesLength;
        int srcOffset;
        if (bytes[0] == 0) {
            bytesLength = bytes.length - 1;
            srcOffset = 1;
        } else {
            bytesLength = bytes.length;
            srcOffset = 0;
        }

        if (bytesLength > length) {
            throw new RuntimeException("Input is too large to put in byte array of size " + length);
        }

        int destOffset = length - bytesLength;
        System.arraycopy(bytes, srcOffset, result, destOffset, bytesLength);
        return result;
    }

    public static String toHexStringNoPrefix(byte[] input) {
        return toHexString(input, 0, input.length, false);
    }

    public static String generateWalletFile(String Password, String address, String PrivateKey) throws Exception{
        // 1. Password로 Derived Key 생성하기 (SCRYPT)
        byte[] salt = generateRandomBytes(32);

        byte[] derivedKey = SCrypt.generate(Password.getBytes(UTF_8), salt, 1 << 12, 8, 6, 32);
        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);

        // 2. AES Encryption
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        byte[] iv = generateRandomBytes(16);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        BigInteger privateKeyBig = new BigInteger(PrivateKey, 16);
        byte[] privateKeyBytes = toBytesPadded(privateKeyBig, 32);

        SecretKeySpec derivedKeySpec = new SecretKeySpec(encryptKey, "AES");

        cipher.init(Cipher.ENCRYPT_MODE, derivedKeySpec, ivSpec);
        byte[] cipherText = cipher.doFinal(privateKeyBytes);

        // Generate Mac value
        byte[] mac = generateMac(derivedKey, cipherText);

        System.out.println("개인키: "+PrivateKey);
        System.out.println("Mac: " + toHexString(mac, 0, mac.length, false));

        // how to make & return JSONObject
        //TODO

        KeyStoreFile KeyStoreFile = new KeyStoreFile();
        KeyStoreFile.setAddress(address);

        KeyStoreFile.Crypto crypto = new KeyStoreFile.Crypto();
        crypto.setCipher("aes-128-ctr");
        crypto.setCiphertext(toHexStringNoPrefix(cipherText));

        KeyStoreFile.CipherParams cipherParams = new KeyStoreFile.CipherParams();
        cipherParams.setIv(toHexStringNoPrefix(iv));
        crypto.setCipherparams(cipherParams);

        crypto.setKdf("scrypt");
        KeyStoreFile.ScryptKdfParams kdfParams = new KeyStoreFile.ScryptKdfParams();
        kdfParams.setDklen(32);
        kdfParams.setN(262144);
        kdfParams.setP(1);
        kdfParams.setR(8);
        kdfParams.setSalt(toHexStringNoPrefix(salt));
        crypto.setKdfparams(kdfParams);

        crypto.setMac(toHexStringNoPrefix(mac));
        KeyStoreFile.setCrypto(crypto);
        KeyStoreFile.setId(UUID.randomUUID().toString());
        KeyStoreFile.setVersion(1);

        DateTimeFormatter format = DateTimeFormatter.ofPattern("'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        String fileName = now.format(format) + KeyStoreFile.getAddress() + ".json";

//        File Destination = new File(destinationDirectory, fileName);
//
//        objectMapper.writeValue(Destination, KeyStoreFile);

        String contents = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(KeyStoreFile);
        return fileName;
    }

    public static void compareMac(String Password) throws Exception{
        //TODO: UPDATE
        String PrivateKey = "[**HIDEN**]";
        String strSalt = "[**HIDEN**]";
        String strIv = "[**HIDEN**]";
        byte[] salt = hexStringToByteArray(strSalt);
        byte[] iv = hexStringToByteArray(strIv);

        byte[] derivedKey = SCrypt.generate(Password.getBytes(UTF_8), salt, 262144, 8, 1, 32);
        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);


        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        BigInteger privateKeyBig = new BigInteger(PrivateKey, 16);
        byte[] privateKeyBytes = toBytesPadded(privateKeyBig, 32);
        SecretKeySpec derivedKeySpec = new SecretKeySpec(encryptKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, derivedKeySpec, ivSpec);
        byte[] cipherText = cipher.doFinal(privateKeyBytes);


        // Generate Mac value
        byte[] mac = generateMac(derivedKey, cipherText);
        System.out.println("AES CipherText: " +toHexString(cipherText, 0, mac.length, false));
        System.out.println("Mac: "+toHexString(mac, 0, mac.length, false));
    }
    static void validate(KeyStoreFile KeyStoreFile) throws CipherException {
        KeyStoreFile.Crypto crypto = KeyStoreFile.getCrypto();

        if (KeyStoreFile.getVersion() != 1) {
            throw new CipherException("Wallet version is not supported");
        }

        if (!crypto.getCipher().equals("aes-128-ctr")) {
            throw new CipherException("Wallet cipher is not supported");
        }

        if (!crypto.getKdf().equals("pbkdf2") && !crypto.getKdf().equals("scrypt")) {
            throw new CipherException("KDF type is not supported");
        }
    }
    public static Credentials decrypt(String password, KeyStoreFile KeyStoreFile) throws CipherException{
        validate(KeyStoreFile);

        KeyStoreFile.Crypto crypto = KeyStoreFile.getCrypto();

        byte[] mac = hexStringToByteArray(crypto.getMac());
        byte[] iv = hexStringToByteArray(crypto.getCipherparams().getIv());
        byte[] cipherText = hexStringToByteArray(crypto.getCiphertext());

        byte[] derivedKey;

        KeyStoreFile.KdfParams kdfParams = crypto.getKdfparams();
        if(kdfParams instanceof KeyStoreFile.ScryptKdfParams){
            KeyStoreFile.ScryptKdfParams scryptKdfParams = (KeyStoreFile.ScryptKdfParams) crypto.getKdfparams();
            int dklen = scryptKdfParams.getDklen();
            int n = scryptKdfParams.getN();
            int p = scryptKdfParams.getP();
            int r = scryptKdfParams.getR();
            byte[] salt = hexStringToByteArray(scryptKdfParams.getSalt());
            derivedKey = SCrypt.generate(password.getBytes(UTF_8), salt, n, r, p, dklen);
        }
        // TODO: 아니면 어떡하는데?
        else{
            throw new CipherException("Unable to deserialize params: " + crypto.getKdf());
        }

        byte[] derivedMac = generateMac(derivedKey, cipherText);

        if(!Arrays.equals(derivedMac, mac)){
            throw new CipherException("Invalid Password provided");
        }

        byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);
        byte[] privateKey;
        // byte[] privateKey = performCipherOperation(Cipher.DECRYPT_MODE, iv, encryptKey, cipherText);
        try{
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            privateKey = cipher.doFinal(cipherText);
        } catch (NoSuchPaddingException
                 | NoSuchAlgorithmException
                 | InvalidAlgorithmParameterException
                 | InvalidKeyException
                 | IllegalBlockSizeException
                 | BadPaddingException e) {
            throw new CipherException("Error performing cipher operation", e);
        }
        return new Credentials(KeyStoreFile.getAddress(), toHexStringNoPrefix(privateKey));
    }


    /**
     * MainBridgeService 연결 관리 객체
     */
    private static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainBridgeService = IMainBridgeService.Stub.asInterface(service);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainBridgeService = null;
            isBound = false;
        }
    };

    /**
     * MainBridgeService에 연결
     *
     * @param context Android Context (Activity 또는 Service)
     *
     * 사용 예시:
     * KeyStoreManager.connectToService(this);
     */
    public static void connectToService(Context context) {
        Intent intent = new Intent(context, MainBridgeService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * MainBridgeService로부터 지갑 정보를 가져와 암호화된 키스토어 파일 생성
     *
     * @param context Android Context
     * @param password 키스토어 암호화에 사용할 패스워드
     * @return 생성된 키스토어 파일명
     * @throws Exception 서비스 미연결, 지갑 정보 없음, 암호화 실패 등
     *
     * 사용 예시:
     * try {
     *     String fileName = KeyStoreManager.encryptWalletFromService(this, "myPassword123");
     *     System.out.println("키스토어 파일 생성: " + fileName);
     * } catch (Exception e) {
     *     e.printStackTrace();
     * }
     */
    public static String encryptWalletFromService(Context context, String password) throws Exception {
        if (!isBound || mainBridgeService == null) {
            throw new Exception("MainBridgeService not connected");
        }

        String privateKey = mainBridgeService.getPrivateKey();
        String address = mainBridgeService.getAddress();

        if (privateKey.isEmpty() || address.isEmpty()) {
            throw new Exception("No wallet data found in MainBridgeService");
        }

        // 기존 generateWalletFile 메서드 호출
        return generateWalletFile(password, address, privateKey);
    }

    /**
     * MainBridgeService 연결 해제
     *
     * @param context Android Context
     *
     * 사용 예시:
     * KeyStoreManager.disconnectService(this);
     *
     * 주의: 작업 완료 후 반드시 호출하여 메모리 누수 방지
     */
    public static void disconnectService(Context context) {
        if (isBound) {
            context.unbindService(serviceConnection);
            isBound = false;
        }
    }

}