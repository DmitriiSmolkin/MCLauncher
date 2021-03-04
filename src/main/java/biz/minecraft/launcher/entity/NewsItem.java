package biz.minecraft.launcher.entity;

import java.util.Date;

public class NewsItem implements Comparable<NewsItem> {

    private String cover;
    private String title;
    private String body;
    private Date date;

    public NewsItem() { }

    public String getCover() { return cover; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public Date getDate() { return date; }


    @Override
    public int compareTo(NewsItem newsItem) {
        return getDate().compareTo(newsItem.getDate());
    }

}
