import smtplib
from email.message import EmailMessage

msg = EmailMessage()
msg.set_content("This is a test email from the AntiGravity agent to verify Brevo SMTP credentials.")
msg['Subject'] = "Brevo SMTP Test"
msg['From'] = "a72e2f001@smtp-brevo.com"
msg['To'] = "chethankumarcr2004@gmail.com"

try:
    server = smtplib.SMTP('smtp-relay.brevo.com', 587)
    server.starttls()
    server.login('a72e2f001@smtp-brevo.com', 'your_brevo_smtp_password_here')
    server.send_message(msg)
    server.quit()
    print("SUCCESS: Email sent via Brevo.")
except Exception as e:
    print(f"FAILED: {e}")
