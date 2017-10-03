package jp.techacademy.kusumi.daiju.taskapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static io.realm.Realm.getDefaultInstance;


public class InputActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String EXTRA_CATEGORY = "jp.techacademy.kusumi.daiju.taskapp.Category";

    private int mYear, mMonth, mDay, mHour, mMinute;
    private Button mButtonCategory, mDateButton, mTimeButton, mNewCategoryButton, mEditCategoryButton;
    private EditText mTitleEdit, mContentEdit;
    private Task mTask = null;
    private Realm mRealm;
    private String newCategory = "";
    private String selectedCategory = "";
    private String errorTitle, errorContent;
    private int position;
    private Intent intent;
    private AlertDialog dialog;
    private int selectedItem;

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
                            mDateButton.setText(dateString);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
                            mTimeButton.setText(timeString);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    };

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addTask();

        }
    };

    private View.OnClickListener mOnNewCategoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            intent = new Intent(InputActivity.this, CategoryActivity.class);
            int requestCode = 1;
            startActivityForResult(intent, requestCode);
        }
    };

    private View.OnClickListener mOnEditCategoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            intent = new Intent(InputActivity.this, CategoryEditActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    newCategory = bundle.getString(EXTRA_CATEGORY);

                } else {
                    newCategory = null;
                }
            break;
        default:
             break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        //OnCreate時にinitailize()メソッドを実行する

        initialize();

    }
    @Override
    public void onResume() {
        super.onResume();

        selectedCategory = "";

        initialize();

    }


    private void initialize() {
        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI部品の設定
        mButtonCategory = (Button) findViewById(R.id.buttonCategory);
        mButtonCategory.setOnClickListener(this);
        mDateButton = (Button) findViewById(R.id.buttonEdit);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton = (Button) findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        mNewCategoryButton = (Button) findViewById(R.id.new_category_button);
        mNewCategoryButton.setOnClickListener(mOnNewCategoryClickListener);
        mEditCategoryButton = (Button) findViewById(R.id.edit_category_button);
        mEditCategoryButton.setOnClickListener(mOnEditCategoryClickListener);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);
        mTitleEdit = (EditText) findViewById(R.id.title_edit_text);
        mContentEdit = (EditText) findViewById(R.id.content_edit_text);

        mButtonCategory.setText(getString(R.string.category_status_select));



        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する

        Intent intent = getIntent();
        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);

        mRealm = Realm.getDefaultInstance();
        mTask = mRealm.where(Task.class).equalTo("id", taskId).findFirst();
        mRealm.close();

        if (mTask == null) {
            // 新規作成の場合
            if (newCategory != null && newCategory != ""){
                mButtonCategory.setText(newCategory);
            }
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

        } else {
            // 更新の場合
            //Categoryのpositionを取得し、表示する際にセットしておく
            mButtonCategory.setText(mTask.getCategory());

            //その他の項目をセットする
            mTitleEdit.setText(mTask.getTitle());
            mContentEdit.setText(mTask.getContents());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mTask.getDate());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            String dateString = mYear + "/" + String.format("%02d", (mMonth + 1)) + "/" + String.format("%02d", mDay);
            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);
        }
    }

 private void addTask() {
        //エラーメッセージを入力不備の状況に応じて定義
         String title = mTitleEdit.getText().toString();
         String content = mContentEdit.getText().toString();

        //1.タイトルの入力不備チェック
        if (title.length() == 0) {
            errorTitle = "「タイトル」";
        } else {
            errorTitle = null;
        }

         //2.内容の入力不備チェック
         if (content.length() == 0) {
             errorContent = "「内容」";
         } else {
             errorContent = null;
         }

        if (title.length() == 0  || content.length() == 0) {
            errorInputDialog();

        } else {
            mRealm = Realm.getDefaultInstance();

            mRealm.beginTransaction();

            if (mTask == null) {
                // 新規作成の場合
                mTask = new Task();

                RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll();

                int identifier;
                if (taskRealmResults.max("id") != null) {
                    identifier = taskRealmResults.max("id").intValue() + 1;
                } else {
                    identifier = 0;
                }
                mTask.setId(identifier);
            }

            if (mButtonCategory.equals(getString(R.string.category_status_select))) {
                mTask.setCategory("");
            } else {
                mTask.setCategory(mButtonCategory.getText().toString());
            }
            mTask.setTitle(title);
            mTask.setContents(content);
            GregorianCalendar calendar = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
            Date date = calendar.getTime();
            mTask.setDate(date);

            mRealm.copyToRealmOrUpdate(mTask);
            mRealm.commitTransaction();

            mRealm.close();

            Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
            resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask.getId());
            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(this, mTask.getId(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);

            finish();
        }
    }

    private void errorInputDialog() {
        AlertDialog.Builder alertDialogInputError;
        alertDialogInputError = new AlertDialog.Builder(this);
        alertDialogInputError.setTitle("タスクの入力エラー");
        alertDialogInputError.setMessage(errorTitle + errorContent + "に入力不備があります。入力内容を確認してください。");
        alertDialogInputError.setPositiveButton("OK", null);
        alertDialogInputError.show();

    }

    @Override
    public void onClick(View view) {

        mRealm = getDefaultInstance();

        //Realmデータベースから、すべてのデータのうちカテゴリの項目を重複なく取得する
        RealmResults<Category> categoryRealmResults = mRealm.where(Category.class).distinct("category").sort("category", Sort.ASCENDING);
        int categoryNumber = categoryRealmResults.size();

        ArrayList<String> mCategoryList = new ArrayList<>();

        for (int count = 0; count < categoryNumber; count++) {
            String mCategory = categoryRealmResults.get(count).getCategory();
            mCategoryList.add(mCategory);
        }

        final String[] items = mCategoryList.toArray(new String[mCategoryList.size()]);
        items[0] = "指定なし";

        // デフォルトでチェックされているアイテム
        int defaultItem = selectedItem;

        android.app.AlertDialog.Builder selectCategoryDialog = new android.app.AlertDialog.Builder(this);
        selectCategoryDialog.setTitle("カテゴリの選択");
        selectCategoryDialog.setSingleChoiceItems(items, defaultItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    selectedItem = 0;
                    selectedCategory = "";
                } else {
                    selectedItem = i;
                    selectedCategory = items[i];
                }
            }
        });
        selectCategoryDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mButtonCategory.setText(selectedCategory);
            }
        });
        selectCategoryDialog.setNegativeButton("Cancel", null);
        selectCategoryDialog.show();
        mRealm.close();
    }
}
