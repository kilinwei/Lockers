package com.xyf.lockers.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xyf.lockers.R;
import com.xyf.lockers.adapter.StorageRecordAdapter;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.model.bean.StorageBean;
import com.xyf.lockers.utils.StorageDBManager;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class StorageRecordActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener {
    private static final String TAG = "StorageRecordActivity";
    @BindView(R.id.recyclerview_storage_record)
    RecyclerView recyclerviewStorageRecord;
    @BindView(R.id.btn_back)
    Button btnBack;


    @Override
    protected int getLayout() {
        return R.layout.activity_storage_record;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        Disposable subscribe = Observable.just(1).subscribeOn(Schedulers.io())
                .map(new Function<Integer, List<StorageBean>>() {
                    @Override
                    public List<StorageBean> apply(Integer integer) throws Exception {
                        return StorageDBManager.getAllStorageUser();
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<StorageBean>>() {
                    @Override
                    public void accept(List<StorageBean> storageBeans) throws Exception {
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(StorageRecordActivity.this);
                        linearLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
                        recyclerviewStorageRecord.setLayoutManager(linearLayoutManager);
                        StorageRecordAdapter adapter = new StorageRecordAdapter(R.layout.storage_record_item, storageBeans);
                        recyclerviewStorageRecord.setAdapter(adapter);
                        adapter.setOnItemChildClickListener(StorageRecordActivity.this);
                    }
                });
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

    }


    @OnClick(R.id.btn_back)
    public void onViewClicked() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
