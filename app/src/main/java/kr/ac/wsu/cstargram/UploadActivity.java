package kr.ac.wsu.cstargram;

import static android.view.View.GONE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadActivity extends AppCompatActivity {

    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    /*갤러리 이미지 선택용 변수*/
    Button imgpick_Btn;
    boolean img = false;
    ImageView imgView;
    Uri imageUri;
    String get_imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Button upload_Btn = (Button) findViewById(R.id.dm_Upload_Button);
        EditText nickname_et = (EditText) findViewById(R.id.dm_nickname_editText);
        EditText place_et = (EditText) findViewById(R.id.dm_place_editText);
        EditText description_et = (EditText) findViewById(R.id.dm_description_editText);
        EditText imgUrl_et = (EditText) findViewById(R.id.dm_imgUrl_editText);
        imgpick_Btn = (Button) findViewById(R.id.dm_imagepick_Button);
        imgView = (ImageView) findViewById(R.id.dm_imageView);


        String time = getCurrentTime();

        DatabaseReference myRef = database.getReference("idKey");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String id = dataSnapshot.getValue(String.class);
                int idKey = Integer.valueOf(id).intValue() - 1;

                /*Upload 과정*/
                upload_Btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference myRef = database.getReference("feed").push();
                        myRef.child("nickname").setValue(nickname_et.getText().toString());
                        myRef.child("place").setValue(place_et.getText().toString());
                        myRef.child("description").setValue(description_et.getText().toString());

                        if(!img) myRef.child("iv_Url").setValue(imgUrl_et.getText().toString());
                        else myRef.child("iv_Url").setValue(get_imgUrl);

                        myRef.child("up_time").setValue(getCurrentTime2());
                        myRef.child("comment_count").setValue(0+"");
                        myRef.child("idKey").setValue(idKey);
                        myRef = database.getReference("idKey");
                        myRef.setValue(idKey+"");
                        Toast.makeText(UploadActivity.this, "업로드 완료", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        //이미지 버튼
        imgpick_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!img){
                    //이미지 선택
                    imgUrl_et.setText("");
                    imgUrl_et.setVisibility(GONE);
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityResult.launch(intent);
                } else {
                    //이미지 업로드
                    StorageReference storageRef = mStorage.getReference().child("image"+imageUri.getLastPathSegment()+getCurrentTime());
                    UploadTask uploadTask = storageRef.putFile(imageUri); // 업로드할 파일과 업로드할 위치 설정
                    //파일 업로드 시작
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    get_imgUrl = uri.toString();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //업로드 실패 시 동작
                            Toast.makeText(UploadActivity.this, "업로드 실패", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    imgpick_Btn.setVisibility(GONE);
                }
            }
        });

    }

    public static String getCurrentTime() {
        long now = System.currentTimeMillis();
        Date currentTime = new Date(now);
        DateFormat dateFormat = new SimpleDateFormat("yyMMddHHMMSSMS");

        return dateFormat.format(currentTime);
    }
    public static String getCurrentTime2() {
        long now = System.currentTimeMillis();
        Date currentTime = new Date(now);
        DateFormat dateFormat = new SimpleDateFormat("yy.MM.dd.HH:MM:SS");

        return dateFormat.format(currentTime);
    }

    //이미지 가져오기 과정(갤러리에서 bitmap으로 서버는 아직)
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if ( result.getResultCode() == RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    imgView.setVisibility(View.VISIBLE);
                    imgpick_Btn.setText("사진 업로드");
                    img = true;
                    imgView.setImageBitmap(bitmap);	//이미지를 띄울 이미지뷰 설정
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}