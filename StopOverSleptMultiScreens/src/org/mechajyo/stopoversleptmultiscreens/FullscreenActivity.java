package org.mechajyo.stopoversleptmultiscreens;

import org.mechajyo.stopoversleptmultiscreens.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.mechajyo.stopoversleptmultiscreens.util.SystemUiHider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener, SearchView.OnQueryTextListener{
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	double okiro_km = 0.0;
	private LocationClient mLClient;
	private LocationRequest mLRequest;
	private Vibrator viberator = null;
	
	private boolean firstVibe = true;
	private MediaPlayer mp = null;
	
	enum SandV {
		SOUND_ONLY,
		VIBELATION_ONLY,
		SOUND_AND_VIBELATION
	}
	
	SandV sv = SandV.SOUND_AND_VIBELATION;
	
	Location now = null;
	Address  target = null;
	String   query = "";
	
	@SuppressLint({ "WorldReadableFiles", "WorldWriteableFiles" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final FullscreenActivity fsact = this;
		setContentView(R.layout.activity_fullscreen);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);
        final SearchView sview = (SearchView) findViewById(R.id.searchView1);
        // SearchViewの初期表示状態を設定
        sview.setIconifiedByDefault(false);
 
        // SearchViewにOnQueryChangeListenerを設定
        sview.setOnQueryTextListener((OnQueryTextListener) this);
 
        // SearchViewのSubmitボタンを使用不可にする
        sview.setSubmitButtonEnabled(false);
 
        // SearchViewに何も入力していない時のテキストを設定
        sview.setQueryHint("検索文字を入力して下さい。");

        // Get instance of Vibrator from current Context
		this.viberator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    	@SuppressWarnings("deprecation")
		SharedPreferences pref =
    			getSharedPreferences("pref",MODE_WORLD_READABLE|MODE_WORLD_WRITEABLE);
    	String str = pref.getString("target","");
    	Log.v("SAVE",str);
    	if (! str.equals("")) {
    		sview.setQuery(str,true);
    	}

    	final Button button = (Button) findViewById(R.id.go_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
        		if (sv != SandV.SOUND_ONLY) {
        			fsact.viberator.cancel();
        		}
        		if (mp.isPlaying()) {
        			mp.pause();
        		}
            }
        });
        


		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
