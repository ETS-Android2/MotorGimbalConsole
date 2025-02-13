package com.motorgimbalconsole.flights;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import com.motorgimbalconsole.ConsoleApplication;
import com.motorgimbalconsole.R;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.afree.data.xy.XYSeries;
import org.afree.data.xy.XYSeriesCollection;
import org.afree.graphics.geom.Font;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import static java.lang.Math.abs;

public class FlightViewTabActivity extends AppCompatActivity {
    private FlightData myflight = null;
    private ViewPager mViewPager;
    SectionsPageAdapter adapter;
    private Tab1Fragment flightPage1 = null;
    private Tab2Fragment flightPage2 = null;
    private Button btnDismiss, btnPlay, butSelectCurves;
    private static ConsoleApplication myBT;
    private static double FEET_IN_METER = 1;

    private static String curvesNames[] = null;
    private static String currentCurvesNames[] = null;
    private static boolean[] checkedItems = null;
    private XYSeriesCollection allFlightData = null;
    private static XYSeriesCollection flightData = null;
    private static ArrayList<ILineDataSet> dataSets;
    static int colors[] = {Color.RED, Color.BLUE, Color.BLACK,
            Color.GREEN, Color.CYAN, Color.GRAY, Color.MAGENTA, Color.YELLOW, Color.RED,
            Color.BLUE, Color.BLACK,
            Color.GREEN, Color.CYAN, Color.GRAY, Color.MAGENTA, Color.YELLOW, Color.RED, Color.BLUE, Color.BLACK,
            Color.GREEN, Color.CYAN, Color.GRAY, Color.MAGENTA, Color.YELLOW};
    static Font font;
    private static String FlightName = null;
    private static XYSeries altitude;
    private static XYSeries speed;
    private static XYSeries accel;

    private static String[] units = null;
    //private LineChart mChart;
    public static String SELECTED_FLIGHT = "MyFlight";
    public static int numberOfCurves = 0;

