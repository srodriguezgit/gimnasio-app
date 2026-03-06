package ar.argentech.domain;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {

  private static final SecureRandom RNG = new SecureRandom();
  private static final int SALT_LEN = 16;
  private static final int ITERATIONS = 120_000;
  private static final int KEY_LEN_BITS = 256;

  // Formato guardado: pbkdf2$iter$saltBase64$hashBase64
  public static String hash(String password) {
    try {
      byte[] salt = new byte[SALT_LEN];
      RNG.nextBytes(salt);

      byte[] dk = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LEN_BITS);
      return "pbkdf2$" + ITERATIONS + "$" +
          Base64.getEncoder().encodeToString(salt) + "$" +
          Base64.getEncoder().encodeToString(dk);

    } catch (Exception e) {
      throw new RuntimeException("No se pudo hashear password", e);
    }
  }

  public static boolean verify(String password, String stored) {
    try {
      if (stored == null || !stored.startsWith("pbkdf2$")) return false;

      String[] parts = stored.split("\\$");
      int it = Integer.parseInt(parts[1]);
      byte[] salt = Base64.getDecoder().decode(parts[2]);
      byte[] hash = Base64.getDecoder().decode(parts[3]);

      byte[] test = pbkdf2(password.toCharArray(), salt, it, hash.length * 8);

      // compare constante
      int diff = 0;
      for (int i = 0; i < hash.length; i++) diff |= (hash[i] ^ test[i]);
      return diff == 0;

    } catch (Exception e) {
      return false;
    }
  }

  private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLenBits) throws Exception {
    PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLenBits);
    SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    return skf.generateSecret(spec).getEncoded();
  }
}
