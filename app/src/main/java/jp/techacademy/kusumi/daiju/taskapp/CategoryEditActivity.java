package jp.techacademy.kusumi.daiju.taskapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static io.realm.Realm.getDefaultInstance;

public class CategoryEditActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEditText;
    private Button mButtonEdit, mButtonDelete, mButtonCategory;
    private String selectedCategory = "";
    private String revisedCategory = "";

    private Task mTask;
    private Category mCategory;
    private Realm mRealm;
    private int count;
    private int selectedItem;

    private int screenStatus;
    final int STATUS_SELECT = 0;
    final int STATUS_EDIT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_edit);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //部品の設定
        mEditText = (EditText) findViewById(R.id.category_edit_text);
//        mSpinner = (Spinner) findViewById(R.id.spinnerEdit);
        mButtonEdit = (Button) findViewById(R.id.buttonEdit);
        mButtonDelete = (Button) findViewById(R.id.buttonDelete);
        mButtonCategory = (Button) findViewById(R.id.buttonCategory);

        mButtonEdit.setOnClickListener(this);
        mButtonDelete.setOnClickListener(this);
        mButtonCategory.setOnClickListener(this);


        mButtonEdit.setVisibility(View.INVISIBLE);
        mButtonDelete.setVisibility(View.INVISIBLE);
        mEditText.setVisibility(View.INVISIBLE);

        //画面のステータスをSELECTモードにする
        screenStatus = STATUS_SELECT;


        //SoinnerのOnClick設定
