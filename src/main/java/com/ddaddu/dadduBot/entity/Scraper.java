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

    public BookInfo scrapDetailPage(String url) {
        Document document = connect(url).orElseThrow(() -> new IllegalArgumentException("url이 잘못된 것 같습니다"));

        String title = document.select("p.itemTitle").first().text();
        String author = document.select("ul.AuthorsName").first().text();
        String isbn = document.select("div.mainItemTable tr:contains(ISBN)").first().select("span.codeSelect").text();
        String price = document.select("div.mainItemTable tr:contains(税込価格) td").first().text().replace(",", "").replace("円", "");

        Element mainImageUrlElement = document.select("div.mainItemImg img").first();
        String mainImageUrl = (mainImageUrlElement != null) ? mainImageUrlElement.attr("src") : "";

        String description = makeDescription(document, mainImageUrlElement);

        return BookInfo.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .price(Integer.parseInt(price))
                .mainImageUrl(mainImageUrl)
                .description(description)
                .build();
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

    private static Optional<Document> connect(String pageUrl) {
        try {
            Connection conn = Jsoup.connect(pageUrl);
            return Optional.of(conn.get());
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


}
