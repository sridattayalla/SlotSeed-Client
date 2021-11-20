package com.sridatta.busyhunkproject;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ShareActivity extends AppCompatActivity implements RefferalRedeemServiceDialogFragment.OnFragmentInteractionListener {
    Toolbar toolbar;
    Uri mInvitationUrl;
    int REQUEST_INVITE=1;
    FloatingActionButton email_share,sms_share,whatsApp_share,share;
    WebView share_webView;
    ConstraintLayout redeem_layout;
    TextView status;
    Button redeem;
    final String EMAIL="EMAIL",SMS="SMS",WHATSAPP="WHATSAPP",SHARE="SHARE";
    ProgressDialog pd;
    LayoutInflater layoutInflater;
    String UID;
    int INVITES_COUNT_TO_REDEEM = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        //uid
        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //inflater
        layoutInflater=LayoutInflater.from(getApplicationContext());
        //initiating ui
        status = findViewById(R.id.status);
        redeem_layout = findViewById(R.id.redeem_layout);
        redeem = findViewById(R.id.redeem);
        email_share=findViewById(R.id.Email);
        sms_share=findViewById(R.id.sms);
        whatsApp_share=findViewById(R.id.whatsapp);
        share=findViewById(R.id.share);
        share_webView = findViewById(R.id.terms_refer);
        toolbar=findViewById(R.id.toolbar8);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Share & Earn");
        pd=new ProgressDialog(this);
        pd.setMessage("Preparing your invitation");


        //email onClick
        email_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateShortLink(EMAIL);
            }
        });

        //sms onClick
        sms_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateShortLink(SMS);
            }
        });
        //whatsAppshare onClick
        whatsApp_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateShortLink(WHATSAPP);
            }
        });
        //share onClick
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateShortLink(SHARE);
            }
        });

        //redeem onClick
        redeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                RefferalRedeemServiceDialogFragment dialog = new RefferalRedeemServiceDialogFragment();
                dialog.show(ft, "dialog");
            }
        });

        //referrals
        setReferralStatus();

        //terms
        setTerms();

    }

    private void setReferralStatus() {
        DatabaseReference offer_usage_ref = FirebaseDatabase.getInstance().getReference().child("offer_usage/promo_codes/REFERRAL100");
        offer_usage_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("datta_text", dataSnapshot.toString());
                if(!dataSnapshot.hasChild(UID)){
                    setEligibility();
                    Log.d("datta_text", "Entered in onDatachnge");
                }
                else{
                    redeem_layout.setVisibility(View.GONE);
                    Log.d("datta_text", dataSnapshot.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTerms(){
        StorageReference terms_ref = FirebaseStorage.getInstance().getReference().child("html_files/share_terms.html");
        terms_ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri uri = task.getResult();
                    Log.d("datta_text", uri.toString());
                    share_webView.setWebViewClient(new WebViewClient(){

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            // on page load started, you may show progress spinner to user.
                            //progressBar.setVisibility(View.VISIBLE);
                            super.onPageStarted(view, url, favicon);
                        }

                        @Override
                        public void onReceivedError(WebView view,
                                                    WebResourceRequest request, WebResourceError error) {
                            // ERROR handling
                            super.onReceivedError(view, request, error);

                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            // actions on page load
                            super.onPageFinished(view, url);
                        }

                    });
                    share_webView.loadUrl(uri.toString());
                }
            }
        });
    }

    private void setEligibility() {
        DatabaseReference referral_ref = FirebaseDatabase.getInstance().getReference().child("referrals/"+UID);
        referral_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int)dataSnapshot.getChildrenCount();
                if(count >= INVITES_COUNT_TO_REDEEM){
                    status.setText("Congrats!, Redeem your benifit ");
                    //changing button
                    redeem.setBackground(getResources().getDrawable(R.drawable.primary_gradient_button));
                    redeem.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    redeem.setText("Redeem");
                    redeem.setEnabled(true);
                    Log.d("datta_text", "count>3");
                    Log.d("datta_text", Integer.toString(redeem.getVisibility()));
                }
                else if(count == 0){
                    redeem.setVisibility(View.GONE);
                    status.setText("Start by referring your friend.");
                    Log.d("datta_text", "count = 0");
                }
                else {
                    redeem.setVisibility(View.GONE);
                    status.setText(Integer.toString(INVITES_COUNT_TO_REDEEM - count) + " more referral to redeem");
                    Log.d("datta_text", "count");
                }

                redeem_layout.setVisibility(View.VISIBLE);
                Log.d("datta_text", "redeem layout Visible");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    Uri generateShortLink(final String msg){
        pd.show();
        //invitation url
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String link = "https://www.sergment.com/?invitedby=" + uid;
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDynamicLinkDomain("busyhunkproject.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.sridatta.busyhunkproject")
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                    @Override
                    public void onSuccess(ShortDynamicLink shortDynamicLink) {
                        mInvitationUrl = shortDynamicLink.getShortLink();
                        Log.d("datta_text","dynmic link is "+mInvitationUrl.toString());
                        pd.dismiss();
                        sendInvitation(msg);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("datta_text","exception while generating dynmic link "+e.toString());
                pd.dismiss();
            }
        });
        return mInvitationUrl;
    }

    void sendInvitation(String msg) {
        switch (msg){
            case SHARE:
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Sergment");
                intent.putExtra(Intent.EXTRA_TEXT,"I'm using "+getString(R.string.app_name)+". Get free Hair cut and facial, reward in app cash using my referral "+mInvitationUrl.toString());
                startActivity(Intent.createChooser(intent,"Share via"));
                break;
            case EMAIL:
                String referrerName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                String subject = String.format("%s wants you to install "+getString(R.string.app_name)+"!", referrerName);
                String invitationLink = mInvitationUrl.toString();
                String message = "Let's get free haircut and facial: "
                        + invitationLink;
                String msgHtml = String.format("<p>Let's celebrate joy of beauty together! Use my "
                        + "<a href=\"%s\">referrer link</a>!</p>", invitationLink);

                Intent email_intent = new Intent(Intent.ACTION_SENDTO);
                email_intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                email_intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                email_intent.putExtra(Intent.EXTRA_TEXT, message);
                email_intent.putExtra(Intent.EXTRA_HTML_TEXT, msgHtml);
                if (email_intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(email_intent);
                }
                break;
            case SMS:
                Intent sms_intent = new AppInviteInvitation.IntentBuilder(getString(R.string.app_name))
                        .setMessage("I'm using "+getString(R.string.app_name)+". Get free Hair cut and facial, reward in app cash using my referral ")
                        .setDeepLink(mInvitationUrl)
                        .setCustomImage(Uri.parse("android.resource://com.sridatta.busyhunkproject/drawable/sale_time"))
                        .build();
                startActivityForResult(sms_intent, REQUEST_INVITE);
                break;
            case WHATSAPP:
                Intent whatsAppIntent=new Intent(Intent.ACTION_SEND);
                whatsAppIntent.setType("text/plain");
                whatsAppIntent.putExtra(Intent.EXTRA_TEXT,"I,m using "+getString(R.string.app_name)+". Get free Hair cut and facial, reward in app cash using my referral "+mInvitationUrl.toString());
                whatsAppIntent.setPackage("com.whatsapp");
                try{
                startActivity(whatsAppIntent);}
                catch (Exception e){
                    Toast.makeText(getApplicationContext(),"WhatsApp may not have installed",Toast.LENGTH_SHORT).show();
                }
                break;

        }

    }

    @Override
    public void onFragmentInteraction(String category, String gender) {
        //getting selected category
        //sending category to providers activity
        Intent providersIntent = new Intent(ShareActivity.this, ProvidersActivity.class);
        providersIntent.putExtra("category", category);
        providersIntent.putExtra("gender", gender);
        providersIntent.putExtra("referral_reward_category", category);
        startActivity(providersIntent);
    }
}
