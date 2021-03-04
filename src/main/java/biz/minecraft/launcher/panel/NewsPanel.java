package biz.minecraft.launcher.panel;

import biz.minecraft.launcher.entity.NewsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NewsPanel extends JScrollPane {

    private final static Logger LOGGER = LoggerFactory.getLogger(NewsPanel.class);

    public NewsPanel(List<NewsItem> news) {

        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        int  width = 700; // 600
        int height = 450; // 450

        setPreferredSize(new Dimension(width, height));
        getVerticalScrollBar().setUnitIncrement(7); // Increase scroll speed
        //setBorder(new EmptyBorder(0, 0, 0, 0)); // Padding 0
        setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.decode("#425b67")));

        JPanel newsPanel = new JPanel();

        newsPanel.setLayout(new MigLayout("insets 0"));
        newsPanel.setBackground(UIManager.getColor( "List.background" ));

        Collections.sort(news, Collections.reverseOrder());

        for (NewsItem item : news) {

            JLabel cover = null;

            try {
                URL coverURL = new URL(item.getCover());
                BufferedImage coverImage = ImageIO.read(coverURL); // 700x250px
                cover = new JLabel(new ImageIcon(coverImage));
            } catch (Exception e) {
                LOGGER.warn("Failed to get news item image by URL, trying to load default from resources.", e);
                cover = new JLabel(new ImageIcon(getClass().getResource("/images/news-cover.jpg")));
            } finally {

                Date dateMSK = item.getDate();
                ZonedDateTime zonedDateTime = new Timestamp(dateMSK.getTime()).toLocalDateTime().atZone(ZoneId.systemDefault());
                Date currentTimeZone = Date.from(zonedDateTime.toInstant());

                JLabel title = new JLabel(item.getTitle());
                JLabel body = new JLabel("<html><div width=\"680\">" + item.getBody() + "</div></html>");
                JLabel date = new JLabel(currentTimeZone.toString()); // Originally news time zone MSK -> Current

                title.setFont(new Font("Helvetica Neue", Font.PLAIN, 20));
                date.setForeground(Color.gray);

                newsPanel.add(cover, "wrap");
                newsPanel.add(title, "gapleft 10, gaptop 5, gapbottom 5, wrap");
                newsPanel.add(body, "gapleft 10, wrap");
                newsPanel.add(date, "gapleft 10, gaptop 5, gapbottom 10, wrap");
            }
        }

        setViewportView(newsPanel);
    }

}
