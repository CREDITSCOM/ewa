package com.credits.wallet.desktop.utils.crypto;

import com.credits.client.node.crypto.Ed25519;
import com.credits.general.util.GeneralConverter;
import com.credits.wallet.desktop.utils.crypto.sodium.SodiumLibrary;
import com.credits.wallet.desktop.utils.crypto.sodium.SodiumLibraryException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;

import static org.junit.Assert.assertEquals;

public class SodiumLibraryTest
{
	private final static Logger LOGGER = LoggerFactory.getLogger(SodiumLibraryTest.class);

	@Rule
    public ExpectedException thrown = ExpectedException.none();

	@Before
	public void initSodium() {
		SodiumLibrary.initSodium();
	}

	@Test
    @Ignore("libsodium dependency may be miss")
	public void testEncryptPrivateKey() throws SodiumLibraryException
	{
		String passPhrase = "This is a passphrase";
		byte[] salt = SodiumLibrary.randomBytes(SodiumLibrary.cryptoPwhashSaltBytes());
		String saltBase58 = GeneralConverter.encodeToBASE58(salt);

		// create salt for derive key from pass phrase
		LOGGER.info("Generated " + salt.length + " bytes of salt");
		LOGGER.info(saltBase58);
		LOGGER.info("Derive key from passphrase");
		byte[] key = SodiumLibrary.cryptoPwhashArgon2i(passPhrase.getBytes(), salt);
		LOGGER.info("Dervived " + key.length + " bytes long key");
		String keyBase58 = GeneralConverter.encodeToBASE58(key);
		LOGGER.info(keyBase58);

		// generate key pair
		LOGGER.info("Generate key pair");
		KeyPair kp  = Ed25519.generateKeyPair();
		byte[] publicKey  = Ed25519.publicKeyToBytes(kp.getPublic());
		byte[] privateKey = Ed25519.privateKeyToBytes(kp.getPrivate());
		String publicKeyBase58 = GeneralConverter.encodeToBASE58(publicKey);
		String privateKeyBase58 = GeneralConverter.encodeToBASE58(privateKey);
		LOGGER.info("Generated Public key " + publicKey.length + " bytes");
		LOGGER.info(publicKeyBase58);
		LOGGER.info("Generated Private key " + privateKey.length + " bytes");
		LOGGER.info(privateKeyBase58);

		// create nonce for encrypting private key
		byte[] nonce = SodiumLibrary.randomBytes(SodiumLibrary.cryptoSecretBoxNonceBytes().intValue());
		saltBase58 = GeneralConverter.encodeToBASE58(salt);
		LOGGER.info("Generated " + nonce.length + " bytes of nonce");
		LOGGER.info(saltBase58);

		// encrypt the private key with nonce and key
		byte[] encryptedPrivateKey = SodiumLibrary.cryptoSecretBoxEasy(privateKey, nonce, key);
		// decrypt the private key again
		byte[] decryptedPrivateKey = SodiumLibrary.cryptoSecretBoxOpenEasy(encryptedPrivateKey, nonce, key);
		String decryptedPrivateKeyBase58 = GeneralConverter.encodeToBASE58(decryptedPrivateKey);
		LOGGER.info("Decrypted private key: " + decryptedPrivateKeyBase58);
		// use a wrong key, we expect decryption to fail
		String wrongPassPhrase = "This is a wrong passphrase";
		key = SodiumLibrary.cryptoPwhashArgon2i(wrongPassPhrase.getBytes(), salt);
		thrown.expect(SodiumLibraryException.class);
		SodiumLibrary.cryptoSecretBoxOpenEasy(encryptedPrivateKey, nonce, key);
		assertEquals(privateKeyBase58, decryptedPrivateKeyBase58);
	}
}
