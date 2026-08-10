package com.erpilot.app.ragschema;

import com.pgvector.PGvector;


public final class VectorFormat {

    private VectorFormat() {
    }

    public static String toPgVectorString(float[] vector) {
        return new PGvector(vector).toString();
    }
}