//        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                if (mSpinner.getSelectedItem().toString() != null || mSpinner.getSelectedItem().toString() != "") {
//                    selectedCategory = mSpinner.getSelectedItem().toString();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//            }
//        });


    }

    @Override
    public void onClick(View view) {
        int buttonId = view.getId();

        switch (buttonId) {
            case R.id.buttonCategory:
                mRealm = getDefaultInstance();
                RealmResults<Category> categoryRealmResults = mRealm.where(Category.class).distinct("category").sort("category", Sort.ASCENDING);

                int categoryNumber = categoryRealmResults.size();

                ArrayList<String> mCategoryList = new ArrayList<>();

                for (int count = 1; count < categoryNumber; count++) {
                    String mCategory = categoryRealmResults.get(count).getCategory();
                    mCategoryList.add(mCategory);
                }

                mRealm.close();

                final String[] items = mCategoryList.toArray(new String[mCategoryList.size()]);

                // デフォルトでチェックされているアイテム
                int defaultItem = 0;
                selectedCategory = items[0];

                final AlertDialog.Builder selectCategoryDialog = new AlertDialog.Builder(this);
                selectCategoryDialog.setTitle("カテゴリの選択");
                selectCategoryDialog.setSingleChoiceItems(items, defaultItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedItem = i;
                        selectedCategory = items[i];

                    }
                });
                selectCategoryDialog.setPositiveButton("削除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (selectedCategory != null && selectedCategory != "") {
                            categoryDelete();
                        } else {

                        }
                    }
                });
                selectCategoryDialog.setNeutralButton("編集", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (selectedCategory != null && selectedCategory != "") {
                            editChange();
                            screenStatus = STATUS_EDIT;
                        } else {

                        }
                    }
                });
                selectCategoryDialog.setNegativeButton("Cancel", null);
                selectCategoryDialog.show();
                break;

            case R.id.buttonEdit:
                revisedCategory = mEditText.getText().toString();

                if (revisedCategory != null && revisedCategory != "") {
                    categoryEdit();

                }
                break;
            case R.id.buttonDelete:
                selectChange();
                break;
            }
    }

    private void editChange() {
        mEditText.setVisibility(View.VISIBLE);
        mButtonCategory.setVisibility(View.INVISIBLE);
        mButtonEdit.setVisibility(View.VISIBLE);
        mButtonDelete.setVisibility(View.VISIBLE);

        //選択されたカテゴリ
        mEditText.setText(selectedCategory);
    }

    private void categoryDelete() {
        AlertDialog.Builder alertDialogDelete = new AlertDialog.Builder(this);
        alertDialogDelete.setTitle("カテゴリの削除");
        alertDialogDelete.setMessage("カテゴリ「" + selectedCategory + "」で登録されているタスクも削除しますか？");
        alertDialogDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Realmを開始
                mRealm = Realm.getDefaultInstance();

                //Categoryクラスを削除
                mCategory = new Category();
                RealmResults<Category> categoryRealmResults = mRealm.where(Category.class).findAll();
                int categoryNumber = categoryRealmResults.size();

                count = 0;

                while (count < categoryNumber - 1){


                    mCategory = categoryRealmResults.get(count);
                    if (mCategory.getCategory().equals(selectedCategory)) {
                        mRealm.beginTransaction();
                        categoryRealmResults.deleteFromRealm(count);
                        mRealm.commitTransaction();

                    }
                    count++;
                }

                //Taskの削除
                mTask = new Task();

                RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll();
                    int taskNumber = taskRealmResults.size();

                count = 0;

                while (count < taskNumber){
                    mTask = taskRealmResults.get(count);

                    if (mTask.getCategory().equals(selectedCategory)) {
                        mRealm.beginTransaction();
                        taskRealmResults.deleteFromRealm(count);
                        mRealm.commitTransaction();

                    }
                    count++;
                }

                mRealm.close();

                //前画面に戻る
                Intent intent = new Intent(CategoryEditActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        alertDialogDelete.setNeutralButton("Category Only", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Realmを開始
                mRealm = Realm.getDefaultInstance();


                //Categoryクラスを削除
                mCategory = new Category();
                RealmResults<Category> categoryRealmResults = mRealm.where(Category.class).findAll();
                int categoryNumber = categoryRealmResults.size();

                count = 0;

                while (count < categoryNumber){
                    mCategory = categoryRealmResults.get(count);

                    if (mCategory.getCategory().equals(selectedCategory)) {
                        mRealm.beginTransaction();
                        categoryRealmResults.deleteFromRealm(count);
                        mRealm.commitTransaction();
                    }
                    count++;
                }


                mTask = new Task();

                RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll();
                int taskNumber = taskRealmResults.size();

                count = 0;


                while (count < taskNumber){
                   mTask = taskRealmResults.get(count);

                    if (mTask.getCategory().equals(selectedCategory)) {
                        mRealm.beginTransaction();

                        mTask.setCategory("");
                        mRealm.copyToRealmOrUpdate(mTask);

                        mRealm.commitTransaction();
                    }
                    count++;
                }
                mRealm.close();

                //前画面に戻る
                finish();
            }
        });
        alertDialogDelete.setNegativeButton("Cancel", null);
        alertDialogDelete.show();
    }
    private void categoryEdit() {

        if (revisedCategory.length() == 0) {
            AlertDialog.Builder dialogInputError01 = new AlertDialog.Builder(this);
            dialogInputError01.setTitle("カテゴリーの入力エラー");
            dialogInputError01.setMessage("「カテゴリ」が入力されていません。");
            dialogInputError01.setPositiveButton("OK", null);
            dialogInputError01.show();
        } else if (revisedCategory.equals(selectedCategory)) {
            AlertDialog.Builder dialogInputError02 = new AlertDialog.Builder(this);
            dialogInputError02.setTitle("カテゴリーの入力エラー");
            dialogInputError02.setMessage("「カテゴリ」が変更されていません。");
            dialogInputError02.setPositiveButton("OK", null);
            dialogInputError02.show();
        } else {
            AlertDialog.Builder alertDialogChange = new AlertDialog.Builder(this);
            alertDialogChange.setTitle("カテゴリの変更");
            alertDialogChange.setMessage("カテゴリ「" + revisedCategory +"」に変更してもよろしいですか？");
            alertDialogChange.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Realmを開始
                    mRealm = Realm.getDefaultInstance();

                    //Categoryクラスを変更
                    mCategory = new Category();
                    RealmResults<Category> categoryRealmResults = mRealm.where(Category.class).findAll();
                    int categoryNumber = categoryRealmResults.size();

                    count = 0;


                    while (count < categoryNumber){
                        mCategory = categoryRealmResults.get(count);


                        if (mCategory.getCategory().equals(selectedCategory)) {
                            mRealm.beginTransaction();

                            mCategory.setCategory(revisedCategory);

                            mRealm.copyToRealmOrUpdate(mCategory);
                            mRealm.commitTransaction();
                        }
                        count++;
                    }

                    //Taskの変更

                    //Realmを開始

                    mTask = new Task();

                    RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll();
                    int taskNumber = taskRealmResults.size();

                    count = 0;

                    while (count < taskNumber - 1){
                        mTask = taskRealmResults.get(count);

                        if (mTask.getCategory().equals(selectedCategory)) {
                            mRealm.beginTransaction();

                            mTask.setCategory(revisedCategory);
                            mRealm.copyToRealmOrUpdate(mTask);
                            mRealm.commitTransaction();
                        }
                        count++;
                    }
                    mRealm.close();

                    screenStatus = STATUS_SELECT;
                    selectChange();

                    //前画面に戻る
                    finish();
                }
            });
            alertDialogChange.setNegativeButton("Cancel", null);
            alertDialogChange.show();
        }
    }

    private void selectChange() {
        mEditText.setVisibility(View.INVISIBLE);
        mButtonEdit.setVisibility(View.INVISIBLE);
        mButtonDelete.setVisibility(View.INVISIBLE);
        mButtonCategory.setVisibility(View.VISIBLE);
    }
}
