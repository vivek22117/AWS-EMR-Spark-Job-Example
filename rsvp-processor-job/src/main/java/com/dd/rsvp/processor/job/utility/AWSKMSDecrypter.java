package com.dd.rsvp.processor.job.utility;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Base64;

public class AWSKMSDecrypter implements Serializable {
    private AWSKMS awskms;

    public AWSKMSDecrypter() {
        this(AWSClientUtil.getKMSClient());
    }

    public AWSKMSDecrypter(AWSKMS awskms) {
        this.awskms = awskms;
    }

    public String decrypt(String base64EncodedValue) {
        DecryptRequest decryptRequest = (new DecryptRequest()).withCiphertextBlob(ByteBuffer.wrap(Base64.getDecoder().decode(base64EncodedValue)));
        DecryptResult decryptResult = this.awskms.decrypt(decryptRequest);
        return new String(decryptResult.getPlaintext().array());
    }
}
