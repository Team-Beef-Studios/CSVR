
package com.drbeef.externalhapticsservice;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import java.util.Vector;

public class HapticsAPI
{
	private static final Vector<HapticServiceClient> externalHapticsServiceClients = new Vector<>();

	//Use a vector of pairs, it is possible a given package _could_ in the future support more than one haptic service
	//so a map here of Package -> Action would not work.
	private static final Vector<Pair<String, String>> externalHapticsServiceDetails = new Vector<>();

	static
	{
		//Add possible external haptic service details here
		externalHapticsServiceDetails.add(Pair.create(HapticsConstants.BHAPTICS_PACKAGE, HapticsConstants.BHAPTICS_ACTION_FILTER));
		externalHapticsServiceDetails.add(Pair.create(HapticsConstants.FORCETUBE_PACKAGE, HapticsConstants.FORCETUBE_ACTION_FILTER));
	}

	private static final String APPLICATION = "Haptics";

	public static void onCreate(Context context) {
		for (Pair<String, String> serviceDetail : externalHapticsServiceDetails) {
			HapticServiceClient client = new HapticServiceClient(context, (state, desc) -> {
				Log.v(APPLICATION, "ExternalHapticsService " + serviceDetail.second + ": " + desc);
			}, new Intent(serviceDetail.second).setPackage(serviceDetail.first));

			client.bindService();
			externalHapticsServiceClients.add(client);
		}
	}

	public static void onDestroy()
	{
		try {
			for (HapticServiceClient externalHapticsServiceClient : externalHapticsServiceClients) {
				externalHapticsServiceClient.stopBinding();
			}
		} catch (Exception e) {
			Log.v(APPLICATION, e.toString());
		}
	}

	public static void onPause() {

		for (HapticServiceClient externalHapticsServiceClient : externalHapticsServiceClients) {

			if (externalHapticsServiceClient.hasService()) {
				try {
					externalHapticsServiceClient.getHapticsService().hapticDisable();
				} catch (RemoteException r) {
					Log.v(APPLICATION, r.toString());
				}
			}
		}
	}

	public static void onResume() {

		for (HapticServiceClient externalHapticsServiceClient : externalHapticsServiceClients) {

			if (externalHapticsServiceClient.hasService()) {
				try {
					externalHapticsServiceClient.getHapticsService().hapticEnable();
				} catch (RemoteException r) {
					Log.v(APPLICATION, r.toString());
				}
			}
		}
	}

	public static void event(String event, int position, int flags, int intensity, float angle, float yHeight)  {

		for (HapticServiceClient externalHapticsServiceClient : externalHapticsServiceClients) {

			if (externalHapticsServiceClient.hasService()) {
				try {
					externalHapticsServiceClient.getHapticsService().hapticEvent(APPLICATION, event, position, flags, intensity, angle, yHeight);
				}
				catch (RemoteException r)
				{
					Log.v(APPLICATION, r.toString());
				}
			}
		}
	}

	public static void updateevent(String event, int intensity, float angle) {

		for (HapticServiceClient externalHapticsServiceClient : externalHapticsServiceClients) {

			if (externalHapticsServiceClient.hasService()) {
				try {
					externalHapticsServiceClient.getHapticsService().hapticUpdateEvent(APPLICATION, event, intensity, angle);
				} catch (RemoteException r) {
					Log.v(APPLICATION, r.toString());
				}
			}
		}
	}

	public static void stopevent(String event) {

		for (HapticServiceClient externalHapticsServiceClient : externalHapticsServiceClients) {

			if (externalHapticsServiceClient.hasService()) {
				try {
					externalHapticsServiceClient.getHapticsService().hapticStopEvent(APPLICATION, event);
				} catch (RemoteException r) {
					Log.v(APPLICATION, r.toString());
				}
			}
		}
	}

	public static void endframe() {

		for (HapticServiceClient externalHapticsServiceClient : externalHapticsServiceClients) {

			if (externalHapticsServiceClient.hasService()) {
				try {
					externalHapticsServiceClient.getHapticsService().hapticFrameTick();
				} catch (RemoteException r) {
					Log.v(APPLICATION, r.toString());
				}
			}
		}
	}
}
