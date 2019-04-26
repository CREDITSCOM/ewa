package com.credits.wallet.desktop.utils.crypto.sodium;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SodiumLibrary is a Java binding to <a href="https://download.libsodium.org/doc/" target="_blank">libsodium</a> crypto C APIs 
 * using <a href="https://github.com/java-native-access/jna" target="_blank">Java Native Access</a>.
 * All the methods are
 * static methods.
 * <p> 
 * Please look at <a href="https://github.com/muquit/libsodium-jna/" target="_blank">libsodium-jna Homepage</a> for
 * instructions on how to get started.
 * </p>
 * 
 * @see <a href="https://download.libsodium.org/doc/" target="_blank">Native libsodium</a> documentation
 * <a href="https://github.com/muquit/libsodium-jna/" target="_blank">libsodium-jna Homepage</a>
 */
public class SodiumLibrary
{
    private final static Logger logger = LoggerFactory.getLogger(SodiumLibrary.class);
    
    private static String libPath;

    private SodiumLibrary(){}
    
   /**
    * Set the absolute path of the libsodium shared library/DLL.
    *<p> 
    * This method 
    * must be called before calling any methods in libsodium-jna. Although JNA supports loading
    * a shared library from path, libsodium-jna requires specifying the absolute
    * path to make sure that the exact library is being loaded.
    * For example, in Linux, it might be /usr/local/lib/libsodium.so, in Windows, 
    * it might be c:/libs/libsodium.dll, in MacOS, it might be 
    * /usr/local/lib/libsodium.dylib etc. The point is there is no
    * ambiguity, I want to load the library I want, not one from somewhere in the path. 
    * </p>
    * @param libraryPath The absolute path of the libsodium library. 
    * <h3>Example</h3>
    * <pre>
    * <code>
    * private static String libraryPath = null;
    *
    * if (Platform.isMac())
    * {
    *     // MacOS
    *     libraryPath = "/usr/local/lib/libsodium.dylib";
    *     libraryPath = libraryPath;
    *     logger.info("Library path in Mac: " + libraryPath);
    * }
    * else if (Platform.isWindows())
    * {
    *     // Windows
    *     libraryPath = "C:/libsodium/libsodium.dll";
    *     logger.info("Library path in Windows: " + libraryPath);
    * }
    * else
    * {
    *     // Linux
    *     libraryPath = "/usr/local/lib/libsodium.so";
    *     logger.info("Library path: " + libraryPath);
    * }
    * 
    * logger.info("loading libsodium...");
    * SodiumLibrary.setLibraryPath(libraryPath);
    * // To check the native library is actually loaded, print the version of 
    * // native sodium library
    * String v = SodiumLibrary.libsodiumVersionString();
    * logger.info("libsodium version: " + v);
    * </code>
    * </pre>
    */
    public static void setLibraryPath(String libraryPath)
    {
        SodiumLibrary.libPath = libraryPath;
    }

    /**
     * @return The singleton Sodium object. The singleton pattern is adapted from 
     * kalium java library. Other than that, this library does not use any code
     * from kalium.
     *<p> 
     * Although libsodium seems to be thread safe now, the code is written 
     * sometime back and I don't have plan to remove it at this time. 
     * </p>
     * @throws RuntimeException at run time if the libsodium library path 
     * is not set by calling {@link SodiumLibrary#setLibraryPath(String)}
     */
    public static Sodium sodium()
    {
        if (SodiumLibrary.libPath == null)
        {
            logger.info("libpath not set, throw exception");
            throw new RuntimeException("Please set the absolute path of the libsodium libary by calling SodiumLibrary.setLibraryPath(path)");
        }
        Sodium sodium = SingletonHelper.instance;
//        String h = Integer.toHexString(System.identityHashCode(sodium));
        int rc = sodium.sodium_init();
        if (rc == -1)
        {
            logger.error("ERROR: sodium_init() failed: " + rc);
            throw new RuntimeException("sodium_init() failed, rc=" + rc);
        }
        return sodium;
    }

