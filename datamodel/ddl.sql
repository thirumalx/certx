-- DDL 
----
-- Unique constraint for the application id

CREATE OR REPLACE FUNCTION certx.enforce_ap_uid_application_uniqueness()
RETURNS trigger AS $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM certx.lap_uid_application_uniqueid
    WHERE ap_uid_application_uniqueid = NEW.ap_uid_application_uniqueid
      AND ap_uid_ap_id <> NEW.ap_uid_ap_id
  ) THEN
    RAISE EXCEPTION
      'Application Unique ID % already exists',
      NEW.ap_uid_application_uniqueid
      USING ERRCODE = 'unique_violation';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


---
CREATE TRIGGER trg_ap_uid_application_unique
BEFORE INSERT ON certx.ap_uid_application_uniqueid
FOR EACH ROW
EXECUTE FUNCTION certx.enforce_ap_uid_application_uniqueness();

---

--Status Knot
INSERT INTO certx.sta_status(sta_id, sta_status, metadata_sta) VALUES (0, 'DELETED', 0);
INSERT INTO certx.sta_status(sta_id, sta_status, metadata_sta) VALUES (1, 'ACTIVE', 1);
-----
