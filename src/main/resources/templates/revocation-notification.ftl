<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }
        .container { width: 80%; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
        .header { background-color: #f8f9fa; padding: 10px; border-bottom: 2px solid #dc3545; margin-bottom: 20px; }
        .content { margin-bottom: 20px; }
        .footer { font-size: 0.8em; color: #777; border-top: 1px solid #eee; padding-top: 10px; }
        .tag { font-weight: bold; color: #555; }
        .value { color: #000; }
        .alert { color: #dc3545; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2 class="alert">Certificate Revocation Alert</h2>
        </div>
        <div class="content">
            <p>Dear CertX User,</p>
            <p>This is an urgent notification that the following certificate has been <span class="alert">REVOKED</span> according to the Certificate Revocation List (CRL):</p>
            <ul>
                <li><span class="tag">Serial Number:</span> <span class="value">${serialNumber}</span></li>
                <li><span class="tag">Revoked On:</span> <span class="value">${revokedOn}</span></li>
                <li><span class="tag">Certificate Path:</span> <span class="value">${path}</span></li>
            </ul>
            <p>Please discontinue the use of this certificate immediately and replace it if necessary.</p>
        </div>
        <div class="footer">
            <p>This is an automated notification from the CertX system. Please do not reply to this email.</p>
        </div>
    </div>
</body>
</html>
