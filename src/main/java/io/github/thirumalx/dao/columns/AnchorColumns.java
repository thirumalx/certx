package io.github.thirumalx.dao.columns;

/**
 * Column name constants for Anchor tables.
 */
public interface AnchorColumns {

    interface Application {
        String TABLE = "certx.ap_application";
        String ID = "ap_id";
        String METADATA = "metadata_ap";
    }

    interface Client {
        String TABLE = "certx.cl_client";
        String ID = "cl_id";
        String METADATA = "metadata_cl";
    }

    interface Certificate {
        String TABLE = "certx.ce_certificate";
        String ID = "ce_id";
        String METADATA = "metadata_ce";
    }
}
