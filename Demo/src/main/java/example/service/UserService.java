package example.service;

import example.RSAUtil;
import example.common.Result;
import example.dao.UserMapper;
import example.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public Result<String> updatePassword(String username, String oldEncrypted, String newEncrypted, RSAPrivateKey privateKey) {
        if (username == null || username.isBlank()) {
            return Result.error(401, "Unauthorized");
        }
        if (privateKey == null) {
            return Result.error(400, "Please request public key first");
        }
        if (oldEncrypted == null || oldEncrypted.isBlank() || newEncrypted == null || newEncrypted.isBlank()) {
            return Result.error(400, "Missing encrypted password");
        }

        User user = userMapper.selectById(username);
        if (user == null) {
            return Result.error(404, "User not found");
        }

        String storedPwd = user.getPsw();
        if (storedPwd == null) {
            storedPwd = "";
        }

        DecryptedPair oldPair = decryptPair(privateKey, oldEncrypted);
        if (oldPair == null) {
            return Result.error(400, "Missing encrypted password");
        }

        boolean storedMatchesA = storedPwd.equals(oldPair.a);
        boolean storedMatchesB = storedPwd.equals(oldPair.b);
        if (!storedMatchesA && !storedMatchesB) {
            return Result.error(401, "Wrong password");
        }

        DecryptedPair newPair = decryptPair(privateKey, newEncrypted);
        if (newPair == null) {
            return Result.error(400, "Missing encrypted password");
        }

        String newPwdToStore = storedMatchesA ? newPair.a : newPair.b;
        if (newPwdToStore == null || newPwdToStore.isBlank()) {
            return Result.error(400, "Invalid new password");
        }

        user.setPsw(newPwdToStore);
        userMapper.updateById(user);
        return Result.success("success");
    }

    private static DecryptedPair decryptPair(RSAPrivateKey privateKey, String hexEncrypted) {
        byte[] encryptedBytes = hexStringToBytes(hexEncrypted);
        if (encryptedBytes == null) {
            return null;
        }

        byte[] decryptedBytes;
        try {
            decryptedBytes = RSAUtil.decrypt(privateKey, encryptedBytes);
        } catch (Exception ex) {
            try {
                decryptedBytes = RSAUtil.decryptNoPadding(privateKey, encryptedBytes);
            } catch (Exception e) {
                return null;
            }
        }

        String decrypted = rstripNullChars(new String(decryptedBytes, StandardCharsets.UTF_8));
        String reversed = new StringBuilder(decrypted).reverse().toString();
        return new DecryptedPair(decrypted, reversed);
    }

    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return null;
        }
        String s = hexString.toUpperCase();
        int length = s.length() / 2;
        if (length <= 0) {
            return null;
        }
        char[] hexChars = s.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static String rstripNullChars(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == '\u0000') {
            end--;
        }
        return end == s.length() ? s : s.substring(0, end);
    }

    private static class DecryptedPair {
        private final String a;
        private final String b;

        private DecryptedPair(String a, String b) {
            this.a = a;
            this.b = b;
        }
    }
}
