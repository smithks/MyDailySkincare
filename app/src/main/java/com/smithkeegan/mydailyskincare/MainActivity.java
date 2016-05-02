package com.smithkeegan.mydailyskincare;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.roomorama.caldroid.CaldroidFragment;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        CaldroidFragment caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH,cal.get(Calendar.MONTH)+1);
        args.putInt(CaldroidFragment.YEAR,cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        ColorDrawable excellent = new ColorDrawable(getResources().getColor(R.color.excellent));

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar,caldroidFragment);
        t.commit();

        caldroidFragment.setBackgroundDrawableForDate(excellent,date);
        calendar.set(Calendar.DAY_OF_MONTH,3);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources().getColor(R.color.veryGood)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,4);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources().getColor(R.color.good)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,5);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources().getColor(R.color.fair)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,6);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources().getColor(R.color.poor)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,7);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources().getColor(R.color.veryPoor)),calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH,8);
        caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources().getColor(R.color.terrible)),calendar.getTime());


        caldroidFragment.refreshView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
