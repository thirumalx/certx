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
            <h2>Client Assignment Notification</h2>
        </div>
        <div class="content">
            <p>Dear <span class="value">${userName}</span>,</p>
            <p>You have been assigned to the following client:</p>
            <ul>
                <li><span class="tag">Client Name:</span> <span class="value">${clientName}</span></li>
                <li><span class="tag">Client Email:</span> <span class="value">${clientEmail}</span></li>
            </ul>
            <p>Please review the client details and take the necessary actions.</p>
        </div>
        <div class="footer">
            <p>This is an automated notification from the CertX system. Please do not reply to this email.</p>
        </div>
    </div>
</body>
</html>
