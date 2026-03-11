<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }
        .container { width: 80%; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
        .header { background-color: #f8f9fa; padding: 10px; border-bottom: 2px solid #007bff; margin-bottom: 20px; }
        .content { margin-bottom: 20px; }
        .footer { font-size: 0.8em; color: #777; border-top: 1px solid #eee; padding-top: 10px; }
        .tag { font-weight: bold; color: #555; }
        .value { color: #000; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>Certificate Expiry Notification</h2>
        </div>
        <div class="content">
            <p>Dear <span class="value">${recipientName}</span>,</p>
            <p>This is a reminder that a certificate for the following client is approaching its expiry date:</p>
            <ul>
                <li><span class="tag">Client Name:</span> <span class="value">${clientName}</span></li>
                <li><span class="tag">Serial Number:</span> <span class="value">${serialNumber}</span></li>
                <li><span class="tag">Expiry Date:</span> <span class="value">${expiryDate}</span></li>
                <li><span class="tag">Certificate File Name:</span> <span class="value">${path}</span></li>
            </ul>
            <p>Please take the necessary actions to renew it before it expires.</p>
        </div>
        <div class="footer">
            <p>This is an automated notification from the CertX system. Please do not reply to this email.</p>
        </div>
    </div>
</body>
</html>
