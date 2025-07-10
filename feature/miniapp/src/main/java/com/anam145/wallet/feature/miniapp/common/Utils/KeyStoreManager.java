package com.anam145.wallet.feature.miniapp.common.Utils;

import static android.content.ContentValues.TAG;

import org.web3j.crypto.CipherException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.crypto.generators.SCrypt;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
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
    private static Context applicationContext;

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

    public static String generateWalletJson(String Password, String address, String PrivateKey) throws Exception{
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
        return contents;
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
    public static Map<String, String> decrypt(String password, String KeyStoreFileJson) throws CipherException{
        KeyStoreFile KeyStoreFile;
        try {
            KeyStoreFile = objectMapper.readValue(KeyStoreFileJson, KeyStoreFile.class);
        } catch (IOException e) {
            throw new CipherException("Invalid keystore JSON", e);
        }
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

        Map<String, String> result = new HashMap<>(2);
        result.put("Address",    KeyStoreFile.getAddress());
        result.put("PrivateKey", toHexStringNoPrefix(privateKey));
        return result;
    }

    /**
     * MainBridgeService 연결 관리 객체
     */
    private static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainBridgeService = IMainBridgeService.Stub.asInterface(service);
            isBound = true;
            Log.d(TAG, "MainBridgeService 연결됨");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainBridgeService = null;
            isBound = false;
            Log.d(TAG, "MainBridgeService 연결 해제됨");
        }
    };

    /**
     * 지갑 암호화 전체 프로세스 실행 (템플릿 메서드)
     *
     * 연결 -> 암호화 -> 해제를 자동으로 처리
     *
     * @param context Android Context
     * @param password 암호화에 사용할 패스워드
     * @return 암호화 결과 (성공 시 키스토어 파일명, 실패 시 null)
     */
    public static String processWalletEncryption(Context context, String password) {
        String result = null;

        try {
            // 1. MainBridgeService 연결
            connectToService(context);

            // 2. 연결 대기 (최대 5초)
            if (waitForConnection(5000)) {
                Log.d(TAG, "MainBridgeService 연결 완료");

                // 3. 지갑 데이터 가져오기
                String privateKey = getPrivateKeyFromService();
                String address = getAddressFromService();

                // 4. 암호화 실행
                result = encryptWalletData(privateKey, address, password);

            } else {
                Log.e(TAG, "MainBridgeService 연결 타임아웃");
            }

        } catch (Exception e) {
            Log.e(TAG, "지갑 암호화 프로세스 실패", e);

        } finally {
            // 5. 연결 해제 (항상 실행)
            disconnectService(context);
        }

        return result;
    }

    /**
     * MainBridgeService에 연결
     */
    private static void connectToService(Context context) {
        applicationContext = context.getApplicationContext();
        Intent intent = new Intent(applicationContext, MainBridgeService.class);
        applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "MainBridgeService 연결 시도 중...");
    }

    /**
     * 서비스 연결 대기
     *
     * @param timeoutMs 대기 시간 (밀리초)
     * @return 연결 성공 여부
     */
    private static boolean waitForConnection(long timeoutMs) {
        long startTime = System.currentTimeMillis();

        while (!isServiceConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            if (System.currentTimeMillis() - startTime > timeoutMs) {
                Log.e(TAG, "서비스 연결 타임아웃");
                return false;
            }
        }

        return true;
    }

    /**
     * MainBridgeService로부터 개인키 가져오기
     */
    private static String getPrivateKeyFromService() throws Exception {
        if (!isServiceConnected()) {
            throw new Exception("MainBridgeService not connected");
        }

        String privateKey = mainBridgeService.getPrivateKey();

        if (privateKey == null || privateKey.isEmpty()) {
            throw new Exception("No private key found in MainBridgeService");
        }

        Log.d(TAG, "개인키 조회 성공: " + privateKey.length() + "자");
        return privateKey;
    }

    /**
     * MainBridgeService로부터 주소 가져오기
     */
    private static String getAddressFromService() throws Exception {
        if (!isServiceConnected()) {
            throw new Exception("MainBridgeService not connected");
        }

        String address = mainBridgeService.getAddress();

        if (address == null || address.isEmpty()) {
            throw new Exception("No address found in MainBridgeService");
        }

        Log.d(TAG, "주소 조회 성공: " + address);
        return address;
    }

    /**
     * MainBridgeService 연결 해제
     */
    private static void disconnectService(Context context) {
        if (isBound && applicationContext != null) {
            try {
                applicationContext.unbindService(serviceConnection);
                isBound = false;
                Log.d(TAG, "MainBridgeService 연결 해제됨");
            } catch (Exception e) {
                Log.e(TAG, "Service 연결 해제 중 오류", e);
            }
        }
    }

    /**
     * 서비스 연결 상태 확인
     */
    private static boolean isServiceConnected() {
        return isBound && mainBridgeService != null;
    }

    // =========================================================================
    // 개발자가 구현해야 할 부분
    // =========================================================================

    /**
     * 지갑 데이터 암호화 구현 영역
     *
     * 이 메서드를 구현하여 개인키와 주소를 암호화하세요.
     *
     * @param privateKey MainBridgeService로부터 가져온 개인키
     * @param address MainBridgeService로부터 가져온 지갑 주소
     * @param password 사용자가 입력한 암호화 패스워드
     * @return 암호화 결과 (예: 키스토어 파일명, 암호화된 데이터 등)
     */
    private static String encryptWalletData(String privateKey, String address, String password) {
        // TODO: 암호화 로직

        Log.d(TAG, "암호화 시작");
        Log.d(TAG, "개인키 길이: " + privateKey.length());
        Log.d(TAG, "주소: " + address);
        Log.d(TAG, "패스워드 설정됨: " + (password != null && !password.isEmpty()));

        try {
            // 수정.

            return null;

        } catch (Exception e) {
            Log.e(TAG, "암호화 실패", e);
            return null;
        }
    }

}