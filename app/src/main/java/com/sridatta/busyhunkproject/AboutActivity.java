package com.sridatta.busyhunkproject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    Toolbar toolbar;
    Button playstore, email, terms;
    TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //initiating ui
        title = findViewById(R.id.toolbar_title);
        playstore = findViewById(R.id.playstore);
        email = findViewById(R.id.email);
        terms = findViewById(R.id.terms);
        toolbar=findViewById(R.id.toolbar11);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        //title
        String first = "Slot";
        String next = "<font color = '#388E3C'>Seed</font>";
        title.setText(Html.fromHtml("<b>" + first + next + "</b>"));

        //playstore button onClick
        playstore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                }
            }
        });

        //email button onClick
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent email_intent = new Intent(Intent.ACTION_SENDTO);
                email_intent.setType("text/plain");
                email_intent.setData(Uri.parse("mailto:slotseed@gmail.com"));
                email_intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(email_intent);
            }
        });

        //terms onClick
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent terms_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://busyhunkproject.firebaseapp.com/"));
                startActivity(terms_intent);
            }
        });
    }
}
