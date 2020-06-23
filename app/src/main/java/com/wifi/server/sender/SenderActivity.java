package com.wifi.server.sender;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.wifi.server.R;
import com.wifi.server.base.BaseActivity;
import com.wifi.server.dialogs.DialogEventListener;
import com.wifi.server.dialogs.DialogHelper;
import com.wifiscanner.listener.WifiP2PConnectionCallback;
import com.wifiscanner.service.WifiP2PService;
import com.wifiscanner.service.WifiP2PServiceImpl;
import com.wifiscanner.utils.Utils;

import java.util.Map;

public class SenderActivity extends BaseActivity implements WifiP2PConnectionCallback, OnSenderUIListener, DialogEventListener {

    private boolean isSuccess = Boolean.FALSE;
    public WifiP2PService wifiP2PService;
    String fullMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        initToolBar();
        updateTitle(getString(R.string.title_receiver));
        redirectToDeviceListScreen();

        wifiP2PService = new WifiP2PServiceImpl.Builder()
                .setSender(this)
                .setWifiP2PConnectionCallback(this)
                .build();
        wifiP2PService.onCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        wifiP2PService.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        wifiP2PService.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiP2PService.onDestroy();
    }

    @Override
    public void redirectToDeviceListScreen() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                DeviceListFragment.newInstance()).commitAllowingStateLoss();
    }

    @Override
    public void onScanStarted() {
        Fragment fragment = getCurrentFragment();
        if(fragment instanceof DeviceListFragment) ((DeviceListFragment)fragment).onScanStarted();
    }

    @Override
    public void onDeviceAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Fragment fragment = getCurrentFragment();
        if(fragment instanceof DeviceListFragment) ((DeviceListFragment)fragment).onDeviceAvailable(wifiP2pDeviceList);
    }


    @Override
    public void redirectToProcessScreen(WifiP2pDevice wifiP2pDevice) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, SenderFragment.newInstance(wifiP2pDevice))
                .commitAllowingStateLoss();
    }

    @Override
    public void onConnectionCompleted() {
        Fragment fragment = getCurrentFragment();
        if(fragment instanceof SenderFragment) ((SenderFragment)fragment).onConnectCompleted();
    }

    @Override
    public void onDataTransferStarted() {
        Fragment fragment = getCurrentFragment();
        if(fragment instanceof SenderFragment) ((SenderFragment)fragment).onDataStarted();
    }

    @Override
    public void onDataTransferCompleted(String s) {
        redirectToSuccessScreen(R.drawable.ic_p_success, s);
    }

    @Override
    public void redirectToSuccessScreen(final int imageResourceId, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!isSuccess) {
                    //isSuccess = Boolean.TRUE;
                    if(message.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$")) {
                        byte[] imageBytes = Base64.decode(message, Base64.DEFAULT);
                        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        Fragment fragment = getCurrentFragment();
                        if (fragment instanceof SenderFragment) {
                            ((SenderFragment) fragment).decodeAndDisplayImage(decodedImage);
                        }
                    } else {
                        //DialogHelper.getInstance().displayDialog(SenderActivity.this, fullMessage += message,
                        //        imageResourceId, false, SenderActivity.this);
                        Toast.makeText(SenderActivity.this, "Message received" + message, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }


    private Fragment getCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        return currentFragment;
    }

    @Override
    public void onPositiveButtonClicked(View view) {
        finish();
    }

    @Override
    public void onInitiateDiscovery() {
        onScanStarted();
    }

    @Override
    public void onDiscoverySuccess() {

    }

    @Override
    public void onDiscoveryFailure() {

    }

    @Override
    public void onPeerAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        onDeviceAvailable(wifiP2pDeviceList);
    }

    @Override
    public void onPeerStatusChanged(WifiP2pDevice wifiP2pDevice) {
        Toast.makeText(this, "onPeerStatusChanged", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPeerConnectionSuccess() {
        onConnectionCompleted();
        onDataTransferStarted();
    }

    @Override
    public void onPeerConnectionFailure() {

    }

    @Override
    public void onPeerDisconnectionSuccess() {
        Toast.makeText(this, "onPeerDisconnectionSuccess", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPeerDisconnectionFailure() {
        Toast.makeText(this, "onPeerDisconnectionFailure", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDataTransferring() {}

    @Override
    public void onDataTransferredSuccess() {}

    @Override
    public void onDataTransferredFailure() {}

    @Override
    public void onDataReceiving() {
        onDataTransferStarted();
    }

    @Override
    public void onDataReceivedSuccess(String s) {
        onDataTransferCompleted(s);
    }

    @Override
    public void onDataReceivedFailure() {}
}