    public static void initSodium()
    {
        if (Platform.isMac())
        {
            setLibraryPath("/lib/sodium/libsodium.dylib");
        }
        else if (Platform.isWindows())
        {
            setLibraryPath("/lib/sodium/libsodium.dll");
        }
        else
        {
            setLibraryPath("/lib/sodium/libsodium.so");
        }
        sodium();
    }

    private static final class SingletonHelper
    {
        public static final Sodium instance = (Sodium) Native.loadLibrary(libPath,Sodium.class);
    }

    /**
     * Declare all the supported <a href="https://download.libsodium.org/doc/" target="_blank">libsodium</a> C functions in this interface and 
     * implement them in this class as static methods.
     * 
     */
    public interface Sodium extends Library
    {
        int sodium_init();

        /*
         * @return version string of the libsodium library
         * include/sodium/version.h
         */
        String sodium_version_string();
        
        /*
         * Fills size bytes starting at buf with an unpredictable sequence of bytes.
         * @param buf  buffer to fill with random bytes
         * @param size number of random bytes
         * <a href="https://download.libsodium.org/doc/generating_random_data/">Generating random data</a> libsodium page
         */
        void randombytes_buf(byte[] buf, int size);

        /*
         * see include/sodium/crypto_pwhash.h
         */
        int crypto_pwhash_alg_argon2i13();
        int crypto_pwhash_alg_argon2id13(); // added in libsodium 1.0.15
        int crypto_pwhash_alg_default();
        int crypto_pwhash_saltbytes();
        int crypto_pwhash_strbytes();
        Pointer crypto_pwhash_strprefix();
        long crypto_pwhash_opslimit_interactive();
        NativeLong crypto_pwhash_memlimit_interactive();
        long crypto_pwhash_opslimit_moderate();
        NativeLong crypto_pwhash_memlimit_moderate();
        long crypto_pwhash_opslimit_sensitive();
        NativeLong crypto_pwhash_memlimit_sensitive();
        
        /* sodium/crypto_box.h */
        NativeLong crypto_box_seedbytes();
        NativeLong crypto_box_publickeybytes();
        NativeLong crypto_box_secretkeybytes();
        NativeLong crypto_box_noncebytes();
        NativeLong crypto_box_macbytes();
        NativeLong crypto_box_sealbytes();

        /* sodium/crypto_auth.h */
        NativeLong crypto_auth_bytes();
        NativeLong crypto_auth_keybytes();
        
        int crypto_pwhash(byte[] key, long keylen,
                          byte[] passwd, long passwd_len,
                          byte[] in_salt,
                          long opslimit,
                          NativeLong memlimit,
                          int alg);

        int crypto_pwhash_scryptsalsa208sha256(byte[] key, long keyLength,
                                               byte[] passwd, long passwd_len,
                                               byte[] in_salt,
                                               long opslimit,
                                               NativeLong memlimit);

        int crypto_pwhash_str(byte[] hashedPassword,
                              byte[] password, long passwordLen,
                              long opslimit, NativeLong memlimit);

        int crypto_pwhash_str_verify(byte[] hashedPassword,
                                     byte[] password, long passwordLen);

        /* sodium/crypto_pwhash_scryptsalsa208sha256.h */
        NativeLong crypto_pwhash_scryptsalsa208sha256_saltbytes();

        /* Secret Key */
        NativeLong  crypto_secretbox_keybytes();
        NativeLong  crypto_secretbox_noncebytes();
        NativeLong  crypto_secretbox_macbytes();

        int crypto_secretbox_easy(byte[] cipherText,
                                  byte[] message, long mlen, byte[] nonce,
                                  byte[] key);

        int crypto_secretbox_open_easy(byte[] decrypted,
                                       byte[] cipherText, long ct_len, byte[] nonce,
                                       byte[] key);

        int crypto_secretbox_detached(byte[] cipherText,
                                      byte[] mac,
                                      byte[] message, long mlen,
                                      byte[] nonce, byte[] key);

        int crypto_secretbox_open_detached(byte[] message,
                                           byte[] cipherText, byte[] mac, long cipherTextLength,
                                           byte[] nonce, byte[] key);

