package com.motorgimbalconsole.telemetry;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.support.annotation.Nullable;
import androidx.annotation.Nullable;
/*import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;*/
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.motorgimbalconsole.ConsoleApplication;
import com.motorgimbalconsole.R;
import com.motorgimbalconsole.utils.Rocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import processing.android.PFragment;
import processing.core.PApplet;

/**
 * @description: Gimbal real time telemetry
 * @author: boris.dureau@neuf.fr
 **/
public class ConsoleTabStatusActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    SectionsStatusPageAdapter adapter;
    Tab1StatusFragment statusPage1 =null;
    Tab2StatusFragment statusPage2 =null;
    Tab3StatusFragment statusPage3 =null;
    private Button btnDismiss, btnRecording;
    ConsoleApplication myBT ;
    Thread altiStatus;
    boolean status = true;
    boolean recording = false;

    Handler handler = new Handler () {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //Value 1 contains the GyroX
                    statusPage1.setGyroXValue((String)msg.obj);
                    //statusPage2.setAltitudeValue((String)msg.obj);
                    break;
                case 2:
                    //Value 2 contains the GyroY
                    statusPage1.setGyroYValue((String)msg.obj);
                    break;
                case 3:
                    //Value 3 contains the GyroZ
                    statusPage1.setGyroZValue((String)msg.obj);
                    break;
                case 4:
                    //Value 4 contains the AccelX
                    statusPage1.setAccelXValue((String)msg.obj);
                    break;
                case 5:
                    //Value 5 contains the AccelY
                    statusPage1.setAccelYValue((String)msg.obj);
                    break;
                case 6:
                    //Value 6 contains the AccelZ
                    statusPage1.setAccelZValue((String)msg.obj);
                    break;
                case 7:
                    //Value 7 contains the OrientX
                    statusPage1.setOrientXValue((String)msg.obj);
                    break;
                case 8:
                    //Value 8 contains the OrientY
                    statusPage1.setOrientYValue((String)msg.obj);
                    break;
                case 9:
                    // Value 9 contains the OrientZ
                    statusPage1.setOrientZValue((String)msg.obj);
                    break;
                case 10:
                    // Value 10 contains the altitude
                    statusPage2.setAltitudeValue((String)msg.obj);
                    break;
                case 11:
                    // Value 11 contains the temperature
                    statusPage2.setTempValue((String)msg.obj);
                    //txtViewOutput3Status.setText(outputStatus((String)msg.obj));
                    break;
                case 12:
                    //Value 12 contains the pressure
                    statusPage2.setPressureValue((String)msg.obj);
                    //txtViewOutput4Status.setText(outputStatus((String)msg.obj));
                    break;

                case 13:
                    //Value 13 contains the battery voltage
                    String voltage = (String)msg.obj;
                    if(voltage.matches("\\d+(?:\\.\\d+)?")) {
                       /* double batVolt;

                        batVolt =  (3.05*((Double.parseDouble(voltage) * 3300) / 4096)/1000);

                        statusPage2.setBatteryVoltage(batVolt);*/
                        statusPage2.setBatteryVoltage(voltage + " Volts");
                        //statusPage2.setBatteryVoltage(Double.parseDouble(voltage ));

                    }
                    else {
                        //txtViewVoltage.setText("NA");
                    }
                    break;
                case 14:
                    //Value 14 contains the graphic
                    statusPage3.setInputString((String)msg.obj+",");
                    break;
                case 19:
                    //Value 19 contains the eeprom usage
                    statusPage2.setEEpromUsage((String)msg.obj+"%");
                    break;
                case 20:
                    //Value 20 contains the angle correction
                    statusPage3.setInputCorrect((String)msg.obj);
                    break;
                case 21:
                    //Value 21 contains the servoX
                    statusPage3.setServoX((String)msg.obj);
                    break;
                case 22:
                    //Value 22 contains the servoY
                    statusPage3.setServoY((String)msg.obj);
                    break;
                case 28:
                    //Value 28 contains the nbr of flight
                    statusPage2.setNbrOfFlights((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myBT = (ConsoleApplication) getApplication();

        setContentView(R.layout.activity_console_tab_status);

        mViewPager =(ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);
        myBT.setHandler(handler);
        btnDismiss = (Button)findViewById(R.id.butDismiss);
        btnRecording = (Button)findViewById(R.id.butRecording);
        btnDismiss.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (status) {

                    status = false;
                    myBT.write("h;".toString());

                    myBT.setExit(true);
                    myBT.clearInput();
                    myBT.flush();
                }
                if(myBT.getConnected()) {
                    //turn off telemetry
                    myBT.flush();
                    myBT.clearInput();
                    myBT.write("y0;".toString());
                }
                finish();      //exit the  activity
            }
        });

        btnRecording.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (recording) {

                    recording = false;
                    myBT.write("w0;".toString());

                    //myBT.setExit(true);
                    myBT.clearInput();
                    myBT.flush();
                    btnRecording.setText("Start Rec");
                }
                else
                {
                    recording = true;
                    myBT.write("w1;".toString());
                    myBT.clearInput();
                    myBT.flush();
                    btnRecording.setText("Stop");
                }

            }
        });

        Runnable r = new Runnable() {

            @Override
            public void run() {
                while (true){
                    if(!status) break;
                    myBT.ReadResult(10000);
                }
            }
        };

        altiStatus = new Thread(r);
        altiStatus.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(myBT.getConnected() && !status) {
            myBT.flush();
            myBT.clearInput();

            myBT.write("y1;".toString());
            status = true;
            altiStatus.start();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();

        if (status) {
            status = false;
            myBT.write("h;".toString());

            myBT.setExit(true);
            myBT.clearInput();
            myBT.flush();

        }


        myBT.flush();
        myBT.clearInput();
        myBT.write("h;".toString());
        try {
            while (myBT.getInputStream().available() <= 0) ;
        } catch (IOException e) {

        }
        String myMessage = "";
        long timeOut = 10000;
        //long startTime = System.currentTimeMillis();

        myMessage =myBT.ReadResult(timeOut);
    }
    private void setupViewPager(ViewPager viewPager) {
        adapter = new SectionsStatusPageAdapter(getSupportFragmentManager());
        statusPage1 = new Tab1StatusFragment();
        statusPage2 = new Tab2StatusFragment();
        statusPage3 = new Tab3StatusFragment();


        adapter.addFragment(statusPage1, "TAB1");
        adapter.addFragment(statusPage2, "TAB2");
        adapter.addFragment(statusPage3, "TAB3");



        viewPager.setAdapter(adapter);
    }
    public class SectionsStatusPageAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList();
        private final List<String> mFragmentTitleList= new ArrayList();

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        public SectionsStatusPageAdapter (FragmentManager fm){
            super(fm);
        }


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }
/*
This is the first tab which contains
The gyros values
the accelerometer
the orientation on all axis
 */
    public static class Tab1StatusFragment extends Fragment {
        private static final String TAG = "Tab1StatusFragment";
        private boolean ViewCreated = false;
        private TextView txtViewGyroXValue,txtViewGyroYValue, txtViewGyroZValue;
        private TextView txtViewAccelXValue, txtViewAccelYValue, txtViewAccelZValue;
        private TextView txtViewOrientXValue, txtViewOrientYValue, txtViewOrientZValue;

        public void setGyroXValue(String value) {
            this.txtViewGyroXValue.setText(value);
        }
        public void setGyroYValue(String value) {
            this.txtViewGyroYValue.setText(value);
        }
        public void setGyroZValue(String value) {
            this.txtViewGyroZValue.setText(value);
        }
        public void setAccelXValue(String value) {
            this.txtViewAccelXValue.setText(value);
        }
        public void setAccelYValue(String value) {
            this.txtViewAccelYValue.setText(value);
        }
        public void setAccelZValue(String value) {
            this.txtViewAccelZValue.setText(value);
        }
        public void setOrientXValue(String value) {
            this.txtViewOrientXValue.setText(value);
        }
        public void setOrientYValue(String value) {
            this.txtViewOrientYValue.setText(value);
        }
        public void setOrientZValue(String value) {
            this.txtViewOrientZValue.setText(value);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.tabstatuspart1_fragment,container,false);
            txtViewGyroXValue =(TextView)view.findViewById(R.id.textViewGyroXValue);
            txtViewGyroYValue =(TextView)view.findViewById(R.id.textViewGyroYValue);
            txtViewGyroZValue =(TextView)view.findViewById(R.id.textViewGyroZValue);
            txtViewAccelXValue =(TextView)view.findViewById(R.id.textViewAccelXValue);
            txtViewAccelYValue =(TextView)view.findViewById(R.id.textViewAccelYValue);
            txtViewAccelZValue =(TextView)view.findViewById(R.id.textViewAccelZValue);
            txtViewOrientXValue = (TextView)view.findViewById(R.id.textViewOrientXValue);
            txtViewOrientYValue = (TextView)view.findViewById(R.id.textViewOrientYValue);
            txtViewOrientZValue = (TextView)view.findViewById(R.id.textViewOrientZValue);

            ViewCreated = true;
            return view;
        }
    }
    /*
    This is the second tab
    it contains
    The current altitude
    the battery voltage
    the current pressure
    the current temperature of the sensor
    the % of eeprom used
     */
    public static class Tab2StatusFragment extends Fragment {
        private static final String TAG = "Tab2StatusFragment";
        private TextView txtViewVoltage,txtNbrOfFlightValue;
        private boolean ViewCreated = false;
        private TextView txtViewBatteryVoltage,txtViewAltitudeValue;
        private TextView txtViewPressureValue,txtViewTempValue, txtViewEEpromUsageValue;

        public void setAltitudeValue(String value) {
            this.txtViewAltitudeValue.setText(value);
        }
        public void setPressureValue(String value) {
            this.txtViewPressureValue.setText(value);
        }
        public void setTempValue(String value) {
            this.txtViewTempValue.setText(value);
        }
        public void setBatteryVoltage(String value) {
            this.txtViewBatteryVoltage.setText(value);
        }
        public void setEEpromUsage(String value) {
            this.txtViewEEpromUsageValue.setText(value);
        }
        public void setNbrOfFlights(String value) {
            this.txtNbrOfFlightValue.setText(value);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.tabstatuspart2_fragment,container,false);
            //
            txtViewAltitudeValue = (TextView)view.findViewById(R.id.txtAltitudeValue);
            txtViewPressureValue = (TextView)view.findViewById(R.id.txtPressureValue);
            txtViewTempValue = (TextView)view.findViewById(R.id.txtTempValue);
            txtViewVoltage=(TextView)view.findViewById(R.id.txtBatteryVoltage);
            txtViewBatteryVoltage=(TextView)view.findViewById(R.id.txtBatteryVoltageValue);
            txtViewEEpromUsageValue =(TextView)view.findViewById(R.id.txtEEpromUsageValue);
            txtNbrOfFlightValue=(TextView)view.findViewById(R.id.txtNbrOfFlightValue);
            ViewCreated = true;
            return view;
        }
    }
    /*
    This is the third tab
    it displays the rocket orientation
     */
    public static class Tab3StatusFragment extends Fragment {
        private static final String TAG = "Tab3StatusFragment";
        private PApplet myRocket;
        boolean ViewCreated = false;
        private PFragment fragment;
        View view;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            view = inflater.inflate(R.layout.tabstatuspart3_fragment,container,false);

            myRocket = new Rocket();

            fragment = new PFragment(myRocket);

            getChildFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
            ViewCreated = true;

            return view;
        }

        @Override
        public void onStart() {
            super.onStart();
            fragment.requestDraw();
            view.refreshDrawableState();
            view.bringToFront();
        }
        @Override
        public void onResume() {
            super.onResume();

            fragment.onResume(); //perahps we can remove that
            //This is the only way I can redraw the rocket after leaving the tab
            getChildFragmentManager().beginTransaction().replace(R.id.container,fragment).commit();


        }
        //send the quaternion to the processing widget
        public void setInputString(String value) {
            //if (ViewCreated)
           if(view != null)
                ((Rocket) myRocket).setInputString(value);
        }
        public void setInputCorrect(String value) {
            if(view != null)
                ((Rocket) myRocket).setInputCorrect(value);
        }
        public void setServoX(String value) {
            if(view != null)
                ((Rocket) myRocket).setServoX(value);
        }
        public void setServoY(String value) {
            if(view != null)
                ((Rocket) myRocket).setServoY(value);
        }
    }
}
