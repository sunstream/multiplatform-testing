import base.BaseTestAfterLogin;
import com.worldventures.dreamtrips.api.feed.model.FeedItem;
import com.worldventures.dreamtrips.api.profile.model.PrivateUserProfile;
import data.Platform;
import io.appium.java_client.MobileElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.yandex.qatools.allure.annotations.Issue;
import ru.yandex.qatools.allure.annotations.TestCaseId;
import steps.ActivityFeedSteps;
import steps.SocialAPISteps;
import steps.UserAPISteps;
import utils.FileUtils;
import utils.StringHelper;
import utils.annotations.SkipOn;
import utils.runner.Assert;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsNull.notNullValue;

public class ActivityFeedTests extends BaseTestAfterLogin {
	ActivityFeedSteps activityFeedSteps = getStepsComponent().activityFeedSteps();
	SocialAPISteps socialAPISteps = getStepsComponent().socialAPISteps();
	UserAPISteps userAPISteps = getStepsComponent().userAPISteps();

	private static final String DEFAULT_HASH_TAG = "#AutoTestPost";
	private List<FeedItem> createdFeedItems = new ArrayList<>();
	private PrivateUserProfile defaultUserProfile;
	private File userAvatarFile;

	@BeforeClass
	public void getTestUserProfile() throws IOException {
		defaultUserProfile = userAPISteps.getCurrentUserProfile();
		userAvatarFile = FileUtils.getResourceFile("images/blue.png");
		//upload default avatar if changed
		if (!defaultUserProfile.avatar().thumb().endsWith(userAvatarFile.getName())) {
			userAPISteps.uploadAvatar(userAvatarFile);
		}
	}

	@AfterMethod(alwaysRun = true)
	public void deleteCreatedFeedItems() {
		socialAPISteps.deleteFeedItemsAndRemoveDeletedFromList(createdFeedItems);
	}

	@TestCaseId("https://techery.testrail.net/index.php?/cases/view/213686")
	@Issue("https://techery.atlassian.net/browse/DTAUT-499")
	@Test
	@SkipOn(platforms = {Platform.IPAD, Platform.IPHONE},
			jiraIssue = "https://techery.atlassian.net/browse/DTAUT-505",
			reason = "need to make text field hint visible for Appium")
	public void openShareNewPostScreenAndValidateItsState(){
		activityFeedSteps.openActivityFeedScreen();
		activityFeedSteps.pressSharePostButton();
		activityFeedSteps.assertNewPostScreenIsDisplayed();
		activityFeedSteps.assertExpectedTextDisplayedInPostContentArea("Say something, add #hashtags");
	}

	@TestCaseId("https://techery.testrail.net/index.php?/cases/view/213688")
	@Issue("https://techery.atlassian.net/browse/DTAUT-500")
	@Test
	public void addTextToNewPostAndValidateItOnPopup(){
		String hashTags = getHashTagsWithMethodNameAndTimestamp();
		String postContent = "Text post " + hashTags;
		activityFeedSteps.openActivityFeedScreen();
		activityFeedSteps.pressSharePostButton();
		activityFeedSteps.addTextToTheNewPost(postContent);
		activityFeedSteps.assertExpectedTextDisplayedInPostContentArea(postContent);
	}

	@TestCaseId("https://techery.testrail.net/index.php?/cases/view/213556")
	@Issue("https://techery.atlassian.net/browse/DTAUT-434")
	@Test
	public void createNewTextPostAndValidateItsTextContent() throws IOException {
		String hashTags = getHashTagsWithMethodNameAndTimestamp();
		String postContent = "Text post " + hashTags;
		activityFeedSteps.openActivityFeedScreen();
		activityFeedSteps.createNewTextPost(postContent);
		addCreatedFeedItemToCleanupList(hashTags);

		MobileElement newPostContainer = activityFeedSteps.findNewPostByText(postContent);
		Assert.assertThat("New post with text ["+postContent+"] was not found in Activity Feed", newPostContainer,
				notNullValue());
	}

	@TestCaseId("https://techery.testrail.net/index.php?/cases/view/213556")
	@Issue("https://techery.atlassian.net/browse/DTAUT-434")
	@Test
	@SkipOn(platforms = {Platform.IPHONE, Platform.IPAD},
			jiraIssue = "https://worldventures.atlassian.net/browse/SOCIAL-1039",
			reason = "[iOS] post title in Activity Feed mismatch: 'added Post' instead of 'added a Post'")
	public void createNewTextPostAndValidateItsTitleInFeed() throws IOException {
		String hashTags = getHashTagsWithMethodNameAndTimestamp();
		String postContent = "Text post " + hashTags;
		activityFeedSteps.openActivityFeedScreen();
		activityFeedSteps.createNewTextPost(postContent);
		addCreatedFeedItemToCleanupList(hashTags);

		MobileElement newPostContainer = activityFeedSteps.findNewPostByText(postContent);
		activityFeedSteps.assertThatPostHasValidTitle(newPostContainer, defaultUserProfile);
	}

	@TestCaseId("https://techery.testrail.net/index.php?/cases/view/213556")
	@Issue("https://techery.atlassian.net/browse/DTAUT-434")
	@Test
	public void createNewTextPostAndValidateItsAuthorAvatar() throws IOException {
		String hashTags = getHashTagsWithMethodNameAndTimestamp();
		String postContent = "Text post " + hashTags;
		activityFeedSteps.openActivityFeedScreen();

		activityFeedSteps.createNewTextPost(postContent);
		addCreatedFeedItemToCleanupList(hashTags);

		MobileElement newPostContainer = activityFeedSteps.findNewPostByText(postContent);
		activityFeedSteps.assertThatPostHasValidAvatar(newPostContainer, userAvatarFile);
	}

	@TestCaseId("https://techery.testrail.net/index.php?/cases/view/213556")
	@Issue("https://techery.atlassian.net/browse/DTAUT-434")
	@Test
	@SkipOn(platforms = {Platform.ANDROID_PHONE, Platform.ANDROID_TABLET},
			jiraIssue = "https://worldventures.atlassian.net/browse/SOCIAL-1036",
			reason = "[Android] Timestamp does not contain year value")
	public void createNewTextPostAndValidateItsDateAndTime() throws IOException {
		String hashTags = getHashTagsWithMethodNameAndTimestamp();
		String postContent = "Text post " + hashTags;
		activityFeedSteps.openActivityFeedScreen();

		LocalDateTime timeWhenCreatingPost = LocalDateTime.now();
		activityFeedSteps.createNewTextPost(postContent);
		addCreatedFeedItemToCleanupList(hashTags);

		MobileElement newPostContainer = activityFeedSteps.findNewPostByText(postContent);
		Duration allowedDelayWhenCreatingPost = Duration.ofMinutes(1);
		activityFeedSteps.assertThatPostHasValidTimestamp(newPostContainer, timeWhenCreatingPost,
				allowedDelayWhenCreatingPost);
	}

	private String getHashTagsWithMethodNameAndTimestamp() {
		return String.format("%s #%s #%s", DEFAULT_HASH_TAG, getTestMethodName(), StringHelper.getTimestampSuffix());
	}

	private void addCreatedFeedItemToCleanupList(String hashtags) throws IOException {
		List<FeedItem> itemsByHashtags = socialAPISteps.getFeedItemsByHashtags(hashtags);
		createdFeedItems.addAll(itemsByHashtags);
	}

}


