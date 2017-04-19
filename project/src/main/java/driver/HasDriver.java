package driver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;

public interface HasDriver {
	default AppiumDriver<MobileElement> getDriver() {
		return DriverProvider.get();
	}

	default boolean isAndroid() {
		return DriverProvider.isAndroid();
	}
}
