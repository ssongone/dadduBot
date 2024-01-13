package com.ddaddu.dadduBot.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

public class ExcelFileTest {

//    @Test
//    void 파일생성테스트() {
//        ExcelFile excelFile = new ExcelFile();
//        excelFile.registerAtOnce();
//    }

    @Test
    void 통합테스트() {
        String url = "https://www.e-hon.ne.jp/bec/ZS/ZSRank";
        Scraper scraper = new Scraper();
        List<BookInfo> bookInfos = scraper.scrapeMagazineList(url);
        System.out.println(bookInfos);
        ExcelFile excelFile = new ExcelFile();
        excelFile.registerAtOnce(bookInfos);
    }
}
