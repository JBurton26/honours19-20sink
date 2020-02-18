package com.example.honours19_20;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
//import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import io.moquette.BrokerConstants;
import io.moquette.broker.*;
import io.moquette.broker.config.MemoryConfig;

import org.apache.log4j.BasicConfigurator;
import org.eclipse.paho.android.service.*;

public class MainActivity extends AppCompatActivity {
    Switch hotspotSwitch;
    private WifiManager man;

    //private WifiConfiguration wConf = new WifiConfiguration();
    private String[] perms = {"android.permission.INTERNET", "android.permission.WRITE_EXTERNAL_STORAGE","android.permission.OVERRIDE_WIFI_CONFIG","android.permission.ACCESS_NETWORK_STATE","android.permission.WRITE_SETTINGS","android.permission.CHANGE_NETWORK_STATE","android.permission.ACCESS_FINE_LOCATION", "android.permission.CHANGE_WIFI_STATE","android.permission.ACCESS_WIFI_STATE", "android.permission.ACCESS_COARSE_LOCATION"};
    private Server srv = new Server();
    public WifiManager.LocalOnlyHotspotReservation hotspotres;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, perms, 1);
        BasicConfigurator.configure();

        hotspotSwitch = findViewById(R.id.hotspotSwitch);
        hotspotSwitch.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(hotspotSwitch.isChecked())
                        {
                            turnOnHotspot();
                            try {
                                MemoryConfig memConf = new MemoryConfig(new Properties());
                                srv.startServer(memConf);
                                Log.d("Log","Starting Broker");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        else
                        {
                            turnOffHotspot();

                            srv.stopServer();
                        }
                    }
                }
        );

    }
/*
    private WifiConfiguration wConfSetter() {
        Log.d("Logger", "Here");
        WifiConfiguration wifiConf = hotspotres.getWifiConfiguration();
        wifiConf.SSID = "SinkNode";
        wifiConf.preSharedKey = "SinkPass";
        return wifiConf;
    }
*/
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspot()
    {
        try
        {
            man = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
/*
            wConf = wConfSetter();
            int i = man.updateNetwork(wConf);
            if(i == -1){
                Toast err = Toast.makeText(this, "Error!", Toast.LENGTH_LONG);
                err.show();

            }
*/

            man.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback()
            {
                @Override
                public void onStarted(WifiManager.LocalOnlyHotspotReservation res)
                {
                    super.onStarted(res);
                    Log.d("Log: ","Wifi Hotspot on");
                    Log.d("Log: ",res.getWifiConfiguration().SSID + ", "+res.getWifiConfiguration().preSharedKey);
                    hotspotres = res;
                }
                @Override
                public void onStopped()
                {
                    super.onStopped();
                    Log.d("Stopped", "onStopped: ");
                }

                @Override
                public void onFailed(int reason)
                {
                    super.onFailed(reason);
                    Log.d("Fail", "onFailed: ");
                }
            }, new Handler());

        } catch (Exception e) {
            Toast toastisto = Toast.makeText(this, "Error starting hotspot", Toast.LENGTH_LONG);
            toastisto.show();
            e.printStackTrace();
            hotspotSwitch.setChecked(false);
        }

    }
    private void turnOffHotspot()
    {
        if (hotspotres != null)
        {
            WifiManager.LocalOnlyHotspotReservation mRes;
            mRes=(WifiManager.LocalOnlyHotspotReservation) hotspotres;
            mRes.close();
            Log.d("Log", "Shutting down hotspot and server");
        }
    }

}
