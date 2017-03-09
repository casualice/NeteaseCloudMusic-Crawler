package demo;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

public class demo1 {
    public static void main(String[] args) throws IOException {
        Validate.isTrue(args.length == 1, "usage: supply url to fetch");
        String url = args[0];
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");
        System.out.printf("\nMedia: %d", media.size());
        for (Element src : media) {
            if (src.tagName().equals("img"))
            	System.out.printf("\n  %s: %s %sx%s (%s)",
                        src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),
                        trim(src.attr("alt"), 20));
            else
            	System.out.printf("\n  %s: %s", src.tagName(), src.attr("abs:src"));
        }
        System.out.printf("\nImports: %d", imports.size());
        for (Element link : imports) {
        	System.out.printf("\n  %s %s %s", link.tagName(),link.attr("abs:href"), link.attr("rel"));
        }
        System.out.printf("\nLinks: %d", links.size());
        for (Element link : links) {
        	System.out.printf("\n  a: %s  %s", link.attr("abs:href"), trim(link.text(), 35));
        }
    }
    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }
}