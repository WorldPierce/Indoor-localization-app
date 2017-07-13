package com.fastcsu.fastcampusnavigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//double tap event handler

//TODO on picture select make p variable equal to the number of entries in the database
public class UploadActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private static final int RC_OPEN_FILE = 420;
    private static final int SELECT_PICTURE = 100;
    public Spinner building;
    public EditText floor;
    public ImageView imageView;
    private GestureDetector mDetector;
    //public WifiManager wifiManager;
    WifiManager wifi;

    WifiReceiver receiverWifi;
    StringBuilder sb = new StringBuilder();
    private final Handler handler = new Handler();
    Set<Point> uniqueWifi;
    //ListView lv;
    //TextView textStatus;
    //Button buttonScan;
    int p;
    List<ScanResult> results;
    String ITEM_KEY = "key";
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    SimpleAdapter adapter;
    ArrayList<String> bssidRecieved = new ArrayList<>();
    ArrayList<Integer> rssiRecieved = new ArrayList<>();

    int touchX;
    int touchY;
    int iterator;
    boolean storeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        iterator = 1;
        p = 1;
        storeData = false;
        //firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://navupload-8b2c0.appspot.com");
        mDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl("https://navupload-8b2c0.firebaseio.com/");
        building = (Spinner) findViewById(R.id.building);
        floor = (EditText) findViewById(R.id.floor);
        imageView = (ImageView) findViewById(R.id.imageView);
        mDetector = new GestureDetector(this, new MyGestureDetector());

