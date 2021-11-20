package com.sridatta.busyhunkproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class WalletActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView wallet_cash;
    String UID;
    LinkedHashMap<String, Wallet> walletHashMap = new LinkedHashMap<>();
    RecyclerView recyclerView;
    WalletAdapter adapter = new WalletAdapter();
    LottieAnimationView loading_anim;
    LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        //inflater
        layoutInflater = LayoutInflater.from(getApplicationContext());
        //firebase uid
        UID= FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        //initiating ui
        loading_anim = findViewById(R.id.progressbar); loading_anim.playAnimation();
        wallet_cash=findViewById(R.id.wallet_cash);
        toolbar=findViewById(R.id.toolbar7);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.wallet_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        //
        setWalletCash();
        //
        get_n_set_transaction_details();
    }

    private void get_n_set_transaction_details() {
        Query transactionQuery = FirebaseDatabase.getInstance().getReference().child("user_wallet/"+UID+"/transactions").orderByChild("time_stamp");
        transactionQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    try {
                        //date and time
                        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");
                        DateFormat key_date_format = new SimpleDateFormat("yyyyMMddHHmmss");
                        long time_stamp = Long.parseLong(dataSnapshot1.child("time_stamp").getValue().toString());
                        Date resultdate = new Date(time_stamp);
                        String date = dateFormat.format(resultdate);
                        String date_key = key_date_format.format(resultdate);
                        //added
                        boolean added = false;
                        if (dataSnapshot1.hasChild("added")) {
                            added = true;
                        }
                        //money
                        String money;
                        if (added) {
                            money = dataSnapshot1.child("added").getValue().toString();
                        } else {
                            money = dataSnapshot1.child("deducted").getValue().toString();
                        }
                        //type
                        String type = dataSnapshot1.child("type").getValue().toString();
                        //action
                        String action = dataSnapshot1.child("action").getValue().toString();
                        //tid
                        String tid = null;
                        if(dataSnapshot1.child("tid").getValue() != null){
                            tid = dataSnapshot1.child("tid").getValue().toString();
                        }

                        Log.d("datta_text", action + " " + money + " " + date);
                        walletHashMap.put(dataSnapshot1.getKey(), new Wallet(action, money, date, type, tid, added));
                        adapter.notifyDataSetChanged();
                        loading_anim.setVisibility(View.GONE);
                    }
                    catch (Exception e){
                        Log.d("datta_text", "Error in adding wallet transaction to wallet hashmap: "+e.toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error :(", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void setWalletCash(){
        //getting walletcash
        DatabaseReference wallet_ref= FirebaseDatabase.getInstance().getReference().child("user_wallet/"+UID+"/seeds");
        wallet_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    wallet_cash.setText(dataSnapshot.getValue().toString());
                }
                else{
                    wallet_cash.setText("opps!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("datta_text","error in options actvty while getiing wallet details: "+databaseError.toString());
                wallet_cash.setText("opps!");
            }
        });
    }

    class WalletAdapter extends RecyclerView.Adapter<WalletViewHolder>{
        View v;
        @Override
        public WalletViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            v = layoutInflater.inflate(R.layout.wallet_transactions_single, parent, false);
            return new WalletViewHolder(v);
        }

        @Override
        public void onBindViewHolder(WalletViewHolder holder, int position) {
            ArrayList<String> keys = new ArrayList<>(walletHashMap.keySet());
            holder.setDetails(walletHashMap.get(keys.get(keys.size() - 1 - position)));
        }

        @Override
        public int getItemCount() {
            return walletHashMap.size();
        }
    }

    class WalletViewHolder extends RecyclerView.ViewHolder{
        View v;
        public WalletViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }


        public void setDetails(Wallet wallet) {
            TextView action, time, money;
            Button details;
            action = v.findViewById(R.id.action);
            time = v.findViewById(R.id.time);
            money = v.findViewById(R.id.money);
//            details = v.findViewById(R.id.details);
//            details.setTag(wallet.getTid());

            /*try{if(!wallet.getType().equals(getResources().getString(R.string.salon))){
                details.setVisibility(View.GONE);
            }}catch (Exception e){
                details.setVisibility(View.GONE);
            }*/

            action.setText(wallet.getAction());
            time.setText(wallet.getTime());
            //money
            if(wallet.getAdded()){
                money.setTextColor(getResources().getColor(R.color.green800));
                money.setText("+"+wallet.getMoney());
            }
            else {
                money.setTextColor(getResources().getColor(R.color.red400));
                money.setText("-"+wallet.getMoney());
            }

        }
    }

    class Wallet{
        String action, money, time, type, tid;
        boolean added;

        public Wallet(String action, String money, String time, String type, String tid, boolean added) {
            this.action = action;
            this.money = money;
            this.time = time;
            this.added = added;
            this.type = type;
            this.tid = tid;
        }

        public String getAction() {
            return action;
        }

        public String getMoney() {
            return money;
        }

        public String getTime() {
            return time;
        }

        public boolean getAdded(){return added;}

        public String getType() {
            return type;
        }

        public String getTid() {
            return tid;
        }
    }

}
