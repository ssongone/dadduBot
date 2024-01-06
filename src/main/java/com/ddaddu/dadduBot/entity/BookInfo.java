package com.ddaddu.dadduBot.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
@Getter
public class BookInfo {
    private String title;
    private String author;
    private String isbn;
    private int price;
    private String mainImageUrl;
    private String description;
}
