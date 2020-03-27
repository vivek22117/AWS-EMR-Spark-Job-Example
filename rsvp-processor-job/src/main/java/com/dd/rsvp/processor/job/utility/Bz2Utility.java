package com.dd.rsvp.processor.job.utility;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class Bz2Utility implements Serializable {

    public byte[] compress(byte[] uncompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BZip2CompressorOutputStream compressorStream = new BZip2CompressorOutputStream(outputStream);

        compressorStream.write(uncompressedData);
        compressorStream.close();

        return outputStream.toByteArray();
    }

    public byte[] decompress(byte[] compressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BZip2CompressorInputStream decompressorStream = new BZip2CompressorInputStream(new ByteArrayInputStream(compressedData));

        IOUtils.copy(decompressorStream, outputStream);
        decompressorStream.close();

        return outputStream.toByteArray();
    }
}