        int crypto_box_seal(byte[] cipherText,
                            byte[] message, long messageLen,
                            byte[] recipientPublicKey);

        int crypto_box_seal_open(byte[] decrypted,
                                 byte[] cipherText, long cipherTextLen,
                                 byte[] recipientPublicKey, byte[] reciPientPrivateKey);

        int crypto_auth(byte[] mac, byte[] message, long messageLen, byte[] key);
        int crypto_auth_verify(byte[] mac, byte[] message, long messagelen, byte[] key);


        /* Public key authenticated encryption */
        int crypto_box_keypair(byte[] pk, byte[] sk);

       /**
        * Compute Public key from Private Key
        * @param pk - Public Key returns
        * @param sk - Private Key
        * @return 0 on success -1 on failure
        */
        int crypto_scalarmult_base(byte[] pk, byte[] sk);

        int crypto_box_easy(byte[] cipher_text,
                            byte[] plain_text, long pt_len,
                            byte[] nonce,
                            byte[] public_key, byte[] private_key);

        int crypto_box_open_easy(byte[] decrypted, byte[] cipher_text,
                                 long ct_len, byte[] nonce,
                                 byte[] public_key, byte[] private_key);

        // Signing/Signed keys
        long crypto_sign_secretkeybytes();
        long crypto_sign_publickeybytes();
        int crypto_sign_keypair(byte[] pk, byte[] sk);
        int crypto_sign_ed25519_bytes();
        int crypto_sign_bytes();

        // actual signing and verification operations of the Signing key, first detached mode, then combined mode
        int crypto_sign_detached(byte[] sig, long siglen_p,
                                 byte[] m, long mlen,
                                 byte[] sk);

        int crypto_sign_verify_detached(byte[] sig, byte[] m,
                                        long mlen, byte[] pk);

        int crypto_sign(byte[] sm, long smlen_p,
                        byte[] m, long mlen,
                        byte[] sk);

        int crypto_sign_open(byte[] m, long mlen_p,
                             byte[] sm, long smlen,
                             byte[] pk);


        // libsodium's generichash (blake2b), this function will only return outlen number of bytes
        // key can be null and keylen can be 0
        int crypto_generichash(byte[] out, int outlen,
                               byte[] in, int inlen,
                               byte[] key, long keylen);
        
        // key conversion from ED to Curve so that signed key can be used for encryption
        int crypto_sign_ed25519_sk_to_curve25519(byte[] curveSK, byte[] edSK); // secret key conversion
        int crypto_sign_ed25519_pk_to_curve25519(byte[] curvePK, byte[] edPK); // public key conversion
    
    }

