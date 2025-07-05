package com.anam145.wallet.feature.main.Utils;

public class Credentials {
    private final String address;
    private final String privateKey;

    Credentials(String address, String privateKey){
        this.address = address;
        this.privateKey = privateKey;
    }
    // TODO public String getPrivateKey -> ?
    public String getAddress(){ return address; }
    public String getPrivatekey(){ return privateKey; }

}