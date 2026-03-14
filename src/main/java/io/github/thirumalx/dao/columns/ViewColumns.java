package io.github.thirumalx.dao.columns;

/**
 * Column name constants for View tables.
 */
public interface ViewColumns {

    interface ApplicationLatest {
        String TABLE = "certx.lAP_Application";
        String ID = "AP_ID";
        String NAME = "AP_NAM_Application_Name";
        String STATUS = "AP_STA_STA_Status";
        String UNIQUE_ID = "AP_UID_Application_UniqueId";
    }

    interface ApplicationNow {
        String TABLE = "certx.nAP_Application";
        String ID = "AP_ID";
        String NAME = "AP_NAM_Application_Name";
        String STATUS = "AP_STA_STA_Status";
        String STATUS_ID_COL = "ap_sta_sta_id";
        String UNIQUE_ID = "AP_UID_Application_UniqueId";
    }

    interface ClientNow {
        String TABLE = "certx.nCL_Client";
        String ID = "CL_ID";
        String NAME = "CL_NAM_Client_Name";
        String EMAIL = "CL_EID_Client_Email";
        String MOBILE_NUMBER = "CL_MNO_Client_MobileNumber";
        String UNIQUE_ID = "CL_UID_Client_UniqueId";
        String STATUS_ID_COL = "cl_sta_sta_id";
        String STATUS = "CL_STA_STA_Status";
    }

    interface CertificateLatest {
        String TABLE = "certx.lCE_Certificate";
        String ID = "CE_ID";
        String SERIAL_NUMBER = "CE_SNO_Certificate_SerialNumber";
        String ISSUED_ON = "CE_ION_Certificate_IssuedOn";
        String REVOKED_ON = "CE_RON_Certificate_RevokedOn";
        String CERTIFICATE_PATH = "CE_CEP_Certificate_CertificatePath";
        String NOT_AFTER = "CE_NAF_Certificate_NotAfter";
        String LAST_TIME_VERIFIED_ON = "CE_LTV_Certificate_LastTimeVerifiedOn";
        String STATUS_ID_COL = "ce_sta_sta_id";
        String STATUS = "CE_STA_STA_Status";
        String PASSWORD = "CE_PAS_Certificate_Password";
    }

    interface CertificateNow {
        String TABLE = "certx.nCE_Certificate";
        String ID = "CE_ID";
        String SERIAL_NUMBER = "CE_SNO_Certificate_SerialNumber";
        String ISSUED_ON = "CE_ION_Certificate_IssuedOn";
        String REVOKED_ON = "CE_RON_Certificate_RevokedOn";
        String CERTIFICATE_PATH = "CE_CEP_Certificate_CertificatePath";
        String NOT_AFTER = "CE_NAF_Certificate_NotAfter";
        String LAST_TIME_VERIFIED_ON = "CE_LTV_Certificate_LastTimeVerifiedOn";
        String STATUS_ID_COL = "ce_sta_sta_id";
        String STATUS = "CE_STA_STA_Status";
        String PASSWORD = "CE_PAS_Certificate_Password";
    }

    interface UserNow {
        String TABLE = "certx.nUS_User";
        String ID = "US_ID";
        String USER_ID = "US_UID_User_UserId";
        String NAME = "US_NAM_User_Name";
        String EMAIL = "US_EID_User_EmailId";
        String MOBILE_NUMBER = "US_MNO_User_MobileNumber";
    }
}
