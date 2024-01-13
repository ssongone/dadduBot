package com.ddaddu.dadduBot.entity;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelFile {

    static final String WINDOWS_FILE_PATH = "C:\\ddadduBot\\";
    static final String MAC_FILE_PATH = "/Users/ssongwon/Desktop/따뚜/";

    static final int PRODUCT_CODE_COLUMN = 0;

    static final int CATEGORY_CODE_COLUMN = 1;
    static final String CATEGORY_CODE_VALUE = "50005752";

    static final int PRODUCT_NAME_COLUMN = 2;
    static final int PRODUCT_PRICE_COLUMN = 4;

    static final int PRODUCT_STOCK_COLUMN = 6;
    static final int PRODUCT_STOCK_VALUE = 100;

    static final int PRODUCT_MAIN_IMAGE_COLUMN = 17;
    static final int PRODUCT_DESCRIPTION_COLUMN = 19;

    static final int ORIGIN_CODE_COLUMN = 24;
    static String ORIGIN_CODE_VALUE = "0200036";

    static final int IMPORTER_COLUMN = 25;
    static final int MINOR_COLUMN = 28;

    static final int DELIVERY_CODE_COLUMN = 29;
    static final String DELIVERY_CODE_VALUE = "2618409";

    static final int AS_NUMBER_COLUMN = 51;
    static final int AS_INFO_COLUMN = 52;
    static String AS_NUMBER_VALUE = "010-7268-5664";

    static final int ISBN_COLUMN = 75;
    static final int PUBLICATION_DATE_COLUMN = 78;
    static final int PUBLISHER_COLUMN = 79;

    static final int WRITER_COLUMN = 80;
    static final int ILLUSTRATOR_COLUMN = 81;
    static final String ILLUSTRATOR_VALUE = "상세페이지 표기";

    static final int INCOME_DEDUCTION_COLUMN = 83;

    static String Y_VALUE = "Y";
    static String N_VALUE = "N";


    public XSSFWorkbook workbook; // 엑셀 쓰기전 workbook 지정
    public XSSFSheet sheet;


    public void registerAtOnce(List<BookInfo> bookList) {
        String path = setFilePath() + "일괄등록" + calcCurrentTime() + ".xlsx";
        try(FileOutputStream fos = new FileOutputStream(path)) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("sheet1"); //맨앞시트만 쓰니까 지정해줌
            fillExcel(bookList);
//            tempMethod();
            workbook.write(fos); // 작업이 끝난후 해당 workbook객체를 FileOutputStream에 쓰기
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("오류오류");
        }
    }

    void fillExcel(List<BookInfo> bookList) {
        for (int i = 0; i < bookList.size(); i++) {
            createCell(i+2, bookList.get(i));
        }
    }

    void tempMethod() {
        BookInfo bookInfo = BookInfo.builder().build();
        createCell(3, bookInfo);
    }

    private void createCell(int order, BookInfo bookInfo) {
        XSSFRow curRow = sheet.createRow(order);

        curRow.createCell(PRODUCT_CODE_COLUMN).setCellValue("code");
        curRow.createCell(CATEGORY_CODE_COLUMN).setCellValue(CATEGORY_CODE_VALUE);
        curRow.createCell(PRODUCT_NAME_COLUMN).setCellValue(bookInfo.getTitle());
        curRow.createCell(PRODUCT_PRICE_COLUMN).setCellValue(bookInfo.getPrice());
        curRow.createCell(PRODUCT_MAIN_IMAGE_COLUMN).setCellValue(bookInfo.getMainImageUrl());
        curRow.createCell(PRODUCT_DESCRIPTION_COLUMN).setCellValue(bookInfo.getDescription());
        curRow.createCell(PRODUCT_STOCK_COLUMN).setCellValue(PRODUCT_STOCK_VALUE);

        curRow.createCell(ORIGIN_CODE_COLUMN).setCellValue(ORIGIN_CODE_VALUE);
        curRow.createCell(IMPORTER_COLUMN).setCellValue(N_VALUE);
        curRow.createCell(MINOR_COLUMN).setCellValue(Y_VALUE);
        curRow.createCell(DELIVERY_CODE_COLUMN).setCellValue(DELIVERY_CODE_VALUE);

        curRow.createCell(AS_NUMBER_COLUMN).setCellValue(AS_NUMBER_VALUE);
        curRow.createCell(AS_INFO_COLUMN).setCellValue(AS_NUMBER_VALUE);

        curRow.createCell(ISBN_COLUMN).setCellValue("978-4-86593-532-5");
        curRow.createCell(77).setCellValue(N_VALUE);
        curRow.createCell(PUBLICATION_DATE_COLUMN).setCellValue(bookInfo.getDate());
        curRow.createCell(PUBLISHER_COLUMN).setCellValue(bookInfo.getPublisher());
        curRow.createCell(WRITER_COLUMN).setCellValue(ILLUSTRATOR_VALUE);
        curRow.createCell(ILLUSTRATOR_COLUMN).setCellValue(ILLUSTRATOR_VALUE);
        curRow.createCell(INCOME_DEDUCTION_COLUMN).setCellValue(Y_VALUE);

    }

    private String setFilePath() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win"))
            return WINDOWS_FILE_PATH;
         else
            return MAC_FILE_PATH;
    }

    private String calcCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        return now.format(formatter);
    }

}
