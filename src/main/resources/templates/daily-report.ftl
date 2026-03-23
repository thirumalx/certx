<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; }
        .footer { font-size: 0.8em; color: #777; margin-top: 20px; }
    </style>
</head>
<body>
    <h2>Daily Certificate Status Report</h2>
    <p>Hello,</p>
    <p>Please find below the report for certificates that are expired today, revoked today, or about to expire in the next few days.</p>
    
    <#if items?? && (items?size > 0)>
    <table border="1" cellpadding="10" cellspacing="0" style="border-collapse: collapse; width: 100%; margin-top: 20px;">
        <thead style="background-color: #f2f2f2;">
            <tr>
                <th>Category</th>
                <th>Certificate ID</th>
                <th>Client ID</th>
                <th>Expiry Date</th>
                <th>Revoked On</th>
            </tr>
        </thead>
        <tbody>
            <#list items as item>
            <tr>
                <td>${item.category()}</td>
                <td>${item.certificateId()}</td>
                <td>${item.clientId()!""}</td>
                <td>${item.expiryDate()!""}</td>
                <td>${item.revokedOn()!""}</td>
            </tr>
            </#list>
        </tbody>
    </table>
    <#else>
    <p>No certificates found for this report.</p>
    </#if>

    <p style="margin-top: 20px;">Report Date: ${date}</p>
    <p>Best regards,<br/>CertX System</p>
    <div class="footer">
        This is an automated message. Please do not reply.
    </div>
</body>
</html>
