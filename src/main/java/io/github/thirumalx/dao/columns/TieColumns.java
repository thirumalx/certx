package io.github.thirumalx.dao.columns;

/**
 * Column name constants for Tie tables.
 */
public interface TieColumns {

    interface ApplicationClientServedby {
        String TABLE = "certx.ap_serves_cl_servedby";
        String ANCHOR1 = "ap_id_serves";
        String ANCHOR2 = "cl_id_servedby";
        String METADATA = "metadata_ap_serves_cl_servedby";
        String CHANGED_AT = "ap_serves_cl_servedby_changedat";
    }

    interface CertificateClientOwns {
        String TABLE = "certx.ce_belongsto_cl_owns";
        String ANCHOR1 = "ce_id_belongsto";
        String ANCHOR2 = "cl_id_owns";
        String METADATA = "metadata_ce_belongsto_cl_owns";
    }
}
