package nisagic.nisagic.NisaUserController;


import nisagic.nisagic.NisaUserService.NisaUserServ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class NisaController {
    @Autowired
    private NisaUserServ nisaUserServ;

    @PostMapping("/checkEmail")
    public ResponseEntity<Map<String, String>> CheckEmail(@RequestBody Map<String, String> emailMap) {
        String email = emailMap.get("email");
        String code = nisaUserServ.checkAndGenerateCode(email);

        if (code == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid email or error occurred.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Details validated successfully. Check your email for the QR code.");
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/verifiyQR")
//    public ResponseEntity<Map<String,Object>> verifiyQRCode(@RequestParam("qrcode") String qrCode) {
//       boolean found = nisaUserServ.checkCodeInDataBase(qrCode);
//
//       Map<String, Object> response = new HashMap<>();
//
//       if (found) {
//           response.put("status", "success");
//           response.put("message", "QR code verified successfully ✅");
//       }else {
//           response.put("status", "failure");
//           response.put("message", "QR code verification failed ❌");
//           response.put("invalidCode", qrCode);
//       }
//       return ResponseEntity.ok(response);
//    }

    @PostMapping("/verfiyQR")
    public ResponseEntity<Map<String,Object>> verfiyQRCode(@RequestBody Map<String,String> request) {
        String qrCode = request.get("qrCode");
        boolean found = nisaUserServ.checkCodeInDataBase(qrCode); //Check if QR code exist in DB

        Map<String, Object> response = new HashMap<>();
        response.put("found", found);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/testConnection")
    public String testConnection() {
        return "200";
    }

}

