package com.example.knk.topm;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.knk.topm.Object.InputException;
import com.example.knk.topm.Object.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MovieManageActivity extends AppCompatActivity {

    List<Movie> movieData;
    ListView movieList;

    EditText editTitle;
    EditText editDir;
    EditText editRun;

    String title;
    String director;
    String runningTime;

    // 데이터베이스
    private FirebaseDatabase firebaseDatabase;      //firebaseDatabase
    private DatabaseReference rootReference;        //rootReference
    private static String movie_ref = "movie";        //레퍼런스할 이름 - 여기서는 회원가입이므로 user를 root로 참조해 그 아래에 데이터 추가.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_manage);

        init();

    }

    public void init() {

        // ListView 초기화 및 어댑터 연결
        movieList = (ListView) findViewById(R.id.movieList);
        MyAdapter adapter =  new MyAdapter(movieData);
        movieList.setAdapter(adapter);

        // 입력 위젯 초기화
        editTitle = (EditText) findViewById(R.id.edittxttitle);
        editDir = (EditText) findViewById(R.id.edittxtdir);
        editRun = (EditText)findViewById(R.id.edittxtruntime);

        // 데이터베이스
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootReference = firebaseDatabase.getReference(movie_ref);

        // 데이터 베이스에서 정보 받아와서 List에 출력하기
        rootReference.addListenerForSingleValueEvent(new ValueEventListener() { // 최초 한번 실행되고 삭제되는 콜백
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 모든 데이터 가지고 오기
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    Movie movie = data.getValue(Movie.class);
                    // null 아니면서 null object reference 뜸 ㅅ ㅣ ㅂ ㅏ ㄹ ㅏ
                    if(movie == null)
                        Toast.makeText(MovieManageActivity.this, "nullllll", Toast.LENGTH_SHORT).show();
                    else {
                        movieData.add(movie);
                        Toast.makeText(MovieManageActivity.this, movie.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void completebtn(View view) {

        // 입력 값 받아옴
        title = editTitle.getText().toString();
        director = editDir.getText().toString();
        runningTime = editRun.getText().toString();

        if(title.length() <= 0 || director.length() <= 0 || runningTime.length() <= 0) {
            // 입력이 누락된 경우
            try {
                throw new InputException();
            } catch (InputException e) {
                Toast.makeText(this, "모두 입력하세요.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        else {
            // 정상 입력
            // 데이터 베이스에 입력
            int runTime = Integer.parseInt(runningTime);
            Movie movie = new Movie(runTime, title, director, 0); // 객체 생성 후

            rootReference.child(movie.getTitle()).setValue(movie);

            // 다시 관리자 메인으로 이동
            this.finish();
        }
    }

    public boolean deleteCheck(){
        // detail method

        return false;
    }


}

