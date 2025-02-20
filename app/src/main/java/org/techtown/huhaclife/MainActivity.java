package org.techtown.huhaclife;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageView QR_code,userprofile,info,calender;
    private TextView username,userpoint;
    private File file;
    private FirebaseUser firebaseUsser= FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    public DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    public String uid;
    private UserInfo userInfo;
    private Button test;
    public LottieAnimationView plant;
    private float plantpoint;



    //뒤로가기 버튼 두번 누르면 종료
    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("종료");
        builder.setMessage("take-out컵 버릴 때 또 만나요!");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                moveTaskToBack(true) ;//테스크를 백그라운드로 이동
                finishAndRemoveTask();//액티비티 종료+테스크 리스트에서 지우기
                System.exit(0);

            }
        });
        builder.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        QR_code = (ImageView) findViewById(R.id.qr);
        userprofile = (ImageView) findViewById(R.id.iv_userprofile);
        username = (TextView) findViewById(R.id.tv_username);
        userpoint = (TextView) findViewById(R.id.tv_userpoint);
        info=(ImageButton)findViewById(R.id.ib_info);
        calender=(ImageButton)findViewById(R.id.ib_calender);
        databaseReference = firebaseDatabase.getReference();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
        plant=(LottieAnimationView)findViewById(R.id.plant);

        uid = firebaseUsser.getUid().trim();


        //사용자 초기 씨앗선택하는 부분
        test=(Button)findViewById(R.id.btn_test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SeedDialog dialog=new SeedDialog(MainActivity.this, new SeedDialog.SeedDialogListener() {
                    @Override
                    public void ContentTranslate(int seednumber) {
                        if(seednumber>0&&seednumber<8) {
                            String plantname = "plant" + seednumber + ".json";
                            plant.setAnimation(plantname);
                        }
                    }
                });
                dialog.setContentView(R.layout.dialog_seed);
                dialog.show();


            }

        });

        //알람버튼
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,AlarmActivity.class);
                startActivity(intent);
            }
        });
        //캘린더
        calender.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,Calender.class);
                startActivity(intent);
            }
        });


        //사용자 프로필 둥글게 처리
        userprofile.setBackground(new ShapeDrawable(new OvalShape()));
        userprofile.setClipToOutline(true);

        if(uid!=null){
            System.out.println("여기가 반복해서 실행되나?");
            storageReference.child("User/"+uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(MainActivity.this).load(uri).into(userprofile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }
        databaseReference.child("User").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo = snapshot.getValue(UserInfo.class);
                username.setText(userInfo.getName());
                userpoint.setText(String.valueOf(userInfo.getPoint()));
                int value= userInfo.getPoint();
                plantpoint= (float)(value*0.01);
                growplant();
                qrcreate(userInfo.getEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        //사용자 프로필
        userprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,UserPageActivity.class);
                startActivity(intent);

            }
        });

    }


    //qr코드 생성
    private void qrcreate(String useremail){
        MultiFormatWriter multiFormatWriter=new MultiFormatWriter();
        try {
            int qrColor=0xFF6E917A;
            int qrbackroundColor=0xFFF3D6AB;

            BitMatrix bitMatrix=multiFormatWriter.encode(uid, BarcodeFormat.QR_CODE,200,200);
            Bitmap bitmap=Bitmap.createBitmap(bitMatrix.getWidth(),bitMatrix.getHeight(),Bitmap.Config.ARGB_8888);
            for(int x=0; x<200;x++){
                for(int y=0;y<200;y++){
                    bitmap.setPixel(x,y,bitMatrix.get(x,y)? qrColor:qrbackroundColor);
                }
            }
            QR_code.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    //포인트에 따라 식물 성장
    public void growplant(){
        ValueAnimator animator = ValueAnimator.ofFloat(0f,plantpoint).setDuration(1000);
        animator.addUpdateListener(valueAnimator -> {
            plant.setProgress((Float)valueAnimator.getAnimatedValue());
        });
        animator.start();
    }
}