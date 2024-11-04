package nisagic.nisagic.NisaUserService;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import nisagic.nisagic.model.NisaUser;
import nisagic.nisagic.repository.NisaUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NisaUserServ {
    @Autowired
    private NisaUserRepo nisaUserRepo;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;


    public String checkAndGenerateCode(String email) {
        Optional<NisaUser> userOptional = nisaUserRepo.findByEmail(email);
        String conCode = userOptional.get().getConfirmationCode();

        if (userOptional.isPresent() && conCode == null ) {

            NisaUser user = userOptional.get();
            String code = generateRandomCode();
            user.setConfirmationCode(code);

                // Generate QR Code
                String qrCodeFilePath = generateQRCodeImage(code);
            // Send email with the QR code
            try {
                sendEmailWithQRCode(email, qrCodeFilePath, code);

                  nisaUserRepo.save(user);


            } catch (MessagingException e) {
                e.printStackTrace();
                return "Error sending email";
            }

            return code;
        }
        return null;
    }

//    private String generateRandomCode() {
//        return UUID.randomUUID().toString().substring(0, 6);
//    }

    private String generateRandomCode() {
        // Generate first three random alphabetic characters
        Random random = new Random();
        StringBuilder alphabetPart = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            char randomChar = (char) ('A' + random.nextInt(26)); // Generate a random letter (A-Z)
            alphabetPart.append(randomChar);
        }

        // Generate last three numeric characters in ascending order
        int[] numberPart = new int[3];
        for (int i = 0; i < 3; i++) {
            numberPart[i] = random.nextInt(10); // Generate random digits (0-9)
        }
        Arrays.sort(numberPart); // Sort in ascending order

        // Combine both parts
        StringBuilder code = new StringBuilder();
        code.append(alphabetPart);
        for (int num : numberPart) {
            code.append(num); // Append sorted numeric part
        }

        return code.toString();
    }

    private String generateQRCodeImage(String code) {
        try {
            String qrCodePath = "qr_" + code + ".png";
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(code, BarcodeFormat.QR_CODE, 350, 350);

            Path path = FileSystems.getDefault().getPath(qrCodePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            return qrCodePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // In the sendEmailWithQRCode method
    private void sendEmailWithQRCode(String email, String qrCodeFilePath, String mcode) throws MessagingException {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set email details
            helper.setFrom(username);
            if(!email.trim().isEmpty()){
                helper.setTo(email.trim());
            }
           // helper.setTo(email);
            helper.setSubject("Your NISA-GIS Conference Confirmation Code");

            // Check if QR code file exists
            File qrFile = new File(qrCodeFilePath);
            if (qrFile.exists()) {
                // HTML content with inline QR code image
                String htmlContent = "<html>" +
                        "<body>" +
                        "<h2>Your NISA-GIS Conference Confirmation </h2>" +
                        "<p>Please find your confirmation QR code attached and inline.</p>" +
                        "<p>You're required to present it at the conference days.</p>" +
                        "<p><img src='cid:qrcodeImage'></p>" + // Inline QR Code image
                        "<p>Or Validation code: <b>"+mcode+"</b></p>"+
                        "<P> Abraham Olatubosun</p>"+
                        "<P> AOlatubosun@ccfng.org</p>"+
                        "</body>" +
                        "</html>";

                // Set the HTML content as email body
                helper.setText(htmlContent, true);
                // Embed QR code image inline
                helper.addInline("qrcodeImage", qrFile);

                // Attach QR code file as an attachment
                helper.addAttachment("confirmation-code.png", qrFile);
                // Send the email
                javaMailSender.send(mimeMessage);
            } else {
                System.err.println("QR Code file not found: " + qrCodeFilePath);
            }
        } catch (MessagingException e) {
            // Log the exception with more details
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            throw e;  // Rethrow the exception to handle it properly
        }
    }

    public boolean checkCodeInDataBase(String qrCode) {
        //Query the database for the QR code e.g using JPA repository
        return nisaUserRepo.findByConfirmationCode(qrCode.trim()).isPresent();  // Query the database

    }

    //-- Persist excel file into the database

}
