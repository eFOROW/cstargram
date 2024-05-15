package kr.ac.wsu.cstargram;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseDatabase database;
    private final List<DB_class> DB_class = new ArrayList<>();

    //sliding panel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();

        recyclerView = (RecyclerView) findViewById(R.id.main_RecyclerView);

        feed_init(); //피드 초기화(DB에서 불러오기)

        ImageView Dm_Btn = (ImageView) findViewById(R.id.main_dm_imageView);
        Dm_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });

        //댓글 슬라이드 패널 구현
        //SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.comment_slide_layout);
    }

    private void feed_init() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final RecyclerViewAdapter RecyclerViewAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(RecyclerViewAdapter);

        database.getReference().child("feed").orderByChild("idKey").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DB_class.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    DB_class db_class = snapshot.getValue(DB_class.class); //값을 DB클래스의 방식으로 정리
                    DB_class.add(db_class);
                }
                RecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);

            return new CustomViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((CustomViewHolder) holder).nickname_tv.setText(DB_class.get(position).nickname);
            ((CustomViewHolder) holder).place_tv.setText(DB_class.get(position).place);
            ((CustomViewHolder) holder).ft_nickname_tv.setText(DB_class.get(position).nickname + " · ");
            ((CustomViewHolder) holder).ft_description_tv.setText(DB_class.get(position).description);

            String imgUrl = DB_class.get(position).iv_Url;
            Glide.with(MainActivity.this).load(imgUrl).placeholder(getDrawable(R.drawable.cstar_story_my)).into(((CustomViewHolder) holder).item_iv);
        }

        @Override
        public int getItemCount() {
            return DB_class.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView nickname_tv, place_tv, ft_nickname_tv, ft_description_tv;
            ImageView item_iv;

            public CustomViewHolder(View view) {
                super(view);
                nickname_tv = (TextView) view.findViewById(R.id.item_nickname_textView);
                place_tv = (TextView) view.findViewById(R.id.item_place_textView);
                ft_nickname_tv = (TextView) view.findViewById(R.id.itme_foot_nickname_textView);
                ft_description_tv = (TextView) view.findViewById(R.id.itme_foot_description_textView);
                item_iv = (ImageView) view.findViewById(R.id.item_imageView);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MainActivity.this, view.get, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

}