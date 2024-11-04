package nisagic.nisagic.NisaUserController;

import lombok.extern.slf4j.Slf4j;
import nisagic.nisagic.NisaUserService.ExcelUploadService;
import nisagic.nisagic.model.NisaUser;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ExcelUploadController {

    @Autowired
    private ExcelUploadService excelUploadService;

    @PostMapping("/uploadExcel")
    public ResponseEntity<Map<String,Integer>> uploadExcelFile(@RequestParam("file") MultipartFile file) {
    try {
    Map<String,Integer> response = excelUploadService.processExcel(file);
    return ResponseEntity.status(HttpStatus.OK).body(response);

    } catch (Exception e) {
        log.error("An error occured while uploading excel file: {}", e.getMessage(),e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }






}






}
