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
import org.springframework.web.bind.annotation.ResponseBody;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NisaUserServ {

    private static final int TOTAL_TABLES = 47;
    private static final int SEATS_PER_TABLE = 8;

    // Classification codes
    private static final int RESEARCHER = 1;
    private static final int POLICY_MAKER = 2;
    private static final int IMPLEMENTING_PARTNER = 3;


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




    public  Boolean saveNisa(NisaUser nisaUser) {
        //String email = nisaUser.getEmail();
    Optional<NisaUser> existingEmail = nisaUserRepo.findByEmail(nisaUser.getFullName());
        if (existingEmail.isPresent()) {
             return true;  //Email already exist
        }else {
            nisaUserRepo.save(nisaUser);
            return false; // Registration Successful
        }

    }

    public String checkAndGenerateCode(String email) {
        Optional<NisaUser> userOptional = nisaUserRepo.findByEmail(email);
        String conCode = userOptional.get().getClassification();

        if (userOptional.isPresent() && conCode == null ) {

            NisaUser user = userOptional.get();
            String code = generateRandomCode(); // method to generate random numbers
            user.setClassification((code));

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

//    public String generateQRCodeAndSendBulkEmail(){
//        List<NisaUser> userList = nisaUserRepo.findAll();
//        int code = 10;
//        boolean incrementToNextMultipleOfTen = false; // Flag to control incrementing by multiples of 10
//
//
//        for(NisaUser user : userList){
//                if(!user.getEmail().isEmpty() && user.getConfirmationCode() == null){
//                String lastName = user.getLastName();
//                String classification = user.getClassification();
//
//                // Extract the first three letters of the last name
//                String lastNamePart = lastName.length() >= 3 ? lastName.substring(0, 3) : lastName;
//
//                // Construct the confirmation code
//                String mergedCode = lastNamePart + code + "-" + classification;
//                user.setConfirmationCode(mergedCode);
//                nisaUserRepo.save(user);
//
//                    // Increment logic for code
//                    code++;
//
//                    // Skip numbers ending in 9 (19, 29, 39, etc.)
//                    if (code % 10 == 9) {
//                        code += 1; // Jump directly to the next multiple of 10
//                    }
//
//                //-- Generate QR code
//                String qrCodeFilePath  = generateQRCodeImage(mergedCode); // method that generate QR-code
////                    try {
////                        //Use the QR-code to send mails to the participant
////                        sendEmailWithQRCode(user.getEmail().trim(), qrCodeFilePath, mergedCode);
////                    } catch (MessagingException e) {
////                        throw new RuntimeException(e);
////                    }
//
//                }
//        }
//        return null;
//    }

//-------- generateRandomCode is now deprecated and replaced with generateQRCodeAndSendEmail
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
            helper.setSubject("Your NISA-GIS Conference Confirmation Code Demo");

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

    public String generateAndSaveTableCode() {
        List<NisaUser> users = nisaUserRepo.findAll();
        List<NisaUser> allTableCodes = new ArrayList<>();

        int tableNumber = 1;

        // Group users by classification
        List<NisaUser> researchers = users.stream()
                .filter(u -> "1".equals(u.getClassification()))
                .collect(Collectors.toList());

        List<NisaUser> policyMakers = users.stream()
                .filter(u -> "2".equals(u.getClassification()))
                .collect(Collectors.toList());

        List<NisaUser> implementingPartners = users.stream()
                .filter(u -> "3".equals(u.getClassification()))
                .collect(Collectors.toList());

        // Shuffle to create random seating
        Collections.shuffle(researchers);
        Collections.shuffle(policyMakers);
        Collections.shuffle(implementingPartners);

        while (tableNumber <= 47) {
            List<NisaUser> table = new ArrayList<>();

            // Format table number with leading zero for single digits (e.g., "01", "02")
            String formattedTableNumber = String.format("%02d", tableNumber);

            // Add up to 2 Researchers to the table
            List<NisaUser> assignedResearchers = researchers.stream().limit(2).collect(Collectors.toList());
            table.addAll(assignedResearchers);
            researchers = researchers.subList(Math.min(2, researchers.size()), researchers.size());

            // Add up to 1 Policy Maker to the table
            List<NisaUser> assignedPolicyMakers = policyMakers.stream().limit(1).collect(Collectors.toList());
            table.addAll(assignedPolicyMakers);
            policyMakers = policyMakers.subList(Math.min(1, policyMakers.size()), policyMakers.size());

            // Add up to 5 Implementing Partners to the table
            List<NisaUser> assignedImplementingPartners = implementingPartners.stream().limit(5).collect(Collectors.toList());
            table.addAll(assignedImplementingPartners);
            implementingPartners = implementingPartners.subList(Math.min(5, implementingPartners.size()), implementingPartners.size());

            // Set code and table assignment for each user
            String codeTable = "Table" + formattedTableNumber;
            for (NisaUser user : table) {
                String classificationText = getClassificationText(user.getClassification());
                String generatedCode = user.getFullName() + "-" + codeTable + "-" + classificationText;
                user.setCodeTable(generatedCode);
                nisaUserRepo.save(user);  // Persist each user with the generated code
                allTableCodes.add(user);
            }

            tableNumber++;
        }

        exportCodesToExcel(allTableCodes); // Export to Excel
        return "Table codes generated and saved successfully!";
    }

    // Helper method to convert classification to descriptive text
    private String getClassificationText(String classification) {
        switch (classification) {
            case "1": return "Researcher";
            case "2": return "Policy Maker";
            case "3": return "Implementing Partner";
            default: return "Unknown";
        }
    }

    private void exportCodesToExcel(List<NisaUser> tableCodes) {
        // Excel export logic (implementation needed here)
    }


    private String generateCode(String Fullname,int code, int classification){
        return Fullname+code+"-"+classification;
    }

//    private void exportCodesToExcel(List<TableCode> tableCodes) {
//        // Excel export logic (implementation needed here)
//    }





}
