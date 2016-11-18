package vite.demo;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import vite.rxbus.RxBus;
import vite.rxbus.annotation.RxThread;
import vite.rxbus.annotation.Subscribe;

public class MainActivity extends FragmentActivity implements View.OnClickListener, TestFragment.OnFragmentInteractionListener {

    public static final String TAG = "Test";

    Random random = new Random();
    ViewPager vp;
    FragmentPagerAdapter adapter;

    TestFragment f1 = TestFragment.newInstance("one", null);
    TestFragment f2 = TestFragment.newInstance("two", null);
    TestFragment f3 = TestFragment.newInstance("three", null);

    ArrayList<Fragment> arrayList = new ArrayList<>();


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

        RxBus.register(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fl , f3);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_bt:
                RxBus.post(TAG, random.nextInt());
                break;
            case R.id.main_bt_void:
                RxBus.post(null);
                break;
            case R.id.main_bt_tag1:
                RxBus.post("test1", "Main Button Tag1");
                break;
            case R.id.main_bt_tag2:
                RxBus.post("test2", "Main Button Tag2");
                break;
            case R.id.main_bt_tag3:
//                RxBus.post("test3", "Main Button Tag3");
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(f3);
                transaction.commit();
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Subscribe(tag = TAG)
    public void test(int random) {
        Toast.makeText(this, "random:" + random, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void test() {
        Toast.makeText(this, "void", Toast.LENGTH_SHORT).show();
    }

    @Subscribe(thread = RxThread.IO)
    public void testThread() {
        Log.v("testThread", "thread:" + Thread.currentThread());
    }
}
