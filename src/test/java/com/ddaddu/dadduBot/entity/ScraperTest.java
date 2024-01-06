package com.ddaddu.dadduBot.entity;


import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class ScraperTest {

    @Test
    void 상세페이지정보가져오기() {
        Scraper scraper = new Scraper();
        BookInfo bookInfo = scraper.scrapDetailPage("https://www.e-hon.ne.jp/bec/SA/Detail?refShinCode=0100000000000034541798&Rec_id=3002&Action_id=121&Sza_id=A0");

        assertThat(bookInfo.getTitle()).isEqualTo("大ピンチずかん　２");
        assertThat(bookInfo.getPrice()).isEqualTo(1650);
        assertThat(bookInfo.getMainImageUrl()).isEqualTo("https://www1.e-hon.ne.jp/images/syoseki/ac/98/34541798.jpg?impolicy=PC_AC_M");

    }
}
