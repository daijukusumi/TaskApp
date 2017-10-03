package jp.techacademy.kusumi.daiju.taskapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.realm.Realm;

public class CategoryActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String EXTRA_CATEGORY = "jp.techacademy.kusumi.daiju.taskapp.Category";

    private EditText mCategoryEdit;
    private Button mCreateCategoryButton;
    private Category mCategory;
    Realm realm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //部品の設定
        mCategoryEdit = (EditText) findViewById(R.id.category_edit_text);
        mCreateCategoryButton = (Button) findViewById(R.id.create_category_button);

        mCreateCategoryButton.setOnClickListener(this);

        //Realm検索-初回検索・初期値設定 => id==0に何もカテゴリが設定されていなければ、null値をカテゴリに設定
        realm = Realm.getDefaultInstance();

        mCategory = new Category();

        if (realm.where(Category.class).equalTo("id",0).findFirst() == null) {
            realm.beginTransaction();
            mCategory.setId(0);
            mCategory.setCategory("");
            realm.copyToRealmOrUpdate(mCategory);
            realm.commitTransaction();
        }

        realm.close();
    }

    @Override
    public void onClick(View v) {
        realm = Realm.getDefaultInstance();


        int id = realm.where(Category.class).max("id").intValue() + 1;
        String newCategory = mCategoryEdit.getText().toString();


        //EditTextがNullの場合のエラー処理
        if (newCategory.length() == 0) {
            AlertDialog.Builder dialogInputError = new AlertDialog.Builder(this);
            dialogInputError.setTitle("カテゴリーの入力エラー");
            dialogInputError.setMessage("「カテゴリ」が入力されていません。");
            dialogInputError.setPositiveButton("OK", null);
            dialogInputError.show();
        } else {
            if (realm.where(Category.class).equalTo("category",newCategory).findAll().size() == 0) {
                realm.beginTransaction();
                mCategory.setId(id);
                mCategory.setCategory(newCategory);

                realm.copyToRealmOrUpdate(mCategory);

                Intent intent = new Intent(CategoryActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_CATEGORY, newCategory);
                setResult(RESULT_OK, intent);

                realm.commitTransaction();
                realm.close();
                finish();


            }   else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("カテゴリのエラー");
                alertDialogBuilder.setMessage("カテゴリ「"+ newCategory + "」は既に登録されています");
                alertDialogBuilder.setPositiveButton("戻る", null);
                alertDialogBuilder.show();
                mCategoryEdit.setText("");
                realm.close();
            }
        }
        }
    }


