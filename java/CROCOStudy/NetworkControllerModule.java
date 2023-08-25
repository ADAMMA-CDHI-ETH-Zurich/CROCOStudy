package CROCOStudy;

import static android.content.Context.POWER_SERVICE;

import android.content.Context;
import android.os.PowerManager;

import JavaCLAID.CLAID;
import JavaCLAID.Channel;
import JavaCLAID.ChannelData;
import JavaCLAID.Module;
import JavaCLAIDDataTypes.BatteryData;
import JavaCLAIDDataTypes.NetworkStateChangeRequest;

class NetworkControllerModule extends Module
{
    private Channel<BatteryData> batteryInfoChannel;
    private Channel<NetworkStateChangeRequest> networkStateChangeRequestChannel;
    private PowerManager.WakeLock wakeLock;

    public void initialize()
    {
        this.batteryInfoChannel = this.subscribe(BatteryData.class, "BatteryDataMonitored", data -> onBatteryData(data));
        this.networkStateChangeRequestChannel = this.publish(NetworkStateChangeRequest.class, "//CLAID/LOCAL/NetorkClientRequests");

        Context context = (Context) CLAID.getContext();
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
    }

    public void onBatteryData(ChannelData<BatteryData> data)
    {
        BatteryData batteryData = data.value();

        /*
        UNKNOWN = 0
        UNPLUGGED = 1
        FULL = 2
        CHARGING = 3
        USB_CHARGING = 4
        AC_CHARGING = 5
        WIRELESS_CHARGING = 6,
         */
        if(batteryData.get_state() >= 3)
        {
            // We are currently charging.
            //         ENABLE_NETWORK = 0
            //        DISABLE_NETWORK = 1
            System.out.println("CROCOStudy NetworkControllerModule: We are charging, enabling network and wakelock.");
            NetworkStateChangeRequest networkStateChangeRequest = new NetworkStateChangeRequest();
            networkStateChangeRequest.set_networkRequest(0);
            this.networkStateChangeRequestChannel.post(networkStateChangeRequest);
            if(!this.wakeLock.isHeld())
            {
                this.wakeLock.acquire();
            }
        }
        else
        {
            System.out.println("CROCOStudy NetworkControllerModule: We are not charging, disable network and wakelock.");
            NetworkStateChangeRequest networkStateChangeRequest = new NetworkStateChangeRequest();
            networkStateChangeRequest.set_networkRequest(1);
            this.networkStateChangeRequestChannel.post(networkStateChangeRequest);

            if(this.wakeLock.isHeld())
            {
                this.wakeLock.release();
            }
        }
    }

}