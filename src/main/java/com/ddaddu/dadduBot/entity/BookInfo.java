package com.ddaddu.dadduBot.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@ToString
@Getter
public class BookInfo {
    private String title;
    private String author;
    private String isbn;
    private int price;
    private String bookType;
    private String mainImageUrl;
    private String description;
    private String date;
    private String publisher;


    private String jan;
    public void setDescription(String description) {
        this.description = description;
    }
}
