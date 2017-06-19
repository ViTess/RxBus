package vite.demo;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import vite.rxbus.RxBus;
import vite.rxbus.RxThread;
import vite.rxbus.Subscribe;
import vite.rxbus.ThreadType;

public class MainActivity extends FragmentActivity implements View.OnClickListener, TestFragment.OnFragmentInteractionListener, StickyFragment
        .OnFragmentInteractionListener {

    public static final String TAG = "Test";

    Random random = new Random();
    ViewPager vp;
    FragmentPagerAdapter adapter;

    TestFragment f1 = TestFragment.newInstance("one", null);
    TestFragment f2 = TestFragment.newInstance("two", null);
    TestFragment f3 = TestFragment.newInstance("three", null);

    ArrayList<Fragment> arrayList = new ArrayList<>();

    int stickyTagKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_bt).setOnClickListener(this);
        findViewById(R.id.main_bt_void).setOnClickListener(this);
        findViewById(R.id.main_bt_tag1).setOnClickListener(this);
        findViewById(R.id.main_bt_tag2).setOnClickListener(this);
        findViewById(R.id.main_bt_tag3).setOnClickListener(this);

        vp = (ViewPager) findViewById(R.id.main_vp);
        arrayList.add(f1);
        arrayList.add(f2);

        adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return arrayList.get(position);
            }

            @Override
            public int getCount() {
                return arrayList.size();
            }

        };
        vp.setAdapter(adapter);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fl, f3);
        transaction.commit();

        RxBus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_bt:
//                RxBus.post(TAG, random.nextInt());
//                RxBus.post(TAG, new int[]{1, 3, 2});
                stickyTagKey = RxBus.postSticky("FUCK", random.nextInt());
                break;
            case R.id.main_bt_void:
//                RxBus.post(TAG, null);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_fl, StickyFragment.newInstance(null, null));
                transaction.commit();

                break;
            case R.id.main_bt_tag1:
//                RxBus.post("test1", "Main Button Tag1");

                RxBus.removeSticky(stickyTagKey);
                break;
            case R.id.main_bt_tag2:
                RxBus.post("test2", "Main Button Tag2");
                break;
            case R.id.main_bt_tag3:
//                RxBus.post("test3", new Entity("Hello", "Wrold"));
                Log.v("RxBus", RxBus.toStrings());
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Subscribe(TAG)
    public void test(int random) {
        Log.v("MainActivity", "random:" + random);
        Toast.makeText(this, "random:" + random, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(TAG)
    public void test() {
        Toast.makeText(this, "void", Toast.LENGTH_SHORT).show();
    }

    @Subscribe(TAG)
    @RxThread(ThreadType.IO)
    public void testThread() {
        Log.v("testThread", "thread:" + Thread.currentThread().getName());
    }

    @Subscribe(TAG)
    public void testArray2(int[] aaa) {
        Log.v("testArray2", Arrays.toString(aaa));
    }
}
