package jp.techacademy.kusumi.daiju.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String EXTRA_TASK = "jp.techacademy.kusumi.daiju.taskapp.TASK";

    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView();
        }
    };
    private ListView mListView;
    private TaskAdapter mTaskAdapter;
//    private Spinner mSpinner;
    private Button mButton;
    private String presentCategory, selectedCategory;
    private RealmResults<Task> taskRealmResults;
    int selectedItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);

        // Spinnerの設定
//        mSpinner = (Spinner) findViewById(R.id.spinner1);

        // Buttonの設定
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(this);


        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する

                final Task task = (Task) parent.getAdapter().getItem(position);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        selectedCategory = "";
                        reloadListView();

                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

////        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
////            @Override
////            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
////                selectedCategory = mSpinner.getSelectedItem().toString();
////            }
////
////            @Override
////            public void onNothingSelected(AdapterView<?> adapterView) {
////            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presentCategory = "";
        selectedCategory = "";
        reloadListView();

    }

    private void reloadListView() {

        //Realmデータベースから、すべてのデータのうちカテゴリの項目を重複なく取得する
//        RealmResults<Category> categoryRealmResults = mRealm.where(Category.class).distinct("category").sort("category", Sort.ASCENDING);
//        int categoryNumber = categoryRealmResults.size();
//
//        ArrayList<String> mCategoryList = new ArrayList<>();
//
//        for (int count = 0; count < categoryNumber; count++) {
//            String mCategory = categoryRealmResults.get(count).getCategory();
//            mCategoryList.add(mCategory);
//        }
//
//        // Spinner用のAdapterの設定
//        ArrayAdapter<String> mCategoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mCategoryList);
//        mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        // SpinnerにAdapterをセット
//        mSpinner.setAdapter(mCategoryAdapter);

        // Spinnerで選択された条件にもとづいて、Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        if (selectedCategory == "" || selectedCategory == null) {
            taskRealmResults = mRealm.where(Task.class).findAllSorted("date", Sort.DESCENDING);

        } else {
            taskRealmResults = mRealm.where(Task.class).equalTo("category", selectedCategory).findAllSorted("date", Sort.DESCENDING);

        }

        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();

        //selectedCategoryを初期化
        presentCategory = selectedCategory;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    @Override
    public void onClick(View view) {


        //Realmデータベースから、すべてのデータのうちカテゴリの項目を重複なく取得する
        RealmResults<Category> categoryRealmResults = mRealm.where(Category.class).distinct("category").sort("category", Sort.ASCENDING);
        int categoryNumber = categoryRealmResults.size();

        if (categoryNumber > 0) {

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
                reloadListView();
            }
        });
        selectCategoryDialog.setNegativeButton("Cancel", null);
        selectCategoryDialog.show();

        } else {

        }
    }
}