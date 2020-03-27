package com.dd.rsvp.processor.job.utility;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public class FileWriterUtil implements Serializable {

    private Bz2Utility bz2Utility = new Bz2Utility();

    public FileWriterUtil() {
    }

    public byte[] write(List<Object> rsvpRecords) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Throwable var3 = null;

        try {
            Iterator var4 = rsvpRecords.iterator();

            while (var4.hasNext()) {
                Object rsvpRecord = (Object) var4.next();
                stream.write(JsonUtility.writeObjectAsString(rsvpRecord).getBytes());
                stream.write("\n".getBytes());
            }

            byte[] var15 = this.bz2Utility.compress(stream.toByteArray());
            return var15;
        } catch (Throwable var13) {
            var3 = var13;
            throw var13;
        } finally {
            if (stream != null) {
                if (var3 != null) {
                    try {
                        stream.close();
                    } catch (Throwable var12) {
                        var3.addSuppressed(var12);
                    }
                } else {
                    stream.close();
                }
            }

        }
    }
}
