package steps;

import actions.ActivityFeedActions;
import actions.NavigationActions;
import ru.yandex.qatools.allure.annotations.Step;
import utils.annotations.UseActions;

public class NavigationSteps  {
	private final ActivityFeedActions activityFeedActions;
	private final NavigationActions navigationActions;

	@UseActions
	public NavigationSteps(ActivityFeedActions activityFeedActions,
	                       NavigationActions navigationActions) {
		this.activityFeedActions = activityFeedActions;
		this.navigationActions = navigationActions;
	}

	@Step("Verify that landing page is loaded")
	public void assertLandingPageLoaded() {
		navigationActions.assertLandingPageLoaded();
	}
}
