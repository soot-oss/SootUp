package de.upb.swt.insecurebank;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;

public class LoginActivity extends AppCompatActivity {

    private Signature sig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(getPrivateKey());
            sig.sign();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
    private PrivateKey getPrivateKey() {
        KeyPair gp = null;
        try {
            KeyPairGenerator kpgen = KeyPairGenerator.getInstance("RSA");
            kpgen.initialize(2048);
            gp = kpgen.generateKeyPair();

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return gp.getPrivate();
    }

}
