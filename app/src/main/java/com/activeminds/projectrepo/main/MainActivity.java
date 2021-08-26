package com.activeminds.projectrepo.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeminds.projectrepo.R;
import com.activeminds.projectrepo.fcm.MyFirebaseMessagingService;
import com.activeminds.projectrepo.openchannel.OpenChannelActivity;
import com.activeminds.projectrepo.uploadproject.UploadProjectActivity;
import com.activeminds.projectrepo.utils.PreferenceUtils;
import com.activeminds.projectrepo.utils.PushUtils;
import com.activeminds.projectrepo.widgets.WaitingDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.SendBirdPushHelper;
import com.sendbird.android.User;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 13;

    public static final String TAG = "MainActiviy";
    private AppBarConfiguration mAppBarConfiguration;
    public static final String GOOGLE_ACCOUNT = "google_sign_in";
    private DrawerLayout mDrawerLayout;
    private ImageView mStudentImage;
    private NavigationView mNavigationView;

    private GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount mUserDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // check network connectivity
        if(!isNetworkConnected()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.no_internet)
                    .setMessage(R.string.internet_needed)
                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
        }

        requestStoragePermission();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, OpenChannelActivity.class)));
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_projects)
                .setOpenableLayout(mDrawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigationView, navController);

        retrieveSignInData();
        checkExtra();
        //connectToSendBird(mUserDetails.getId(), mUserDetails.getDisplayName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUserDetails != null)
            connectToSendBird(mUserDetails.getId(), mUserDetails.getDisplayName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                return true;
            case R.id.action_upload:
                startActivity(new Intent(MainActivity.this, UploadProjectActivity.class));
                return true;
            case R.id.action_signOut:
                signOutFlow();
                return true;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }




    // custom methods start here
    private void retrieveSignInData() {
        mUserDetails = getIntent().getParcelableExtra(GOOGLE_ACCOUNT);
        View navHeader = mNavigationView.getHeaderView(0);
        mStudentImage = navHeader.findViewById(R.id.imageView_student);
        TextView userName = navHeader.findViewById(R.id.textView_userName);
        TextView email = navHeader.findViewById(R.id.textView_email);

        if (mUserDetails != null) {
            String imageUrl = mUserDetails.getPhotoUrl() != null ? mUserDetails.getPhotoUrl().toString() : null;
            Picasso.get().load(imageUrl)
                    .centerInside()
                    .fit()
                    .into(mStudentImage);
            email.setText(mUserDetails.getEmail());
            userName.setText(mUserDetails.getDisplayName());
        }

    }

    private void signOutFlow() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                disconnect();
                Intent intent = new Intent(MainActivity.this, IntroActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG)
                .show();
    }

    // sendbird implementation
    /**
     * Attempts to connect a user to SendBird.
     * @param userId    The unique ID of the user.
     * @param userNickname  The user's nickname, which will be displayed in chats.
     */
    private void connectToSendBird(final String userId, final String userNickname) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userNickname)) {
            return;
        }
        // Show the loading indicator
        showProgressBar(true);
        ConnectionManager.login(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                // Callback received; hide the progress bar.
                showProgressBar(false);

                if (e != null) {
                    // Error!
//                    Toast.makeText(
//                            LoginActivity.this, "" + e.getCode() + ": " + e.getMessage(),
//                            Toast.LENGTH_SHORT)
//                            .show();

                    // Show login failure snackbar
                    //showSnackbar("Login to SendBird failed");
                    return;
                }

                PreferenceUtils.setConnected(true);

                // Update the user's nickname
                updateCurrentUserInfo(userNickname);
                PushUtils.registerPushHandler(new MyFirebaseMessagingService());

                // Proceed to MainActivity
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//                finish();
            }
        });
    }

    /**
     * Updates the user's nickname.
     * @param userNickname  The new nickname of the user.
     */
    private void updateCurrentUserInfo(final String userNickname) {
        SendBird.updateCurrentUserInfo(userNickname, null, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    // Error!
//                    Toast.makeText(
//                            LoginActivity.this, "" + e.getCode() + ":" + e.getMessage(),
//                            Toast.LENGTH_SHORT)
//                            .show();

                    // Show update failed snackbar
                    //showSnackbar("Update user nickname failed");

                    return;
                }

                PreferenceUtils.setNickname(userNickname);
            }
        });
    }

    private void disconnect() {
        PushUtils.unregisterPushHandler(new SendBirdPushHelper.OnPushRequestCompleteListener() {
            @Override
            public void onComplete(boolean isActive, String token) {
                ConnectionManager.logout(new SendBird.DisconnectHandler() {
                    @Override
                    public void onDisconnected() {
                        PreferenceUtils.setConnected(false);
                        finish();
                    }
                });
            }

            @Override
            public void onError(SendBirdException e) {
            }
        });
    }

    private void requestStoragePermission () {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if(permissionDeniedResponse.isPermanentlyDenied()) {
                            new MaterialAlertDialogBuilder(MainActivity.this)
                                    .setTitle("Permission")
                                    .setMessage(R.string.permissions_required)
                                    .show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }

    // Shows or hides the ProgressBar
    private void showProgressBar(boolean show) {
        if (show) {
            WaitingDialog.show(this);
        } else {
            WaitingDialog.dismiss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkExtra();
    }

    private void checkExtra() {
        if (getIntent().hasExtra("groupChannelUrl")) {
            String extraChannelUrl = getIntent().getStringExtra("groupChannelUrl");
            Intent mainIntent = new Intent(MainActivity.this, OpenChannelActivity.class);
            mainIntent.putExtra("groupChannelUrl", extraChannelUrl);
            startActivity(mainIntent);
        }
    }

    private boolean isNetworkConnected(){
        ConnectivityManager connectivityManager =  (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null;
    }

}
