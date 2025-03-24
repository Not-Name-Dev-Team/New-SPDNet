package com.shatteredpixel.shatteredpixeldungeon.spdnet.update;

import com.shatteredpixel.shatteredpixeldungeon.services.updates.UpdateService;

public class UpdateImpl {

	private static UpdateService updateChecker = new SPDNetUpdates();

	public static UpdateService getUpdateService(){
		return updateChecker;
	}

	public static boolean supportsUpdates(){
		return true;
	}

}