//		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
//				HIDER_FLAGS);
//		mSystemUiHider.setup();
//		mSystemUiHider
//				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
//					// Cached values.
//					int mControlsHeight;
//					int mShortAnimTime;
//
//					@Override
//					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//					public void onVisibilityChange(boolean visible) {
//						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//							// If the ViewPropertyAnimator API is available
//							// (Honeycomb MR2 and later), use it to animate the
//							// in-layout UI controls at the bottom of the
//							// screen.
//							if (mControlsHeight == 0) {
//								mControlsHeight = controlsView.getHeight();
//							}
//							if (mShortAnimTime == 0) {
//								mShortAnimTime = getResources().getInteger(
//										android.R.integer.config_shortAnimTime);
//							}
//							controlsView
//									.animate()
//									.translationY(visible ? 0 : mControlsHeight)
//									.setDuration(mShortAnimTime);
//						} else {
//							// If the ViewPropertyAnimator APIs aren't
//							// available, simply show or hide the in-layout UI
//							// controls.
//							controlsView.setVisibility(visible ? View.VISIBLE
//									: View.GONE);
//						}
//
//						if (visible && AUTO_HIDE) {
//							// Schedule a hide().
//							delayedHide(AUTO_HIDE_DELAY_MILLIS);
//						}
//					}
//				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//				if (TOGGLE_ON_CLICK) {
//					mSystemUiHider.toggle();
//				} else {
//					mSystemUiHider.show();
//				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
//		findViewById(R.id.dummy_button).setOnTouchListener(
//				mDelayHideTouchListener);
		
		
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         // アイテムを追加します
        adapter.add("0.01");
        adapter.add("0.1");
        adapter.add("1.0");
        adapter.add("2.0");
        adapter.add("5.0");
        Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
        // アダプターを設定します
        spinner1.setAdapter(adapter);
        // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                 Spinner sp = (Spinner) parent;
                 String item = (String) sp.getSelectedItem();
                 fsact.okiro_km = Double.parseDouble(item);
                 InputMethodManager imm=(InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(sview.getWindowToken(), 0);
            }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         // アイテムを追加します
        adapter2.add("音");
        adapter2.add("バイブレーション");
        adapter2.add("音とバイブレーション");
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        // アダプターを設定します
        spinner2.setAdapter(adapter2);
        // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                 Spinner sp = (Spinner) parent;
                 String item = (String) sp.getSelectedItem();
                 if (item.equals("音")) {
                	 sv = SandV.SOUND_ONLY;
                 } else if (item.equals("バイブレーション")) {
                	 sv = SandV.VIBELATION_ONLY;
                 } else if (item.equals("音とバイブレーション")) {
                	 sv = SandV.SOUND_AND_VIBELATION;
                 } else {
                 	//
                 }
                 InputMethodManager imm=(InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(sview.getWindowToken(), 0);
            }

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         // アイテムを追加します
        adapter3.add("ぴったり");
        adapter3.add("ほぼ");
        adapter3.add("たぶん（エコ）");
        Spinner spinner0 = (Spinner) findViewById(R.id.spinner0);
        // アダプターを設定します
        spinner0.setAdapter(adapter3);
        // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
        spinner0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                int position, long id) {
                Spinner sp = (Spinner) parent;
                String item = (String) sp.getSelectedItem();
 				if (item.equals("ぴったり")) {
 					fsact.mLRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 				} else if (item.equals("ほぼ")) {
 					fsact.mLRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
 				} else { // たぶん（えこw）
 					fsact.mLRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
 				}
 				
 				fsact.mLClient.requestLocationUpdates(mLRequest, fsact);
                InputMethodManager imm=(InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(sview.getWindowToken(), 0);
            }
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
        });
        spinner0.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm=(InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        }) ;
        final int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Google Play service is not available (status=" + result + ")", Toast.LENGTH_LONG).show();
            finish();
        }
        this.mLRequest = LocationRequest.create();
        this.mLRequest.setInterval(5000);
        this.mLRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        this.mLRequest.setFastestInterval(1000);
        this.mLClient = new LocationClient(this,this,this);

        this.mp = MediaPlayer.create(this, R.raw.okiro3);

    }
    @SuppressLint({ "WorldReadableFiles", "WorldWriteableFiles" })
	@Override
    protected void onPause(){
        super.onPause();
        SharedPreferences pref =
        		getSharedPreferences("pref",MODE_WORLD_READABLE|MODE_WORLD_WRITEABLE);
        Editor e = pref.edit();
        e.putString("target", this.query);
        e.commit();
        Log.v("SAVE","SAVE "+this.query);
    }
    
    @SuppressLint({ "WorldReadableFiles", "WorldWriteableFiles" })
	@Override
    protected void onRestart() {
    	super.onRestart();
    	SharedPreferences pref =
    			getSharedPreferences("pref",MODE_WORLD_READABLE|MODE_WORLD_WRITEABLE);
    	String str = pref.getString("target","");
    	Log.v("SAVE",str);
    	this.onQueryTextSubmit(str);
    }
    

	
	@Override
    protected void onResume(){
        super.onResume();
        mLClient.connect();
	
	}

	
	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
	    // Do something in response to button
		Log.v("BUTTON","Push Map Button!");
		Intent intent = new Intent(this, MapActivity.class);
		startActivity(intent);
	}
	
	/** Called when the user clicks the Send button */
	public void sendMessageGo(View view) {
	    // Do something in response to button
		Log.v("BUTTON","Push Go Button!");
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			//mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		//mHideHandler.removeCallbacks(mHideRunnable);
		//mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		this.mLClient.requestLocationUpdates(mLRequest, (LocationListener) this);
		//Location lastLocation = mLClient.getLastLocation();
	
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStop()
	{
		mLClient.disconnect();
		super.onStop();
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		this.now = arg0;
		Log.v("GEO",arg0.toString());
		if (this.target != null) {
			float km = (float) (calcDistance(target.getLatitude(),target.getLongitude(),this.now) / 1000.0);
			TextView textView4 = (TextView)findViewById(R.id.textView4);
			textView4.setText("あと"+ Float.toString(km)+"Km");

			if ((km < okiro_km)&&(this.firstVibe)) {
				this.firstVibe = false;
				vibe();
			}
		}
	}

	public float calcDistance(double lat, double lon, Location here)
	{
		float [] results = new float[3];
		if (here == null) {
			return (float)4649.0; // よろしく（magic number）
		}
		Location.distanceBetween(here.getLatitude(), here.getLongitude(), lat, lon, results);
		return results[0];
	}


	@Override
	public boolean onQueryTextChange(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean onQueryTextSubmit(String arg0) {
		// TODO Auto-generated method stub
		this.query = String.copyValueOf(arg0.toCharArray());
		
		TextView textView4 = (TextView)findViewById(R.id.textView4);
        Geocoder geocoder = new Geocoder( this, Locale.getDefault());
        //List<String> providers = locman.getProviders(true);

        this.firstVibe = true;
        
        try{
        	boolean b = false;
            List<Address> addressList = geocoder.getFromLocationName(arg0, 1);
            if (addressList.size() == 0) {
                if (Geocoder.isPresent() == false)
                {
                	textView4.setText("background service is not available!");
                }
                else
                {
                	textView4.setText("見つかりませんでした");
                }
                b = true;
            }
            if (!b) {
            	Address address = addressList.get(0);
            	this.target = address;
        		float km = (float) (calcDistance(target.getLatitude(),target.getLongitude(),this.now) / 1000.0);
                textView4.setText("あと"+ Float.toString(km)+"Km");
            }
        }catch(IOException e){
        	textView4.setText("IOException 発生");
        }

		return false;
	}
	
	public void vibe()
	{
		if ((sv == SandV.SOUND_AND_VIBELATION)||(sv == SandV.SOUND_ONLY)) {		
			if (mp.isPlaying()) {
				//mp.pause();
			}
			mp.seekTo(0);
			mp.start();
		}
		// Start without a delay
		// Each element then alternates between vibrate, sleep, vibrate, sleep...
		// long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
		long[] pattern = {1000,2500,1000,2500,1000,2500,1000,2500,1000,2500,1000,2500,1000,2500,1000,2500,1000,2500,1000,2500,1000,2500,1000,2500,1000,2500,1000,2500,1000,2500}; 
		// The '-1' here means to vibrate once
		// '0' would make the pattern vibrate indefinitely
		if ((sv == SandV.VIBELATION_ONLY)||(sv == SandV.SOUND_AND_VIBELATION)) {
			this.viberator.vibrate(pattern, -1);
		}
	}
	
	
}
