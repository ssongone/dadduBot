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
    public static double MARGIN = 11;

    public List<BookInfo> scrapeBookList(String url) {
        List<BookInfo> bookInfos = new ArrayList<>();
        Document document = connect(url).orElseThrow(() -> new IllegalArgumentException("url이 잘못된 것 같습니다"));

        Elements elements = document.select("div#contents div.detail p.title a");
        for (Element element : elements) {
            String hrefValue = element.attr("href");
            bookInfos.add(scrapDetailPage(hrefValue));
        }

        System.out.println(bookInfos);
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

        String isbn = document.select("div.mainItemTable tr:contains(ISBN)").first().select("span.codeSelect").text();
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

        String description = makeDescription(document, mainImageUrlElement);

        return BookInfo.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .price((int) (Integer.parseInt(price)*MARGIN))
                .mainImageUrl(mainImageUrl)
                .description(description)
                .build();
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
                .price(((int) (Integer.parseInt(price)*MARGIN) / 10) * 10)
                .mainImageUrl(mainImageUrl)
                .date(date)
                .publisher(publisher)
                .build();

        String description = makeMagazineDescription(document, bookInfo);
        bookInfo.setDescription(description);

        return bookInfo;

    }


    private String makeMagazineDescription(Document document, BookInfo bookInfo) {

        String description = createImgTag(bookInfo.getMainImageUrl());
        description += makeMagazineDescriptionTable(bookInfo.getPublisher(), bookInfo.getDate(), bookInfo.getJan(), bookInfo.getIsbn());
        description += makeMagazineDescriptionDetail(document);

        System.out.println(description);
        return description;
    }

    private String makeDescription(Document document, Element mainImageUrlElement) {
        if (mainImageUrlElement == null)
            return "";

        String description = mainImageUrlElement.toString();
        Elements itemDetailContents = document.select("div.itemDetailContents");
        Elements commentContents = document.select("div.commentContents");
        Elements authorContents = document.select("div.authorContents");

//        별점이 안가져와져서 보류..!
//        Elements reviewContents = document.select("div.reviewContents");

        if (!itemDetailContents.isEmpty())
            description += itemDetailContents;

        if (!commentContents.isEmpty())
            description += commentContents;

        if (!authorContents.isEmpty())
            description += authorContents;

//        if (!reviewContents.isEmpty())
//            description += reviewContents;

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
            builder.append("<div><strong>출판사 정보:</strong> ").append(thirdSection.text()).append("</div>\n");
        }

        return builder.toString();
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