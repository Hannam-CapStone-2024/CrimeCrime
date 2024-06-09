package com.example.navigatorteam;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telecom.Call;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReverseLabelView extends Dialog {
    private TextView str1TextView;
    private TextView str2TextView;
    private TextView str3TextView;
    private TextView str4TextView;
    private Button HomePageButton;
    private Button CallButton;
    private DestinationListener destinationListener;
    public ReverseLabelView(Context context, boolean showRouteButton) {
        super(context);
        setContentView(R.layout.view_marker2);
        str1TextView = findViewById(R.id.marker2_text1);
        str2TextView = findViewById(R.id.marker2_text2);
        str3TextView = findViewById(R.id.marker2_text3);
        str4TextView = findViewById(R.id.marker2_text4);
        HomePageButton = findViewById(R.id.marker2_button);
        CallButton = findViewById(R.id.marker3_button);
        Button routeButton = findViewById(R.id.route_button);
        if (showRouteButton) {
            routeButton.setVisibility(View.VISIBLE);
            routeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss(); // 다이얼로그 닫기
                    if (destinationListener != null) {
                        destinationListener.onDestinationSet();
                    }
                }
            });
        } else {
            routeButton.setVisibility(View.GONE);
        }
    }

    // 다른 코드는 그대로 유지됩니다.

    public void setDestinationListener(DestinationListener listener) {
        this.destinationListener = listener;
    }

    // 다른 코드는 그대로 유지됩니다.

    public interface DestinationListener {
        void onDestinationSet();
    }
    public void setText(String str1, String str2, String str3, String str4) {
        if (str1 != null && !str1.isEmpty()) {
            this.str1TextView.setText(str1);
            this.str1TextView.setVisibility(View.VISIBLE);
        } else {
            this.str1TextView.setVisibility(View.GONE);
        }

        if (str2 != null && !str2.isEmpty()) {
            this.str2TextView.setText(str2);
            this.str2TextView.setVisibility(View.VISIBLE);
        } else {
            this.str2TextView.setVisibility(View.GONE);
        }

        if (str3 != null && !str3.isEmpty()) {
            this.str3TextView.setText(str3);
            CallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tel = str3;
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + tel));
                    getContext().startActivity(callIntent);
                }
            });
            this.str3TextView.setVisibility(View.VISIBLE);
        } else {
            this.str3TextView.setVisibility(View.GONE);
        }

        if (str4 != null && !str4.isEmpty()) {
            this.str4TextView.setText(str4);
            HomePageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = str4;
                    Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"+url));
                    getContext().startActivity(urlIntent);
                }
            });
            this.str4TextView.setVisibility(View.VISIBLE);
        } else {
            this.str4TextView.setVisibility(View.GONE);
        }
    }
}
