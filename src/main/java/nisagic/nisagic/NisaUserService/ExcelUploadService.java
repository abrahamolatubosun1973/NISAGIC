package nisagic.nisagic.NisaUserService;

import nisagic.nisagic.model.NisaUser;
import nisagic.nisagic.repository.NisaUserRepo;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ExcelUploadService {

    @Autowired
    private NisaUserRepo nisaUserRepo;

    public Map<String, Integer> processExcel(MultipartFile file) {
        Map<String, Integer> result = new HashMap<>();
        List<NisaUser> users = new ArrayList<>();

        if(file.isEmpty()) {
            result.put("error", 0); // Indicate an error state
            return result; // You could throw an exception here instead for better handling
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                NisaUser user = new NisaUser();
//               user.setFullName(row.getCell(0).getStringCellValue());
//               user.setAddress(row.getCell(1).getStringCellValue());
//               user.setClassification(row.getCell(2).getStringCellValue());
                user.setFullName(row.getCell(0).getStringCellValue());
                user.setAddress(row.getCell(1).getStringCellValue());

// Convert numeric value to String for the classification field
                if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                    // If the cell is numeric, convert the number to a String
                    user.setClassification(String.valueOf((int) row.getCell(2).getNumericCellValue()));
                } else {
                    // If the cell is already a String, use it directly
                    user.setClassification(row.getCell(2).getStringCellValue());
                }
                users.add(user);
            }

            // Save all users to the database
            nisaUserRepo.saveAll(users);

            // Returning success message with number of records processed
            result.put("success", users.size());
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            result.put("error", 0);
            return result;
        }
    }
}

