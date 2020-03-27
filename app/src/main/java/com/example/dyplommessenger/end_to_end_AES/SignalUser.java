package com.example.dyplommessenger.end_to_end_AES;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.util.List;

public class SignalUser {

    private String name;
    private int registrationId;
    private SignalProtocolAddress address;
    private IdentityKeyPair identityKeyPair;
    private SignedPreKeyRecord signedPreKey;
    private List<PreKeyRecord> preKeys;
    private SignalProtocolStore store;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public SignalProtocolAddress getAddress() {
        return address;
    }

    public void setAddress(SignalProtocolAddress address) {
        this.address = address;
    }

    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyPair;
    }

    public void setIdentityKeyPair(IdentityKeyPair identityKeyPair) {
        this.identityKeyPair = identityKeyPair;
    }

    public SignedPreKeyRecord getSignedPreKey() {
        return signedPreKey;
    }

    public void setSignedPreKey(SignedPreKeyRecord signedPreKey) {
        this.signedPreKey = signedPreKey;
    }

    public List<PreKeyRecord> getPreKeys() {
        return preKeys;
    }

    public void setPreKeys(List<PreKeyRecord> preKeys) {
        this.preKeys = preKeys;
    }

    public SignalProtocolStore getStore() {
        return store;
    }

    public void setStore(SignalProtocolStore store) {
        this.store = store;
    }

    public PreKeyBundle getPreKeyBundle() throws Exception {
        PreKeyRecord firstPreKey = preKeys.get(0);
        if (firstPreKey == null) {
            throw new Exception("Kein PreKey mehr vorhanden");
        }

        preKeys.remove(0);
        return new PreKeyBundle(
                store.getLocalRegistrationId(),
                0,
                firstPreKey.getId(),
                firstPreKey.getKeyPair().getPublicKey(),
                signedPreKey.getId(),
                signedPreKey.getKeyPair().getPublicKey(),
                signedPreKey.getSignature(),
                store.getIdentityKeyPair().getPublicKey());
    }
}
