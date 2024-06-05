package com.example.navigatorteam;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.navigatorteam.Manager.CriminalLoader;
import com.example.navigatorteam.Manager.ReverseGeocodingTask;
import com.example.navigatorteam.Support.ActivityManager;
import com.skt.tmap.TMapData;
import com.skt.tmap.TMapView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private Button emergencyCallButton;
    public static MainActivity Instance;
    private TextView newsTitle1;
    private TextView newsContent1;
    private TextView newsTitle2;
    private TextView newsContent2;
    private TextView newsTitle3;
    private TextView newsContent3;
    private static final List<String> KEYWORDS = Arrays.asList("강도", "살인", "칼부림", "묻지마"); // 검색할 키워드 목록
    private String link1, link2, link3; // 링크 저장 변수
    private TMapView tMapView;
    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        Instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // 레이아웃 설정
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("pRNUlsEpce4d3mB0MUabnMDhHbLmdtlPrUYZI3i0");

        // Button 초기화와 클릭 리스너 설정은 onCreate 메서드 내에서 해야 합니다.
        Button homeButton = findViewById(R.id.Homebutton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Main.class);
                startActivity(intent);
            }
        });

        Init();
        try {
            getLastKnownLocationAndConvertToAddress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SetArticle();

    }

    private void Init()
    {
        CriminalLoader loader = new CriminalLoader(this);

        // TextView 초기화
        TextView theftStatusTextView = findViewById(R.id.theftStatus);
        TextView robberyStatusTextView = findViewById(R.id.robberyStatus);
        TextView assaultStatusTextView = findViewById(R.id.assaultStatus);
        TextView locationTextView = findViewById(R.id.location);
        TextView dateTimeTextView = findViewById(R.id.date_time);
        main_message = findViewById(R.id.main_message);
        TextView main_status = findViewById(R.id.main_status);
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        LinearLayout subLayout = findViewById(R.id.subLayout);
        TableLayout subTableRow = findViewById(R.id.subTableLow);
        TableRow subTableLowTop = findViewById(R.id.subTableLowTop);
        ImageView mainIcon = findViewById(R.id.main_icon);
        TextView cond_4 = findViewById(R.id.cond_4);

        TextView date_1 = findViewById(R.id.date_1);
        ImageView time_1 = findViewById(R.id.image_1);
        TextView cond_1 = findViewById(R.id.cond_1);
        TextView date_2 = findViewById(R.id.date_2);
        ImageView time_2 = findViewById(R.id.image_2);
        TextView cond_2 = findViewById(R.id.cond_2);
        TextView date_3 = findViewById(R.id.date_3);
        ImageView time_3 = findViewById(R.id.image_3);
        TextView cond_3 = findViewById(R.id.cond_3);
        TextView date_4 = findViewById(R.id.date_4);
        ImageView time_4 = findViewById(R.id.image_4);

        // ActivityManager 초기화
        ActivityManager.getInstance().initialize(
                theftStatusTextView,
                robberyStatusTextView,
                assaultStatusTextView,
                locationTextView,
                dateTimeTextView,
                main_message,
                main_status,
                mainLayout,
                subLayout,
                subTableRow,
                subTableLowTop,
                mainIcon,
                date_1,
                time_1,
                cond_1,
                date_2,
                time_2,
                cond_2,
                date_3,
                time_3,
                cond_3,
                date_4,
                time_4,
                cond_4
        );
        emergencyCallButton = findViewById(R.id.emergencyCallButton);
        emergencyCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEmergencyCall();
            }
        });
    }

    private void SetArticle()
    {
        newsTitle1 = findViewById(R.id.newsTitle1);
        newsContent1 = findViewById(R.id.newsContent1);
        newsTitle2 = findViewById(R.id.newsTitle2);
        newsContent2 = findViewById(R.id.newsContent2);
        newsTitle3 = findViewById(R.id.newsTitle3);
        newsContent3 = findViewById(R.id.newsContent3);

        int crimeCategorySid1 = 102; // 예시로 사회 카테고리
        int crimeCategorySid2 = 249; // 범죄 관련 카테고리

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        String startDate = sdf.format(calendar.getTime()); // 현재 날짜

        calendar.add(Calendar.DATE, -30); // 현재 날짜에서 30일 전
        String endDate = sdf.format(calendar.getTime());

        new NewsCrawlerTask().execute(crimeCategorySid1, crimeCategorySid2, startDate, endDate);
    }



    private void getLastKnownLocationAndConvertToAddress() throws IOException {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        double latitude;
        double longitude;
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null)
            {
                latitude = 36.35618653374754;
                longitude = 127.41913010772244;
            }
            else
            {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            convertLocationToAddress(latitude, longitude);
        } catch (SecurityException e) {
            ActivityManager.getInstance().setDateTimeAndLocation("");
            ActivityManager.getInstance().setCrimeStatus("");
            ActivityManager.getInstance().setExplainText("");
        }
    }

    private void convertLocationToAddress(double latitude, double longitude) {
        try {
            new ReverseGeocodingTask(latitude,longitude).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        TMapData tMapData = new TMapData();
        Log.d("TAG", "convertLocationToAddress: " + tMapData.convertGpsToAddress(36.35041, 127.38455));
        tMapData.convertGpsToAddress(latitude, longitude);
        tMapData.convertGpsToAddress(latitude, longitude, new TMapData.OnConvertGPSToAddressListener() {
                    @Override
                    public void onConverGPSToAddress(String s) {
                        Log.d("TAG", "convertLocationToAddress: " + latitude + ", " + longitude);
                        main_message.setText(s);
                        try {
                            ActivityManager.getInstance().setDateTimeAndLocation(s);
                            ActivityManager.getInstance().setCrimeStatus(s);
                            ActivityManager.getInstance().setExplainText(s);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Log.d("TAG", "convertLocationToAddress: " + s);
                    }
                }
                );

         */
    }

    public void makeEmergencyCall() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:01027199447"));
        startActivity(callIntent);
    }

    TextView main_message;

    private class NewsCrawlerTask extends AsyncTask<Object, Void, List<Article>> {
        @Override
        protected List<Article> doInBackground(Object... params) {
            int sid1 = (int) params[0];
            int sid2 = (int) params[1];
            String startDate = (String) params[2];
            String endDate = (String) params[3];

            List<Article> articles = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar calendar = Calendar.getInstance();

            try {
                calendar.setTime(sdf.parse(startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            while (articles.size() < 3 && !calendar.getTime().before(parseDate(endDate))) {
                String currentDate = sdf.format(calendar.getTime());
                for (int page = 1; page <= 10; page++) {
                    String url = String.format("https://news.naver.com/main/list.naver?mode=LS2D&sid2=%d&sid1=%d&mid=sec&date=%s&page=%d", sid2, sid1, currentDate, page);
                    Log.d("NewsCrawlerTask", "Fetching URL: " + url);
                    List<String> articleLinks = getNewsLinks(url);
                    for (String articleUrl : articleLinks) {
                        Article article = fetchArticleDetails(articleUrl);
                        if (article != null && containsKeywords(article)) {
                            articles.add(article);
                            if (articles.size() >= 3) break;
                        }
                    }
                    if (articles.size() >= 3) break;
                }
                calendar.add(Calendar.DATE, -1);
            }
            return articles;
        }

        @Override
        protected void onPostExecute(List<Article> articles) {
            if (articles.size() > 0) {
                newsTitle1.setText(articles.get(0).getTitle());
                newsContent1.setText(articles.get(0).getContent());
                link1 = articles.get(0).getUrl(); // 링크 저장

                newsTitle1.setOnClickListener(view -> openLink(link1));
                newsContent1.setOnClickListener(view -> openLink(link1));
            }
            if (articles.size() > 1) {
                newsTitle2.setText(articles.get(1).getTitle());
                newsContent2.setText(articles.get(1).getContent());
                link2 = articles.get(1).getUrl(); // 링크 저장

                newsTitle2.setOnClickListener(view -> openLink(link2));
                newsContent2.setOnClickListener(view -> openLink(link2));
            }
            if (articles.size() > 2) {
                newsTitle3.setText(articles.get(2).getTitle());
                newsContent3.setText(articles.get(2).getContent());
                link3 = articles.get(2).getUrl(); // 링크 저장

                newsTitle3.setOnClickListener(view -> openLink(link3));
                newsContent3.setOnClickListener(view -> openLink(link3));
            }
        }
        private void openLink(String url) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
        private List<String> getNewsLinks(String url) {
            List<String> articleLinks = new ArrayList<>();
            try {
                Document doc = Jsoup.connect(url).get();
                Elements elements = doc.select("ul.type06 li"); // li 태그를 선택합니다.
                for (Element element : elements) {
                    Element linkElement = element.selectFirst("a"); // 첫 번째 a 태그만 선택합니다.
                    if (linkElement != null) {
                        String link = linkElement.attr("href");
                        Log.d("ArticleLink", "Link: " + link);
                        articleLinks.add(link);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return articleLinks;
        }


        private Article fetchArticleDetails(String articleUrl) {
            try {
                Document doc = Jsoup.connect(articleUrl).get();

                String title = "";
                String content = "";

                // og:title 메타 태그에서 제목 추출
                Elements metaOgTitle = doc.select("meta[property=og:title]");
                if (metaOgTitle != null && metaOgTitle.size() > 0) {
                    title = metaOgTitle.attr("content");
                }

                // og:description 메타 태그에서 내용 추출
                Elements metaOgDescription = doc.select("meta[property=og:description]");
                if (metaOgDescription != null && metaOgDescription.size() > 0) {
                    content = metaOgDescription.attr("content");
                }
                return new Article(title, content, articleUrl);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }


        private boolean containsKeywords(Article article) {
            for (String keyword : KEYWORDS) {
                if (article.getTitle().contains(keyword) || article.getContent().contains(keyword)) {
                    return true;
                }
            }
            return false;
        }

        private Date parseDate(String date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return calendar.getTime();
        }
    }

    private static class Article {
        private String title;
        private String content;
        private String url;

        public Article(String title, String content, String url) {
            this.title = title;
            this.content = content;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getUrl() {
            return url;
        }
    }
}