   /**
    * Return unpredictable sequence of bytes.
    *
    * Excerpt from libsodium documentation:
    * <blockquote>
    * <ul>
    * <li> On Windows systems, the <code>RtlGenRandom()</code> function is used
    * <li> On OpenBSD and Bitrig, the <code>arc4random()</code> function is used
    * <li> On recent Linux kernels, the getrandom system call is used (since Sodium 1.0.3)
    * <li> On other Unices, the /dev/urandom device is used
    * <li>If none of these options can safely be used, custom implementations can easily be hooked.
    * </ul>
    * </blockquote>
    * @param  size Number of random bytes to generate
    * @return Array of random bytes
    * @see <a href="https://download.libsodium.org/doc/generating_random_data/" target="_blank">Generating random data</a> in libsodium page
    * <h3>Example</h3>
    * <pre>
    * <code>
    * // generate 16 bytes of random data
    * byte[] randomBytes = SodiumLibrary.randomBytes(16);
    * String hex = SodiumUtils.binary2Hex(salt);
    *
    * // generate libsodium's standard number of salt bytes
    * int n = SodiumLibrary.cryptoNumberSaltBytes();
    * logger.info("Generate " + n + " random bytes");
    *
    * byte[] salt = SodiumLibrary.randomBytes(n);
    * logger.info("Generated " + salt.length + " random bytes");
    * String hex = SodiumUtils.binary2Hex(salt);
    * logger.info("Random bytes: " + hex);
    * </code>
    * </pre>
    */
    public static byte[] randomBytes(int size)
    {
        byte[] buf = new byte[size];
        sodium().randombytes_buf(buf, size);
        return buf;
    }
    /**
     * Encrypts a message with a key and a nonce to keep it confidential.
     * 
     * The same key is used to encrypt and decrypt the messages. Therefore, the key must be kept confidential.
     *
     * @param message message bytes to encrypt
     * @param nonce  nonce bytes. The nonce must be  bytes long and can be generated by
     * calling {@link SodiumLibrary#randomBytes(int)}
     * @param key They key for encryption
     * @throws SodiumLibraryException on error
     * @return Encrypted cipher text bytes 
     * @see <a href="https://download.libsodium.org/libsodium/content/secret-key_cryptography/authenticated_encryption.html" target="_blank">Secret-key authenticated encryption</a>
     */  
    public static byte[] cryptoSecretBoxEasy(byte[] message, byte[] nonce, byte[] key) throws SodiumLibraryException
    {
        int nonce_length = sodium().crypto_secretbox_noncebytes().intValue();
        if (nonce_length != nonce.length)
        {
            throw new SodiumLibraryException("nonce is " + nonce.length + ", it must be" + nonce_length + " bytes");
        }
        byte[] cipherText = new byte[(sodium().crypto_box_macbytes().intValue() + message.length)];

        int rc = sodium().crypto_secretbox_easy(cipherText,message,message.length,nonce,key);
        if (rc != 0)
        {
            throw new SodiumLibraryException("libsodium crypto_secretbox_easy() failed, returned " + rc + ", expected 0");
        }
        return cipherText;
    }

   /**
    * Verifies and decrypts a ciphertext.
    * 
    *  The ciphertext is created by {@link SodiumLibrary#cryptoSecretBoxEasy(byte[] message, byte[] nonce, byte[] key)}
    * 
    * @param cipherText The ciphertext to decrypt
    * @param nonce The nonce used during encryption
    * @param key The key used in encryption
    * @return decrypted plaintext bytes
    * @throws SodiumLibraryException - if nonce size is incorrect or decryption fails
    * @see <a href="https://download.libsodium.org/libsodium/content/secret-key_cryptography/authenticated_encryption.html" target="_blank">Secret-key authenticated encryption</a>
    * <h3>Example</h3>
    * <pre>
    * <code>
    * // don't forget to load the libsodium library first
    * String message = "This is a message";
    * 
    * // generate nonce
    * long nonceBytesLength = SodiumLibrary.cryptoSecretBoxNonceBytes();
    * byte[] nonceBytes = SodiumLibrary.randomBytes((int) nonceBytesLength);
    * byte[] messageBytes = message.getBytes();
    *
    * // generate the encryption key
    * byte[] key = SodiumLibrary.randomBytes((int) SodiumLibrary.cryptoSecretBoxKeyBytes());
    * 
    * // encrypt
    * byte[] cipherText = SodiumLibrary.cryptoSecretBoxEasy(messageBytes, nonceBytes, key);
    *
    * // now decrypt
    * byte[] decryptedMessageBytes = SodiumLibrary.cryptoSecretBoxOpenEasy(cipherText, nonceBytes, key);
    * String decryptedMessage;
    * try
    * {
    *    decryptedMessage = new String(decryptedMessageBytes, "UTF-8");
    *    System.out.println("Decrypted message: " + decryptedMessageBytes);
    * } catch (UnsupportedEncodingException e)
    * {
    *    e.printStackTrace();
    * }
    * </code>
    * </pre>
    */
    public static byte[] cryptoSecretBoxOpenEasy(byte[] cipherText,byte[] nonce, byte[] key) throws SodiumLibraryException
    {
        if (key.length != sodium().crypto_secretbox_keybytes().intValue())
        {
            throw new SodiumLibraryException("invalid key length " + key.length + " bytes");
        }

        if (nonce.length != sodium().crypto_secretbox_noncebytes().intValue())
        {
            throw new SodiumLibraryException("invalid nonce length " + nonce.length + " bytes");
        }

        byte[] decrypted = new byte[(cipherText.length - sodium().crypto_box_macbytes().intValue())];
        int rc = sodium().crypto_secretbox_open_easy(decrypted,cipherText,cipherText.length,nonce,key);
        if (rc != 0)
        {
            throw new SodiumLibraryException("libsodium crypto_secretbox_open_easy() failed, returned " + rc + ", expected 0");
        }
        return decrypted;
    }

