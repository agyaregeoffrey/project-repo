package com.activeminds.projectrepo.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.activeminds.projectrepo.R;
import com.activeminds.projectrepo.models.IntroScreenModel;
import com.activeminds.projectrepo.adapters.IntroViewPagerAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    public static final String MY_PREF = "myPref";
    public static final String IS_INTRO_OPENED = "isIntroOpened";
    private static final String TAG = "loginActivity";
    private static final int RC_SIGN_IN = 101;
    private ViewPager mViewPager;
    private IntroViewPagerAdapter mIntroViewPagerAdapter;
    private List<IntroScreenModel> mIntroScreenModels;
    TabLayout mTabIndicator;
    Button mButtonNext;
    Button signInButton;
    TextView whyLogin;
    int mCurrentPosition;
    Animation buttonAnimation;

    private boolean isLastScreenLoaded = false;

    private FirebaseAuth mFirebaseAuth;
    private GoogleSignInClient googleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        if(restorePrefData()){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        mIntroScreenModels = new ArrayList<>();
        mIntroScreenModels.add(new IntroScreenModel(getString(R.string.intro_title1), getString(R.string.intro_desc1), R.drawable.image1));
        mIntroScreenModels.add(new IntroScreenModel(getString(R.string.intro_title2), getString(R.string.intro_desc2), R.mipmap.image2));
        mIntroScreenModels.add(new IntroScreenModel(getString(R.string.intro_title3), getString(R.string.intro_desc3), R.drawable.image3));

        // viewPager adapter setup
        mViewPager = findViewById(R.id.screen_viewpager);
        mIntroViewPagerAdapter = new IntroViewPagerAdapter(this, mIntroScreenModels);
        mViewPager.setAdapter(mIntroViewPagerAdapter);

        mTabIndicator = findViewById(R.id.tab_indicator);
        mTabIndicator.setupWithViewPager(mViewPager);

        mTabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == mIntroScreenModels.size() - 1) {
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        signInButton = findViewById(R.id.sign_in_button);
        whyLogin = findViewById(R.id.textView_why_login);
        openMainActivity();

        buttonAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_animation);
        mButtonNext = findViewById(R.id.button_next);
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPosition = mViewPager.getCurrentItem();
                if(mCurrentPosition < mIntroScreenModels.size()) {
                    mCurrentPosition++;
                    mViewPager.setCurrentItem(mCurrentPosition);
                }

                if(mCurrentPosition == mIntroScreenModels.size()){
                    // show the GET STARTED BUTTON
                    loadLastScreen();
                }
            }
        });

        whyLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whyLoginClicked();
            }
        });

        // sign in flow
        mFirebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkConnected()){
                    new MaterialAlertDialogBuilder(IntroActivity.this)
                            .setTitle(getString(R.string.no_internet))
                            .setMessage(getString(R.string.internet_needed))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                } else {
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            }
        });


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(isLastScreenLoaded){
            MenuItem menuItem = menu.findItem(R.id.action_skip);
            if(menuItem != null) {
                menuItem.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.intro_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_skip:{
                moveToLastTab();
                loadLastScreen();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            try {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
                onLoggedIn(account);
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (lastSignedInAccount != null) {
            //Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show();
            onLoggedIn(lastSignedInAccount);
        } else {
            Log.d(TAG, "Not logged in");
        }
    }

    private void onLoggedIn(GoogleSignInAccount googleSignInAccount) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.GOOGLE_ACCOUNT, googleSignInAccount);
        startActivity(intent);
        finish();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
//                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void whyLoginClicked() {
        new MaterialAlertDialogBuilder(IntroActivity.this)
                .setMessage(R.string.why_login_text)
                .setNegativeButton("Ok", null)
                .show();
    }

    private void loadLastScreen() {
        mButtonNext.setVisibility(View.INVISIBLE);
        mTabIndicator.setVisibility(View.INVISIBLE);
        signInButton.setVisibility(View.VISIBLE);
        whyLogin.setVisibility(View.VISIBLE);

        signInButton.setAnimation(buttonAnimation);
        whyLogin.setAnimation(buttonAnimation);
        isLastScreenLoaded = true;
        invalidateOptionsMenu();
    }

    private void openMainActivity(){
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                finish();
            }
        });
    }

    private boolean isNetworkConnected(){
        ConnectivityManager connectivityManager =  (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null;
    }

    private void moveToLastTab() {
        mCurrentPosition += 2;
        mViewPager.setCurrentItem(mCurrentPosition, true);
    }

    private void savePreferences() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(MY_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isIntroOpened", true);
        editor.commit();
    }

    private boolean restorePrefData(){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(MY_PREF, MODE_PRIVATE);
        Boolean hasIntroBeenOpened = preferences.getBoolean(IS_INTRO_OPENED, false);
        return hasIntroBeenOpened;
    }

}