    private void drawGraph() {

        int fontSize;
        fontSize = myBT.getAppConf().ConvertFont(Integer.parseInt(myBT.getAppConf().getFontSize()));

        String myUnits = "";
        if (myBT.getAppConf().getUnits().equals("0"))
            //Meters
            myUnits = getResources().getString(R.string.Meters_fview);
        else
            //Feet
            myUnits = getResources().getString(R.string.Feet_fview);

        //font
        font = new Font("Dialog", Typeface.NORMAL, fontSize);

        int numberOfCurves = flightData.getSeries().size();
        String chartTitle = "";

        for (int i = 0; i < numberOfCurves; i++) {

            if (i < (numberOfCurves - 1))
                chartTitle = chartTitle + flightData.getSeries(i).getKey().toString() + "-";
            else
                chartTitle = chartTitle + flightData.getSeries(i).getKey().toString();
        }
        //chart.setTitle(chartTitle);

        int graphBackColor;
        graphBackColor = myBT.getAppConf().ConvertColor(Integer.parseInt(myBT.getAppConf().getGraphBackColor()));


        int axisColor;
        axisColor = myBT.getAppConf().ConvertColor(Integer.parseInt(myBT.getAppConf().getGraphColor()));

        int labelColor = Color.BLACK;

        int nbrColor = Color.BLACK;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("CURRENT_CURVES_NAMES_KEY", currentCurvesNames);
        outState.putBooleanArray("CHECKED_ITEMS_KEY", checkedItems);

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentCurvesNames = savedInstanceState.getStringArray("CURRENT_CURVES_NAMES_KEY");
        checkedItems = savedInstanceState.getBooleanArray("CHECKED_ITEMS_KEY");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_ASK_PERMISSIONS = 123;
            int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);

            }
        }
        // recovering the instance state
        if (savedInstanceState != null) {
            currentCurvesNames = savedInstanceState.getStringArray("CURRENT_CURVES_NAMES_KEY");
            checkedItems = savedInstanceState.getBooleanArray("CHECKED_ITEMS_KEY");
        }

        //get the bluetooth connection pointer
        myBT = (ConsoleApplication) getApplication();

        //Check the local and force it if needed
        //getApplicationContext().getResources().updateConfiguration(myBT.getAppLocal(), null);

        setContentView(R.layout.activity_flight_view_tab);
        mViewPager = (ViewPager) findViewById(R.id.container);


        btnDismiss = (Button) findViewById(R.id.butDismiss);
        butSelectCurves = (Button) findViewById(R.id.butSelectCurves);
        btnPlay = (Button) findViewById(R.id.butPlay);


        Intent newint = getIntent();
        FlightName = newint.getStringExtra(FlightListActivity.SELECTED_FLIGHT);
        this.setTitle(FlightName);
        myflight = myBT.getFlightData();
        // get all the data that we have recorded for the current flight
        allFlightData = new XYSeriesCollection();
        allFlightData = myflight.GetFlightData(FlightName);

        altitude = null;
        altitude = allFlightData.getSeries(getResources().getString(R.string.altitude));
        //calculate speed
        speed = null;
        speed = allFlightData.getSeries(getResources().getString(R.string.curve_speed));

        // calculate acceleration
        accel = null;
        accel = allFlightData.getSeries(getResources().getString(R.string.curve_accel));


        // by default we will display the altitude
        // but then the user will be able to change the data
        flightData = new XYSeriesCollection();
        //altitude
        flightData.addSeries(allFlightData.getSeries(getResources().getString(R.string.altitude)));

        // get a list of all the curves that have been recorded
        numberOfCurves = allFlightData.getSeries().size();
        Log.d("numberOfCurves", "numberOfCurves:" + allFlightData.getSeries().size());
        curvesNames = new String[numberOfCurves];
        units = new String[numberOfCurves];
        for (int i = 0; i < numberOfCurves; i++) {
            curvesNames[i] = allFlightData.getSeries(i).getKey().toString();
            units[i]="";
        }

        // Read the application config
        myBT.getAppConf().ReadConfig();

        drawGraph();

        if (myBT.getAppConf().getUnits().equals("0")) {
            //Meters
            units[0] = "(" + getResources().getString(R.string.Meters_fview) + ")";

        } else {
            //Feet
            units[0] = getResources().getString(R.string.Feet_fview);

        }
        units[1] = "(°C)";
        units[2] = "(mbar)";
        units[3] ="";
        units[4] ="";
        units[5] ="";
        units[6] ="";
        units[7] ="";
        units[8] ="";
        units[9] ="";
        units[10] ="";

        if (myBT.getAppConf().getUnits().equals("0")) {//meters
            FEET_IN_METER = 1;
        } else {
            FEET_IN_METER = 3.28084;
        }
        if (currentCurvesNames == null) {
            //This is the first time so only display the altitude
            dataSets = new ArrayList<>();
            currentCurvesNames = new String[curvesNames.length];
            currentCurvesNames[0] = this.getResources().getString(R.string.altitude);//"altitude";
            checkedItems = new boolean[curvesNames.length];
            checkedItems[0] = true;
        }

        setupViewPager(mViewPager);


        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allFlightData = null;
                finish();      //exit the application configuration activity
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FlightViewTabActivity.this, PlayFlight.class);
                i.putExtra(SELECTED_FLIGHT, FlightName);
                startActivity(i);
            }
        });
        butSelectCurves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numberOfCurves = flightData.getSeries().size();
                currentCurvesNames = new String[numberOfCurves];

                for (int i = 0; i < numberOfCurves; i++) {
                    currentCurvesNames[i] = flightData.getSeries(i).getKey().toString();
                }
                // Set up the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(FlightViewTabActivity.this);

                checkedItems = new boolean[curvesNames.length];
                // Add a checkbox list
                for (int i = 0; i < curvesNames.length; i++) {
                    if (Arrays.asList(currentCurvesNames).contains(curvesNames[i]))
                        checkedItems[i] = true;
                    else
                        checkedItems[i] = false;
                }


                builder.setMultiChoiceItems(curvesNames, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        // The user checked or unchecked a box
                    }
                });
                // Add OK and Cancel buttons
                builder.setPositiveButton(getResources().getString(R.string.fv_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // The user clicked OK
                        flightPage1.drawGraph();
                        flightPage1.drawAllCurves(allFlightData);
                    }
                });
                //cancel
                builder.setNegativeButton(getResources().getString(R.string.fv_cancel), null);

                // Create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        });

    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new SectionsPageAdapter(getSupportFragmentManager());
        flightPage1 = new Tab1Fragment(allFlightData);
        flightPage2 = new Tab2Fragment(myflight, allFlightData);

        adapter.addFragment(flightPage1, "TAB1");
        adapter.addFragment(flightPage2, "TAB2");

        viewPager.setAdapter(adapter);
    }

    public class SectionsPageAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList();
        private final List<String> mFragmentTitleList = new ArrayList();

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public SectionsPageAdapter(FragmentManager fm) {
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

    public static class Tab1Fragment extends Fragment {
        private LineChart mChart;
        public XYSeriesCollection allFlightData;

        int graphBackColor, fontSize, axisColor, labelColor, nbrColor;

        public Tab1Fragment(XYSeriesCollection data) {
            this.allFlightData = data;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            View view = inflater.inflate(R.layout.tabflight_view_mp_fragment, container, false);

            mChart = (LineChart) view.findViewById(R.id.linechart);


            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            drawGraph();
            drawAllCurves(allFlightData);

            return view;
        }

        private void drawGraph() {
            graphBackColor = myBT.getAppConf().ConvertColor(Integer.parseInt(myBT.getAppConf().getGraphBackColor()));
            fontSize = myBT.getAppConf().ConvertFont(Integer.parseInt(myBT.getAppConf().getFontSize()));
            axisColor = myBT.getAppConf().ConvertColor(Integer.parseInt(myBT.getAppConf().getGraphColor()));
            labelColor = Color.BLACK;
            nbrColor = Color.BLACK;
        }

        private void drawAllCurves(XYSeriesCollection allFlightData) {
            dataSets.clear();

            //dataSets = new ArrayList<>();
            flightData = new XYSeriesCollection();
            for (int i = 0; i < curvesNames.length; i++) {
                if (checkedItems[i]) {
                    flightData.addSeries(allFlightData.getSeries(curvesNames[i]));

                    int nbrData = allFlightData.getSeries(i).getItemCount();

                    ArrayList<Entry> yValues = new ArrayList<>();

                    for (int k = 0; k < nbrData; k++) {
                        yValues.add(new Entry(allFlightData.getSeries(i).getX(k).floatValue(), allFlightData.getSeries(i).getY(k).floatValue()));
                    }

                    LineDataSet set1 = new LineDataSet(yValues, getResources().getString(R.string.Altitude_time));
                    set1.setColor(colors[i]);

                    set1.setDrawValues(false);
                    set1.setDrawCircles(false);
                    set1.setLabel(curvesNames[i] +" "+units[i]);

                    dataSets.add(set1);
                }
            }


            LineData data = new LineData(dataSets);
            mChart.clear();
            mChart.setData(data);
            Description desc = new Description();
            desc.setText("");
            mChart.setDescription(desc);


        }
    }

    /*
    This is the flight information tab
     */
    public static class Tab2Fragment extends Fragment {

        private FlightData myflight;
        XYSeriesCollection allFlightData;
        private TextView nbrOfSamplesValue, flightNbrValue;
        private TextView apogeeAltitudeValue, flightDurationValue, burnTimeValue, maxVelociyValue, maxAccelerationValue;
        private TextView timeToApogeeValue, mainAltitudeValue, maxDescentValue, landingSpeedValue;

        private AlertDialog.Builder builder = null;
        private AlertDialog alert;

        String SavedCurves = "";

        public Tab2Fragment(FlightData data, XYSeriesCollection data2) {

            myflight = data;
            this.allFlightData = data2;
        }

        public void msg(String s) {
            Toast.makeText(getActivity().getApplicationContext(), s, Toast.LENGTH_LONG).show();
        }

        private Button buttonExportToCsv;
        int nbrSeries;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            View view = inflater.inflate(R.layout.tabflight_info_fragment, container, false);

            buttonExportToCsv = (Button) view.findViewById(R.id.butExportToCsv);
            apogeeAltitudeValue = view.findViewById(R.id.apogeeAltitudeValue);
            flightDurationValue = view.findViewById(R.id.flightDurationValue);
            burnTimeValue = view.findViewById(R.id.burnTimeValue);
            maxVelociyValue = view.findViewById(R.id.maxVelociyValue);
            maxAccelerationValue = view.findViewById(R.id.maxAccelerationValue);
            timeToApogeeValue = view.findViewById(R.id.timeToApogeeValue);
            mainAltitudeValue = view.findViewById(R.id.mainAltitudeValue);
            maxDescentValue = view.findViewById(R.id.maxDescentValue);
            landingSpeedValue = view.findViewById(R.id.landingSpeedValue);
            nbrOfSamplesValue = view.findViewById(R.id.nbrOfSamplesValue);
            flightNbrValue = view.findViewById(R.id.flightNbrValue);

            XYSeriesCollection flightData;
            //myflight= myBT.getFlightData();
            flightData = myflight.GetFlightData(FlightName);
            int nbrData = flightData.getSeries(0).getItemCount();
            nbrSeries = flightData.getSeriesCount();
            // flight nbr
            flightNbrValue.setText(FlightName + "");

            //nbr of samples
            nbrOfSamplesValue.setText(nbrData + "");

            //flight duration
            double flightDuration = flightData.getSeries(0).getMaxX() / 1000;
            flightDurationValue.setText(String.format("%.2f",flightDuration) + " secs");
            //apogee altitude
            double apogeeAltitude = flightData.getSeries(0).getMaxY();
            apogeeAltitudeValue.setText(String.format("%.0f",apogeeAltitude) + " " + myBT.getAppConf().getUnitsValue());

            //apogee time
            int pos = searchX(flightData.getSeries(0), apogeeAltitude);
            double apogeeTime = (double) flightData.getSeries(0).getX(pos);
            timeToApogeeValue.setText(String.format("%.2f",apogeeTime / 1000) + " secs");

            //calculate max speed
            double maxSpeed = speed.getMaxY();
            maxVelociyValue.setText((long) maxSpeed + " " + myBT.getAppConf().getUnitsValue() + "/secs");

            //landing speed
            double landingSpeed = 0;
            int timeBeforeLanding =searchXBack(altitude, 30);
            if (timeBeforeLanding != -1)
                if (searchY(speed, altitude.getX(timeBeforeLanding).doubleValue() )!= -1) {
                    landingSpeed =searchY(speed, altitude.getX(timeBeforeLanding).doubleValue() );
                    landingSpeedValue.setText((long) landingSpeed + " " + myBT.getAppConf().getUnitsValue() + "/secs");
                } else {
                    landingSpeedValue.setText("N/A");
                }
            /*if (searchY(speed, flightData.getSeries(0).getMaxX() - 2000) != -1) {
                landingSpeed = speed.getY(searchY(speed, flightData.getSeries(0).getMaxX() - 2000)).doubleValue();
                landingSpeedValue.setText((long) landingSpeed + " " + myBT.getAppConf().getUnitsValue() + "/secs");
            } else {
                landingSpeedValue.setText("N/A");
            }*/
            //max descente speed
            double maxDescentSpeed = 0;
            //int timeBeforeLanding =searchXBack(altitude, 30);

            /*if (searchY(speed, timeBeforeLanding) != -1) {
                maxDescentSpeed = speed.getY(searchY(speed, timeBeforeLanding)).doubleValue();
                maxDescentValue.setText((long) maxDescentSpeed + " " + myBT.getAppConf().getUnitsValue() + "/secs");
            } else {
                maxDescentValue.setText("N/A");
            }*/
           /* if (searchY(speed, apogeeTime + 2000) != -1) {
                maxDescentSpeed = speed.getY(searchY(speed, apogeeTime + 2000)).doubleValue();
                maxDescentValue.setText((long) maxDescentSpeed + " " + myBT.getAppConf().getUnitsValue() + "/secs");
            } else {
                maxDescentValue.setText("N/A");
            }*/
            if (searchY(speed, apogeeTime + 100) != -1) {
                int pos1 = searchY(speed, apogeeTime + 100);
                XYSeries partialSpeed;
                partialSpeed = new XYSeries("partial_speed");
                for(int i = pos1; i < speed.getItemCount(); i++ ) {
                    partialSpeed.add(speed.getX(i), speed.getY(i));
                }
                maxDescentSpeed = partialSpeed.getMaxY();
                //maxDescentSpeed = speed.getY(searchY(speed, apogeeTime + 500)).doubleValue();
                maxDescentValue.setText((long) maxDescentSpeed + " " + myBT.getAppConf().getUnitsValue() + "/secs");
            } else {
                maxDescentValue.setText("N/A");
            }

            //max acceleration value
            double maxAccel = accel.getMaxY();
            maxAccel = (maxAccel * FEET_IN_METER) / 9.80665;

            maxAccelerationValue.setText((long) maxAccel + " G");

            //burntime value
            double burnTime = 0;
            if (searchX(speed, maxSpeed) != -1)
                burnTime = speed.getX(searchX(speed, maxSpeed)).doubleValue();
            if (burnTime != 0)
                burnTimeValue.setText(String.format("%.2f",burnTime / 1000) + " secs");
            else
                burnTimeValue.setText("N/A");
            //main value
            // remain TODO!!!
            mainAltitudeValue.setText(" " + myBT.getAppConf().getUnitsValue());

            buttonExportToCsv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    SavedCurves = "";
                    //export the data to a csv file
                    for (int j = 0; j < numberOfCurves; j++) {
                        Log.d("Flight win", "Saving curve:" + j);
                        saveData(j, allFlightData);
                    }
                    builder = new AlertDialog.Builder(Tab2Fragment.this.getContext());
                    //Running Saving commands
                    builder.setMessage(getResources().getString(R.string.save_curve_msg) + Environment.DIRECTORY_DOWNLOADS + "\\MotorGimbalConsoleFlights \n" + SavedCurves)
                            .setTitle(getResources().getString(R.string.save_curves_title))
                            .setCancelable(false)
                            .setPositiveButton(R.string.save_curve_ok, new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            });

                    alert = builder.create();
                    alert.show();
                    msg(getResources().getString(R.string.curves_saved_msg));

                }
            });

            return view;
        }

        private void saveData(int nbr, XYSeriesCollection Data) {

            String valHeader = "";

            if (nbr == 0) {
                valHeader = getResources().getString(R.string.curve_altitude);
            } else if (nbr == 1) {
                valHeader = getResources().getString(R.string.curve_temperature);
            } else if (nbr == 2) {
                valHeader = getResources().getString(R.string.curve_pressure);
            } else if (nbr == 3) {
                valHeader = "Gravity X";
            }else if (nbr == 4) {
                valHeader = "Gravity Y";
            }else if (nbr == 5) {
                valHeader = "Gravity Z";
            }else if (nbr == 6) {
                valHeader = "Euler X";
            }else if (nbr == 7) {
                valHeader = "Euler Y";
            }else if (nbr == 8) {
                valHeader = "Euler Z";
            }else if (nbr == 9) {
                valHeader = "Yaw";
            }else if (nbr == 10) {
                valHeader = "Pitch";
            }else if (nbr == 11) {
                valHeader = "Roll";
            }else if (nbr == 12) {
                valHeader = "outputX";
            }else if (nbr == 13) {
                valHeader = "outputY";
            }else if (nbr == 14) {
                valHeader = "accelX";
            }else if (nbr == 15) {
                valHeader = "accelY";
            }else if (nbr == 16) {
                valHeader = "accelZ";
            }else if (nbr == 17) {
                valHeader = getResources().getString(R.string.curve_speed);
            } else if (nbr == 18) {
                valHeader = getResources().getString(R.string.curve_accel);
            }

            String csv_data = "time(ms),"+valHeader + " " + units[nbr] +"\n";/// your csv data as string;
            int nbrData = Data.getSeries(nbr).getItemCount();
            for (int i = 0; i < nbrData; i++) {

                csv_data = csv_data + (double) Data.getSeries(nbr).getX(i) + "," + (double) Data.getSeries(nbr).getY(i) + "\n";

            }
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            //if you want to create a sub-dir
            root = new File(root, "MotorGimbalConsoleFlights");
            root.mkdir();

            SimpleDateFormat sdf = new SimpleDateFormat("_dd-MM-yyyy_hh-mm-ss");
            String date = sdf.format(System.currentTimeMillis());

            // select the name for your file
            root = new File(root, FlightName + "-" + Data.getSeries(nbr).getKey().toString() + date + ".csv");
            Log.d("Flight win", FlightName + Data.getSeries(nbr).getKey().toString() + date + ".csv");
            try {
                Log.d("Flight win", "attempt to write");
                FileOutputStream fout = new FileOutputStream(root);
                fout.write(csv_data.getBytes());
                fout.close();
                Log.d("Flight win", "write done");
                SavedCurves = SavedCurves +
                        FlightName + Data.getSeries(nbr).getKey().toString() + date + ".csv\n";

            } catch (FileNotFoundException e) {
                e.printStackTrace();

                boolean bool = false;
                try {
                    // try to create the file
                    bool = root.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                if (bool) {
                    // call the method again
                    saveData(nbr, Data);
                } else {
                    Log.d("Flight win", "Failed to create flight files");
                    //throw new IllegalStateException(getString(R.string.failed_to_create_file_msg));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
        Return the position of the first X value it finds from the beginning
         */
        public int searchX(XYSeries serie, double searchVal) {
            int nbrData = serie.getItemCount();
            int pos = -1;
            for (int i = 1; i < nbrData; i++) {
                if ((searchVal >= serie.getY(i - 1).doubleValue()) && (searchVal <= serie.getY(i).doubleValue())) {
                    pos = i;
                    break;
                }
            }
            return pos;
        }

        /*
          Return the position of the first X value it finds from the beginning
        */
        public int searchXBack(XYSeries serie, double searchVal) {
            int nbrData = serie.getItemCount();
            int pos = -1;
            for (int i = 1; i < nbrData; i++) {
                if ((searchVal >= serie.getY((nbrData - i) - 1).doubleValue()) && (searchVal <= serie.getY(nbrData - i).doubleValue())) {
                    pos = (nbrData - i);
                    break;
                }
            }
            return pos;
        }

        /*
        Return the position of the first Y value it finds from the beginning
        */
        public int searchY(XYSeries serie, double searchVal) {
            int nbrData = serie.getItemCount();
            int pos = -1;
            for (int i = 1; i < nbrData; i++) {
                if ((searchVal >= serie.getX(i - 1).doubleValue()) && (searchVal <= serie.getX(i).doubleValue())) {
                    pos = i;
                    break;
                }
            }
            return pos;
        }
    }
}