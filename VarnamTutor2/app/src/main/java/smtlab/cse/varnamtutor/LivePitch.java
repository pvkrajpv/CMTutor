package smtlab.cse.varnamtutor;


import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class LivePitch extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,DetectTonic.OnFragmentInteractionListener,SongPanel.OnFragmentInteractionListener {
    public int tonic;
    public ArrayList<String[]> songDetailsList;
    public HashMap<Integer,Integer> tonicMap;
    private static final int RECORD_REQUEST_CODE = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int PERMISSION_ALL = 100;
    private static String TAG = "PermissionVT";
    File mFolder;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL: {
                if (grantResults.length > 0) {
                    boolean flag=true;
                    for(int i=0;i<grantResults.length;i++){
                        if(grantResults[i] ==PackageManager.PERMISSION_GRANTED){
                            continue;
                        }else{
                            flag=false;
                        }
                    }
                    if(!flag){
                        closeNow();
                    }

                } else {
                    Log.i(TAG, "Permission has been granted by user");
                }
                break;
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);


        String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }



        //mFolder =  getDir("VarnamTutor", Context.MODE_PRIVATE); //Creating an internal dir;
        mFolder = new File(Environment.getExternalStorageDirectory(), "VarnamTutor");
        System.out.print("mFolder");
        System.out.print(mFolder);
        if (!mFolder.exists()) {
            boolean b =  mFolder.mkdirs();
            System.out.print(mFolder);

        }

        mFolder = new File(Environment.getExternalStorageDirectory(), "VarnamTutor");
        setContentView(R.layout.activity_live_pitch);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        loadSongList();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        int songId,songTonic;
        String songName;
        final Menu menu = navigationView.getMenu();

        for (int i = 0; i < songDetailsList.size(); i++) {
            songId = Integer.parseInt(songDetailsList.get(i)[0]);
            songName = songDetailsList.get(i)[1];
            songTonic = Integer.parseInt(songDetailsList.get(i)[2]);
            tonicMap.put(songId,songTonic);
            MenuItem tmp = menu.add(R.id.list_group,songId,Menu.NONE,songName);
        }
        menu.setGroupEnabled(R.id.list_group, false);
        menu.setGroupCheckable(R.id.list_group, true, true);

        android.support.v4.app.Fragment fragment = null;
        fragment = (android.support.v4.app.Fragment) new DetectTonic();
        String title = "Tonic";


        if (fragment != null) {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        drawer.closeDrawer(GravityCompat.START);



    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.live_pitch, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        android.support.v4.app.Fragment fragment = null;
        String title = getString(R.string.app_name);
        int id = item.getItemId();

        if (id == R.id.nav_tonic) {
            fragment = (android.support.v4.app.Fragment) new DetectTonic();
            title = "Tonic";
        }else if(item.getGroupId()==R.id.list_group){
            fragment = (android.support.v4.app.Fragment) new SongPanel();
            Bundle songDet = new Bundle();
            songDet.putInt("songId", item.getItemId());
            songDet.putInt("tonic", tonic);
            fragment.setArguments(songDet);
            title = item.getTitle().toString();
        }




        if (fragment != null) {
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onFragmentInteraction(Uri uri){

    }

    public void setGlobalTonic(String tonicFound,boolean open){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView tonicText = (TextView) headerView.findViewById(R.id.tonicTextShow);
        tonicText.setText("Tonic: " + tonicFound +"Hz");
        this.tonic = Integer.parseInt(tonicFound);
        final Menu menu = navigationView.getMenu();
        menu.setGroupEnabled(R.id.list_group, true);
        if(open) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.openDrawer(Gravity.LEFT);
        }



    }

    private void loadSongList(){
        tonicMap = new HashMap<Integer, Integer>();
        songDetailsList = new ArrayList<String[]>();
        String [] tmp;
        Context myContext = getApplicationContext();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(myContext.getAssets().open("conf/list")));
            String mLine;

            while ((mLine = reader.readLine()) != null) {
                mLine = mLine.trim();
                if(mLine.length()>0){
                    mLine = mLine.trim();
                    if(mLine.length()>0) {
                        tmp = mLine.split("\t");

                        songDetailsList.add(tmp);
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SongRecord getSongDetails(int songId){
        Context myContext = getApplicationContext();
        SongRecord songDet = new SongRecord();
        songDet.setSongId(songId);
        ArrayList<String> segmentNames=new ArrayList<String>();
        HashMap<String,String> segmentMap=new HashMap<String, String>();
        String[] tmp;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(myContext.getAssets().open(songId+"/metadata")));
            String mLine;
            int lineNo=0;

            String line = reader.readLine();
            while (line != null) {
                lineNo++;
                switch(lineNo) {
                    case 1:
                        songDet.setSongName(line.trim());
                        break;
                    case 2:
                        songDet.setRaga(line.trim());
                        break;

                    case 3:
                        songDet.setTonic(Integer.parseInt(line.trim()));
                        break;

                    default:
                        tmp = line.split(" ");
                        segmentNames.add(tmp[0]);
                        segmentMap.put(tmp[0],tmp[1]);
                        break;

                }
                System.out.println(line);
                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        songDet.setSegmentNames(segmentNames);
        songDet.setSegmentMap(segmentMap);

        return songDet;
    }

    public DataPoint[] getLineReference(String lineName, SongRecord songDet){
        WavSynth wavSynth= new WavSynth();
        DataPoint[] timeData;
        ArrayList<String> allData = wavSynth.getFileData(getApplicationContext(), Integer.toString(songDet.getSongId())+"/"+ songDet.getSegmentMap().get(lineName) );
        timeData = wavSynth.getPitchCurve(allData,songDet.getTonic(),tonic);
        return timeData;
    }

    public float [] getMinMax(String lineName, SongRecord songDet){
        WavSynth wavSynth= new WavSynth();
        float[] minMax;
        ArrayList<String> allData = wavSynth.getFileData(getApplicationContext(), Integer.toString(songDet.getSongId())+"/"+ songDet.getSegmentMap().get(lineName) );
        minMax = wavSynth.getMinMaxPitch(allData);
        return minMax;
    }

    public String getPitchResource(String lineName, SongRecord songDet){
        return Integer.toString(songDet.getSongId())+"/"+ songDet.getSegmentMap().get(lineName);
    }


    public File getGenWav(String lineName, SongRecord songDet){
        File wavFile;
        wavFile = new File(mFolder,songDet.getSongId()+"_"+ songDet.getSegmentMap().get(lineName)+".wav");
        return wavFile.getAbsoluteFile();
    }

    public void writeToFile(String fname,ArrayList<String> dataList) {
        File dataFile;
        FileWriter fWriter;
        dataFile = new File(mFolder,fname);
        try{
            fWriter = new FileWriter(dataFile.getAbsoluteFile());
            for(String data:dataList)
                fWriter.write(data+"\n");
                fWriter.flush();
            fWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }


    protected void makeRequestAudio() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},RECORD_REQUEST_CODE);
    }

    protected void makeRequestStorage() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_REQUEST_CODE);
    }

    private void closeNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            finish();
        }
    }


}


