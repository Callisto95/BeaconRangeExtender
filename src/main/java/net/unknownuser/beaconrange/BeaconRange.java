package net.unknownuser.beaconrange;

import net.fabricmc.api.*;
import org.slf4j.*;

public class BeaconRange implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("BeaconRangeExtender");
	
	@Override
	public void onInitialize() {
		Config.init();
		
		LOGGER.info("Beacon Range Extender loaded!");
	}
}
