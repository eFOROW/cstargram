package kr.ac.wsu.cstargram;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView, cmt_recyclerView;
    FirebaseDatabase database;
    private final List<DB_class> DB_class = new ArrayList<>();
    private final List<DB_cmt_class> DB_cmt_class = new ArrayList<>();
    SlidingUpPanelLayout commentLayout;
    EditText cmt_eT;
    ImageView cmt_Btn;
    int id;

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

        //댓글 슬라이드 패널 초기화
        commentLayout = (SlidingUpPanelLayout) findViewById(R.id.main);
        cmt_recyclerView = (RecyclerView) findViewById(R.id.comment_RecyclerView);
        cmt_eT = (EditText) findViewById(R.id.comment_editText);
        cmt_Btn = (ImageView) findViewById(R.id.comment_Send_imageView);

        feed_init(); //초기 세팅(피드 등)

        ImageView Dm_Btn = (ImageView) findViewById(R.id.main_dm_imageView);
        Dm_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });

        cmt_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    public void comment_update(){

    }

    private void feed_init() {
        //피드 초기 세팅
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final RecyclerViewAdapter RecyclerViewAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(RecyclerViewAdapter);

        database.getReference().child("feed").orderByChild("idKey").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DB_class.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
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
            ((CustomViewHolder) holder).count_tv.setText("댓글 " + DB_class.get(position).comment_count + "개 모두 보기");

            String imgUrl = DB_class.get(position).iv_Url;
            Glide.with(MainActivity.this).load(imgUrl).placeholder(getDrawable(R.drawable.cstar_story_my)).into(((CustomViewHolder) holder).item_iv);
        }

        @Override
        public int getItemCount() {
            return DB_class.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView nickname_tv, place_tv, ft_nickname_tv, ft_description_tv, count_tv;
            ImageView item_iv;

            public CustomViewHolder(View view) {
                super(view);
                nickname_tv = (TextView) view.findViewById(R.id.item_nickname_textView);
                place_tv = (TextView) view.findViewById(R.id.item_place_textView);
                ft_nickname_tv = (TextView) view.findViewById(R.id.itme_foot_nickname_textView);
                ft_description_tv = (TextView) view.findViewById(R.id.itme_foot_description_textView);
                item_iv = (ImageView) view.findViewById(R.id.item_imageView);
                count_tv = (TextView) view.findViewById(R.id.item_comment_count_textView);

                count_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        id = getAdapterPosition();
                        comment_init();
                        if (commentLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                            commentLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                            //Toast.makeText(MainActivity.this, DB_class.get(getAdapterPosition()).idKey+"", Toast.LENGTH_SHORT).show();
                        } else {
                            commentLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
                    }
                });

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MainActivity.this, view.get, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    protected void comment_init() {
        //댓글 초기 세팅
        View comment_Et = (View) findViewById(R.id.comment_editText);
        View comment_send_Btn = (View) findViewById(R.id.comment_Send_imageView);

        cmt_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final cmt_RecyclerViewAdapter cmt_RecyclerViewAdapter = new cmt_RecyclerViewAdapter();
        cmt_recyclerView.setAdapter(cmt_RecyclerViewAdapter);


        //어떤 피드의 댓글 인지 구분 필요 => DB_class.get(getAdapterPosition()).idKey
        //int id 전역변수 ex)210513617059659 => key 값

        database.getReference().child("feed").child(DB_class.get(id).path_key+"/comment").orderByChild("time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DB_cmt_class.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DB_cmt_class dbCmtClass = snapshot.getValue(DB_cmt_class.class);
                    DB_cmt_class.add(dbCmtClass);
                }
                cmt_RecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    public class cmt_RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item_layout, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((CustomViewHolder) holder).nickname_tv.setText(DB_cmt_class.get(position).nickname);
            ((CustomViewHolder) holder).message_tv.setText(DB_cmt_class.get(position).message);


            int time = getCurrentTime() - DB_cmt_class.get(position).time;
            ((CustomViewHolder) holder).time_tv.setText(time == 0 ? " • 오늘" : " • " + time +"일전");
        }

        @Override
        public int getItemCount() {;
            return DB_cmt_class.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView nickname_tv, message_tv, time_tv;

            public CustomViewHolder(View view) {
                super(view);
                nickname_tv = (TextView) view.findViewById(R.id.comment_item_nickname_textView);
                message_tv = (TextView) view.findViewById(R.id.comment_item_message_textView);
                time_tv = (TextView) view.findViewById(R.id.comment_item_time_textView);
            }
        }
    }

    public static int getCurrentTime() {
        long now = System.currentTimeMillis();
        Date currentTime = new Date(now);
        DateFormat dateFormat = new SimpleDateFormat("yyMMdd");

        return Integer.parseInt(dateFormat.format(currentTime));
    }
}