    public static NativeLong cryptoSecretBoxNonceBytes()
    {
        return sodium().crypto_secretbox_noncebytes();

    }

    public static int cryptoPwhashSaltBytes()
    {
        return sodium().crypto_pwhash_saltbytes();
    }

    /**
     * Derive a key using Argon2id password hashing scheme
     * <p>
     * The following is taken from <a href="https://download.libsodium.org/doc/password_hashing/">libsodium documentation</a>:
     * <blockquote>
     * Argon2 is optimized for the x86 architecture and exploits the cache and memory organization of the recent Intel
     * and AMD processors. But its implementation remains portable and fast on other architectures. Argon2 has three
     * variants: Argon2d, Argon2i and Argon2id. Argon2i uses data-independent memory access, which is preferred for
     * password hashing and password-based key derivation. Argon2i also makes multiple passes over the memory to
     * protect from tradeoff attacks. Argon2id combines both.

     * </blockquote>
     *
     * @param passwd Array of bytes of password
     * @param salt Salt to use in key generation. The salt should be unpredictable and can be generated by calling SodiumLibary.randomBytes(int)
     * The salt should be saved by the caller as it will be needed to derive the key again from the
     * password.
     * @return Generated key as an array of bytes
     * @throws SodiumLibraryException if libsodium's crypto_pwhash() does not return 0
     * <code>crypto_pwhash()</code> in <a href="https://download.libsodium.org/doc/password_hashing/">Password hashing</a>
     * libsodium page. <a href="https://github.com/P-H-C/phc-winner-argon2/raw/master/argon2-specs.pdf">Argon2i v1.3 Algorithm</a>.
     * <h3>Example</h3>
     * <pre>
     * <code>
     * String password = "This is a Secret";
     * salt = SodiumLibary.randomBytes(sodium().crypto_pwhash_saltbytes());
     * byte[] key = SodiumLibary.cryptoPwhashArgon2i(password.getBytes(),salt);
     * String keyHex = SodiumUtils.bin2hex(key);
    </code>
     *</pre>
     */
    public static byte[] cryptoPwhashArgon2i(byte[] passwd, byte[] salt) throws SodiumLibraryException
    {
        int saltLength = cryptoPwhashSaltBytes();
        if (salt.length != saltLength)
        {
            throw new SodiumLibraryException("salt is " + salt.length + ", it must be" + saltLength + " bytes");
        }

        byte[] key = new byte[sodium().crypto_box_seedbytes().intValue()];

        int rc = sodium().crypto_pwhash(key, key.length,
                passwd, passwd.length,
                salt,
                sodium().crypto_pwhash_opslimit_interactive(),
                sodium().crypto_pwhash_memlimit_interactive(),
                sodium().crypto_pwhash_alg_argon2id13());

        if (logger.isDebugEnabled()) {
            logger.debug(">>> NavtiveLong size: " + NativeLong.SIZE * 8 + " bits");
            logger.debug(">>> opslimit: " + sodium().crypto_pwhash_opslimit_interactive());
            logger.debug(">>> memlimit: " +  sodium().crypto_pwhash_memlimit_interactive());
            logger.debug(">>> alg: " +  sodium().crypto_pwhash_alg_argon2id13()); // libsodium 1.0.15
            logger.debug("crypto_pwhash returned: " + rc);
        }

        if (rc != 0)
        {
            throw new SodiumLibraryException("cryptoPwhashArgon2i libsodium crypto_pwhash failed, returned " + rc + ", expected 0");
        }
        return key;
    }


}