//        bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);

        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            //loadLogInView();
        }
        //wifi
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if(wifi.isWifiEnabled()==false)
        {
            wifi.setWifiEnabled(true);
        }

        //starts wifi scan and collection
        //doInback();

        //imageview on touch listener for adding wifi values
        imageView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int[] viewCoords = new int[2];
                imageView.getLocationOnScreen(viewCoords);
                //these are the coordinates we want!
                touchX = (int) event.getX();
                touchY = (int) event.getY();

                Log.d("coords", touchX+" "+touchY);

                mDetector.onTouchEvent(event);

                return true;
            }});
    }
    
    //making sure we have proper parameters to save photo
    public void uploadPhoto(View v){
        Log.v("floor/buliding values", building.getSelectedItem().toString() + floor.getText().toString());
        if(building.getSelectedItem().toString().matches("") || floor.getText().toString().matches("")){

            Toast.makeText(UploadActivity.this, "Please enter building intials and floor number before selecting a photo", Toast.LENGTH_LONG).show();
        }
        else{
            selectPhoto();
        }
    }

    public void downloadPhoto(View v){
        //TODO add check for valid room number and building

        //getting storage refernce for image
        StorageReference storageReference = mStorage.child(building.getSelectedItem().toString()).child(floor.getText().toString());
        //adding photo to our imageview
        Glide.with(UploadActivity.this /* context */)
                .using(new FirebaseImageLoader())
                .load(storageReference)
                .into(imageView);

    }

    //selecting photo from phone
    public void selectPhoto(){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }
    //after photo selected this will begin the saving process
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    firebaseUploadPicture(data.getData());
                }
                //file = data.getData();
            }
        }
    }

    private void firebaseUploadPicture(final Uri file) {
        // Display progress dialog and disable input
        final StorageReference photoRef = mStorage.child(building.getSelectedItem().toString()).child(floor.getText().toString());

        // Upload file to Firebase Storage
        //Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                        Toast.makeText(UploadActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        Toast.makeText(UploadActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //handling all the recieved wifi signals and adding to database
    class WifiReceiver extends BroadcastReceiver
    {
        public void onReceive(Context c, Intent intent)
        {

            String floorAndNumber;
            if(building.getSelectedItem().toString().matches("") || floor.getText().toString().matches("")){

                Toast.makeText(UploadActivity.this, "Please enter building intials and floor number(Upload - onRecieve)", Toast.LENGTH_LONG).show();
                return;
            }
            else{
                floorAndNumber = building.getSelectedItem().toString()+floor.getText().toString();
            }
            //TODO scan table to see how many scans are already in
            ArrayList<String> connections=new ArrayList<String>();
            ArrayList<Integer> Signal_Strenth= new ArrayList<Integer>();
            ArrayList<Point> recieved = new ArrayList<Point>();
            Coordinates coords =  new Coordinates(touchX,touchY);

            sb = new StringBuilder();
            List<ScanResult> wifiList;
            wifiList = wifi.getScanResults();

            String scan = Integer.toString(iterator);


            DatabaseReference ref = mDatabase.child(floorAndNumber).child("points");
            for(int i = 0; i < wifiList.size(); i++)
            {
                //String scan = Integer.toString(iterator);

                //TODO global variable that stores this info on form load.

                Signal_Strenth.add(wifiList.get(i).level);
                //frequency = 2412 wifiList.get(i).level = -55 wifiList.get(i).BSSID = 01:80:c2:00:00:03 wifiList.get(i).SSID = csuguest
//                Log.v("signal", wifiList.get(i).SSID+" "+wifiList.get(i).BSSID + " "+wifiList.get(i).level + " " + wifiList.get(i).toString() );
                if(wifiList.get(i).SSID.equals("csuguest")  || wifiList.get(i).SSID.equals("4csuuseonly")){
                    Point newPoint = new Point(wifiList.get(i).BSSID,wifiList.get(i).level,touchX,touchY);

                    connections.add(wifiList.get(i).SSID);
                    //collecting all points into local list
                    recieved.add(newPoint);
//                    Log.v("signal", wifiList.get(i).toString() );
                    //coords = new Coordinates(touchX,touchY);

                    //Log.v("coords", coords.toString());
//
//                    //Map creates points with first arg as parent and second as child
                    Map<String, Point> points = new HashMap<String, Point>();
                    //check if this call came from map double tap


//
                    if(!coords.toString().equals("(0,0)")){
                        points.put(scan, new Point(wifiList.get(i).BSSID,wifiList.get(i).level,touchX,touchY));
                        ref.child(coords.toString()).setValue(recieved);
                    }

//                    iterator++;

                }

            }
            //putting all collected points into our global list
            uniqueWifi = new HashSet<>(recieved);
            Iterator set = uniqueWifi.iterator();
            //create score for recieved wifi and add the bssid's to a List to use contains to determine penalty.
            //next call function to determine score for all stored points and find closest to the recieved wifi

            while(set.hasNext()){
                Point p = (Point) set.next();
                Log.v("uniquWifi", p.BSSID.toString());
                bssidRecieved.add(p.BSSID.toString());
                rssiRecieved.add((p.sig_str));

            }
            //end the wifi scan
            unregisterReceiver(receiverWifi);
            //call function to determine clossest point
            //compare();
        }
    }

    private void updateDatabase(Set<Point> uniqueWifi) {
        for(int i = 0; i < uniqueWifi.size(); i++){
            DatabaseReference ref = mDatabase.child("FH01").child("points");
            ref.setValue(uniqueWifi);

        }
    }

    //loop this to do continuous wifi scans
    public void doInback()
    {
        handler.postDelayed(new Runnable() {

            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

                receiverWifi = new WifiReceiver();
                registerReceiver(receiverWifi, new IntentFilter(
                        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifi.startScan();
//                if(size < 10){
                //this loops the scan for navigation
//                    doInback();
//                }
//                size++;
            }
        }, 1000);

    }
    //class for handling point ID and strength
    public static class Point {

        public String BSSID;
        public int sig_str;
        public int x;
        public int y;

        public Point(String BSSID, int sig_str, int x, int y) {
            // constructor for tapping
            this.BSSID = BSSID;
            this.sig_str = sig_str;
            this.x = x;
            this.y = y;
        }
        public Point(String BSSID, int sig_str) {
            // constructor for scanning
            this.BSSID = BSSID;
            this.sig_str = sig_str;
        }
    }
    //class for formatting coordinates
    public class Coordinates {
        int x;
        int y;

        public Coordinates(int x , int y){
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString(){
            return "("+this.x+","+this.y+")";
        }
    }
    //double tap event handler
    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener
    {

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            //turn on storing data
            //storeData = true;


            doInback();
            Coordinates coords = new Coordinates(touchX,touchY);
            Toast.makeText(UploadActivity.this, "#" + Integer.toString(p)+" ADDED: "+coords.toString(),Toast.LENGTH_SHORT).show();
            p++;
            //turn off storing data
            //storeData = false;
            return true;
        }
    }

    public void locate(View view){
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }
    //finds closest point
    public void compare(){
        //DatabaseReference dinosaursRef = mDatabase.child("points");
        //TODO uncomment
//        String floorAndNumber;
//        if(building.getSelectedItem().toString().matches("") || floor.getText().toString().matches("")){
//
//            Toast.makeText(UploadActivity.this, "Please enter building intials and floor number(Upload - compare)", Toast.LENGTH_LONG).show();
//            return;
//        }
//        else{
//            floorAndNumber = building.getSelectedItem().toString()+floor.getText().toString();
//        }
//        Query query = mDatabase.orderByChild(floorAndNumber);
//        query.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
//                //System.out.println("Key " + dataSnapshot.getKey() + " Value " + dataSnapshot.getValue());
//                //TODO make app wait for this to terminate.
//
//
//                //doInback();
////                Log.v("unique wifi", uniqueWifi.toString());
//
//
//                Iterable<DataSnapshot> sig = dataSnapshot.child("points").getChildren();
//
////                Iterable<DataSnapshot> child;
////                for(DataSnapshot s : sig){
////                    Log.v("iterate", s.getChildren().toString());
////                }
//
//                //loops through every point
//                float lowScore = 1000000000;
//                String nearestCoords = "fail";
//                for (DataSnapshot alert: sig) {
//                    float score = 0;
//                    Log.d("score set", String.valueOf(score));
//
//                    //key is my x,y coordinates
//                    String key = alert.getKey().toString();
//                    Log.v("key", key);
//                    //loops through database (BSSID, sig_str) for each set of coordinates
//                    for (DataSnapshot recipient: alert.getChildren()) {
//                        //recipient.child("sig_str").getValue() = strength of coords in db
//                        //rssiRecieved.get(index).toString() = strength of signal currently recieved
//                        //System.out.println(recipient.child("sig_str").getValue() + " ID " + recipient.child("BSSID").getValue());
//                        //check if BSSID is in uniqueWifi
//                        //yes -> compare strengths regularly
//                        //no -> apply penalty
//                        if(bssidRecieved.contains(recipient.child("BSSID").getValue().toString())){
//                            //Log.v("Does contain", recipient.child("BSSID").getValue().toString());
//                            int index = bssidRecieved.indexOf(recipient.child("BSSID").getValue().toString());
//                            //Log.d("recieved strength", rssiRecieved.get(index).toString());
//                            //Log.d("stored strength", recipient.child("sig_str").getValue().toString());
//                            //score += Math.abs(Math.abs(rssiRecieved.get(index)) - Math.abs(((Long) recipient.child("sig_str").getValue())));
//                            score += Math.pow(rssiRecieved.get(index) - (Long) recipient.child("sig_str").getValue(),2);
//                            //score += Math.sqrt(Math.abs(rssiRecieved.get(index) - (Long) recipient.child("sig_str").getValue()));
//                            Log.d("point found score ", String.valueOf(score));
//                        }
//                        else{
//                            score += Math.pow(25,2);
//                            //scanned point not stored
//                            //score += Math.sqrt(30);
//                            Log.d("penalty score ", String.valueOf(score));
//                            //penalty for not recieving bssid
//                        }
//
//                    }
//                    //stored has more points then recient scan
//                    if(rssiRecieved.size() > alert.getKey().length()){
//                        score += (rssiRecieved.size() - alert.getKey().length()) * Math.pow(25,2);
//                        //score += Math.sqrt((rssiRecieved.size() - alert.getKey().length()) * 30);
//
//                        //Log.d("not recieved penalty = ", String.valueOf(score));
//                    }
//                    if(score < lowScore){
//                        lowScore = score;
//                        nearestCoords = key;
//                        Log.d("closest point", nearestCoords.toString() + "score " + score);
//                    }
//                    Log.d("point",  alert.getKey().toString() +"score " + score);
//                }
//                Log.d("closest point", nearestCoords.toString());
//                int x = Integer.parseInt(nearestCoords.substring(1,nearestCoords.indexOf(",")));
//                int y = Integer.parseInt(nearestCoords.substring(nearestCoords.indexOf(",")+1, nearestCoords.indexOf(")")));
//                //Log.d("x y", x +" "+  y);
//                //attempt at drawing circle at location
////                Paint paint = new Paint();
////                paint.setAntiAlias(true);
////                paint.setColor(Color.RED);
////                //Log.d("width height",imageView.getWidth() +" "+  imageView.getHeight());
////                Bitmap canvasBitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
////                Canvas canvas = new Canvas(canvasBitmap);
////                canvas.drawBitmap(canvasBitmap, 0, 0, null);
////                canvas.drawCircle(x,y, 10, paint);
////                ImageView dotView = (ImageView) findViewById(R.id.imageView2);
////
////                dotView.setImageDrawable(new BitmapDrawable(getResources(), canvasBitmap));
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//
//
//        });
    }




}
