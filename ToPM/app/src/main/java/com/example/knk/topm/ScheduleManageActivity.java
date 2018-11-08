package com.example.knk.topm;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.knk.topm.Object.InputException;
import com.example.knk.topm.Object.Movie;
import com.example.knk.topm.Object.MovieSchedule;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class ScheduleManageActivity extends AppCompatActivity {

    ListView dayScheduleList;       // 영화 스케줄 출력
    Spinner movieSpinner;           // 등록된 영화 목록 Spinner
    ArrayList<Movie> movieData;     // 서버에 저장된 영화 데이터 받아오는 배열
    ArrayAdapter<Movie> adapter;    // 스피너에 연결하는 어댑터

    Button screenBtn;
    Button dateBtn;
    Button timeBtn;

    Movie selectedMovie; // 선택한 영화
    int screenNum; // 선택한 상영관
    int startHour; // 시작 시간
    int startMin; // 시작 분
    int showYear; // 상영 년도
    int showMonth; // 상영 월
    int showDay; // 상영 일

    // 데이터베이스
    private FirebaseDatabase firebaseDatabase;      //firebaseDatabase
    private DatabaseReference rootReference;        //rootReference
    private DatabaseReference scheduleReference;        //rootReference
    private static String movie_ref = "movie";
    private static String schedule_ref = "schedule";


    /* 상수 */
    final static int SCREENS = 5;    // 상영관 5개
    final int DATE_DIALOG = 1111; // 날짜 다이어로그
    final int TIME_DIALOG = 2222; // 시간 다이어로그


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_manage);

        init();
    }

    public void init() {
        // 객체 생성
        dayScheduleList = (ListView) findViewById(R.id.dayScheduleList);
        movieSpinner = (Spinner) findViewById(R.id.movieSpinner);
        movieData = new ArrayList<Movie>();
        adapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, movieData);
        movieSpinner.setAdapter(adapter);

        // 변수 초기화
        selectedMovie = null;
        screenNum = -1; // 상영관 번호 선택 전에는 -1, 선택 후 1~5
        startHour = -1; // 시작 시간
        startMin = -1; // 시작 분
        showYear = -1; // 상영 년도
        showMonth = -1; // 상영 월
        showDay = -1; // 상영 일

        // 영화 정보를 서버에서 받아옵시다.
        // 데이터베이스
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootReference = firebaseDatabase.getReference(movie_ref);
        scheduleReference = firebaseDatabase.getReference(schedule_ref);

        // 데이터 베이스에서 정보 받아와서 movieData에 저장
        rootReference.addListenerForSingleValueEvent(new ValueEventListener() { // 최초 한번 실행되고 삭제되는 콜백
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 모든 데이터 가지고 오기
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    Movie movie = data.getValue(Movie.class);

                    if(movie == null)
                        Toast.makeText(ScheduleManageActivity.this, "null", Toast.LENGTH_SHORT).show();
                    else {
                        movieData.add(movie); // movieData에 삽입
                        // Toast.makeText(MovieManageActivity.this, movie.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
                adapter.notifyDataSetChanged(); // 데이터 갱신 통지
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        movieSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMovie = movieData.get(position); // selectedMovie에 저장
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void selectScreenBtn(View view) {
        // 상영관 선택 다이어로그 (NumberPicker 다이어로그)

        final Dialog dialog = new Dialog(this);
        dialog.setTitle("상영관 선택");
        dialog.setContentView(R.layout.numberpicker_dialog);

        Button setBtn = (Button) dialog.findViewById(R.id.setButn);
        // Button cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);

        final NumberPicker screenPicker = (NumberPicker) dialog.findViewById(R.id.screenPicker);
        screenPicker.setMaxValue(SCREENS); // max value SCREENS (5)
        screenPicker.setMinValue(1);   // min value 1
        screenPicker.setWrapSelectorWheel(false);
        // screenPicker.setOnValueChangedListener((NumberPicker.OnValueChangeListener) this);

        setBtn.setOnClickListener(new View.OnClickListener() {
            // 확인 버튼 클릭
            @Override
            public void onClick(View v) {
                // 상영관 번호 저장
                screenNum = screenPicker.getValue();
                dialog.dismiss(); // 다이어로그 파괴
            }
        });
//        cancelBtn.setOnClickListener(new View.OnClickListener() {
//            // 취소 버튼 클릭
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss(); // 그냥 끔
//            }
//        });
        dialog.show();
    }

    public void inputDateBtn(View view) {
        // 날짜 선택 버튼 (DatePicker 다이어로그)
        showDialog(DATE_DIALOG);
    }

    public void inputStartTimeBtn(View view) {
        // 시작 시간 입력 (TimePicker 다이어로그)
        showDialog(TIME_DIALOG);
    }

    public void scheduleComplete(View view) {
        // 완료 버튼 클릭
        // Input 검사
        int[] args = new int[6];
        args[0] = showYear;
        args[1] = showMonth;
        args[2] = showDay;
        args[3] = startHour;
        args[4] = startMin;
        args[5] = screenNum;

        if(inputCheck(args)) {
            // 검사 통과
            // 데이터 베이스에 업로드
            // String movieTitle, String screenNum, Date screeningDate, int startHour, int startMin
            Date screeningDate = new Date(showYear, showMonth, showDay); // Date로 변환

            MovieSchedule movieSchedule = new MovieSchedule(selectedMovie.getTitle(), String.valueOf(screenNum), screeningDate, startHour, startMin); // 객체 생성후
            scheduleReference.push().setValue(movieSchedule); // 일단 push로 하는데 기본키 뭘로 해야할지 생각해봐야 할듯
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
        }
        else {
            // 틀린 점이 있음
            try {
                throw new InputException();
            } catch (InputException e) {
                Toast.makeText(this, "입력을 확인하세요.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public boolean inputCheck(int[] args) {
        // args가 모두 -1이 아니여야만 true 반환
        for(int i=0; i<args.length; i++) {
            if(args[i] == -1)
                return false;
        }

        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // 입력 값에 따라 다른 다이어로그 생성

        Date currentDate = new Date(); // 오늘 날짜

        switch(id) {
            case DATE_DIALOG: // 날짜 다이어로그
                DatePickerDialog dateDialog = new DatePickerDialog
                        (this, // 현재화면의 제어권자
                                new DatePickerDialog.OnDateSetListener() {
                                    public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
                                        Toast.makeText(getApplicationContext(),year+"년 "+(monthOfYear+1)+"월 "+dayOfMonth+"일 을 선택했습니다",
                                                Toast.LENGTH_SHORT).show();
                                        // 설정 후 변수에 저장
                                        showYear = year;
                                        showMonth = monthOfYear;
                                        showDay = dayOfMonth;
                                    }
                                }
                                , // 사용자가 날짜설정 후 다이얼로그 빠져나올때
                                //    호출할 리스너 등록
                                currentDate.getYear(), currentDate.getMonth(), currentDate.getDay()); // 기본값 연월일
                return dateDialog;

            case TIME_DIALOG: // 시간 다이어로그
                TimePickerDialog timeDialog =
                        new TimePickerDialog(this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        Toast.makeText(getApplicationContext(), hourOfDay +"시 " + minute+"분 을 선택했습니다",
                                                Toast.LENGTH_SHORT).show();
                                        // 설정 후 변수에 저장
                                        startHour = hourOfDay;
                                        startMin = minute;
                                    }
                                }, // 값설정시 호출될 리스너 등록
                                0,0, false); // 기본값 시분 등록
                // true : 24 시간(0~23) 표시
                // false : 오전/오후 항목이 생김
                return timeDialog;
        }
        return super.onCreateDialog(id);
    }
}
