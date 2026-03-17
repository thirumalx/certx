![DataModel](datamodel/Model.svg)

# 📘 Naming Conventions for Models and DAOs (Anchor Modeling)

This project follows **Anchor Modeling**, a highly normalized modeling approach where the schema is divided into:

- **Anchors** → Core business entities  
- **Attributes** → Values describing anchors  
- **Ties** → Relationships between anchors  

To maintain a clean, predictable structure in Spring Boot, the following naming conventions are used.

---

## 🧱 1. Model Naming Conventions

### ✅ Anchor Models

Anchor tables usually follow prefixes like:
*Represent a base entity `(User, Client, Application, Certificate, etc.)`
*Map directly to anchor tables such as:

```SQL  
us_user
ap_application
cl_client
ce_certificate
```


### ✅ Attribute Models

* Represent attribute values for an anchor.

Attribute tables look like:

```SQL
ap_nam_application_name
cl_eid_client_email
ce_sno_certificate_serialnumber
```

| Table Name                      | Model Name              |
| ------------------------------- | ----------------------- |
| ap_nam_application_name         | ApplicationName         |
| cl_eid_client_email             | ClientEmail             |
| cl_mno_client_mobilenumber      | ClientMobileNumber      |
| ce_sno_certificate_serialnumber | CertificateSerialNumber |
| us_pwd_user_password            | UserPassword            |

AnchorName + AttributeName

### ✅ Tie Models

Represents Relationships between anchors  

```
us_has_ro_belongsto
us_accessto_ap_has
ap_serves_cl_servedby
ap_uses_ce_isusedby
```

**Examples**

| Table Name                  | Model Name                 |
|-----------------------------|---------------------------|
| us_has_ro_belongsto         | UserRoleTie               |
| us_accessto_ap_has          | UserApplicationTie        |
| ap_serves_cl_servedby       | ApplicationClientTie      |
| ap_uses_ce_isusedby         | ApplicationCertificateTie |

## Package Structure

src/main/java
├── model/
│   ├── anchor/
│   ├── attribute/
│   └── tie/
│
└── dao/
    ├── generic/
    ├── anchor/
    ├── attribute/
    └── tie/


## Command to check the certificate validity,..

```shell
certutil -dump certificate.pfx
```

## How to build

To build run the following command

```shell
mvn clean install -DskipTests=True
```