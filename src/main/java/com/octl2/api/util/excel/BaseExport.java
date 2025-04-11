package com.octl2.api.util.excel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BaseExport<T> {


    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<T> listData;

    public BaseExport(List<T> listData) {
        this.listData = listData;
        workbook = new XSSFWorkbook();
        workbook.getProperties().getCoreProperties().setCreator("OCTL2 System");
        workbook.getProperties().getCoreProperties().setTitle("Logistics Export");

    }


    public BaseExport<T> writeHeaderLine(int levelMapping) {
        sheet = workbook.createSheet("Logistics");

        Row row = sheet.createRow(0);
        String[] headers = buildHeaders(levelMapping);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(12);
        font.setFontName("Times New Roman");
        style.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            createCell(row, i, headers[i], style);
        }
        return this;

    }


    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue(String.valueOf(value));
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    public BaseExport<T> writeDataLines(String[] fields, Class<T> clazz) {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(12);
        font.setFontName("Times New Roman");
        style.setFont(font);

        for (Object data : listData) {
            if (data == null) continue;
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            for (String fieldName : fields) {
                try {
                    Object value = getNestedFieldValue(data, fieldName);
                    createCell(row, columnCount, value, style);
                } catch (Exception e) {
                    log.error(" Error Export Excel {}", e.getMessage(), e.getCause());
                    e.printStackTrace();
                }
                columnCount++;
            }
        }
        return this;
    }

    // CHức năng tùy chính kích thước cho từng cột
    public BaseExport<T> autoSizeAllColumns() {
        if (sheet.getRow(0) != null) {
            int columnCount = sheet.getRow(0).getPhysicalNumberOfCells();
            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
            }
        }
        return this;
    }


    public void export(HttpServletResponse response) throws IOException {
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            if (workbook == null) {
                throw new IllegalStateException("Workbook is null or already closed.");
            }
            workbook.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            log.error("Export error", e);
            throw e;
        } finally {
            // Close workbook một cách an toàn
            try {
                workbook.close();
            } catch (IOException ex) {
                log.warn("Failed to close workbook", ex);
            }
        }

    }

    public void exportBase64(HttpServletResponse response) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        byteArray.clone();
        String base64EncodeExportString = Base64.getEncoder().encodeToString(byteArray);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(base64EncodeExportString.getBytes());
        outputStream.close();

    }

    // Chức năng xây dựng Heder Excel theo level mapping
    private String[] buildHeaders(int levelMapping) {
        List<String> headers = new ArrayList<>();
        headers.add("Province ID");
        headers.add("Province Name");
        headers.add("Province Code");

        if (levelMapping >= 2) {
            headers.add("District Id");
            headers.add("District Name");
            headers.add("District Code");
        }

        if (levelMapping == 3) {
            headers.add("SubDistrict Id");
            headers.add("SubDistrict Name");
            headers.add("SubDistrict Code");
        }

        headers.add("Fulfilment ID");
        headers.add("Fulfilment Name");
        headers.add("Lastmile ID");
        headers.add("Lastmile Name");
        headers.add("Warehouse ID");
        headers.add("Warehouse Name");
        headers.add("Warehouse Contact");
        headers.add("Warehouse Phone");
        headers.add("Warehouse Address");

        return headers.toArray(new String[0]);
    }

    // Chức năng field Data theo level Mapping
    public String[] buildExportFields(int levelMapping) {
        List<String> fields = new ArrayList<>();


        fields.add("province.id");
        fields.add("province.name");
        fields.add("province.code");


        if (levelMapping >= 2) {
            fields.add("district.id");
            fields.add("district.name");
            fields.add("district.code");
        }

        if (levelMapping == 3) {
            fields.add("subDistrict.id");
            fields.add("subDistrict.name");
            fields.add("subDistrict.code");
        }

        fields.add("fulfilment.id");
        fields.add("fulfilment.name");
        fields.add("lastmile.id");
        fields.add("lastmile.name");
        fields.add("warehouse.id");
        fields.add("warehouse.name");
        fields.add("warehouse.contact");
        fields.add("warehouse.phone");
        fields.add("warehouse.address");


        return fields.toArray(new String[0]);

    }

    // Chức năng lấy giá trị các file con trong Class cha
    private Object getNestedFieldValue(Object object, String fieldPath) throws Exception {
        String[] fieldNames = fieldPath.split("\\.");
        Object currentObject = object;

        for (String fieldName : fieldNames) {
            if (currentObject == null) return null;

            Field field = getFieldRecursive(currentObject.getClass(), fieldName);
            field.setAccessible(true);
            currentObject = field.get(currentObject);

        }
        return currentObject;
    }

    // Chức năng lấy Field của các lớp con kế thừa lớp cha
    private Field getFieldRecursive(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }

        }
        throw new NoSuchFieldException();

    }


}
