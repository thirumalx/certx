package io.github.thirumalx.dao.columns;

/**
 * Column name constants for Attribute tables.
 */
public interface AttributeColumns {

    interface ApplicationName {
        String TABLE = "certx.ap_nam_application_name";
        String FK = "ap_nam_ap_id";
        String VALUE = "ap_nam_application_name";
        String CHANGED_AT = "ap_nam_changedat";
        String METADATA = "metadata_ap_nam";
    }

    interface ApplicationStatus {
        String TABLE = "certx.ap_sta_application_status";
        String FK = "ap_sta_ap_id";
        String VALUE = "ap_sta_sta_id";
        String CHANGED_AT = "ap_sta_changedat";
        String METADATA = "metadata_ap_sta";
    }

    interface ApplicationUniqueId {
        String TABLE = "certx.ap_uid_application_uniqueid";
        String FK = "ap_uid_ap_id";
        String VALUE = "ap_uid_application_uniqueid";
        String CHANGED_AT = "ap_uid_changedat";
        String METADATA = "metadata_ap_uid";
    }

    interface ClientName {
        String TABLE = "certx.cl_nam_client_name";
        String FK = "cl_nam_cl_id";
        String VALUE = "cl_nam_client_name";
        String METADATA = "metadata_cl_nam";
    }

    interface ClientUniqueId {
        String TABLE = "certx.cl_uid_client_uniqueid";
        String FK = "cl_uid_cl_id";
        String VALUE = "cl_uid_client_uniqueid";
        String METADATA = "metadata_cl_uid";
    }

    interface ClientEmail {
        String TABLE = "certx.cl_eid_client_email";
        String FK = "cl_eid_cl_id";
        String VALUE = "cl_eid_client_email";
        String CHANGED_AT = "cl_eid_changedat";
        String METADATA = "metadata_cl_eid";
    }

    interface ClientMobileNumber {
        String TABLE = "certx.cl_mno_client_mobilenumber";
        String FK = "cl_mno_cl_id";
        String VALUE = "cl_mno_client_mobilenumber";
        String CHANGED_AT = "cl_mno_changedat";
        String METADATA = "metadata_cl_mno";
    }

    interface ClientStatus {
        String TABLE = "certx.cl_sta_client_status";
        String FK = "cl_sta_cl_id";
        String VALUE = "cl_sta_sta_id";
        String CHANGED_AT = "cl_sta_changedat";
        String METADATA = "metadata_cl_sta";
    }

    interface CertificateIssuedOn {
        String TABLE = "certx.ce_ion_certificate_issuedon";
        String FK = "ce_ion_ce_id";
        String VALUE = "ce_ion_certificate_issuedon";
        String METADATA = "metadata_ce_ion";
    }

    interface CertificateRevokedOn {
        String TABLE = "certx.ce_ron_certificate_revokedon";
        String FK = "ce_ron_ce_id";
        String VALUE = "ce_ron_certificate_revokedon";
        String METADATA = "metadata_ce_ron";
    }

    interface CertificateSerialNumber {
        String TABLE = "certx.ce_sno_certificate_serialnumber";
        String FK = "ce_sno_ce_id";
        String VALUE = "ce_sno_certificate_serialnumber";
        String METADATA = "metadata_ce_sno";
    }

    interface CertificateLastTimeVerifiedOn {
        String TABLE = "certx.ce_ltv_certificate_lasttimeverifiedon";
        String FK = "ce_ltv_ce_id";
        String VALUE = "ce_ltv_certificate_lasttimeverifiedon";
        String METADATA = "metadata_ce_ltv";
    }

    interface CertificatePath {
        String TABLE = "certx.ce_cep_certificate_certificatepath";
        String FK = "ce_cep_ce_id";
        String VALUE = "ce_cep_certificate_certificatepath";
        String METADATA = "metadata_ce_cep";
    }

    interface CertificateNotAfter {
        String TABLE = "certx.ce_naf_certificate_notafter";
        String FK = "ce_naf_ce_id";
        String VALUE = "ce_naf_certificate_notafter";
        String METADATA = "metadata_ce_naf";
    }

    interface CertificateStatus {
        String TABLE = "certx.ce_sta_certificate_status";
        String FK = "ce_sta_ce_id";
        String VALUE = "ce_sta_sta_id";
        String CHANGED_AT = "ce_sta_changedat";
        String METADATA = "metadata_ce_sta";
    }

    interface CertificatePassword {
        String TABLE = "certx.ce_pas_certificate_password";
        String FK = "ce_pas_ce_id";
        String VALUE = "ce_pas_certificate_password";
        String METADATA = "metadata_ce_pas";
    }

    interface NotificationSentAt {
        String TABLE = "certx.nt_snt_notification_sentat";
        String FK = "nt_snt_nt_id";
        String VALUE = "nt_snt_notification_sentat";
        String METADATA = "metadata_nt_snt";
    }

    interface NotificationRemainderCount {
        String TABLE = "certx.nt_rec_notification_remaindercount";
        String FK = "nt_rec_nt_id";
        String VALUE = "nt_rec_notification_remaindercount";
        String METADATA = "metadata_nt_rec";
    }

    interface UserId {
        String TABLE = "certx.us_uid_user_userid";
        String FK = "us_uid_us_id";
        String VALUE = "us_uid_user_userid";
        String CHANGED_AT = "us_uid_changedat";
        String METADATA = "metadata_us_uid";
    }

    interface UserName {
        String TABLE = "certx.us_nam_user_name";
        String FK = "us_nam_us_id";
        String VALUE = "us_nam_user_name";
        String CHANGED_AT = "us_nam_changedat";
        String METADATA = "metadata_us_nam";
    }

    interface UserMobileNumber {
        String TABLE = "certx.us_mno_user_mobilenumber";
        String FK = "us_mno_us_id";
        String VALUE = "us_mno_user_mobilenumber";
        String CHANGED_AT = "us_mno_changedat";
        String METADATA = "metadata_us_mno";
    }

    interface UserEmail {
        String TABLE = "certx.us_eid_user_emailid";
        String FK = "us_eid_us_id";
        String VALUE = "us_eid_user_emailid";
        String CHANGED_AT = "us_eid_changedat";
        String METADATA = "metadata_us_eid";
    }
}
