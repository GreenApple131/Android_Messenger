package com.example.dyplommessenger.end_to_end_AES;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;

public class SignalWrapper {

    public SignalUser register(String name) throws InvalidKeyException {
        IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
        int registrationId = KeyHelper.generateRegistrationId(false);
        SignalProtocolAddress address = new SignalProtocolAddress(name, 0);

        SignedPreKeyRecord signedPreKey = generateSignedPreKey(identityKeyPair);
        List<PreKeyRecord> preKeys = generatePreKeys(10);

        SignalProtocolStore store = new InMemorySignalProtocolStore(identityKeyPair, registrationId);
        store.storeSignedPreKey(signedPreKey.getId(), signedPreKey);
        for (PreKeyRecord preKey : preKeys) {
            store.storePreKey(preKey.getId(), preKey);
        }

        SignalUser user = new SignalUser();
        user.setName(name);
        user.setRegistrationId(registrationId);
        user.setAddress(address);
        user.setIdentityKeyPair(identityKeyPair);
        user.setSignedPreKey(signedPreKey);
        user.setPreKeys(preKeys);
        user.setStore(store);
        return user;
    }

    private SignedPreKeyRecord generateSignedPreKey(IdentityKeyPair identityKeyPair) throws InvalidKeyException {
        Random rand = new Random();
        int signedPreKeyId = rand.nextInt(5000);
        return KeyHelper.generateSignedPreKey(identityKeyPair, signedPreKeyId);
    }

    private List<PreKeyRecord> generatePreKeys(int count) {
        return KeyHelper.generatePreKeys(1, count);
    }

    public void initSession(SignalUser me, SignalUser other) throws Exception {
        SessionBuilder sessionBuilder = new SessionBuilder(me.getStore(), other.getAddress());
        sessionBuilder.process(other.getPreKeyBundle());
    }

    public SessionCipher createSessionCipher(SignalUser me, SignalUser other) {
        return new SessionCipher(me.getStore(), other.getAddress());
    }

    public CiphertextMessage encrypt(SessionCipher sessionCipher, String plaintext) throws UnsupportedEncodingException, UntrustedIdentityException {
        return sessionCipher.encrypt(plaintext.getBytes("UTF-8"));
    }

    public String decrypt(SessionCipher sessionCipher, CiphertextMessage ciphertext) throws Exception {
        PreKeySignalMessage preKeySignalMessage;
        SignalMessage signalMessage;
        byte[] plaintext;

        if (ciphertext.getType() == CiphertextMessage.PREKEY_TYPE) {
            preKeySignalMessage = new PreKeySignalMessage(ciphertext.serialize());
            plaintext = sessionCipher.decrypt(preKeySignalMessage);

        } else {
            signalMessage = new SignalMessage(ciphertext.serialize());
            plaintext = sessionCipher.decrypt(signalMessage);

        }

        return new String(plaintext);
    }
}
