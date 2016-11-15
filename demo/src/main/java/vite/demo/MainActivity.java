package vite.demo;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Random;

import vite.rxbus.RxBus;
import vite.rxbus.annotation.Subscribe;

public class MainActivity extends FragmentActivity implements View.OnClickListener, TestFragment.OnFragmentInteractionListener {

    public static final String TAG = "Test";

    Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_bt).setOnClickListener(this);
        findViewById(R.id.main_bt_void).setOnClickListener(this);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_fl, new TestFragment());
        ft.commit();

        RxBus.register(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.main_bt:
                RxBus.post(TAG, random.nextInt());
                break;
            case R.id.main_bt_void:
                RxBus.post(null);
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Subscribe(tags = TAG)
    public void test(int random) {
        Toast.makeText(this, "random:" + random, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void test() {
        Toast.makeText(this, "void", Toast.LENGTH_SHORT).show();
    }
}
