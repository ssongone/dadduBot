package com.ddaddu.dadduBot.entity;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Scraper {
    public static double BOOK_MARGIN = 12;
    public static double MAGAZINE_MARGIN = 11;

    private String endDescription = "<a href='https://ifh.cc/v-Xsvyts' target='_blank'><img src='https://ifh.cc/g/Xsvyts.jpg' border='0'></a>";

    public List<BookInfo> scrapeBookList(String url) {
        List<BookInfo> bookInfos = new ArrayList<>();
        Document document = connect(url).orElseThrow(() -> new IllegalArgumentException("url이 잘못된 것 같습니다"));

        Elements elements = document.select("div#contents div.detail p.title a");
        for (Element element : elements) {
            String hrefValue = element.attr("href");
            bookInfos.add(scrapDetailPage(hrefValue));
        }

        return bookInfos;
    }
    public List<BookInfo> scrapeMagazineList(String url) {
        List<BookInfo> bookInfos = new ArrayList<>();
        Document document = connect(url).orElseThrow(() -> new IllegalArgumentException("url이 잘못된 것 같습니다"));

        Elements elements = document.select("div#contents div.detail p.title a");
        for (Element element : elements) {
            String hrefValue = element.attr("href");
            bookInfos.add(scrapDetailMagazine(hrefValue));
        }

        System.out.println(bookInfos);
        return bookInfos;
    }

    public BookInfo scrapDetailPage(String url) {
        Document document = connect(url).orElseThrow(() -> new IllegalArgumentException("url이 잘못된 것 같습니다"));

        String title = document.select("p.itemTitle").first().text();
        String author = document.select("ul.AuthorsName").first().text();
        String date = document.select("div.mainItemTable tr:contains(出版年月) td").first().text().replace("年", ".").replace("月","").replace("日", "");
        String page = document.select("div.mainItemTable tr:contains(頁数) td").first().text();

        String isbn = document.select("div.mainItemTable tr:contains(ISBN)").first().select("span.codeSelect").text();
        String price = document.select("div.mainItemTable tr:contains(税込価格) td").first().text().replace(",", "").replace("円", "");
        String publisher = document.select("div.mainItemTable tr:contains(出版社名) td").first().text();

        Element mainImageUrlElement = document.select("div.mainItemImg img").first();
        String mainImageUrl = "";
        if (mainImageUrlElement != null) {
            mainImageUrl = mainImageUrlElement.attr("src");
            int questionMarkIndex = mainImageUrl.indexOf("?");
            if (questionMarkIndex != -1) {
                mainImageUrl = mainImageUrl.substring(0, questionMarkIndex);
            }
        }


        BookInfo bookInfo = BookInfo.builder()
                .title(title)
                .author(author)
                .publisher(publisher)
                .isbn(isbn)
                .page(page)
                .date(date)
                .price(((int) (Integer.parseInt(price)*BOOK_MARGIN) / 10) * 10)
                .mainImageUrl(mainImageUrl)
                .build();

        String description = makeBookDescription(document, bookInfo);
        bookInfo.setDescription(description);

        return bookInfo;
    }

    public BookInfo scrapDetailMagazine(String url) {
        Document document = connect(url).orElseThrow(() -> new IllegalArgumentException("url이 잘못된 것 같습니다"));

        String title = document.select("p.itemTitle").first().text();
        String publisher = document.select("div.mainItemTable tr:contains(出版社名) td").first().text();
        String date = document.select("div.mainItemTable tr:contains(発売日) td").first().text().replace("年", ".").replace("月",".").replace("日", "");
        String jan = document.select("div.mainItemTable tr:contains(JAN) td").first().text();
        String code = document.select("div.mainItemTable tr:contains(雑誌コード) td").first().text();

        String price = document.select("div.mainItemTable tr:contains(税込価格) td").first().text().replace(",", "").replace("円", "");
        Element mainImageUrlElement = document.select("div.mainItemImg img").first();
        String mainImageUrl = "";
        if (mainImageUrlElement != null) {
            mainImageUrl = mainImageUrlElement.attr("src");
            int questionMarkIndex = mainImageUrl.indexOf("?");
            if (questionMarkIndex != -1) {
                mainImageUrl = mainImageUrl.substring(0, questionMarkIndex);
            }
        }
        BookInfo bookInfo =  BookInfo.builder()
                .title(title)
                .author("")
                .isbn(code)
                .jan(jan)
                .price(((int) (Integer.parseInt(price)*MAGAZINE_MARGIN) / 10) * 10)
                .mainImageUrl(mainImageUrl)
                .date(date)
                .publisher(publisher)
                .build();

        String description = makeMagazineDescription(document, bookInfo);
        bookInfo.setDescription(description);

        return bookInfo;

    }

    private String makeBookDescription(Document document, BookInfo bookInfo) {

        String description = createImgTag(bookInfo.getMainImageUrl());
        description += makeBookDescriptionTable(bookInfo);
        description += "<br><br>";
        description += makeBookDescriptionDetail(document);

        description += endDescription;
        return description;
    }


    private String makeMagazineDescription(Document document, BookInfo bookInfo) {

        String description = createImgTag(bookInfo.getMainImageUrl());
        description += makeMagazineDescriptionTable(bookInfo.getPublisher(), bookInfo.getDate(), bookInfo.getJan(), bookInfo.getIsbn());
        description += "<br><br>";
        description += makeMagazineDescriptionDetail(document);

        description += endDescription;
        return description;
    }


    private Optional<Document> connect(String pageUrl) {
        try {
            Connection conn = Jsoup.connect(pageUrl);
            return Optional.of(conn.get());
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    private String createImgTag(String imageUrl) {
        StringBuilder imgTagBuilder = new StringBuilder();

        imgTagBuilder.append("<img src=\"").append(imageUrl).append("\">");

        return imgTagBuilder.toString();
    }
    private String makeBookDescriptionDetail(Document document) {
        StringBuilder builder = new StringBuilder();
        // 저자 소개

        Elements authorContents = document.select("div.authorContents dl.authorDescList");
        if (authorContents != null && authorContents.size()>0) {
            builder.append("<h2 class=\"heading02\">저자 소개</h2>\n");
            for (Element element : authorContents) {
                builder.append("<div><strong>저자:</strong> ").append(element.select("dt").text()).append("<br>").append(authorContents.select("dd").text()).append("</div>");
                builder.append("<br>");
            }
            builder.append("<br>");
        }

        // 상품 내용
        Element itemDetailTable = document.select("div.itemDetailTable tr:contains(要旨) td").first();
        if (itemDetailTable != null) {
            builder.append("<h2 class=\"heading02\">상품 내용</h2>\n");
            builder.append("<div><strong>요약:</strong> ").append(itemDetailTable.text()).append("</div>");
            builder.append("<br>");
        }

        Element itemDetailTable2 = document.select("div.itemDetailTable tr:contains(目次) td").first();
        if (itemDetailTable2 != null) {
            builder.append("<div><strong>목차:</strong> ").append(itemDetailTable2.text()).append("</div>");
            builder.append("<br>");
        }
        Element itemDetailTable3 = document.select("div.itemDetailTable tr:contains(文学賞情報) td").first();
        if (itemDetailTable3 != null) {
            builder.append("<div><strong>문학상 정보:</strong> ").append(itemDetailTable3.text()).append("</div>");
            builder.append("<br><br>");
        }

        // 출판사 메이커 코멘트
        Element commentContents = document.select("div.commentContents div.commentDetail").first();
        if (commentContents != null) {
            builder.append("<h2 class=\"heading02\">코멘트</h2>\n");
            builder.append("<div><strong>출판사 메이커 코멘트:</strong> ").append(commentContents.text()).append("</div>");
            builder.append("<br>");
        }

        //NetGalley 회원 리뷰
        Elements netGalleyReviews = document.select("div#netgalley div.tx_area01 div.review");
        if (netGalleyReviews.size()>0) {
            builder.append("<h2 class=\"heading02\">NetGalley 회원 리뷰</h2>\n");
            for (Element element : netGalleyReviews) {
                builder.append("<div><strong>").append(element.select("div.reviewerclass span").first().text()).append("</strong> <br>");
                builder.append(element.select("p.f_tx").text());
                builder.append("<br>");
            }
            builder.append("<br><br>");
        }
        return builder.toString();
    }


    private String makeMagazineDescriptionDetail(Document document) {
        StringBuilder builder = new StringBuilder();
        builder.append("<h2 class=\"heading02\">상품내용</h2>\n");

        // 잡지유명상표정보
        Element firstSection = document.select("div.itemDetailTable tr:contains(雑誌銘柄情報) td").first();
        if (firstSection != null) {
            builder.append("<div><strong>잡지유명상표정보:</strong> ").append(firstSection.text()).append("</div>\n");
        }

        // 특집 정보
        Element secondSection = document.select("div.itemDetailTable tr:contains(特集情報) td").first();
        if (secondSection != null) {
            builder.append("<div><strong>특집 정보:</strong> ").append(secondSection.text()).append("</div>\n");
        }

        // 출판사 정보
        Element thirdSection = document.select("div.itemDetailTable tr:contains(出版社情報) td").first();
        if (thirdSection != null) {
            builder.append("<div><strong>출판사:</strong> ").append(thirdSection.text()).append("</div>\n");
        }

        return builder.toString();
    }

    private String makeBookDescriptionTable(BookInfo bookInfo) {
        StringBuilder divBuilder = new StringBuilder();
        divBuilder.append("<h2 class=\"heading02\">도서정보</h2>\n");
        divBuilder.append("<div class=\"mainItemTable\">\n");

        divBuilder.append("<div><strong>제목:</strong> ").append(bookInfo.getTitle()).append("</div>\n");

        divBuilder.append("<div><strong>저자:</strong> ").append(bookInfo.getAuthor()).append("</div>\n");

        // 출판사 이름
        divBuilder.append("<div><strong>출판사:</strong> ").append(bookInfo.getPublisher()).append("</div>\n");

        // 발매일
        divBuilder.append("<div><strong>발매일:</strong> ").append(bookInfo.getDate()).append("</div>\n");

        // 잡지 Jan
        divBuilder.append("<div><strong>ISBN:</strong> <span class=\"codeSelect\">").append(bookInfo.getDate()).append("</span></div>\n");

        // 잡지 코드
        divBuilder.append("<div><strong>페이지 수:</strong> ").append(bookInfo.getPage()).append("</div>\n");

        divBuilder.append("</div>\n");
        return divBuilder.toString();
    }

    private String makeMagazineDescriptionTable(String publisher, String date, String jan, String code) {
        StringBuilder divBuilder = new StringBuilder();
        divBuilder.append("<h2 class=\"heading02\">잡지정보</h2>\n");
        divBuilder.append("<div class=\"mainItemTable\">\n");

        // 출판사 이름
        divBuilder.append("<div><strong>출판사 이름:</strong> ").append(publisher).append("</div>\n");

        // 발매일
        divBuilder.append("<div><strong>발매일:</strong> ").append(date).append("</div>\n");

        // 잡지 Jan
        divBuilder.append("<div><strong>잡지 Jan:</strong> <span class=\"codeSelect\">").append(jan).append("</span></div>\n");

        // 잡지 코드
        divBuilder.append("<div><strong>잡지 코드:</strong> ").append(code).append("</div>\n");

        divBuilder.append("</div>\n");
        return divBuilder.toString();
    }


}