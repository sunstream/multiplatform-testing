package actions;

import ui.screens.LoginScreen;
import utils.runner.Assert;
import utils.waiters.Waiter;

import java.time.Duration;
import java.util.Arrays;

import static utils.waiters.Waiter.*;

public abstract class LoginActions extends BaseUiActions {

	private LoginScreen loginPage = new LoginScreen();

	public void setLogin(String username) {
		setText(loginPage.fldLogin, username);
	}

	public void setPassword(String password) {
		setText(loginPage.fldPassword, password);
	}

	public void submit(){
		click(loginPage.btnLogin);
	}

	public String getCurrentLoginValue(){
		return getText(loginPage.fldLogin);
	}

	@Override
	public void waitForScreen() {
		boolean areFieldsPresent = areAllDisplayedForElements(Arrays.asList(loginPage.fldLogin, loginPage.fldPassword));
		Assert.assertThat("Login and password fields are present on the screen", areFieldsPresent);
	}

	public void waitUntilLoginScreenGone() {
		Waiter.waitAbsent(loginPage.fldLogin, Duration.ofMinutes(1));
	}

	public boolean isScreenActive() {
		return isDisplayed(loginPage.fldLogin);
	}

}
