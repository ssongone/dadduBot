package com.ddaddu.dadduBot.entity;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Optional;

public class Scraper {

    public BookInfo scrapDetailPage(String url) {
        Document document = connect(url).orElseThrow(() -> new IllegalArgumentException("url이 잘못된 것 같습니다"));

        String title = document.select("p.itemTitle").first().text();
        String author = document.select("ul.AuthorsName").first().text();
        String isbn = document.select("div.mainItemTable tr:contains(ISBN)").first().select("span.codeSelect").text();
        String price = document.select("div.mainItemTable tr:contains(税込価格) td").first().text().replace(",", "").replace("円", "");

        Element mainImageUrlElement = document.select("div#cover img").first();
        String mainImageUrl = mainImageUrlElement.attr("src");

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
        String description = mainImageUrlElement.toString();
        Elements itemDetailContents = document.select("div.itemDetailContents");
        Elements commentContents = document.select("div.commentContents");
        Elements authorContents = document.select("div.authorContents");

        if (!itemDetailContents.isEmpty())
            description += itemDetailContents;

        if (!commentContents.isEmpty())
            description += commentContents;

        if (!authorContents.isEmpty())
            description += authorContents;

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
